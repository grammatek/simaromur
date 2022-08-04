package com.grammatek.simaromur;

import static com.grammatek.simaromur.cache.AudioFormat.AUDIO_FMT_PCM;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_16KHZ;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_22KHZ;
import static com.grammatek.simaromur.cache.UtteranceCacheManager.buildVoiceKey;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.util.Log;

import com.grammatek.simaromur.cache.AudioEntry;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.cache.PhonemeEntry;
import com.grammatek.simaromur.cache.Utterance;
import com.grammatek.simaromur.cache.UtteranceCache;
import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.cache.VoiceAudioDescription;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class UtteranceCacheManagerTest {
    private final static String LOG_TAG = UtteranceCacheManagerTest.class.getSimpleName();
    private static UtteranceCacheManager mCacheManager = null;
    private static final String MainUtteranceCacheFilename = "utterance_cache_test.pb";
    private static final String AltUtteranceCacheFilename = "utterance_cache_test_alternative.pb";
    private static long nBytesInCache = 0;
    private static final long LowWatermark = 7*1024*1024;
    private static final long HighWatermark = 2 * LowWatermark;

    // some values used for prefilling the cache

    // only tList values need to be unique
    final List<String> tList = new ArrayList<>(List.of("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8"));
    // nList needs to have the same size as tList
    final List<String> nList = new ArrayList<>(List.of("n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8"));
    final List<String> p1List = new ArrayList<>(List.of("p1", "p2", "p3", "p4"));
    final List<String> p2List = new ArrayList<>(List.of("p12", "p22", "p32", "p42",
            "p42", "p62", "p72", "p82"));
    final List<String> p3List = new ArrayList<>(List.of("p13", "p23", "p33", "p43",
            "p411", "p412", "p413", "p414", "p421", "p422", "p423", "p424"));

    public UtteranceCacheManagerTest() {
        synchronized(MainUtteranceCacheFilename) {
            if (mCacheManager == null) {
                // tests can be executed in multiple threads, so we need to make sure that
                // the stqtic object is a singleton
                mCacheManager = new UtteranceCacheManager(MainUtteranceCacheFilename, LowWatermark, HighWatermark);
            }
        }
    }

    // prefill cache with utterances, but no audio
    private void prefillCacheWithUtterances() {
        for (int i = 0; i < tList.size(); ++i) {
            mCacheManager.addUtterance(tList.get(i), nList.get(i), p1List);
        }
    }

    /**
     * Creates audio buffer with nSeconds seconds of audio for given frequency.
     * Samples have 16 Bit resolution.
     *
     * @param frequency     The frequency to use for the audio
     * @param nSeconds      number of seconds the audio should be
     * @param sampleRate    sample rate to use (e.g. 16000, 22050, 44100 ...)
     *
     * @return audio buffer of nSeconds seconds sinus audio
     */
    private static byte[] createSinusAudio(double frequency, int nSeconds, int sampleRate) {
        final int NSamples = sampleRate * nSeconds;
        byte[] bytes = new byte[NSamples*2];
        final double SamplesPerPeriod = sampleRate / frequency;
        for (int i = 0; i < NSamples; ++i) {
            double angle = 2.0 * Math.PI * i / SamplesPerPeriod;
            int val = (int) (Math.sin(angle) * 65535);
            bytes[i*2] = (byte) (val >> 8);
            bytes[i*2+1] = (byte) (val);
        }
        return bytes;
    }

    /**
     * Prefills cache with cache items: add dummy utterances and dummy audio
     */
    private void prefillCache() {
        prefillCacheWithUtterances();

        final String VoiceName = "Alfur";
        final String VoiceVersion = "v1";

        for (CacheItem item:mCacheManager.getCache().getEntriesMap().values()) {
            int phonemeCount = 1;
            for (PhonemeEntry phonemeEntry: item.getUtterance().getPhonemesList()) {
                final byte[] audioBuf =
                        createSinusAudio(440 * phonemeCount, 3 * phonemeCount, 22050);
                nBytesInCache += audioBuf.length;
                phonemeCount++;
                final VoiceAudioDescription vad = UtteranceCacheManager.newAudioDescription(AUDIO_FMT_PCM,
                        SAMPLE_RATE_22KHZ, audioBuf.length, VoiceName, VoiceVersion);
                assertTrue(mCacheManager.addAudioToCacheItem(item.getUuid(), phonemeEntry, vad, audioBuf));
            }
        }
        // TODO: we need several entries with different voice/voice version combinations
    }

    /**
     * Overfills cache with more audio data than the high watermark to trigger automatic cache expiration
     */
    private void overfillCache() {
        prefillCacheWithUtterances();

        final String VoiceName = "Alfur";
        final String VoiceVersion = "v1";

        for (CacheItem item:mCacheManager.getCache().getEntriesMap().values()) {
            int phonemeCount = 1;
            for (PhonemeEntry phonemeEntry: item.getUtterance().getPhonemesList()) {
                final byte[] audioBuf =
                        createSinusAudio(660 * phonemeCount, 5 * phonemeCount, 22050);
                phonemeCount++;
                final VoiceAudioDescription vad = UtteranceCacheManager.newAudioDescription(AUDIO_FMT_PCM,
                        SAMPLE_RATE_22KHZ, audioBuf.length, VoiceName, VoiceVersion);
                // this might or might not succeed, depending on if the cache item has been expired
                if (mCacheManager.addAudioToCacheItem(item.getUuid(), phonemeEntry, vad, audioBuf)) {
                    nBytesInCache += audioBuf.length;
                }
            }
        }
    }

    /**
     * Returns audio files of cache item belonging to a specific voice
     *
     * @param item          Cache item
     * @param voiceName     Voice name
     * @param voiceVersion  Voice version
     * @return      List of audio files
     */
    private List<String> audioFilesOfItemForVoice(CacheItem item, String voiceName, String voiceVersion) {
        final String voiceKey = buildVoiceKey(voiceName, voiceVersion);
        List<String> audioFiles = new ArrayList<>();
        for (VoiceAudioDescription vad : item.getVoiceAudioEntriesOrThrow(voiceKey).getAudioDescriptorsList()) {
            assertTrue(FileUtils.exists(vad.getPath()));
            audioFiles.add(vad.getPath());
        }
        return audioFiles;
    }

    /**
     * Returns all audio files of cache item independently from the voice
     *
     * @param item  cache item
     *
     * @return List of audio files
     */
    private List<String> audioFilesOfItem(CacheItem item) {
        List<String> audioFiles = new ArrayList<>();
        for (AudioEntry entry:item.getVoiceAudioEntriesMap().values()) {
            for (VoiceAudioDescription vad : entry.getAudioDescriptorsList()) {
                assertTrue(FileUtils.exists(vad.getPath()));
                audioFiles.add(vad.getPath());
            }
        }
        return audioFiles;
    }

    /**
     * Check given voice audio description against the one registered for cache item provided by uuid.
     * Also test, if the provided audio buffer matches the one on disk.
     *
     * @param uuid      The uuid of the cache item
     * @param vad       voice audio description to be tested against the registered descriptions
     * @param audioBuf  audio buffer
     */
    private void checkVoiceAudioInCacheAndOnDisk(String uuid, VoiceAudioDescription vad, byte[] audioBuf) {
        Optional<CacheItem>  optItem = mCacheManager.findItemByUuid(uuid);
        assertTrue(optItem.isPresent());
        final AudioEntry audioEntry = optItem.get().getVoiceAudioEntriesOrThrow(buildVoiceKey(vad.getVoiceName(), vad.getVoiceVersion()));
        for (VoiceAudioDescription listVad: audioEntry.getAudioDescriptorsList()) {
            if (vad == listVad && listVad.getFileSize() == audioBuf.length) {
                File audioFile = new File(listVad.getPath());
                assertTrue(audioFile.exists());
                try {
                    final byte[] bytesOnDisk = Files.readAllBytes(Paths.get(listVad.getPath()));
                    assertEquals(bytesOnDisk, audioBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }
    }

    private void testAvailabilityAndSorted(List<String> uuidsSorted) {
        long ts = 0;
        for (String s : uuidsSorted) {
            Optional<CacheItem> optItem = mCacheManager.findItemByUuid(s);
            assertTrue(optItem.isPresent());
            CacheItem item = optItem.get();

            long millis = UtteranceCacheManager.convertTimestampToMillis(item.getTimestamp());
            assertTrue(millis > ts);
            ts = millis;
            Log.i(LOG_TAG, "" + millis);
        }
    }

    @Before
    public void clearCache() {
        mCacheManager.clearCache();
        assertEquals(0, mCacheManager.getCache().getEntriesCount());
        assertEquals(0, mCacheManager.getAudioFileSize());
        nBytesInCache = 0;
    }

    @Test
    public void createAndRemoveFreshUtteranceCacheManager() {
        UtteranceCacheManager aFreshCacheManager = new UtteranceCacheManager(AltUtteranceCacheFilename, LowWatermark, HighWatermark);
        assertEquals(0, aFreshCacheManager.getCache().getEntriesCount());
        aFreshCacheManager.close();
    }

    @Test
    public void createAndRemoveUtteranceCacheManagerMultiple() {

        UtteranceCacheManager aCacheManager =
                new UtteranceCacheManager(AltUtteranceCacheFilename, LowWatermark, HighWatermark);
        assertEquals(0, aCacheManager.getCache().getEntriesCount());
        // needed to create a new UtteranceCacheManager, otherwise IllegalStateException is thrown
        aCacheManager.close();
        aCacheManager = new UtteranceCacheManager(AltUtteranceCacheFilename, LowWatermark, HighWatermark);
        assertEquals(0, aCacheManager.getCache().getEntriesCount());
        aCacheManager.close();
    }

    @Test
    public void createNewUtteranceEntryInCache() {
        String text = "texti";
        String normalized = "texti";
        String phoneme = "t_h E k s t I";

        // create the new utterance
        CacheItem item = mCacheManager.addUtterance(text, normalized, List.of(phoneme));
        Utterance anUtterance = item.getUtterance();

        // test for existence of creation parameters
        assertEquals(text, anUtterance.getText());
        assertEquals(normalized, anUtterance.getNormalized());
        assertEquals(1, anUtterance.getPhonemesList().size());

        // test for generated properties
        assertFalse(anUtterance.getFrontendVersion().isEmpty());

        // test if new utterance is part of the cache
        UtteranceCache cache = mCacheManager.getCache();
        assertEquals(1, cache.getEntriesCount());
        assertEquals(anUtterance, cache.getEntriesOrThrow(item.getUuid()).getUtterance());
    }

    @Test
    public void createNewUtteranceEntryInCacheOneByOne() {
        String text = "morgunblaðið";
        String normalized = "morgunblaðið .";
        String phonemes = "m O r k Y n p l a D I D ";
        CacheItem item = mCacheManager.addUtterance(text);
        assertTrue(item.getUtterance().getNormalized().isEmpty());

        Utterance updatedUtterance = UtteranceCacheManager.newUtterance(text, normalized, new ArrayList<>());
        item = mCacheManager.saveUtterance(updatedUtterance);
        assertFalse(item.getUtterance().getNormalized().isEmpty());

        Optional<CacheItem> optItem = mCacheManager.findItemByText(text);
        assertTrue(optItem.isPresent());
        CacheItem foundItem = optItem.get();
        assertEquals(normalized, foundItem.getUtterance().getNormalized());

        updatedUtterance = UtteranceCacheManager.newUtterance(text, normalized, List.of(phonemes));
        item = mCacheManager.saveUtterance(updatedUtterance);
        assertFalse(item.getUtterance().getPhonemesList().isEmpty());
        assertEquals(item.getUtterance().getPhonemesList().get(0).getSymbols(), phonemes);
    }

    @Test
    public void createNewCacheItemWithJustTextInCache() {
        String text = "a lonely raw text";

        // create the new utterance
        CacheItem item = mCacheManager.addUtterance(text);
        Utterance anUtterance = item.getUtterance();

        // test for existence of creation parameters
        assertEquals(text, anUtterance.getText());
        assertEquals("", anUtterance.getNormalized());
        assertEquals(0, anUtterance.getPhonemesList().size());

        // test for generated properties
        assertFalse(anUtterance.getFrontendVersion().isEmpty());

        // test if new utterance is part of the cache
        UtteranceCache cache = mCacheManager.getCache();
        assertEquals(1, cache.getEntriesCount());
        assertEquals(anUtterance, cache.getEntriesOrThrow(item.getUuid()).getUtterance());

        // the same text should return the same item and never increase the cache count
        CacheItem anotherItem = mCacheManager.addUtterance(text);
        Utterance anotherUtterance = anotherItem.getUtterance();
        assertEquals(anUtterance, anotherUtterance);
        assertEquals(1, cache.getEntriesCount());
    }

    @Test
    public void createMultipleUtteranceEntriesInCache() {
        prefillCacheWithUtterances();

        // test if all cache elements are found in original lists
        UtteranceCache cache = mCacheManager.getCache();
        assertEquals(tList.size(), cache.getEntriesCount());

        Map<String, CacheItem> cacheEntries = cache.getEntriesMap();
        for (CacheItem cacheEntry: cacheEntries.values()) {
            Utterance utterance = cacheEntry.getUtterance();
            assertTrue(tList.contains(utterance.getText()));
            assertTrue(nList.contains(utterance.getNormalized()));
            assertTrue(p1List.contains(utterance.getPhonemes(0).getSymbols()));
        }
    }

    @Test
    public void findExistingUtteranceInCache() {
        prefillCacheWithUtterances();
        Optional<CacheItem> item = mCacheManager.findItem("t4", "n4", p1List);
        assertTrue(item.isPresent());
        assertEquals("t4", item.get().getUtterance().getText());
        assertEquals("n4", item.get().getUtterance().getNormalized());
        assertEquals(p1List, item.get().getUtterance().getPhonemesList().stream().map(PhonemeEntry::getSymbols).collect(Collectors.toList()));
    }

    @Test
    public void findExistingTextInCache() {
        prefillCacheWithUtterances();
        Optional<CacheItem> item = mCacheManager.findItemByText("t4");
        assertTrue(item.isPresent());
        assertEquals("t4", item.get().getUtterance().getText());
    }

    @Test
    public void findExistingCacheItemByUUid() {
        prefillCacheWithUtterances();
        Optional<CacheItem> item1 = mCacheManager.findItem("t4", "n4", p1List);
        assertTrue(item1.isPresent());
        Optional<CacheItem> item2 = mCacheManager.findItemByUuid(item1.get().getUuid());
        assertTrue(item2.isPresent());
        assertEquals(item1.get(), item2.get());
    }

    @Test
    public void updateUtteranceOfExistingCacheItem() {
        prefillCache();
        Optional<CacheItem> optCacheItem = mCacheManager.findItem("t4", "n4", p1List);
        assertTrue(optCacheItem.isPresent());
        CacheItem t4item = optCacheItem.get();

        // add another phoneme and update the item in the cache
        Utterance.Builder newUtteranceBuilder = t4item.getUtterance().toBuilder();
        PhonemeEntry phonemeEntry = UtteranceCacheManager.newPhoneme("p4");

        Utterance utterance = newUtteranceBuilder.addPhonemes(phonemeEntry).build();
        assertNotNull(mCacheManager.updateUtterance(t4item.getUuid(), utterance));

        // find the item again and see if it has the updated field
        List<String> shouldHavePhonemes = new ArrayList<>(p1List);
        shouldHavePhonemes.add("p4");

        optCacheItem = mCacheManager.findItem("t4", "n4", shouldHavePhonemes);
        assertTrue(optCacheItem.isPresent());
        t4item = optCacheItem.get();
        assertEquals(shouldHavePhonemes.size(), t4item.getUtterance().getPhonemesList().size());

        // all audio should have been deleted, if the utterance has been updated
        assertEquals(0, t4item.getVoiceAudioEntriesCount());
    }

    @Test
    public void deleteCacheItem() {
        prefillCacheWithUtterances();
        // Fetch existing item from cache
        Optional<CacheItem> optCacheItem = mCacheManager.findItem("t2", "n2", p1List);
        assertTrue(optCacheItem.isPresent());
        CacheItem item = optCacheItem.get();
        String itemUuid = item.getUuid();
        assertTrue(mCacheManager.findItemByUuid(itemUuid).isPresent());
        int nEntries = mCacheManager.getCache().getEntriesCount();

        // delete item and test for unavailability of meta data
        assertTrue(mCacheManager.deleteCacheItem(itemUuid));
        assertFalse(mCacheManager.findItemByUuid(itemUuid).isPresent());

        // cache size should be one less than before
        assertEquals(nEntries - 1, mCacheManager.getCache().getEntriesCount());
    }

    @Test
    public void deleteCacheItemAndAllAudio() {
        // if a cache Item is deleted, the related audio should be deleted from the file system
        prefillCache();
        Optional<CacheItem> optCacheItem = mCacheManager.findItem("t2", "n2", p1List);
        assertTrue(optCacheItem.isPresent());
        CacheItem item = optCacheItem.get();

        // remember all audios of the item
        Collection<AudioEntry> audioEntries = item.getVoiceAudioEntriesMap().values();
        assertTrue(audioEntries.size() > 0);

        // delete item
        assertTrue(mCacheManager.deleteCacheItem(item.getUuid()));

        // test for unavailability of files
        for (AudioEntry audioEntry: audioEntries) {
            for (VoiceAudioDescription vad:audioEntry.getAudioDescriptorsList()) {
                assertFalse(FileUtils.exists(vad.getPath()));
            }
        }
    }

    @Test
    public void noDoubleCacheItems() {
        prefillCacheWithUtterances();
        UtteranceCache prevCache = mCacheManager.getCache();

        int nItems = prevCache.getEntriesCount();
        prefillCacheWithUtterances();
        assertEquals(nItems, prevCache.getEntriesCount());
        assertEquals(prevCache, mCacheManager.getCache());
    }

    @Test
    public void addAudioForPhoneme() {
        // find item, generate audio for all phonemes and attach audio to cache item
        prefillCacheWithUtterances();

        Utterance utterance = UtteranceCacheManager.newUtterance("t1", "n1", p1List);
        Optional<CacheItem> optItem = mCacheManager.findItemByText(utterance.getText());
        assertTrue(optItem.isPresent());
        CacheItem item = optItem.get();

        final String voiceName = "Alfur";
        final String voiceVersion = "v1";
        int phonemeCount = 1;
        // TODO: this needs to be simplified
        final List<PhonemeEntry> phonemeList = item.getUtterance().getPhonemesList();
        assertTrue(phonemeList.size() != 0);

        for (PhonemeEntry phonemeEntry: phonemeList) {
            final byte[] audioBuf =
                    createSinusAudio(440 * phonemeCount, 3 * phonemeCount, 22050);
            phonemeCount++;
            final VoiceAudioDescription vad = UtteranceCacheManager.newAudioDescription(AUDIO_FMT_PCM,
                    SAMPLE_RATE_22KHZ, audioBuf.length, voiceName, voiceVersion);
            // attach it to a cacheItem
            long ts1 = mCacheManager.getTimestampMillis(utterance);
            long usageCnt = mCacheManager.getUsageCount(utterance);

            assertTrue(mCacheManager.addAudioToCacheItem(item.getUuid(), phonemeEntry, vad, audioBuf));

            assertTrue(mCacheManager.getUsageCount(utterance) > usageCnt);
            assertTrue(mCacheManager.getTimestampMillis(utterance) > ts1);
            checkVoiceAudioInCacheAndOnDisk(item.getUuid(), vad, audioBuf);
        }
    }

    @Test
    public void updateAudioForPhoneme() {
        // update one well-known utterance with a different audio
        prefillCache();

        Utterance utterance = UtteranceCacheManager.newUtterance("t1", "n1",p1List);
        Optional<CacheItem> optItem = mCacheManager.findItemByText(utterance.getText());
        assertTrue(optItem.isPresent());
        CacheItem item = optItem.get();

        final String voiceName = "Alfur";
        final String voiceVersion = "v1";
        int phonemeCount = 1;
        final List<PhonemeEntry> phonemeList = item.getUtterance().getPhonemesList();
        assertTrue(phonemeList.size() != 0);

        for (PhonemeEntry phonemeEntry: phonemeList) {
            final byte[] audioBuf =
                    createSinusAudio(1600 * phonemeCount, 3 * phonemeCount, 16000);
            phonemeCount++;
            final VoiceAudioDescription vad = UtteranceCacheManager.newAudioDescription(AUDIO_FMT_PCM,
                    SAMPLE_RATE_16KHZ, audioBuf.length, voiceName, voiceVersion);
            // update the cacheItem
            long ts1 = mCacheManager.getTimestampMillis(utterance);
            assertTrue(mCacheManager.updateAudio(item, phonemeEntry, vad, audioBuf));
            assertTrue(mCacheManager.getTimestampMillis(utterance) > ts1);

            String uuid = item.getUuid();
            // test if the new audio has been updated in meta-data and on the file system
            checkVoiceAudioInCacheAndOnDisk(uuid, vad, audioBuf);
        }
    }

    @Test
    public void deleteAudioForVoice() {
        // delete all audio of a voice
        prefillCache();

        // find cache item
        final String aText="t2";
        Optional<CacheItem> optItem = mCacheManager.findItemByText(aText);
        assertTrue(optItem.isPresent());
        CacheItem item = optItem.get();

        // try to delete audio for non-existing voice
        final String voiceName = "Alfur";
        final String voiceVersionInvalid = "V2";
        final String voiceVersionValid = "v1";
        assertFalse(mCacheManager.deleteAudioForVoice(item, voiceName, voiceVersionInvalid));

        // note references of existing audio files for the voice
        final List<String> deletedFiles = audioFilesOfItemForVoice(item, voiceName, voiceVersionValid);

        final long oldAudioFileSize = mCacheManager.getAudioFileSize(item);
        final long oldCacheFileSize = mCacheManager.getAudioFileSize();

        // remember audio entries
        final String voiceKey = buildVoiceKey(voiceName, voiceVersionValid);
        AudioEntry audioEntry = item.getVoiceAudioEntriesOrThrow(voiceKey);
        assertEquals(audioEntry.getAudioDescriptorsList().stream().mapToLong(VoiceAudioDescription::getFileSize).sum(), oldAudioFileSize);

        // delete audio for voice
        assertTrue(mCacheManager.deleteAudioForVoice(item, voiceName, voiceVersionValid));

        // test if the deleted audio has been wiped from meta-data and on the file system
        // for this to work, we need to retrieve item from the cache again
        optItem = mCacheManager.findItemByText(aText);
        assertTrue(optItem.isPresent());
        item = optItem.get();

        try {
            item.getVoiceAudioEntriesOrThrow(voiceKey);
            // the above should throw
            fail();
        } catch (Exception e) {
            // expected: no audio with given voiceKey available
        }
        // also all related files should have disappeared
        for (String fileName:deletedFiles) {
            assertFalse(FileUtils.exists(fileName));
        }

        // and the cache size has decreased
        final long newAudioFileSize = mCacheManager.getAudioFileSize(item);
        assertTrue(oldAudioFileSize > newAudioFileSize);
        assertEquals(mCacheManager.getAudioFileSize(), oldCacheFileSize - oldAudioFileSize + newAudioFileSize);
    }

    @Test
    public void deleteAudioForUtterance() {
        prefillCache();
        final String aValidText="t2";
        final String anInvalidText="t321";

        // try to delete audio for non-existing text
        assertFalse(mCacheManager.deleteAudioForText(anInvalidText));

        // get all files belonging to a text
        Optional<CacheItem> opItem = mCacheManager.findItemByText(aValidText);
        assertTrue(opItem.isPresent());
        CacheItem item = opItem.get();
        final List<String> files = audioFilesOfItem(item);
        assertTrue(files.size() > 0);
        assertTrue(mCacheManager.deleteAudioForText(aValidText));
    }

    @Test
    public void getOverallAudioFileSize() {
        prefillCache();
        assertEquals(nBytesInCache, mCacheManager.getAudioFileSize());
        Log.i(LOG_TAG, "Overall cache audio file size: " + mCacheManager.getAudioFileSize());
        clearCache();
        assertEquals(nBytesInCache, mCacheManager.getAudioFileSize());
        assertEquals(0, mCacheManager.getAudioFileSize());
    }

    @Test
    public void getAudioFileSizePerVoice() {
        final List<String> voicesInCache = mCacheManager.getAvailableVoices();
        for (String voiceKey: voicesInCache) {
            final String[] splitVoiceKey = voiceKey.split(":");
            assertEquals(2, splitVoiceKey.length);
            String voiceName = splitVoiceKey[0];
            String voiceVersion = splitVoiceKey[1];
            assertTrue(mCacheManager.getAudioFileSize(voiceName, voiceVersion) > 0);
        }
    }

    @Test
    public void getAvailableVoices() {
        prefillCache();
        final List<String> voicesInCache = mCacheManager.getAvailableVoices();
        assertTrue(voicesInCache.size() > 0);
        assertEquals(1, voicesInCache.size());
    }

    @Test
    public void updateUsageCountForPhoneme() {
        // update the usage count for one well-known utterance/audio description
        prefillCache();
        final String voiceName = "Alfur";
        final String voiceVersionInvalid = "V2";
        final String voiceVersionValid = "v1";
        Utterance utterance = UtteranceCacheManager.newUtterance("t1", "n1", p1List);

        // retrieve usage count of utterance
        long utteranceUsageCount = mCacheManager.getUsageCount(utterance);

        // test invalid voice first
        List<byte[]>  audioList = mCacheManager.getAudioForUtterance(utterance, voiceName, voiceVersionInvalid);
        assertTrue(audioList.isEmpty());
        assertEquals(utteranceUsageCount, mCacheManager.getUsageCount(utterance));

        // 1. implicitly increase usage counter by retrieving audio
        audioList = mCacheManager.getAudioForUtterance(utterance, voiceName, voiceVersionValid);
        assertFalse(audioList.isEmpty());
        assertEquals(utteranceUsageCount + 1, mCacheManager.getUsageCount(utterance));

        // 2. explicitly increase usage counter
        assertTrue(mCacheManager.increaseUsageCount(utterance));
        assertEquals(utteranceUsageCount + 2, mCacheManager.getUsageCount(utterance));
    }

    @Test
    public void updateTimestampOfCacheItem() {
        // update the timestamp for well-known utterance
        prefillCache();
        Utterance utterance = UtteranceCacheManager.newUtterance("t1", "n1", p1List);

        // retrieve last usage timestamp of utterance
        long ts1 = mCacheManager.getTimestampMillis(utterance);
        assertTrue(ts1 > 0);
        assertTrue(System.currentTimeMillis() > ts1);

        // explicitly update timestamp
        mCacheManager.updateTimestamp(utterance);
        assertTrue(mCacheManager.getTimestampMillis(utterance) > ts1);
    }

    @Test
    public void findOldestCacheItems() {
        prefillCache();
        // find the N oldest cache items
        long nItems = mCacheManager.getItemCount();
        assertEquals(tList.size(), nItems);

        List<String> uuidsSorted = mCacheManager.getUuidsSortedByTimestamp();
        assertEquals(tList.size(), uuidsSorted.size());
        testAvailabilityAndSorted(uuidsSorted);

        // now we want only the N oldest entries
        for (int i = 0; i < nItems; ++i) {
            uuidsSorted = mCacheManager.getUuidsSortedByTimestamp(i);
            assertEquals(i, uuidsSorted.size());
            testAvailabilityAndSorted(uuidsSorted);
        }
    }

    @Test
    public void deleteOldestAudioUntilSize() {
        prefillCache();

        // delete half of the cache audio
        final long audioFileSizeToDelete = mCacheManager.getAudioFileSize() / 2;
        long audioFileSizeDeleted = mCacheManager.deleteAudioSortedByTimestamp(audioFileSizeToDelete);
        assertTrue(audioFileSizeDeleted >= audioFileSizeToDelete);
        assertTrue(mCacheManager.getAudioFileSize() <= audioFileSizeToDelete);

        // if we pass a negative value, 0 is returned
        assertEquals(0, mCacheManager.deleteAudioSortedByTimestamp(-1));

        // if we pass mCacheManager.getAudioFileSize(), exactly this size is deleted and returned
        final long remainingAudioFileSize = mCacheManager.getAudioFileSize();
        assertEquals(remainingAudioFileSize, mCacheManager.deleteAudioSortedByTimestamp(remainingAudioFileSize));

        // there should be no audio left
        assertEquals(0, mCacheManager.getAudioFileSize());

        // ... and we cannot delete any more audio
        assertEquals(0, mCacheManager.deleteAudioSortedByTimestamp(1000));
    }

    @Test
    public void expireLeastUsedCacheItemsUntilSize() {
        prefillCache();

        // delete half of the cache audio
        final long audioFileSizeToDelete = mCacheManager.getAudioFileSize() / 2;
        long audioFileSizeDeleted = mCacheManager.deleteAudioSortedByUsage(audioFileSizeToDelete);
        assertTrue(audioFileSizeDeleted >= audioFileSizeToDelete);
        assertTrue(mCacheManager.getAudioFileSize() <= audioFileSizeToDelete);

        // if we pass a negative value, 0 is returned
        assertEquals(0, mCacheManager.deleteAudioSortedByUsage(-1));

        // if we pass mCacheManager.getAudioFileSize(), exactly this size is deleted and returned
        final long remainingAudioFileSize = mCacheManager.getAudioFileSize();
        assertEquals(remainingAudioFileSize, mCacheManager.deleteAudioSortedByUsage(remainingAudioFileSize));

        // there should be no audio left
        assertEquals(0, mCacheManager.getAudioFileSize());

        // ... and we cannot delete any more audio
        assertEquals(0, mCacheManager.deleteAudioSortedByUsage(1000));
    }

    @Test
    public void lowHighWatermarkCacheSize() {
        prefillCache();
        final long audioCacheSize = mCacheManager.getAudioFileSize();
        overfillCache();
        assertNotEquals(audioCacheSize, mCacheManager.getAudioFileSize());
        assertTrue(mCacheManager.getAudioFileSize() < HighWatermark);
        assertTrue(mCacheManager.getAudioFileSize() < LowWatermark);
    }

}

