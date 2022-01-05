package com.grammatek.simaromur;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.db.AppData;
import com.grammatek.simaromur.db.AppDataDao;
import com.grammatek.simaromur.db.ApplicationDb;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.db.VoiceDao;
import com.grammatek.simaromur.device.TTSEngineController;
import com.grammatek.simaromur.device.TTSEnginePyTorch;
import com.grammatek.simaromur.frontend.FrontendManager;
import com.grammatek.simaromur.device.AssetVoiceManager;
import com.grammatek.simaromur.device.pojo.DeviceVoice;
import com.grammatek.simaromur.network.ConnectionCheck;
import com.grammatek.simaromur.network.tiro.SpeakController;
import com.grammatek.simaromur.network.tiro.VoiceController;
import com.grammatek.simaromur.network.tiro.pojo.SpeakRequest;
import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.grammatek.simaromur.audio.AudioManager.SAMPLE_RATE_MP3;
import static com.grammatek.simaromur.audio.AudioManager.SAMPLE_RATE_WAV;


/**
 * Abstracted application repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
public class AppRepository {
    private final static String LOG_TAG = "Simaromur_" + AppRepository.class.getSimpleName();
    private final static long NETWORK_VOICE_QUERY_TIME_MS = 1000*60*30;   // 30 min.
    private final AppDataDao mAppDataDao;
    private final VoiceDao mVoiceDao;
    private LiveData<AppData> mAppData;
    private LiveData<List<com.grammatek.simaromur.db.Voice>> mAllVoices;
    private AppData mCachedAppData;
    private List<com.grammatek.simaromur.db.Voice> mAllCachedVoices;
    private final VoiceController mTiroVoiceController;
    private final SpeakController mTiroSpeakController;
    private List<VoiceResponse> mTiroVoices;
    private final ApiDbUtil mApiDbUtil;
    private final AssetVoiceManager mAVM;
    ScheduledExecutorService mScheduler;
    FrontendManager mFrontend;
    TTSEngineController mTTSEngineController;

    // this saves the voice name to use for the next speech synthesis
    private Voice mSelectedVoice;

    private MediaPlayObserver mMediaPlayer;

    /**
     * Observer for Tiro voice query results.
     */
    class TiroVoiceQueryObserver implements VoiceController.VoiceObserver {
        public TiroVoiceQueryObserver() {}
        public void update(List<VoiceResponse> voices) {
            for (VoiceResponse voice: voices) {
                if (voice == null) {
                    Log.e(LOG_TAG, "Tiro API returned null voice ?!");
                    return;
                }
                Log.v(LOG_TAG, "Tiro API returned: " + voice.VoiceId);
            }
            mTiroVoices = voices;
            new updateVoicesAsyncTask(mApiDbUtil, "tiro").execute(mTiroVoices);
            new updateAppDataVoiceListTimestampAsyncTask(mAppDataDao).execute();
        }
        public void error(String errorMsg) {
            Log.e(LOG_TAG, "TiroVoiceQueryObserver()::error: " + errorMsg);
        }
    }

    // Note that in order to unit test the AppRepository, you have to remove the Application
    // dependency.
    public AppRepository(Application application) throws IOException {
        Log.v(LOG_TAG, "AppRepository()");
        ApplicationDb db = ApplicationDb.getDatabase(application);
        mAppDataDao = db.appDataDao();
        mVoiceDao = db.voiceDao();
        mApiDbUtil = new ApiDbUtil(mVoiceDao);
        mAVM = new AssetVoiceManager(App.getContext());
        mFrontend = new FrontendManager(App.getContext());
        mTTSEngineController = new TTSEngineController(App.getContext().getAssets(), mFrontend);
        mTiroSpeakController = new SpeakController();
        mTiroVoiceController = new VoiceController();
        mAppData = mAppDataDao.getLiveAppData();
        mAppData.observeForever(appData -> {
            Log.v(LOG_TAG, "mAppData update: " + appData);
            // Update cached appData
            mCachedAppData = appData;
        });
        mAllVoices = mVoiceDao.getAllVoices();
        mAllVoices.observeForever(voices -> {
            Log.v(LOG_TAG, "mAllVoices update: " + voices);
            // Update cached voices
            mAllCachedVoices = voices;
        });

        mScheduler = Executors.newSingleThreadScheduledExecutor();
        mScheduler.scheduleAtFixedRate(timerRunnable, 0, 20, TimeUnit.SECONDS);
        mScheduler.schedule(assetVoiceRunnable, 1, TimeUnit.SECONDS);
        mMediaPlayer = new MediaPlayObserver();
    }

    /**
     * Returns AppData
     *
     * @return  single instance of the AppData as LiveData
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
     * @return  single instance of the AppData
     */
    public AppData getCachedAppData() {
        Log.v(LOG_TAG, "getCachedAppData");
        return mCachedAppData;
    }

    /**
     * Returns list of all voices.
     *
     * @return  list of all persisted voices as LiveData
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
     * @return  list of all cached voices
     */
    public final List<com.grammatek.simaromur.db.Voice> getCachedVoices() {
        Log.v(LOG_TAG, "getCachedVoices");
        return mAllCachedVoices;
    }

    /**
     * Returns list of all Tiro voices from its API endpoint.
     *
     * @param languageCode  language code, e.g. "is-IS"
     *
     * @return  list of all cached Tiro API voices.
     */
    public List<VoiceResponse> queryTiroVoices(String languageCode) throws IOException {
        Log.v(LOG_TAG, "queryTiroVoices");
        if (mTiroVoices == null) {
            streamTiroVoices(languageCode);
        }
        return mTiroVoices;
    }

    /**
     * Request Tiro voices from its API endpoint.
     *
     * @param languageCode  language code, e.g. "is-IS"
     *
     * @todo Do this regularly via a timer
     */
    public void streamTiroVoices(String languageCode) {
        Log.v(LOG_TAG, "streamTiroVoices");
        mTiroVoiceController.streamQueryVoices(languageCode, new TiroVoiceQueryObserver());
    }

    /**
     * Request to Tiro TTS to speak given text and return Audio asynchronously.
     *
     * @param voiceId   the voice name identifier of the TTS API
     * @param text      text to speak
     * @param langCode  language code, e.g. "is-IS"
     * @param speed     speed to use for the voice audio
     * @param pitch     pitch to use for the voice audio
     */
    public void startTiroSpeak(String voiceId, String text, String langCode, float speed, float pitch) {
        final String SampleRate = "" + SAMPLE_RATE_MP3;
        SpeakRequest request = new SpeakRequest("standard", langCode,
                "mp3", SampleRate, text, "text", voiceId);
        mMediaPlayer.stop();
        mTiroSpeakController.streamAudio(request , mMediaPlayer);
    }

    /**
     * Stops speaking current voice, if playing.
     */
    public void stopTiroSpeak() {
        mTiroSpeakController.stop();
    }

    /**
     * Request to Tiro TTS to return audio and call Android TTS asynchronously.
     *
     * @param synthCb   The Synthesize callback to use
     * @param voice     The voice to use
     * @param text      text to speak
     * @param speed     speed to use for the voice audio
     * @param pitch     pitch to use for the voice audio
     */
    public void startTiroTts(SynthesisCallback synthCb, Voice voice, String text, float speed, float pitch) {
        // map given voice to voiceId
        if (voice != null) {
            if (text.trim().isEmpty()) {
                Log.w(LOG_TAG, "startTiroTts: given text is whitespace only ?!");
            }

            final String SampleRate = "" + SAMPLE_RATE_WAV;
            SpeakRequest request = new SpeakRequest("standard", voice.languageCode,
                    "pcm", SampleRate, text, "text", voice.internalName);
            mTiroSpeakController.streamAudio(request , new TTSObserver(synthCb, pitch, speed));
        } else {
            Log.e(LOG_TAG, "startTiroTts: given voice is null ?!");
        }
    }

    public void startTorchSpeak(Voice voice, String text, float speed, float pitch) {
        try {
            mTTSEngineController.LoadEngine(voice);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mTTSEngineController.StartSpeak(text, speed, pitch, SAMPLE_RATE_WAV);
    }

    public void startFliteSpeak(String voiceId, String text, String langCode, float speed, float pitch) {
        mMediaPlayer.stop();
        // mTiroSpeakController.streamAudio(request , mMediaPlayer);
    }

    /**
     * Request to update local voices.
     *
     * @param languageCode  language code, e.g. "is-IS"
     *
     */
    public void getLocalVoices(String languageCode) {
        Log.v(LOG_TAG, "getLocalVoices");
        mTiroVoiceController.streamQueryVoices(languageCode, new TiroVoiceQueryObserver());
    }

    /**
     * Insert a voice into the db.
     *
     * @param voice Voice to be saved into db
     */
    public void insertVoice(com.grammatek.simaromur.db.Voice voice) {
        Log.v(LOG_TAG, "insertVoice");
        new insertVoiceAsyncTask(mVoiceDao).execute(voice);
    }

    /**
     * Loads given voice, e.g. from disk. Can also access network to query voice availability.
     *
     * @param voiceName     Name of the voice to load
     *
     * @return  TextToSpeech.SUCCESS in case operation successful, TextToSpeech.ERROR otherwise
     *
     * @note : Is called on the synthesis thread, so it's safe to call even DB-methods here
     */
    public int loadVoice(String voiceName) {
        Log.v(LOG_TAG, "loadVoice: (" + voiceName + ")");
        List<Voice> voices = mVoiceDao.getAnyVoices();
        for (final Voice voice:voices) {
            if (voice.name.equals(voiceName)) {
                Log.v(LOG_TAG, "SUCCESS");
                mSelectedVoice = voice;
                mAppDataDao.selectCurrentVoice(voice);
                return TextToSpeech.SUCCESS;
            }
        }
        Log.v(LOG_TAG, "ERROR");
        return TextToSpeech.ERROR;
    }

    /**
     * Use our DB model to query availability of voices.
     *
     * @param language  ISO 639-3 language code, passed from Android
     * @param country   ISO 3166 ALPHA3 country code, passed from Android
     * @param variant   Language variant, passed from Android
     *
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

        for (final Voice voice:availableVoicesList){
            if (voice.supportsIso3(language, country, variant)) {
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
                la = TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
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
     * @param iso3Language  Iso3 language
     * @param iso3Country   Iso3 country
     * @param variant   variant
     *
     * @return  name of default voice for given parameter
     */
    public String getDefaultVoiceFor(String iso3Language, String iso3Country, String variant) {
        Log.v(LOG_TAG, "getDefaultVoiceFor: (" + iso3Language + "/" + iso3Country + "/" + variant + ")");
        String rv = null;

        if (mSelectedVoice == null) {
            Log.d(LOG_TAG, "getDefaultVoiceFor: selected voice not yet loaded");
            if (mCachedAppData != null && mCachedAppData.currentVoiceId != 0) {
                for (final Voice voice : getCachedVoices()) {
                    if (voice.voiceId == mCachedAppData.currentVoiceId) {
                        mSelectedVoice = voice;
                        Log.d(LOG_TAG, "getDefaultVoiceFor: found preferred voice in list");
                    }
                }
            }
        }

        /* Favor our selected voice before falling back to other choices */
        if (mSelectedVoice != null && mSelectedVoice.supportsIso3(iso3Language, iso3Country, variant)) {
            return mSelectedVoice.name;
        }

        Log.v(LOG_TAG, "getDefaultVoiceFor: not matching selected voice");

        for (final Voice voice : getCachedVoices()) {
            // if the voice is the exact fit for given parameters, return it directly
            if (voice.supportsIso3(iso3Language, iso3Country, variant)) {
                rv = voice.name;
                break;
            }
        }
        Log.v(LOG_TAG, "chosen default voice: (" + rv + ")");
        return rv;
    }

    public void showTtsBackendWarningDialog(Context context) {
        AlertDialog warningDialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String audioAssetFile = "";
        int messageId =  R.string.try_again_later;
        if (! ConnectionCheck.isNetworkConnected()) {
            messageId =  R.string.check_internet;
            builder.setPositiveButton(R.string.doit, (dialog, id) -> {
                openWifiSettings(context); })
                    .setNegativeButton(R.string.not_yet, (dialog, id) -> {});
            audioAssetFile = "audio/check_internet_dora.mp3";
        } else if (! ConnectionCheck.isTTSServiceReachable()) {
            messageId =  R.string.speech_service_not_available;
            builder.setPositiveButton(R.string.ok, (dialog, id) -> {});
            audioAssetFile = "audio/service_not_available_dora.mp3";
        }
        builder
                .setMessage(messageId)
                .setTitle(R.string.speech_service_connection_problem)
                .setCancelable(true);

        if (! audioAssetFile.isEmpty()) {
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
     * @param callback       callback given in onSynthesizeText()
     * @param assetFilename  file to speak from assets
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
            TTSObserver observer=new TTSObserver(callback, (float) 1.0, (float) 1.1);
            observer.update(buffer);
            observer.stop();
        } catch (Exception e) {
            Log.e(LOG_TAG, "playAssetFile Exception: " + e.getLocalizedMessage());
        }
    }

    // Open Wifi preferences
    private void openWifiSettings(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored){
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

    Voice getVoiceForLocale(String iso3Language, String iso3Country, String variant) {
        for (final Voice voice : getCachedVoices()) {
            // if the voice is the exact fit for given parameters, return it directly
            if (voice.supportsIso3(iso3Language, iso3Country, variant)) {
                return voice;
            }
        }
        return null;
    }

    String getLoadedVoiceName() {
        if (mSelectedVoice != null) {
            return mSelectedVoice.name;
        }
        return "";
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Date lastUpdateTime = new Date(System.currentTimeMillis() - NETWORK_VOICE_QUERY_TIME_MS);
            try {
                // if update time > intervalInMillis, get new voices
                if (mAppDataDao.voiceListUpdateTimeOlderThan(lastUpdateTime)) {
                    if (ConnectionCheck.isNetworkConnected()) {
                        Log.v(LOG_TAG, "voice update time expired ("+lastUpdateTime+"), fetch current list");
                        // send request for network voice list
                        streamTiroVoices("");
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
     * Update the Db according to Asset voices
     */
    Runnable assetVoiceRunnable = new Runnable() {
        @Override
        public void run() {
            List<Voice> aVoices = mAVM.getVoiceDbList();
            List<Voice> aVoicesInDb = mVoiceDao.getAssetVoices();
            List<Voice> toDeleteVoices = new ArrayList<>(aVoicesInDb);

            // delete all voices that don't exist anymore
            toDeleteVoices.removeAll(aVoices);
            mVoiceDao.deleteVoices(toDeleteVoices.toArray(new Voice[]{}));

            for (Voice voice : aVoices) {
                if (! mVoiceDao.findVoiceWithName(voice.name).isEmpty()) {
                    // update existing voices
                    mVoiceDao.updateVoices(voice);
                } else {
                    // enter new voices
                    mVoiceDao.insertVoice(voice);
                }
            }
        }
    };

    /**
     * Set the accept privacy notice boolean in AppData table
     *
     * @param setter    true for accepting the privacy notice, false for not accepting it
     */
    public void doAcceptPrivacyNotice(Boolean setter) {
        Log.v(LOG_TAG, "insertVoice");
        new doAcceptPrivacyNoticeAsyncTask(mAppDataDao).execute(setter);
    }

    /**
     * Asynchronously update the database for the privacy notice acceptance flag
     */
    private static class doAcceptPrivacyNoticeAsyncTask extends AsyncTask<Boolean, Void, Void> {
        private AppDataDao mAsyncTaskDao;

        doAcceptPrivacyNoticeAsyncTask(AppDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Boolean... params) {
            mAsyncTaskDao.doAcceptPrivacyNotice(params[0]);
            return null;
        }
    }

    private static class insertVoiceAsyncTask extends AsyncTask<com.grammatek.simaromur.db.Voice, Void, Void> {
        private VoiceDao mAsyncTaskDao;

        insertVoiceAsyncTask(VoiceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Voice... params) {
            mAsyncTaskDao.insertVoice(params[0]);
            return null;
        }
    }

    private static class updateVoicesAsyncTask extends AsyncTask<List<VoiceResponse>, Void, Void> {
        private ApiDbUtil mApiDbUtil;
        private String mVoiceType;

        updateVoicesAsyncTask(ApiDbUtil apiDbUtil, String voiceType) {
            mApiDbUtil = apiDbUtil;
            mVoiceType = voiceType;
        }

        @Override
        protected Void doInBackground(final List<VoiceResponse>... voices) {
            mApiDbUtil.updateApiVoices(voices[0], mVoiceType);
            return null;
        }
    }

    private static class updateAppDataVoiceListTimestampAsyncTask extends AsyncTask<Void, Void, Void> {
        private AppDataDao mAppDataDao;

        updateAppDataVoiceListTimestampAsyncTask(AppDataDao appDataDao) {  mAppDataDao = appDataDao;  }

        @Override
        protected Void doInBackground(Void... voids) {
            mAppDataDao.updateVoiceListTimestamp();
            return null;
        }
    }

}
