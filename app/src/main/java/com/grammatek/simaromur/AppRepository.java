package com.grammatek.simaromur;

import static android.speech.tts.TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.cache.Utterance;
import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.db.AppData;
import com.grammatek.simaromur.db.AppDataDao;
import com.grammatek.simaromur.db.ApplicationDb;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.db.VoiceDao;
import com.grammatek.simaromur.device.DownloadVoiceManager;
import com.grammatek.simaromur.device.SymbolsLvLIs;
import com.grammatek.simaromur.device.TTSAudioControl;
import com.grammatek.simaromur.device.TTSEngineController;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.frontend.FrontendManager;
import com.grammatek.simaromur.device.AssetVoiceManager;
import com.grammatek.simaromur.network.ConnectionCheck;
import com.grammatek.simaromur.utils.FileUtils;
import com.grammatek.simaromur.network.api.SpeakController;
import com.grammatek.simaromur.network.api.VoiceController;
import com.grammatek.simaromur.network.api.pojo.SpeakRequest;
import com.grammatek.simaromur.network.api.pojo.VoiceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstracted application repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
//@Singleton
public class AppRepository {
    private final static String LOG_TAG = "Simaromur_" + AppRepository.class.getSimpleName();
    private final static long NETWORK_VOICE_QUERY_TIME_MS = 1000 * 60 * 30;   // 30 min.
    private final AppDataDao mAppDataDao;
    private final VoiceDao mVoiceDao;
    private LiveData<AppData> mAppData;
    private LiveData<List<com.grammatek.simaromur.db.Voice>> mAllVoices;
    private AppData mCachedAppData;
    private List<com.grammatek.simaromur.db.Voice> mAllCachedVoices;
    private final VoiceController mNetworkVoiceController;
    private final SpeakController mNetworkSpeakController;
    private final ApiDbUtil mApiDbUtil;
    private final AssetVoiceManager mAVM;
    private final DownloadVoiceManager mDVM;
    // audio cache low/high watermark: 48/72MB, @todo: make configurable
    private static final long CacheLowWatermark = 48 * 1024 * 1024;
    private static final long CacheHighWatermark = (long) (1.5 * CacheLowWatermark);
    private final UtteranceCacheManager mUtteranceCacheManager;
    // in TTSService.onSynthesizeText() we receive items of this queue and send them from either
    // a.) TTS worker threads or b.) via TTSService.onStop()
    private final LinkedBlockingQueue<TTSProcessingResult> mTTSProcessingResultQueue = new LinkedBlockingQueue<>();

    ScheduledExecutorService mScheduler;
    FrontendManager mFrontend;
    static TTSEngineController mTTSEngineController;

    // this saves the voice name to use for the next speech synthesis
    private Voice mSelectedVoice;

    private final MediaPlayObserver mMediaPlayer;

    // contains the currently handled tts request from onSynthesizeText()
    private TTSRequest mCurrentRequest;

