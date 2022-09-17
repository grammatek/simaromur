package com.grammatek.simaromur.cache;

import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.rxjava3.RxDataStoreBuilder;

import com.google.protobuf.Timestamp;
import com.grammatek.simaromur.App;
import com.grammatek.simaromur.utils.FileUtils;
import com.grammatek.simaromur.frontend.FrontendManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;

@OptIn(markerClass = kotlinx.coroutines.ExperimentalCoroutinesApi.class)

/*
  This class implements higher level operations for the UtteranceCache.
 */
public class UtteranceCacheManager {
    private final static String LOG_TAG = "Simaromur_" + UtteranceCacheManager.class.getSimpleName();
    private final static String CACHE_SUBDIR_PATH = "voice_cache";
    private final RxDataStore<UtteranceCache> mUtteranceCacheDataStore;
    private final UtteranceCache.Builder mUtteranceCacheBuilder;
    private boolean mIsClosed = false;
    private long mCurrentCacheSize;
    private final long mCacheSizeHighWatermark;
    private final long mCacheSizeLowWatermark;

    /**
     * Constructor. A single instance of this should be put into an application as there must only
     *              be a single DataStore object for the same file in an application.
     * @param dataStoreFileName         the data store filename to use for storing the Protobuf data
     *                                  store file
     * @param cacheSizeLowWatermark     low watermark, i.e the target cache size to be reached, when
     *                                  expiring elements from the cache
     * @param cacheSizeHighWatermark    high watermark, i.e. max. allowed size of the audio that can
     *                                  be set in the cache
     */
    public UtteranceCacheManager(String dataStoreFileName, long cacheSizeLowWatermark, long cacheSizeHighWatermark) {
        if (cacheSizeHighWatermark <= cacheSizeLowWatermark) {
            throw new RuntimeException("cacheSizeHighWatermark <= cacheSizeLowWatermark ?!");
        }

        mUtteranceCacheDataStore =
                new RxDataStoreBuilder<>(App.getContext(), dataStoreFileName,
                        new UtteranceCacheSerializer()).build();
        mUtteranceCacheBuilder = readCache().toBuilder();
        // create data cache directory
        final String path = getCacheDirectoryPath();
        if (! FileUtils.mkdir(path)) {
            throw new RuntimeException("Couldn't create cache directory");
        }
        mCurrentCacheSize = summarizeAudioFileSize();
        mCacheSizeHighWatermark = cacheSizeHighWatermark;
        mCacheSizeLowWatermark = cacheSizeLowWatermark;
        updateMD5SumsOfAllCacheItems();
    }

    /**
     * Returns absolute path to the cache directory
     *
     * @return  Path to cache directory
     */
    private static String getCacheDirectoryPath() {
        return new File(App.getDataPath()) + "/" + CACHE_SUBDIR_PATH;
    }

    /**
     * Closes the data store. After calling this method, one shouldn't call any other methods
     * anymore afterwards.
     */
    synchronized public void close() {
        assertNotClosed();
        mUtteranceCacheDataStore.dispose();
        mUtteranceCacheDataStore.shutdownComplete().blockingAwait();
        mIsClosed = true;
    }

    /**
     * Asserts in case object is used after method close() has been used.
     */
    private void assertNotClosed() {
        if (mIsClosed) {
            throw new RuntimeException("UtteranceCacheManager has already been closed !");
        }
    }

    /**
     * Returns the whole UtteranceCache
     *
     * @return Utterance cache
     */
    synchronized
    public UtteranceCache getCache() {
        assertNotClosed();
        return mUtteranceCacheBuilder.build();
    }

    /**
     * Returns the number of cache items inside the cache
     *
     * @return Utterance cache
     */
    synchronized
    public long getItemCount() {
        assertNotClosed();
        return mUtteranceCacheBuilder.getEntriesCount();
    }

    /**
     * Reads the cache from disk. This is a blocking call.
     *
     * This method is used to read from disk all cache meta data and should only be used at the
     * very beginning.
     *
     * @return The UtteranceCache meta data
     */
    synchronized
    private UtteranceCache readCache() {
        assertNotClosed();
        return mUtteranceCacheDataStore.data().blockingFirst();
        // TODO: cleanup audio cache files, not found in the cache meta-data
    }

    /**
     * Sets a new Utterance Cache. This can be a modified utterance
     * cache or a completely new one. It replaces an already existing one.
     *
     * @param anUtteranceCache    the utterance cache to be updated
     */
    synchronized
    public void persistCache(UtteranceCache anUtteranceCache) {
        assertNotClosed();
        mUtteranceCacheDataStore.updateDataAsync(currentCache ->
                Single.just(anUtteranceCache));
    }

    /**
     * Save given Utterance instance in cache. If utterance already exists, update it. If
     * it doesn't, add it to the cache.
     *
     * @param utterance  the utterance to be persisted
     * @return CacheItem created when saving the utterance, or in case utterance already
     *         existed, the updated cache item
     */
    synchronized
    public CacheItem saveUtterance(Utterance utterance) {
        assertNotClosed();

        // Update existing utterance
        Optional<CacheItem> optItem = findItemByText(utterance.getText());
        if (optItem.isPresent()) {
            return updateUtterance(optItem.get().getUuid(), utterance);
        } else {
            // ... or add a new one
            final String uuid = UUID.randomUUID().toString();
            CacheItem item = CacheItem.newBuilder()
                    .setUtterance(utterance)
                    .setUuid(uuid)
                    .build();
            mUtteranceCacheBuilder.putEntries(uuid, item);
            mUtteranceCacheBuilder.putMd5Entries(utterance.getTextMd5Sum(), uuid);
            persistCache(mUtteranceCacheBuilder.build());
            optItem = Optional.of(item);
        }
        return optItem.get();
    }

    /**
     * Finds an element in the cache by given utterance.
     *
     * @param utterance     The utterance to search for
     *
     * @return  a CacheItem in case the given utterance matched with an item in the cache
     */
    private Optional<CacheItem> findItem(Utterance utterance) {
        return findItemByText(utterance.getText());
    }

    /**
     * Finds a CacheItem by given UUID. There should only be one Cache Item available by the
     * given combination of these parameters.
     *
     * @param uuid          UUID of the cache Item.
     * @return  a CacheItem in case the given UUID matched with an item in the cache, or none
     *          otherwise
     */
    synchronized
    public Optional<CacheItem> findItemByUuid(String uuid) {
        assertNotClosed();
        Optional<CacheItem> opt = Optional.empty();
        CacheItem item = mUtteranceCacheBuilder.getEntriesMap().get(uuid);
        if (item != null) {
            opt = Optional.of(item);
        }
        return opt;
    }

    /**
     * Find a cache item just by the utterance text. The utterance text is the unique distinction
     * point for a Cache Item.
     *
     * @param text  the raw text of an utterance
     *
     * @return a CacheItem in case the given text matched with an item in the cache
     */
    synchronized
    public Optional<CacheItem> findItemByText(String text) {
        assertNotClosed();
        Optional<CacheItem> opt = Optional.empty();
        final String uuid = mUtteranceCacheBuilder.getMd5EntriesMap().get(FileUtils.getMD5SumOfString(text));
        if (uuid != null && !uuid.isEmpty()) {
            opt = findItemByUuid(uuid);
        }
        return opt;
    }

    /**
     * Build a new Utterance instance from given parameters and immediately saves it into the
     * cache if not already existing. This entry doesn't need to have an audio attached.
     * This can be done later.
     *
     * @param text          The raw text as given to the speak request
     * @param normalized    normalization result
     * @param phonemes      G2P result
     *
     * @return  Utterance instance
     */
    synchronized
    public CacheItem addUtterance(String text, String normalized, List<String> phonemes) {
        assertNotClosed();

        Optional<CacheItem> optItem = findItem(text, normalized, phonemes);
        if (optItem.isPresent()) {
            return optItem.get();
        }

        // save only if not already exists
        Utterance utterance = newUtterance(text, normalized, phonemes);
        return saveUtterance(utterance);
    }

    /**
     * Add utterance to cache and return its cache item. If there is a corresponding cache item for
     * the given text, this is returned instead.
     *
     * @param text  text to be added to the cache
     *
     * @return new or existing cache item corresponding to text
     */
    synchronized
    public CacheItem addUtterance(String text) {
        assertNotClosed();

        Optional<CacheItem> optItem = findItemByText(text);
        if (optItem.isPresent()) {
            return optItem.get();
        }

        // save only if not already exists
        Utterance utterance = newUtterance(text, "", new ArrayList<>());
        return saveUtterance(utterance);
    }

    /**
     * Build a new Utterance instance from given parameters. This is not saved into the cache yet.
     *
     * @param text              The raw text as given to the speak request
     * @param normalized        normalization result
     * @param phonemeSymbols    G2P phoneme symbols
     *
     * @return new Utterance instance
     */
    public static Utterance newUtterance(String text, String normalized, List<String> phonemeSymbols) {
        List<PhonemeEntry> phonemeList = new ArrayList<>();
        for (String symbols: phonemeSymbols) {
            phonemeList.add(newPhoneme(symbols));
        }
        return Utterance.newBuilder()
                .setFrontendVersion(FrontendManager.getVersion())
                .setText(text)
                .setTextMd5Sum(FileUtils.getMD5SumOfString(text))
                .setNormalized(normalized)
                .addAllPhonemes(phonemeList)
                .build();
    }

    /**
     * Build a new Phoneme entry from given symbols
     *
     * @param symbols   symbols of the Phoneme entry
     *
     * @return new PhonemeEntry usable for updating elements in the cache
     */
    public static PhonemeEntry newPhoneme(String symbols) {
        return PhonemeEntry.newBuilder()
            .setSymbols(symbols)
            .setMd5(FileUtils.getMD5SumOfString(symbols))
            .build();
    }

    /**
     * Clears the cache from all entries.
     */
    synchronized
    public void clearCache() {
        assertNotClosed();
        for (String uuid: getUuids()) {
            deleteCacheItem(uuid);
        }
        mUtteranceCacheBuilder.clearEntries();
        persistCache(mUtteranceCacheBuilder.build());
    }

    @NonNull
    private List<String> getUuids() {
        return mUtteranceCacheBuilder.getEntriesMap().values()
                .stream()
                .map(CacheItem::getUuid)
                .collect(Collectors.toList());
    }

    /**
     * Returns all cache item uuids ascending sorted by their latest timestamp.
     *
     * @return  timely sorted list of uuids
     */
    synchronized
    public List<String> getUuidsSortedByTimestamp() {
        assertNotClosed();
        List<CacheItem> items = getCacheItemsSortedByTimestamp();
        return items
                .stream()
                .map(CacheItem::getUuid)
                .collect(Collectors.toList());
    }

    /**
     * Returns all cache item uuids ascending sorted by their latest timestamp.
     *
     * @return  timely sorted list of uuids
     */
    synchronized
    public List<CacheItem> getCacheItemsSortedByTimestamp() {
        assertNotClosed();
        return mUtteranceCacheBuilder.getEntriesMap().values()
                .stream()
                .sorted(Comparator.comparing(i -> convertTimestampToMillis(i.getTimestamp())))
                .collect(Collectors.toList());
    }