    /**
     * Returns configuration value for given key in the config.properties file.
     *
     * @param key   key to look up
     * @return    value for key or empty string if not found
     */
    public String getAssetConfigValueFor(String key) {
        String rv = "";
        try {
            String value = FileUtils.getAssetConfigProperty(App.getContext().getAssets(), key);
            if (value != null) {
                rv = value;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rv;
    }

    /**
     * Deletes voice model data for given voice. The voice needs to be a downloadable voice. In case
     * The voice model data hasn't been downloaded yet or is the current voice, false is returned.
     */
    public boolean deleteVoiceAsync(Voice voice, VoiceInfo.DeleteVoiceObserver deleteVoiceObserver) {
        if (voice == null) {
            Log.v(LOG_TAG, "voice is NULL");
            return false;
        }
        if (isCurrentVoice(voice)) return false;

        mDVM.deleteVoice(voice, deleteVoiceObserver, mVoiceDao);
        mTTSEngineController.UnloadEngine();
        return true;
    }

    /**
     * Returns true if the given voice is the currently selected voice.
     * @param voice     voice to check
     * @return          true if voice is currently selected
     */
    public boolean isCurrentVoice(Voice voice) {
        Voice existingVoice = mVoiceDao.findVoiceWithId(voice.voiceId);
        return mCachedAppData.currentVoiceId == voice.voiceId || existingVoice == null;
    }

    /**
     * Download given voice from voice repository asynchronously. After download is finished or
     * has failed, the given observer is called.
     *
     * @param voice             Voice to download
     * @param finishedObserver  VoiceInfo object to update
     */
    public void downloadVoiceAsync(Voice voice, DownloadVoiceManager.Observer finishedObserver) {
        // when the download is successful, the voice is updated in the database. This happens
        // asynchonously.
        mDVM.downloadVoiceAsync(voice, finishedObserver, mVoiceDao);
    }

    /**
     * Cancels an eventual download of a voice.
     */
    public void cancelDownloadVoice() {
        mDVM.cancelCurrentDownload();
    }

    /**
     * Get the voice download path.
     *
     * @return  Path to download voice files to.
     */
    public String getVoicePath() {
        return mDVM.getVoiceDownloadPath();
    }

    /**
     * Observer for Network voice query results.
     */
    class NetworkVoiceQueryObserver implements VoiceController.VoiceObserver {
        public NetworkVoiceQueryObserver() {
        }

        public void update(List<VoiceResponse> voices) {
            for (VoiceResponse voice : voices) {
                if (voice == null) {
                    Log.e(LOG_TAG, "Network API returned null voice ?!");
                    return;
                }
                Log.v(LOG_TAG, "Network API returned: " + voice.VoiceId);
            }
            new updateVoicesAsyncTask(mApiDbUtil, Voice.TYPE_NETWORK).execute(voices);
            new updateAppDataVoiceListTimestampAsyncTask(mAppDataDao).execute();
        }

        public void error(String errorMsg) {
            Log.e(LOG_TAG, "NetworkVoiceQueryObserver()::error: " + errorMsg);
        }
    }

    // Note that in order to unit test the AppRepository, you have to remove the Application
    // dependency.
    public AppRepository(Application application) throws IOException {
        Log.v(LOG_TAG, "AppRepository()");
        mAllCachedVoices = new ArrayList<>();
        ApplicationDb db = ApplicationDb.getDatabase(application);
        mUtteranceCacheManager = new UtteranceCacheManager("utterance_cache.pb",
                CacheLowWatermark, CacheHighWatermark);
        mAppDataDao = db.appDataDao();
        mVoiceDao = db.voiceDao();
        mApiDbUtil = new ApiDbUtil(mVoiceDao);
        mAVM = new AssetVoiceManager(App.getContext());
        mDVM = new DownloadVoiceManager();
        mFrontend = new FrontendManager(App.getContext());
        mTTSEngineController = new TTSEngineController(mAVM, mDVM);
        mNetworkSpeakController = new SpeakController();
        mNetworkVoiceController = new VoiceController();
        mAppData = mAppDataDao.getLiveAppData();
        mAppData.observeForever(appData -> {
            Log.v(LOG_TAG, "mAppData update: " + appData);
            if (appData == null) {
                return;
            }
            // Update cached appData
            mCachedAppData = appData;
            // don't access any network from here, as this is called from the main thread
            mDVM.readVoiceDescription(false);
            // preload selected voice
            Voice selectedVoice = mVoiceDao.findVoiceWithId(mCachedAppData.currentVoiceId);
            if (selectedVoice != null) {
                if (mSelectedVoice == null || (!mSelectedVoice.name.equals(selectedVoice.name))) {
                    loadVoice(selectedVoice.name);
                }
            }
            // user consent can change anytime
            final Boolean setCrashLytics = appData.crashLyticsUserConsentGiven;
            App.setFirebaseAnalytics(setCrashLytics);
        });
        mAllVoices = mVoiceDao.getAllVoices();
        mAllVoices.observeForever(voices -> {
            Log.v(LOG_TAG, "mAllVoices update: " + voices);
            // Update cached voices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                mAllCachedVoices = Objects.requireNonNullElseGet(voices, ArrayList::new);
            } else {
                mAllCachedVoices = Objects.requireNonNullElse(voices, new ArrayList<>());
            }
        });

        mMediaPlayer = new MediaPlayObserver();
        mScheduler = Executors.newSingleThreadScheduledExecutor();
        // only do this once at the beginning
        mScheduler.schedule(assetVoiceRunnable, 0, TimeUnit.SECONDS);
    }

    /**
     * Enqueues a TTS processing result to be processed by the TTS engine.
     *
     * @param element the element to be processed
     */
    public void enqueueTTSProcessingResult(TTSProcessingResult element) {
        mTTSProcessingResultQueue.offer(element);
    }

    /**
     * Returns the next TTS processing result from the queue.
     *
     * @return the next TTS processing result from the queue
     * @throws InterruptedException if the thread is interrupted
     */
    public TTSProcessingResult dequeueTTSProcessingResult() throws InterruptedException {
        Log.v(LOG_TAG, "dequeueTTSProcessingResult()");
        return mTTSProcessingResultQueue.take();
    }

    /**
     * Triggers an update of the server voice description asynchronously. The update is triggered
     * immediately.
     */
    public void triggerServerVoiceUpdate() {
        Log.v(LOG_TAG, "triggerServerVoiceUpdate()");
        mScheduler.schedule(onDeviceVoicesUpdateRunnable, 0, TimeUnit.SECONDS);
    }

    /**
     * Returns the utterance cache manager
     *
     * @return instance of the utterance cache manager
     */
    public UtteranceCacheManager getUtteranceCache() {
        return mUtteranceCacheManager;
    }

    synchronized
    public TTSRequest getCurrentTTsRequest() {
        return mCurrentRequest;
    }

    synchronized
    public void setCurrentTTSRequest(TTSRequest currentRequest) {
        mCurrentRequest = currentRequest;
        if (mCurrentRequest != null) {
            Log.v(LOG_TAG, "setCurrentTTSRequest(): " + currentRequest.serialize());
        } else {
            Log.v(LOG_TAG, "setCurrentTTSRequest(): Stopping current utterance");
        }
    }

    public FrontendManager getFrontendManager() {
        return mFrontend;
    }

    /**
     * Returns AppData
     *
     * @return single instance of the AppData as LiveData
     */
    public LiveData<AppData> getAppData() {
        Log.v(LOG_TAG, "getAppData");
        if (mAppData == null) {
            mAppData = mAppDataDao.getLiveAppData();
        }
        return mAppData;
    }

    /**
     * Returns observed/cached AppData
     *
     * @return single instance of the AppData
     */
    public AppData getCachedAppData() {
        Log.v(LOG_TAG, "getCachedAppData");
        return mCachedAppData;
    }

    /**
     * Returns list of all voices.
     *
     * @return list of all persisted voices as LiveData
     */
    public LiveData<List<com.grammatek.simaromur.db.Voice>> getAllVoices() {
        Log.v(LOG_TAG, "getAllVoices");
        if (mAllVoices == null) {
            mAllVoices = mVoiceDao.getAllVoices();
        }
        return mAllVoices;
    }

    /**
     * Returns list of all observed/cached voices. If there are any changes in the model,
     * this list will be updated.
     *
     * @return list of all cached voices
     */
    public final List<com.grammatek.simaromur.db.Voice> getCachedVoices() {
        Log.v(LOG_TAG, "getCachedVoices");
        return mAllCachedVoices;
    }

    /**
     * Request network voices from its API endpoint.
     *
     * @param languageCode language code, e.g. "is-IS"
     * @todo Do this regularly via a timer
     * @todo If we feed "is-IS" here, then TTS service doesn' proceed, check it out !
     */
    public void streamNetworkVoices(String languageCode) {
        Log.v(LOG_TAG, "streamNetworkVoices");
        mNetworkVoiceController.streamQueryVoices(languageCode, new NetworkVoiceQueryObserver());
    }

    /**
     * Request to network TTS to speak given text and return Audio asynchronously.
     *  @param voiceId  the voice name identifier of the TTS API
     * @param version   the voice version identifier of the TTS API
     * @param item     cache item of the utterance
     * @param langCode language code, e.g. "is-IS"
     * @param finishedObserver the observer to be notified when the audio is finished. e.g. for
     */
    public void startNetworkSpeak(String voiceId, String version, CacheItem item, String langCode,
                                  TTSAudioControl.AudioFinishedObserver finishedObserver) {
        mMediaPlayer.stop();
        TTSRequest ttsRequest = new TTSRequest(item.getUuid());
        if (playIfAudioCacheHit(voiceId, version, item, finishedObserver, ttsRequest)) return;

        final String SampleRate = "" + AudioManager.SAMPLE_RATE_MP3;
        // get normalized text from item
        String normalizedText = item.getUtterance().getNormalized();
        if (normalizedText.isEmpty()) {
            Log.w(LOG_TAG, "startNetworkSpeak: text has no content ?!");
            // TODO: play silence ?
            return;
        }
        SpeakRequest request = new SpeakRequest("standard", langCode,
                "mp3", SampleRate, normalizedText, "text", voiceId);
        mMediaPlayer.getMediaPlayer().setOnCompletionListener(new MediaPlayObserver.MPOnCompleteListener(finishedObserver));
        mNetworkSpeakController.streamAudio(request, mMediaPlayer, item, ttsRequest);
    }

    /**
     * Query the speech audio cache for existence of already produced audio. If the audio is found,
     * play it directly with the media player and return true. Otherwise, return false. The
     * given AudioFinishedObserver's update() method is called in case of any error or if the
     * MediaPlayer finishes playback.
     *
     * @param voiceId          Voice id / voice name of the audio entry for given cache item
     * @param item             Cache item to examine for an audio entry
     * @param finishedObserver the AudioFinishedObserver to be called in case of completion of media
     *                         playback or in case of an error
     * @param ttsRequest       the TTSRequest for the current request
     * @return true in case audio speech entry has been found and playback started, false otherwise
     */
    private boolean playIfAudioCacheHit(String voiceId, String voiceVersion, CacheItem item, TTSAudioControl.AudioFinishedObserver finishedObserver, TTSRequest ttsRequest) {
        Log.v(LOG_TAG, "playIfAudioCacheHit(1)");
        UtteranceCacheManager ucm = App.getAppRepository().getUtteranceCache();
        final List<byte[]> audioBuffers =
                ucm.getAudioForUtterance(item.getUtterance(), voiceId, voiceVersion);
        byte[] data;
        if (!audioBuffers.isEmpty()) {
            data = audioBuffers.get(0);
            // TODO: check if correct audio format !
            Log.v(LOG_TAG, "Playing back cached audio of size " + data.length);
            mMediaPlayer.getMediaPlayer().setOnCompletionListener(new MediaPlayObserver.MPOnCompleteListener(finishedObserver));
            mMediaPlayer.update(data, ttsRequest);
            return true;
        }
        return false;
    }

    /**
     * Query the speech audio cache for existence of already produced audio. If the audio is found,
     * play it directly with the media player and return true. Otherwise, return false. The
     * given TTSObserver's update() method is called in case the MediaPlayer finishes playback.
     *
     * @param voiceId     Voice id / voice name of the audio entry for given cache item
     * @param item        Cache item to examine for an audio entry
     * @param ttsObserver the TTSObserver to be called for audio data
     * @param ttsRequest  the TTSRequest to be passed to the TTSObserver
     * @return true in case audio speech entry has been found and playback started, false otherwise
     */
    private boolean playIfAudioCacheHit(String voiceId, String voiceVersion, CacheItem item, TTSObserver ttsObserver, TTSRequest ttsRequest) {
        Log.v(LOG_TAG, "playIfAudioCacheHit(2)");
        UtteranceCacheManager ucm = App.getAppRepository().getUtteranceCache();
        final List<byte[]> audioBuffers =
                ucm.getAudioForUtterance(item.getUtterance(), voiceId, voiceVersion);
        byte[] audioData;
        if (!audioBuffers.isEmpty()) {
            audioData = audioBuffers.get(0);
            Log.v(LOG_TAG, "Playing back cached audio of size " + audioData.length);
            ttsObserver.update(audioData, ttsRequest);
            return true;
        }
        return false;
    }

    /**
     * Stops speaking current voice, if playing.
     */
    public void stopNetworkSpeak() {
        mNetworkSpeakController.stop();
        mMediaPlayer.stop();
    }

    /**
     * Initiates voice audio generation via network TTS api and returns it asynchronously via
     * mTTSProcessingResultQueue.
     *
     * @param voice      The voice to use
     * @param item       utterance cache item for retrieving text to speak
     * @param ttsRequest the TTSRequest assigned to the utterance
     * @param speed      speed to use for the voice audio
     * @param pitch      pitch to use for the voice audio
     */
    public void startNetworkTTS(Voice voice, CacheItem item, TTSRequest ttsRequest, float speed, float pitch) {
        Log.v(LOG_TAG, "startNetworkTTS: " + item.getUuid());
        // map given voice to voiceId
        if (voice != null) {
            final TTSObserver ttsObserver = new TTSObserver(pitch, speed, AudioManager.SAMPLE_RATE_WAV);
            if (playIfAudioCacheHit(voice.internalName, voice.version, item, ttsObserver, ttsRequest)) return;

            final String SampleRate = "" + AudioManager.SAMPLE_RATE_WAV;
            final String normalized = item.getUtterance().getNormalized();
            if (normalized.trim().isEmpty()) {
                Log.w(LOG_TAG, "startNetworkTTS: given text is whitespace only ?!");
            }
            SpeakRequest request = new SpeakRequest("standard", voice.languageCode,
                    "pcm", SampleRate, normalized, "text", voice.internalName);
            mNetworkSpeakController.streamAudio(request, ttsObserver, item, ttsRequest);
        } else {
            Log.e(LOG_TAG, "startNetworkTTS: given voice is null ?!");
        }
    }

    /**
     * Initiates voice audio generation on the device and returns it asynchronously via
     * mTTSProcessingResultQueue.
     *
     * @param voice The voice to use
     * @param item  utterance cache item for retrieving text to speak
     * @param ttsRequest TTSRequest assigned from TTSService.onSynthesizeText()
     * @param speed speed to use for the voice audio
     * @param pitch pitch to use for the voice audio
     */
    public void startDeviceTTS(Voice voice, CacheItem item, TTSRequest ttsRequest, float speed, float pitch) {
        Log.v(LOG_TAG, "startDeviceTTS: " + item.getUuid());
        if (voice != null) {
            try {
                mTTSEngineController.LoadEngine(voice);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            final TTSObserver ttsObserver = new TTSObserver(pitch, speed, mTTSEngineController.getEngine().GetNativeSampleRate());
            if (playIfAudioCacheHit(voice.internalName, voice.version, item, ttsObserver, ttsRequest)) return;
            mTTSEngineController.StartSpeak(new TTSObserver(pitch, speed,
                    mTTSEngineController.getEngine().GetNativeSampleRate()), ttsRequest);
        } else {
            Log.e(LOG_TAG, "startDeviceTTS: given voice is null ?!");
        }
    }

    /**
     * Initiates voice audio generation on the device and returns it asynchronously via
     * given observer.
     *
     * @param voice The voice to use
     * @param item utterance cache item for retrieving text to speak
     * @param speed speed to use for the voice audio
     * @param pitch pitch to use for the voice audio
     * @param observer the TTSObserver to be called for audio data
     * @return  the speak task associated with the request
     */
    public TTSEngineController.SpeakTask startDeviceSpeak(Voice voice, CacheItem item, float speed,
                                                          float pitch, TTSAudioControl.AudioFinishedObserver observer) {
        try {
            mTTSEngineController.LoadEngine(voice);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return mTTSEngineController.StartSpeak(item, speed, pitch,
                mTTSEngineController.getEngine().GetNativeSampleRate(), observer, getCurrentTTsRequest());
    }

    /**
     * Stops speaking current voice, if playing.
     * @param speakTask the speak task to be stopped
     */
    public void stopDeviceSpeak(TTSEngineController.SpeakTask speakTask) {
        mTTSEngineController.StopSpeak(speakTask);
    }

    /**
     * Loads given voice, e.g. from disk. Can also access network to query voice availability.
     *
     * @param voiceName Name of the voice to load
     * @return TextToSpeech.SUCCESS in case operation successful, TextToSpeech.ERROR otherwise
     * @note : Is called on the synthesis thread, so it's safe to call even DB-methods here
     */
    public int loadVoice(String voiceName) {
        Log.v(LOG_TAG, "loadVoice: (" + voiceName + ")");
        List<Voice> voices = mVoiceDao.getAnyVoices();
        for (final Voice voice : voices) {
            if (voice.name.equals(voiceName)) {
                if (voice.type.equals(Voice.TYPE_ONNX)) {
                    try {
                        mTTSEngineController.LoadEngine(voice);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return TextToSpeech.ERROR_NOT_INSTALLED_YET;
                    }
                }
                mSelectedVoice = voice;
                mAppDataDao.selectCurrentVoice(voice);
                Log.v(LOG_TAG, "SUCCESS");
                return TextToSpeech.SUCCESS;
            }
        }
        Log.v(LOG_TAG, "ERROR");
        return TextToSpeech.ERROR;
    }

    /**
     * Returns the version of the given voice.
     *
     * @param internalVoiceName the internal name of the voice
     * @return  the version of the voice
     */
    public String getVersionOfVoice(String internalVoiceName) {
        List<Voice> voices = mVoiceDao.getAnyVoices();
        for (final Voice voice : voices) {
            if (voice.internalName.equals(internalVoiceName)) {
                return voice.version;
            }
        }
        return null;
    }

    /**
     * Find if we have the specified language available.
     * Use our DB model to query availability of voices
     *
     * @param language ISO 639-3 language code, passed from Android
     * @param country  ISO 3166 ALPHA3 country code, passed from Android
     * @param variant  Language variant, passed from Android
     * @return -2 .. 2, depending on availability
     */
    public int isLanguageAvailable(String language, String country, String variant) {
        Log.v(LOG_TAG, "isLanguageAvailable: (" + language + "/" + country + "/" + variant + ")");

        final List<Voice> availableVoicesList = getCachedVoices();

        if (availableVoicesList == null || availableVoicesList.isEmpty()) {
            Log.v(LOG_TAG, "No voices registered yet");
            return TextToSpeech.ERROR_NOT_INSTALLED_YET;
        }

        boolean supportsLanguage = false;
        boolean supportsVariant = false;
        boolean supportsCountry = false;

        for (final Voice voice : availableVoicesList) {
            if (voice.supportsIso3(language, country, variant)) {
                if (voice.needsDownload()) {
                    Log.v(LOG_TAG, "Voice needs download");
                    return TextToSpeech.LANG_MISSING_DATA;
                }
                supportsLanguage = true;
                if (!variant.isEmpty()) {
                    supportsVariant = true;
                    // no more need to iterate further
                    break;
                } else if (!country.isEmpty()) {
                    supportsCountry = true;
                }
            }
        }

        int la = TextToSpeech.LANG_NOT_SUPPORTED;
        if (supportsLanguage) {
            la = TextToSpeech.LANG_AVAILABLE;
            if (supportsCountry) {
                la = TextToSpeech.LANG_COUNTRY_AVAILABLE;
            }
            if (supportsVariant) {
                la = LANG_COUNTRY_VAR_AVAILABLE;
            }
        }
        Log.v(LOG_TAG, "isLanguageAvailable: returns " + la);
        return la;
    }

    /**
     * Get the name of the voice for given values. Parameter iso3Language is mandatory,
     * iso3Country and variant are optional. In case not all parameters are given, we hard-code
     * here directly, which voice is our default voice for given iso3Language/iso3Country.
     *
     * @param iso3Language Iso3 language
     * @param iso3Country  Iso3 country
     * @param variant      variant
     * @return name of default voice for given parameter
     */
    public String getDefaultVoiceFor(String iso3Language, String iso3Country, String variant) {
        Log.v(LOG_TAG, "getDefaultVoiceFor: (" + iso3Language + "/" + iso3Country + "/" + variant + ")");
        String rv = null;

        if (isLanguageAvailable(iso3Language, iso3Country, variant) == LANG_COUNTRY_VAR_AVAILABLE) {
            Log.v(LOG_TAG, "Language available, no need to find default voice");
            return variant;
        }

        // only at first start, if user has neither selected a voice, nor the selected voice is
        // persisted
        if (mSelectedVoice == null) {
            if (variant.isEmpty()) {
                // no specific voice selected, decide default voice depending on network availability /
                // or voice RTF in case of an on-device-voice
                Voice bestVoice;
                if (ConnectionCheck.isTTSServiceReachable()) {
                    bestVoice = selectBestNetworkVoice();
                    if (bestVoice == null) {
                        Log.v(LOG_TAG, "getDefaultVoiceFor(): no TTS voices (yet), trying on-device voices ..");
                        bestVoice = selectFastestOnDeviceVoice();
                    }
                } else {
                    bestVoice = selectFastestOnDeviceVoice();
                }
                if (bestVoice != null) {
                    rv = bestVoice.name;
                }
            }
        } else {
            rv = mSelectedVoice.name;
        }

        // TODO DS: check for unsupported language
        //          maybe the user has a non-Icelandic locale set and selects voice ("use system locale")
        //          then we need to decide what we return here as default voice
        //          rv = null;
        Log.v(LOG_TAG, "getDefaultVoiceFor(): chosen default voice: (" + rv + ")");
        return rv;
    }

    /**
     * Select the currently fastest on device voice, as propagated by its RTF (Realtime factor).
     *
     * @return Voice in DB that has the fastest RTF.
     */
    private Voice selectFastestOnDeviceVoice() {
        DeviceVoice bestODVoice = null;

        for (DeviceVoice voice : mAVM.getVoiceList().Voices) {
            if (bestODVoice == null) {
                bestODVoice = voice;
                continue;
            }
            if (voice.RTF > bestODVoice.RTF) {
                bestODVoice = voice;
            }
        }
        if (bestODVoice == null) {
            throw (new RuntimeException("No on device voices available ?!"));
        }
        // map between bestVoice and Voice
        return mapDeviceVoiceToDbVoice(bestODVoice);
    }

    /**
     * Select the "best" network voice.
     * On the long term, this has to be based on metrics. For now, we just use the first one in
     * the list of available network voices.
     *
     * @return Best network voice in DB
     */
    private Voice selectBestNetworkVoice() {
        for (Voice voice : getCachedVoices()) {
            if (voice.needsNetwork()) {
                return voice;
            }
        }
        Log.w(LOG_TAG, "No network voices available ?!");
        return null;
    }

    /**
     * Maps given DeviceVoice to database voice.
     *
     * @param odVoice on device voice
     * @return corresponding db Voice
     */
    private Voice mapDeviceVoiceToDbVoice(DeviceVoice odVoice) {
        if (odVoice == null) return null;
        Voice dbVoiceOD = odVoice.convertToDbVoice();
        for (Voice voice : getCachedVoices()) {
            if (voice.internalName.equals(dbVoiceOD.internalName)) {
                return voice;
            }
        }
        Log.w(LOG_TAG, "On device voice not in DB ?!");
        return null;
    }

    /**
     * Show a toast message in case of a network error.
     * @param context application context
     */
    public void showTtsBackendWarningDialog(Context context) {
        AlertDialog warningDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String audioAssetFile = "";
        int messageId = R.string.try_again_later;
        if (!ConnectionCheck.isNetworkConnected()) {
            messageId = R.string.check_internet;
            builder.setPositiveButton(R.string.doit, (dialog, id) -> openWifiSettings(context))
                    .setNegativeButton(R.string.not_yet, (dialog, id) -> {
                    });
            audioAssetFile = "audio/check_internet_dora.mp3";
        } else if (!ConnectionCheck.isTTSServiceReachable()) {
            messageId = R.string.speech_service_not_available;
            builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            });
            audioAssetFile = "audio/service_not_available_dora.mp3";
        }
        builder
                .setMessage(messageId)
                .setTitle(R.string.speech_service_connection_problem)
                .setCancelable(true);

        if (!audioAssetFile.isEmpty()) {
            mMediaPlayer.stop();
            mMediaPlayer.update(context, audioAssetFile);
        }

        warningDialog = builder.create();
        warningDialog.show();
    }

    /**
     * Play given asset file via given SynthesisCallback. This method is only valid inside an
     * onSynthesizeText() callback.
     *
     * @param callback      callback given in onSynthesizeText()
     * @param assetFilename file to speak from assets
     */
    public void speakAssetFile(SynthesisCallback callback, String assetFilename) {
        Log.v(LOG_TAG, "playAssetFile: " + assetFilename);
        try {
            InputStream inputStream = App.getContext().getAssets().open(assetFilename);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            if (size != inputStream.read(buffer)) {
                Log.w(LOG_TAG, "playAssetFile: not enough bytes ?");
            }
            // don't provide rawText: there are no speech marks to update
            callback.start(AudioManager.SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT,
                    AudioManager.N_CHANNELS);
            feedBytesToSynthesisCallback(callback, buffer, "");
            callback.done();
        } catch (Exception e) {
            Log.e(LOG_TAG, "playAssetFile Exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * Update the speech marks for the current utterance.
     *
     * @param callback      TTS callback
     * @param bufSize       size of the audio buffer
     * @param offsetInBuf   offset in the buffer
     * @param textLen       max length of the utterance text
     * @param nextChunkSize size of next chunk of audio data to send
     */
    private static void updateSpeechMarks(SynthesisCallback callback, int bufSize, int offsetInBuf, int textLen, int nextChunkSize) {
        final int frameSizeInBytes = 2; // 16-bit samples
        final int markerInFrames = offsetInBuf / frameSizeInBytes;
        int offsetInText = Math.round((float) offsetInBuf / (float) bufSize * (float) textLen);
        // because we are already setting the speech mark to [0,1] in the beginning, we need to begin
        // with a minimum of 1
        offsetInText = Math.max(1, offsetInText);
        int endInText = Math.round((float) (offsetInBuf + nextChunkSize) / (float) bufSize * (float) textLen);
        Log.v(LOG_TAG, "TTSObserver: markerInFrames = " + markerInFrames + " offsetInText = " + offsetInText + " endInText = " + endInText);
        callback.rangeStart(markerInFrames, offsetInText, endInText);
    }

    /**
     * Feed given bytes to given SynthesisCallback. The callback is called consecutively with the
     * audio data. Speech marks are also updated in case the given parameter rawText.size() != 0
     *
     * @param callback callback given in onSynthesizeText()
     * @param buffer   bytes to feed
     * @param rawText  original text as given via "text" parameter in onSynthesizeText()
     */
    public static void feedBytesToSynthesisCallback(SynthesisCallback callback, byte[] buffer, String rawText) {
        int offset = 0;
        final int maxBytes = callback.getMaxBufferSize();
        while (offset < buffer.length) {
            Log.v(LOG_TAG, "TTSObserver: offset = " + offset);
            final int bytesLeft = buffer.length - offset;
            final int bytesConsumed = Math.min(maxBytes, bytesLeft);
            if (callback.hasStarted()) {
                // this feeds audio data to the callback, which will then be consumed by the TTS
                // client. In case the current utterance is stopped(), all remaining audio data is
                // consumed and discarded and afterwards TTSService.onStopped() is executed.
                int cbStatus = callback.audioAvailable(buffer, offset, bytesConsumed);
                switch(cbStatus) {
                    case TextToSpeech.SUCCESS:
                        if (!rawText.isEmpty()) {
                            updateSpeechMarks(callback, buffer.length, offset, rawText.length(), bytesConsumed);
                        }
                        break;
                    case TextToSpeech.ERROR:
                        // This is also called, if the user skips the current utterance
                        Log.w(LOG_TAG, "TTSObserver: callback.audioAvailable() returned ERROR");
                        return;
                    case TextToSpeech.STOPPED:
                        Log.w(LOG_TAG, "TTSObserver: callback.audioAvailable() returned STOPPED");
                        return;
                    default:
                        Log.e(LOG_TAG, "TTSObserver: callback.audioAvailable() returned " + cbStatus);
                        return;
                }
            }
            offset += bytesConsumed;
        }
    }

    // Open Wifi preferences
    private void openWifiSettings(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.wifi.WifiSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    Voice getVoiceForName(String name) {
        for (final Voice voice : getCachedVoices()) {
            if (voice.name.equals(name)) {
                return voice;
            }
        }
        return null;
    }

    public String getLoadedVoiceName() {
        if (mSelectedVoice != null) {
            return mSelectedVoice.name;
        }
        return "";
    }

    /**
     * Execute normalization and G2P for given text and save the results for the cache item into
     * the audio speech cache.
     *
     * @param text Raw text as received by the TTS service
     * @param item cache item to save into the speech audio cache
     * @return updated cache item
     */
    synchronized
    public CacheItem executeFrontendAndSaveIntoCache(String text, CacheItem item, com.grammatek.simaromur.db.Voice voice) {
        String phonemes = "";
        if (item.getUtterance().getNormalized().isEmpty()) {
            // we always need to normalize the text, but it doesn't hurt, if we always do G2P as well
            // for network voices, this is currently all that is needed.
            String normalizedText = mFrontend.getNormalizationManager().process(text);
            phonemes = mFrontend.transcribe(normalizedText, voice.type, voice.version);
            Log.v(LOG_TAG, "onSynthesizeText: original (\"" + text + "\"), normalized (\"" + normalizedText + "\"), phonemes (\"" + phonemes + "\")");
            if (!phonemes.isEmpty()) {
                Utterance updatedUtterance = UtteranceCacheManager.newUtterance(text, normalizedText, List.of(phonemes));
                item = mUtteranceCacheManager.saveUtterance(updatedUtterance);
                Log.v(LOG_TAG, "... normalization/G2P saved into cache");
            }
        } else if (item.getUtterance().getPhonemesCount() == 0) {
            final String normalizedText = item.getUtterance().getNormalized();
            phonemes = mFrontend.transcribe(normalizedText, voice.type, voice.version);
            Log.v(LOG_TAG, "onSynthesizeText: normalized (\"" + normalizedText + "\"), phonemes (\"" + phonemes + "\")");
            if (!phonemes.isEmpty()) {
                Utterance updatedUtterance = UtteranceCacheManager.newUtterance(text, normalizedText, List.of(phonemes));
                item = mUtteranceCacheManager.saveUtterance(updatedUtterance);
                Log.v(LOG_TAG, "... G2P saved into cache");
            }
        } else {
            Log.v(LOG_TAG, "normalization/G2P skipped (hot cache)");
        }
        return item;
    }

    /**
     * Update DB according to network voice
     */
    final Runnable networkVoicesUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Date lastUpdateTime = new Date(System.currentTimeMillis() - NETWORK_VOICE_QUERY_TIME_MS);
            try {
                // if update time > intervalInMillis, get new voices
                if (mAppDataDao.voiceListUpdateTimeOlderThan(lastUpdateTime)) {
                    if (ConnectionCheck.isNetworkConnected()) {
                        Log.v(LOG_TAG, "voice update time expired (" + lastUpdateTime + "), fetch current list");
                        // send request for network voice list
                        streamNetworkVoices("");
                    } else {
                        Log.w(LOG_TAG, "voice update time expired, no Internet connection");
                    }
                }
            } catch (final Exception e) {
                Log.e(LOG_TAG, "timerRunnable error: " + e.getMessage());
            }
        }
    };

    /**
     * Update DB according to On-Device voices. This polls regularly the voice list from the
     * server and updates the DB accordingly. A voice is considered to be "new" if it is not
     * present in the DB yet or not anymore after deletion.
     *
     */
    final Runnable onDeviceVoicesUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v(LOG_TAG, "onDeviceVoicesUpdateRunnable()");

            // fetch on-device voice lists
            mDVM.readVoiceDescription(true);

            // only insert if not already in DB
            final List<Voice> onDeviceVoices = mDVM.getVoiceDbList();
            for (Voice voice : onDeviceVoices) {
                // check if voice is already in DB
                if (mVoiceDao.findVoice(voice.name, voice.internalName, voice.languageCode, voice.languageName, voice.variant, voice.version) == null) {
                    Log.w(LOG_TAG, "onDeviceVoiceRunnable Add on device voice " + voice.name);
                    mVoiceDao.insertVoices(voice);
                } else {
                    Log.w(LOG_TAG, "onDeviceVoicesUpdateRunnable Voice " + voice.name + " already in DB");
                }
            }
        }
    };

    /**
     * Update the Db according to Asset voices
     */
    final Runnable assetVoiceRunnable = new Runnable() {
        @Override
        public void run() {
            // delete all voices
            final List<Voice> allDbVoices = mVoiceDao.getAnyVoices();
            for (Voice voice : allDbVoices) {
                Log.w(LOG_TAG, "assetVoiceRunnable() Delete voice " + voice.name);
                mVoiceDao.deleteVoices(voice);
            }

            final List<Voice> assetVoices = mAVM.getVoiceDbList();
            for (Voice voice : assetVoices) {
                // enter new voices
                mVoiceDao.insertVoice(voice);
            }
        }
    };

    /**
     * Set the accept privacy notice boolean in AppData table
     *
     * @param setter true for accepting the privacy notice, false for not accepting it
     */
    public void doAcceptPrivacyNotice(Boolean setter) {
        Log.v(LOG_TAG, "doAcceptPrivacyNotice");
        new doAcceptPrivacyNoticeAsyncTask(mAppDataDao).execute(setter);
    }

    /**
     * Asynchronously update the database for the privacy notice acceptance flag
     */
    private static class doAcceptPrivacyNoticeAsyncTask extends AsyncTask<Boolean, Void, Void> {
        private final AppDataDao mAsyncTaskDao;

        doAcceptPrivacyNoticeAsyncTask(AppDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Boolean... params) {
            mAsyncTaskDao.doAcceptPrivacyNotice(params[0]);
            return null;
        }
    }

    /**
     * Set the CrashLytics user consent. If this is set to true, we can use CrashLytics to provide
     * better developer-friendly feedback like crash reports and usage statistics. If set to false,
     * developers and the CrashLytics cloud provider (i.e. Google) will not receive any usage
     * statistics or crash reports.
     *
     * @param setter true for allowing to send usage statistics and crash reports to the cloud
     *               provider of CrashLytics, false for disabling crash reports and usage statistics
     */
    public void doGiveCrashLyticsUserConsent(Boolean setter) {
        Log.v(LOG_TAG, "doGiveCrashLyticsUserConsent");
        new doGiveCrashLyticsUserConsentAsyncTask(mAppDataDao).execute(setter);
        // the real Firebase settings are updated asynchronously in appData observer
    }

    /**
     * Asynchronously update the database for the privacy notice acceptance flag
     */
    private static class doGiveCrashLyticsUserConsentAsyncTask extends AsyncTask<Boolean, Void, Void> {
        private final AppDataDao mAsyncTaskDao;

        doGiveCrashLyticsUserConsentAsyncTask(AppDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Boolean... params) {
            mAsyncTaskDao.doGiveCrashLyticsUserConsent(params[0]);
            return null;
        }
    }

    private static class updateVoicesAsyncTask extends AsyncTask<List<VoiceResponse>, Void, Void> {
        private final ApiDbUtil mApiDbUtil;
        private final String mVoiceType;

        updateVoicesAsyncTask(ApiDbUtil apiDbUtil, String voiceType) {
            mApiDbUtil = apiDbUtil;
            mVoiceType = voiceType;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(final List<VoiceResponse>... voices) {
            mApiDbUtil.updateApiVoices(voices[0], mVoiceType);
            return null;
        }
    }

    private static class updateAppDataVoiceListTimestampAsyncTask extends AsyncTask<Void, Void, Void> {
        private final AppDataDao mAppDataDao;

        updateAppDataVoiceListTimestampAsyncTask(AppDataDao appDataDao) {
            mAppDataDao = appDataDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAppDataDao.updateVoiceListTimestamp();
            return null;
        }
    }

}