    /**
     * Returns all cache item uuids ascending sorted by their usage.
     *
     * @return  timely sorted list of uuids
     */
    synchronized
    public List<String> getUuidsSortedByUsage() {
        assertNotClosed();
        List<CacheItem> items = mUtteranceCacheBuilder.getEntriesMap().values()
                .stream()
                .sorted(Comparator.comparing(CacheItem::getUsageCount))
                .collect(Collectors.toList());
        return items
                .stream()
                .map(CacheItem::getUuid)
                .collect(Collectors.toList());
    }

    /**
     * Returns all cache item uuids sorted by their latest timestamp.
     *
     * @param nOldestItems  max number of oldest items from the cache
     *
     * @return  timely sorted list of uuids
     */
    synchronized
    public List<String> getUuidsSortedByTimestamp(int nOldestItems) {
        return getUuidsSortedByTimestamp().stream().limit(nOldestItems).collect(Collectors.toList());
    }

    /**
     * Finds a CacheItem entry by given parameters. There should only be one Cache Item available by the
     * given combination of these parameters. The cache item itself contains a map of available audios
     * with voice:version as key
     *
     * @param text          Text of an utterance
     * @param normalized    Normalized text
     * @param phonemes      Phonemes of the normalized text
     * @return  a CacheItem in case the given parameters matched with an item in the cache, or none
     *          otherwise
     */
    synchronized
    public Optional<CacheItem> findItem(String text, String normalized, List<String> phonemes) {
        assertNotClosed();
        Utterance utterance = newUtterance(text, normalized, phonemes);
        return findItem(utterance);
    }

    /**
     * Update given item inside the cache with given utterance. It also deletes all audio data
     * as this data would be stale anyway. As an optimization this operation
     * doesn't do anything in case the given utterance is the same as in the cache.
     *
     * @param itemUuid  Item identified by given uuid to be updated with given utterance
     * @param utterance utterance
     *
     * @return  true in case item could be found and has been successfully updated, false otherwise
     */
    synchronized
    public CacheItem updateUtterance(String itemUuid, Utterance utterance) {
        assertNotClosed();
        CacheItem item = mUtteranceCacheBuilder.getEntriesMap().get(itemUuid);
        if (item != null) {
            if (utterance != item.getUtterance()) {
                // persist only if different
                CacheItem.Builder itemBuilder = item.toBuilder();
                itemBuilder.setUtterance(utterance);
                CacheItem updateItem = itemBuilder.build();
                mUtteranceCacheBuilder.putEntries(itemUuid, updateItem);
                mUtteranceCacheBuilder.putMd5Entries(utterance.getTextMd5Sum(), itemUuid);
                deleteAudioForText(item.getUtterance().getText());
                persistCache(mUtteranceCacheBuilder.build());
                return updateItem;
            }
        }
        return item;
    }

    /**
     * Delete a cache item from the cache. In case the item exists and could be deleted, true is
     * returned, false otherwise.
     *
     * @param itemUuid  the uuid of the cache item to be deleted
     * @return  true in case the cache item has been successfully be deleted, false otherwise
     */
    synchronized
    public boolean deleteCacheItem(String itemUuid) {
        assertNotClosed();
        boolean isDeleted = false;
        Optional<CacheItem> optItem = findItemByUuid(itemUuid);
        if (optItem.isPresent()) {
            CacheItem item = optItem.get();
            final Collection<AudioEntry> audioEntries = item.getVoiceAudioEntriesMap().values();
            // remove meta data
            mUtteranceCacheBuilder.removeEntries(itemUuid);
            // remove related audio files
            for (AudioEntry audioEntry: audioEntries) {
                for (VoiceAudioDescription vad:audioEntry.getAudioDescriptorsList()) {
                    FileUtils.delete(vad.getPath());
                    mCurrentCacheSize -= vad.getFileSize();
                }
            }
            isDeleted = true;
            persistCache(mUtteranceCacheBuilder.build());
        }
        return isDeleted;
    }

    /**
     * Returns a new voice audio description for provided parameters.
     *
     * @param format        audio format (wav, mp3, ...)
     * @param rate          one of the predefined sample rates
     * @param fileSize      the size of the audio data in bytes
     * @param voiceName     name of the corresponding voice
     * @param voiceVersion  version of the voice
     * @return  a VoiceAudioDescription instance
     */
    public static VoiceAudioDescription newAudioDescription(AudioFormat format, SampleRate rate, int fileSize, String voiceName, String voiceVersion) {
        return VoiceAudioDescription.newBuilder()
                .setFormat(format)
                .setRate(rate)
                .setFileSize(fileSize)
                .setVoiceName(voiceName)
                .setVoiceVersion(voiceVersion)
                .build();
    }

    /**
     * Returns audio filename  for given phonemeEntry and voice audio description. The returned filename
     * is deterministic.
     *
     * @param phonemeEntry      phoneme entry, needs the MD5 sum to be prefilled
     * @param vad               voice audio description, necessary for the filename suffix
     * @return  filename for an audio file corresponding to passed parameters
     */
    synchronized
    public static String getAudioFilenameForPhoneme(PhonemeEntry phonemeEntry, VoiceAudioDescription vad) {
        String fileName = phonemeEntry.getMd5();
        switch (vad.getFormat()) {
            case AUDIO_FMT_PCM:
                fileName += ".pcm";
                break;
            case AUDIO_FMT_MP3:
                fileName += ".mp3";
                break;
            default:
                fileName += ".unknown";
                break;
        }
        String voicePrefix = vad.getVoiceName() + "_" + vad.getVoiceVersion();
        return getCacheDirectoryPath() + "/" + voicePrefix + "_" + fileName;
    }

    /**
     * Add audio data and description to a cache item. This writes the data to the cache and updates
     * the cache meta data. In case the high water mark of the cache is reached, the operation fails.
     * Calling this method successfully increases the usage counter of the cache item.
     *
     * @param uuid              uuid of cache item for which the audio should be added
     * @param phonemeEntry      Phoneme entry that corresponds to the attached audio
     * @param vad               voice audio descriptions about the audio specifics
     * @param data              the raw audio data itself, will be saved on disk to the cache
     *
     * @return                  true in case the audio has been added successfully to the given
     *                          CacheItem, false otherwise
     */
    synchronized
    public boolean addAudioToCacheItem(String uuid, PhonemeEntry phonemeEntry, VoiceAudioDescription vad, byte[] data) {
        assertNotClosed();
        if ((data.length == 0) || (vad.getVoiceName().isEmpty() || vad.getVoiceVersion().isEmpty())) {
            Log.w(LOG_TAG, "addAudioToCacheItem(): at least one of the given parameters is invalid");
            return false;
        }
        Optional<CacheItem> optItem = findItemByUuid(uuid);
        if (!optItem.isPresent()) {
            Log.w(LOG_TAG, "addAudioToCacheItem(): no such item: " + uuid);
            return false;
        }
        CacheItem item = optItem.get();

        final String fileName = getAudioFilenameForPhoneme(phonemeEntry, vad);
        VoiceAudioDescription newVad = vad.toBuilder()
                .setPath(fileName)
                .setFileSize(data.length)
                .build();
        final String voiceKey = buildVoiceKey(vad);

        AudioEntry audioForVoice;
        if (item.containsVoiceAudioEntries(voiceKey)) {
            // if list entries inside item for given voice already exist, add newVad to that list
            audioForVoice = item.getVoiceAudioEntriesOrThrow(voiceKey).toBuilder().addAudioDescriptors(newVad).build();
        } else {
            // otherwise create a new list with a single entry
            audioForVoice = AudioEntry.newBuilder().addAudioDescriptors(newVad).build();
        }
        CacheItem newItem = item.toBuilder()
                .setUsageCount(item.getUsageCount() + 1)
                .setTimestamp(getCurrentTimestamp())
                .putVoiceAudioEntries(voiceKey, audioForVoice)
                .build();
        boolean hasUpdated = updateCacheItem(newItem);

        // write audio buffer to file
        try {
            OutputStream outStream = new FileOutputStream(fileName);
            FileUtils.copyFile(new ByteArrayInputStream(data), outStream);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (hasUpdated) {
            mCurrentCacheSize += data.length;
        }
        expireCache();
        return hasUpdated;
    }

    /**
     * Get the current time as protobuf Timestamp
     *
     * @return Protobuf Timestamp of current time
     */
    public static Timestamp getCurrentTimestamp() {
        final long millis = System.currentTimeMillis();
        return Timestamp.newBuilder().setSeconds(millis / 1000)
                .setNanos((int) ((millis % 1000) * 1000000)).build();
    }

    /**
     * Converts a given Protobuf Timestamp to milliseconds since the epoch.
     *
     * @param aTimestamp  Protobuf timestamp
     * @return  timestamp as milliseconds since the epoch
     */
    public static long convertTimestampToMillis(Timestamp aTimestamp) {
        return aTimestamp.getSeconds()*1000 + aTimestamp.getNanos()/1000000;
    }

    /**
     * Builds the voice key for the voice audio entries map.
     *
     * @param vad   Voice audio descriptor
     *
     * @return  Key value used for the voice audio entries map.
     */
    @NonNull
    public static String buildVoiceKey(VoiceAudioDescription vad) {
        return buildVoiceKey(vad.getVoiceName(), vad.getVoiceVersion());
    }

    /**
     * Builds the voice key for the voice audio entries map.
     *
     * @param voiceName     voice name
     * @param voiceVersion  voice version
     *
     * @return  Key value used for the voice audio entries map.
     */
    @NonNull
    public static String buildVoiceKey(String voiceName, String voiceVersion) {
        return voiceName + ":" + voiceVersion;
    }

    /**
     * Updates given item meta data in cache.
     *
     * @param item  cache item to be updated
     *
     * @return  true in case item has been found and updated, false otherwise
     */
    synchronized
    private boolean updateCacheItem(CacheItem item) {
        assertNotClosed();
        boolean hasUpdated = false;
        if (mUtteranceCacheBuilder.getEntriesMap().get(item.getUuid()) != null) {
            mUtteranceCacheBuilder.putEntries(item.getUuid(), item);
            mUtteranceCacheBuilder.putMd5Entries(item.getUtterance().getTextMd5Sum(), item.getUuid());
            persistCache(mUtteranceCacheBuilder.build());
            hasUpdated = true;
        }
        return hasUpdated;
    }

    /**
     * Update all MD5sums of utterances in case, we haven't them already saved because of an older
     * cache version.
     */
    private void updateMD5SumsOfAllCacheItems() {
        final Collection<CacheItem> allItems = mUtteranceCacheBuilder.getEntriesMap().values();
        for (CacheItem item: allItems) {
            if (item.getUtterance().getTextMd5Sum().isEmpty()) {
                Log.v(LOG_TAG, "Updating MD5sum of item " + item.getUuid());
                Utterance.Builder updatedUtterance = item.getUtterance().toBuilder();
                updatedUtterance.setTextMd5Sum(FileUtils.getMD5SumOfString(updatedUtterance.getText()));
                updateCacheItem(item.toBuilder().setUtterance(updatedUtterance).build());
            }
        }
    }

    /**
     * Update the given cache item/phoneme entry/voice name/voice version tuple with the provided
     * audio description and audio buffer. The item/phoneme entry and a voice audio description for
     * the provided voice name/voice version of the new audio description needs to exist for this
     * function to succeed.
     *
     * @param item          The cache item to be updated
     * @param phonemeEntry  The specific phoneme entry to be updated
     * @param newVad        New voice audio description
     * @param newAudioBuf   New audio buffer to be saved to the cache
     *
     * @return  true in case cache item and phoneme entry exists and the audio has been updated,
     *          false otherwise
     */
    synchronized
    public boolean updateAudio(CacheItem item, PhonemeEntry phonemeEntry, VoiceAudioDescription newVad, byte[] newAudioBuf) {
        assertNotClosed();
        if ((newAudioBuf.length == 0) || (newVad.getVoiceName().isEmpty() || newVad.getVoiceVersion().isEmpty())) {
            Log.w(LOG_TAG, "updateAudio(): at least one of the given parameters is invalid");
            return false;
        }
        Optional<CacheItem> optItem = findItemByUuid(item.getUuid());
        if (!optItem.isPresent()) {
            Log.w(LOG_TAG, "updateAudio(): no such item: " + item.getUuid());
            return false;
        }

        final String voiceAudioKey = buildVoiceKey(newVad);
        final CacheItem foundItem = optItem.get();
        if (! foundItem.containsVoiceAudioEntries(voiceAudioKey)) {
            Log.w(LOG_TAG, "updateAudio(): no entry found for voice " + voiceAudioKey + ","
                    + " item: "+ item.getUuid());
            return false;
        }

        final Utterance foundUtterance = foundItem.getUtterance();
        // get position of phoneme entry
        int i = 0;
        for (; i < foundUtterance.getPhonemesCount(); ++i) {
            if (foundUtterance.getPhonemes(i) == phonemeEntry) {
                break;
            }
        }
        AudioEntry foundAudioEntry = foundItem.getVoiceAudioEntriesOrThrow(voiceAudioKey);
        if (foundAudioEntry.getAudioDescriptorsCount() + 1 < i) {
            Log.w(LOG_TAG, "updateAudio(): no corresponding entry at phoneme position " + i
                    + " found, please add a new entry instead");
            return false;
        }

        final long oldAudioSizeOfItem = getAudioFileSize(foundItem);

        // sets the filename in description
        // update audio file
        String fileName = getAudioFilenameForPhoneme(phonemeEntry, newVad);
        VoiceAudioDescription newDescription = newVad.toBuilder()
                .setPath(fileName)
                .setFileSize(newAudioBuf.length)
                .build();

        AudioEntry audioEntryForVoice = foundAudioEntry.toBuilder()
                .setAudioDescriptors(i, newDescription)
                .build();
        CacheItem updatedItem = foundItem.toBuilder()
                .putVoiceAudioEntries(voiceAudioKey, audioEntryForVoice)
                .setUsageCount(foundItem.getUsageCount() + 1)
                .setTimestamp(getCurrentTimestamp())
                .build();
        final long newAudioSizeOfItem = getAudioFileSize(updatedItem);
        if (! updateCacheItem(updatedItem)) {
            Log.w(LOG_TAG, "updateAudio(): couldn't update audio entry " + foundItem.getUuid());
            return false;
        }
        mCurrentCacheSize = mCurrentCacheSize - oldAudioSizeOfItem + newAudioSizeOfItem;
        try {
            OutputStream outStream = new FileOutputStream(fileName);
            FileUtils.copyFile(new ByteArrayInputStream(newAudioBuf), outStream);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Delete all audio files of a cache item belonging to a specific voice.
     *
     * @param item          The cache item to be deleted
     * @param voiceName     Voice name of the generated audio
     * @param voiceVersion  Voice version
     * @return  true in case operation was successful, false otherwise
     */
    synchronized
    public boolean deleteAudioForVoice(CacheItem item, String voiceName, String voiceVersion) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItemByUuid(item.getUuid());
        if (!optItem.isPresent()) {
            Log.w(LOG_TAG, "deleteAudioForVoice(): no such item: " + item.getUuid());
            return false;
        }
        CacheItem itemInCache = optItem.get();
        final String voiceAudioKey = buildVoiceKey(voiceName, voiceVersion);
        if (! itemInCache.containsVoiceAudioEntries(voiceAudioKey)) {
            Log.w(LOG_TAG, "deleteAudioForVoice(): no entry found for voice " + voiceAudioKey + ","
                    + " item: "+ itemInCache.getUuid());
            return false;
        }

        long audioDeleted = 0;
        try {
            AudioEntry foundAudioEntry = itemInCache.getVoiceAudioEntriesOrThrow(voiceAudioKey);
            if (foundAudioEntry.getAudioDescriptorsCount() == 0) {
                Log.w(LOG_TAG, "deleteAudioForVoice(): no audio entries found ?!");
            }

            // delete all attached audio files
            for (VoiceAudioDescription vad : foundAudioEntry.getAudioDescriptorsList()) {
                audioDeleted += vad.getFileSize();
                FileUtils.delete(vad.getPath());
            }
            // clear audio meta data
            // note: don't delete usage count or last access timestamp. We want to preserve this info
            //       even in case of cache invalidation of the audio files/metadata
            if (! updateCacheItem(itemInCache.toBuilder().removeVoiceAudioEntries(voiceAudioKey).build())) {
                Log.w(LOG_TAG, "deleteAudioForVoice(): couldn't update audio entry " + itemInCache.getUuid());
                return false;
            }
            mCurrentCacheSize -= audioDeleted;
        } catch (Exception e) {
            Log.i(LOG_TAG, "deleteAudioForVoice(): no audio entries found for "
                    + voiceName + "/" + voiceVersion);
            // non-fatal: no voice audio entries found is as good as deleting them
        }
        return true;
    }

    /**
     * Delete all audio data related to given text. The text is mapped to a CacheItem, which again can
     * contain audio entries for several voices. These audio entries and their corresponding files
     * are deleted by this call.
     *
     * @param aText     text to be searched
     * @return  true in case audio for given text could be found and successfully deleted, false otherwise
     */
    synchronized
    public boolean deleteAudioForText(String aText) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItemByText(aText);
        if (!optItem.isPresent()) {
            Log.w(LOG_TAG, "deleteAudioForText(): no such text: " + aText);
            return false;
        }

        long audioDeleted = 0;
        CacheItem itemInCache = optItem.get();
        for (AudioEntry entry : itemInCache.getVoiceAudioEntriesMap().values()) {
            for (VoiceAudioDescription vad : entry.getAudioDescriptorsList()) {
                audioDeleted += vad.getFileSize();
                // remove related files
                FileUtils.delete(vad.getPath());
            }
        }
        // clear meta data
        if (! updateCacheItem(itemInCache.toBuilder().clearVoiceAudioEntries().build())) {
            Log.w(LOG_TAG, "deleteAudioForText(): couldn't clear audio entries for " + itemInCache.getUuid());
            return false;
        }
        mCurrentCacheSize -= audioDeleted;
        return true;
    }

    /**
     * Delete all audio data related to given cache item. The cache can contain audio entries for
     * several voices. These audio entries and their corresponding files are deleted by this call.
     *
     * @param item      Cache item to be used
     * @return  true in case item could be found and audio could be successfully deleted,
     *          false otherwise
     */
    synchronized
    public boolean deleteAudioForItem(CacheItem item) {
        assertNotClosed();
        long audioDeleted = 0;
        for (AudioEntry entry : item.getVoiceAudioEntriesMap().values()) {
            for (VoiceAudioDescription vad : entry.getAudioDescriptorsList()) {
                // remove related files
                audioDeleted += vad.getFileSize();
                FileUtils.delete(vad.getPath());
            }
        }
        // clear meta data
        if (! updateCacheItem(item.toBuilder().clearVoiceAudioEntries().build())) {
            Log.w(LOG_TAG, "deleteAudioForItem(): couldn't clear audio entries for " + item.getUuid());
            return false;
        }
        mCurrentCacheSize -= audioDeleted;
        return true;
    }

    /**
     * Returns overall audio file size as used on disk by summing up all audio entries for all
     * cache items.
     *
     * @return Size of all audio files in cache for all utterances and all voices.
     */
    private long summarizeAudioFileSize() {
        assertNotClosed();
        long audioFileSizeInBytes = 0;
        for (CacheItem item: mUtteranceCacheBuilder.getEntriesMap().values()) {
            for (AudioEntry entry: item.getVoiceAudioEntriesMap().values()) {
                for (VoiceAudioDescription vad: entry.getAudioDescriptorsList()) {
                    audioFileSizeInBytes += vad.getFileSize();
                }
            }
        }
        return audioFileSizeInBytes;
    }

    /**
     * Returns overall audio file size as used on disk.
     *
     * @return Size of all audio files in cache for all utterances and all voices.
     */
    synchronized
    public long getAudioFileSize() {
        assertNotClosed();
        return mCurrentCacheSize;
    }

    /**
     * Return the audio file size as used by given voice.
     *
     * @param voiceName     Voice name
     * @param voiceVersion  Voice version
     *
     * @return  file size of all audio files for a voice
     */
    synchronized
    public long getAudioFileSize(String voiceName, String voiceVersion) {
        assertNotClosed();
        long fileSize = 0;
        for (CacheItem item: mUtteranceCacheBuilder.getEntriesMap().values()) {
            for (AudioEntry entry : item.getVoiceAudioEntriesMap().values()) {
                for (VoiceAudioDescription vad : entry.getAudioDescriptorsList()) {
                    if (vad.getVoiceName().equals(voiceName) &&
                        vad.getVoiceVersion().equals(voiceVersion)) {
                        fileSize += vad.getFileSize();
                    }
                }
            }
        }
        return fileSize;
    }

    /**
     * Return the audio file size as used by given cache item.
     *
     * @param item  the cache item
     * @return file size of all audio files for an item
     */
    synchronized
    public long getAudioFileSize(CacheItem item) {
        long audioSizeOfItem = 0;

        for (AudioEntry entry: item.getVoiceAudioEntriesMap().values()) {
            for (VoiceAudioDescription vad: entry.getAudioDescriptorsList()) {
                audioSizeOfItem += vad.getFileSize();
            }
        }
        return audioSizeOfItem;
    }

    /**
     * Returns a list of all available voices inside the cache.
     *
     * @return List of available voices as list of strings.
     */
    synchronized
    public List<String> getAvailableVoices() {
        assertNotClosed();
        Set<String> voicesSet = new ArraySet<>();
        for (CacheItem item:mUtteranceCacheBuilder.getEntriesMap().values()) {
            voicesSet.addAll(item.getVoiceAudioEntriesMap().keySet());
        }
        return new ArrayList<>(voicesSet);
    }

    /**
     * Look up utterance in cache and if found, return a list of audio data, each corresponding to
     * the phoneme list of the given utterance.
     * This call increases the overall usage counter of the utterance in cache and updates the last
     * access timestamp.
     *
     * @param utterance             The utterance to be looked up in the cache.
     * @param voiceName             voice name to be used for audio
     * @param voiceVersion          voice version
     * @return  list of audio data in case audio was available in the cache, otherwise an empty
     *          list in case, no audio data could be found for the given utterance or if some error
     *          occurred while reading the audio file from file system
     */
    synchronized
    public List<byte[]> getAudioForUtterance(Utterance utterance, String voiceName, String voiceVersion) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItem(utterance);
        if (!optItem.isPresent()) {
            Log.w(LOG_TAG, "getAudioForUtterance(): no such utterance: " + utterance.getText());
            return new ArrayList<>();
        }

        List<byte[]> audioDataList = new ArrayList<>();
        CacheItem itemInCache = optItem.get();
        for (AudioEntry entry : itemInCache.getVoiceAudioEntriesMap().values()) {
            for (VoiceAudioDescription vad : entry.getAudioDescriptorsList()) {
                if (vad.getVoiceName().equals(voiceName)
                        && vad.getVoiceVersion().equals(voiceVersion)) {
                    try {
                        InputStream inStream = new FileInputStream(vad.getPath());
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(vad.getFileSize());
                        FileUtils.copyFile(inStream, outputStream);
                        audioDataList.add(outputStream.toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ArrayList<>();
                    }
                }
            }
        }
        if (! audioDataList.isEmpty()) {
            // increases usage count and bump timestamp as well
            if (! updateCacheItem(itemInCache.toBuilder()
                    .setUsageCount(itemInCache.getUsageCount() + 1)
                    .setTimestamp(getCurrentTimestamp())
                    .build())) {
                Log.w(LOG_TAG, "getAudioForUtterance(): couldn't increase usage count for "
                        + utterance.getText());
                return new ArrayList<>();
            }
        }
        return audioDataList;
    }

    /**
     * Returns the usage count for all audio of the given utterance. Usage count is increased
     * implicitly by successfully calling addAudioToCacheItem(), getAudioForUtterance() or
     * explicitly via increaseUsageCount().
     *
     * If either the given utterance doesn't exist in the cache or neither of the above methods
     * have been called before, 0 is returned.
     *
     * @param utterance utterance to be looked up in cache
     * @return usage count of utterance
     */
    synchronized
    public long getUsageCount(Utterance utterance) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItem(utterance);
        if (! optItem.isPresent()) {
            Log.w(LOG_TAG, "getUsageCount(): no such utterance: "
                    + utterance.getText());
            return 0;
        }
        return optItem.get().getUsageCount();
    }

    /**
     * Increases usage count of given utterance in cache. If there is no audio yet, the usage count
     * is left unchanged and false is returned.
     *
     * @param utterance the utterance for which the usage count should be increased
     * @return  true in case the usage count has been increased, false otherwise
     */
    synchronized
    public boolean increaseUsageCount(Utterance utterance) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItem(utterance);
        if (! optItem.isPresent()) {
            Log.w(LOG_TAG, "increaseUsageCount(): no such utterance: "
                    + utterance.getText());
            return false;
        }

        CacheItem item = optItem.get();
        if (item.getVoiceAudioEntriesCount() == 0) {
            Log.w(LOG_TAG, "increaseUsageCount(): no audio for utterance: "
                    + utterance.getText());
            return false;
        }
        if (! updateCacheItem(item.toBuilder().setUsageCount(item.getUsageCount() + 1).build())) {
            Log.w(LOG_TAG, "increaseUsageCount(): couldn't increase usage count for "
                    + utterance.getText());
            return false;
        }
        return true;
    }

    /**
     * Updates timestamp of given utterance in cache. If there is no audio yet, the timestamp is
     * left unchanged and false is returned.
     *
     * @param utterance the utterance for which the usage count should be increased
     * @return  true in case the usage count has been increased, false otherwise
     */
    synchronized
    public boolean updateTimestamp(Utterance utterance) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItem(utterance);
        if (! optItem.isPresent()) {
            Log.w(LOG_TAG, "updateTimestamp(): no such utterance: "
                    + utterance.getText());
            return false;
        }

        CacheItem item = optItem.get();
        if (item.getVoiceAudioEntriesCount() == 0) {
            Log.w(LOG_TAG, "updateTimestamp(): no audio for utterance: "
                    + utterance.getText());
            return false;
        }
        if (! updateCacheItem(item.toBuilder().setTimestamp(getCurrentTimestamp()).build())) {
            Log.w(LOG_TAG, "updateTimestamp(): couldn't set timestamp for "
                    + utterance.getText());
            return false;
        }
        return true;
    }

    /**
     * Returns the last update timestamp of the utterance audio in milliseconds.
     * If no audio was ever attached or the given utterance doesn't exist, 0 is returned.
     *
     * @param utterance     utterance to use for the operation
     *
     * @return  audio update timestamp as milliseconds since 1.1.1970 (i.e. the epoch)
     */
    public long getTimestampMillis(Utterance utterance) {
        assertNotClosed();
        Optional<CacheItem> optItem = findItem(utterance);
        if (! optItem.isPresent()) {
            Log.w(LOG_TAG, "updateTimestamp(): no such utterance: "
                    + utterance.getText());
            return 0;
        }
        final Timestamp lastUsage = optItem.get().getTimestamp();
        return lastUsage.getSeconds() * 1000 + lastUsage.getNanos() / 1000000;
    }

    /**
     * Collect cache items sorted by timestamp that together try to fulfill given audio file size
     * criterion.
     *
     * @param minAudioByteSize  minimal byte size to reach for collected cache items
     *
     * @return collected cache items
     */
    private List<CacheItem> collectItemsWithAudioSortedByTimestamp(long minAudioByteSize) {
        long collectedAudio = 0;
        final List<CacheItem> allItemsSorted = getCacheItemsSortedByTimestamp();
        List<CacheItem> collectedItems = new ArrayList<>();
        for (CacheItem item:allItemsSorted) {
            long itemAudioFileSize = getAudioFileSize(item);
            if (itemAudioFileSize > 0) {
                collectedAudio += itemAudioFileSize;
                collectedItems.add(item);
            }
            if (collectedAudio >= minAudioByteSize) {
                break;
            }
        }
        return collectedItems;
    }

    /**
     * Delete all audio files from all cache items, starting with the oldest cache item and delete
     * as many audio files as necessary to fulfill given parameter minAudioFileSizeToDelete. Always
     * all files of a cache item are deleted even if the given size is exceeded.
     *
     * The method returns the number of bytes deleted. This can be less, equal or bigger than the
     * given size depending on the following conditions:
     *
     *   - if there is not enough audio, the returned size is less than the given size
     *   - if the size of the audio of the last cache item is bigger than the difference between the
     *     accumulated size before the last cache item was deleted, the returned size is bigger than
     *     the given size
     *   - if the accumulated size of all deleted cache items is exactly the same as the given size,
     *     the returned size is equal to the given size
     *   - if the given number is negative, or if there is no audio in the cache, 0 is returned
     *
     * @param minAudioFileSizeToDelete  minimum size of audio files to delete from the cache
     *
     * @return number of bytes deleted
     */
    synchronized
    public long deleteAudioSortedByTimestamp(long minAudioFileSizeToDelete) {
        assertNotClosed();
        long deletedAudio = 0;
        List<CacheItem> allItemsSorted = collectItemsWithAudioSortedByTimestamp(minAudioFileSizeToDelete);
        for (CacheItem item:allItemsSorted) {
            long audioSizeOfItem = getAudioFileSize(item);
            if (deleteAudioForItem(item)) {
                deletedAudio += audioSizeOfItem;
            }
            if (deletedAudio >= minAudioFileSizeToDelete) {
                break;
            }
        }
        return deletedAudio;
    }

    /**
     * Delete all audio files from all cache items, starting with the least used cache item and delete
     * as many audio files as necessary to fulfill given parameter minAudioFileSizeToDelete. Always
     * all files of a cache item are deleted even if the given size is exceeded.
     *
     * The method returns the number of bytes deleted. This can be less, equal or bigger than the
     * given size depending on the following conditions:
     *
     *   - if there is not enough audio, the returned size is less than the given size
     *   - if the size of the audio of the last cache item is bigger than the difference between the
     *     accumulated size before the last cache item was deleted, the returned size is bigger than
     *     the given size
     *   - if the accumulated size of all deleted cache items is exactly the same as the given size,
     *     the returned size is equal to the given size
     *   - if the given number is negative, or if there is no audio in the cache, 0 is returned
     *
     * @param minAudioFileSizeToDelete  minimum size of audio files to delete from the cache
     * @return number of bytes deleted
     */
    synchronized
    public long deleteAudioSortedByUsage(long minAudioFileSizeToDelete) {
        assertNotClosed();
        long deletedAudio = 0;
        final List<String> uuids = getUuidsSortedByUsage();
        final int count = uuids.size();
        for (int i=0; i<count; ++i) {
            Optional<CacheItem> optItem = findItemByUuid(uuids.get(i));
            if (optItem.isPresent()) {
                CacheItem item = optItem.get();
                long audioSizeOfItem = getAudioFileSize(item);
                if (deleteAudioForItem(item)) {
                    deletedAudio += audioSizeOfItem;
                }
                if (deletedAudio >= minAudioFileSizeToDelete) {
                    break;
                }
            }
        }
        return deletedAudio;
    }

    /**
     * Expire cache contents. For this operation the high and low watermarks are taken into account.
     * In case the current cache size is less than the high watermark, nothing will be done.
     * If it's higher than the high watermark, cache items are deleted until the audio cache size
     * is lower or equal to the low watermark setting. Cache items are deleted starting from their
     * oldest access timestamp.
     */
    private void expireCache() {
        long currentCacheSize = getAudioFileSize();
        if (currentCacheSize < mCacheSizeHighWatermark) {
            // we are still fine: nothing to expire
            return;
        }
        long sizeToBeFreed = currentCacheSize - mCacheSizeLowWatermark;
        if (sizeToBeFreed > 0) {
            // collect uuids of cache items necessary to fulfill freed size
            final List<CacheItem> allItemsSorted = collectItemsWithAudioSortedByTimestamp(sizeToBeFreed);
            // bulk delete found cache items
            for (CacheItem item: allItemsSorted) {
                final String utteranceText = item.getUtterance().getText();
                final String uuid = item.getUuid();
                final String textExcerpt = uuid + " ("
                        + utteranceText.substring(0, Math.min(8, utteranceText.length()))
                        + " ..)";
                if (deleteCacheItem(uuid)) {
                    Log.i(LOG_TAG, "Expired cache item " + textExcerpt);
                } else {
                    Log.e(LOG_TAG, "Couldn't delete cache item " + textExcerpt);
                }
            }
        }
    }
}
