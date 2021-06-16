package com.grammatek.simaromur;

import android.app.Application;
import android.media.AudioFormat;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.db.AppData;
import com.grammatek.simaromur.db.AppDataDao;
import com.grammatek.simaromur.db.ApplicationDb;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.db.VoiceDao;
import com.grammatek.simaromur.network.tiro.SpeakController;
import com.grammatek.simaromur.network.tiro.VoiceController;
import com.grammatek.simaromur.network.tiro.pojo.SpeakRequest;
import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstracted application repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
public class AppRepository {
    private final static String LOG_TAG = "Simaromur_" + AppRepository.class.getSimpleName();
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
    private final MediaPlayer mMediaPlayer;
    static final int SAMPLE_RATE_WAV= 22050;
    static final int SAMPLE_RATE_MP3= 22050;
    // this saves the voice name to use for the next speech synthesis
    private Voice mSelectedVoice;

    /**
     * Observer for Tiro voice query results.
     */
    class TiroVoiceQueryObserver implements VoiceController.VoiceObserver {
        public TiroVoiceQueryObserver() {}
        public void update(List<VoiceResponse> voices) {
            for (VoiceResponse voice: voices) {
                Log.v(LOG_TAG, "Tiro API returned: " + voice.VoiceId);
            }
            mTiroVoices = voices;
            new updateVoicesAsyncTask(mApiDbUtil).execute(mTiroVoices);
        }
        public void error(String errorMsg) {
            Log.e(LOG_TAG, "TiroVoiceQueryObserver()::error: " + errorMsg);
        }
    }

    /**
     * This class transforms a byte array into a MediaDataSource consumable by the Media Player
     */
    public static class ByteArrayMediaDataSource extends MediaDataSource {
        private final byte[] data;

        public ByteArrayMediaDataSource(byte []data) {
            assert data != null;
            this.data = data;
        }
        @Override
        public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
            if (position > getSize()) {
                return 0;
            }
            int adaptedSize = size;
            if (position + (long) size > getSize()) {
                adaptedSize = (int) getSize() - (int) position;
            }
            System.arraycopy(data, (int)position, buffer, offset, adaptedSize);
            return adaptedSize;
        }

        @Override
        public long getSize() throws IOException {
            return data.length;
        }

        @Override
        public void close() throws IOException {
            // Nothing to do here
        }
    }

    class TiroAudioPlayObserver implements SpeakController.AudioObserver {
        public TiroAudioPlayObserver() {}
        public void update(byte[] audioData) {
            Log.v(LOG_TAG, "Tiro API returned: " + audioData.length + "bytes");

            ByteArrayMediaDataSource dataSource = new ByteArrayMediaDataSource(audioData);
                try {
                    // resetting mediaplayer instance to evade problems
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(dataSource);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    // @todo: implement MediaPlayer completion callbacks for visual feedback
                } catch (IOException ex) {
                    String s = ex.toString();
                    ex.printStackTrace();
                }
        }
        public void error(String errorMsg) {
            Log.e(LOG_TAG, "TiroAudioPlayObserver()::error: " + errorMsg);
        }
    }

    static class TiroTtsObserver implements SpeakController.AudioObserver {
        SynthesisCallback m_synthCb;
        public TiroTtsObserver(SynthesisCallback synthCb) { m_synthCb = synthCb; }
        public void update(byte[] ttsData) {
            Log.v(LOG_TAG, "TiroTtsObserver: Tiro API returned: " + ttsData.length + " bytes");
            if (ttsData.length == 0) {
                playSilence();
                return;
            }
            m_synthCb.start(SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT, 1);
            final int maxBytes = m_synthCb.getMaxBufferSize();
            Log.v(LOG_TAG, "TiroTtsObserver: maxBufferSize = " + maxBytes);
            int offset = 0;
            while (offset < ttsData.length) {
                Log.v(LOG_TAG, "TiroTtsObserver: offset = " + offset);
                final int bytesConsumed = Math.min(maxBytes, (ttsData.length - offset));
                m_synthCb.audioAvailable(ttsData, offset, bytesConsumed);
                offset += bytesConsumed;
            }
            m_synthCb.done();
        }
        public void error(String errorMsg) {
            Log.e(LOG_TAG, "TiroTtsObserver()::error: " + errorMsg);
            playSilence();
        }

        private void playSilence() {
            Log.v(LOG_TAG, "TiroTtsObserver()::playing silence ...");
            m_synthCb.start(SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT, 1);
            byte[] silenceData = new byte[m_synthCb.getMaxBufferSize()];
            m_synthCb.audioAvailable(silenceData, 0, silenceData.length);
            m_synthCb.done();
        }
    }

    // Note that in order to unit test the AppRepository, you have to remove the Application
    // dependency.
    public AppRepository(Application application) {
        ApplicationDb db = ApplicationDb.getDatabase(application);
        mAppDataDao = db.appDataDao();
        mVoiceDao = db.voiceDao();
        mApiDbUtil = new ApiDbUtil(mVoiceDao);
        mTiroSpeakController = new SpeakController();
        mTiroVoiceController = new VoiceController();
        mMediaPlayer = new MediaPlayer();
        mAllVoices = mVoiceDao.getAllVoices();
        mAllCachedVoices = new ArrayList<>();
        getAllVoices().observeForever(voices -> {
            Log.v(LOG_TAG, "onChanged - voices size: " + voices.size());
            // Update cached voices
            mAllCachedVoices = voices;
        });
        getAppData().observeForever(appData -> {
            Log.v(LOG_TAG, "onChanged - appData currentVoice: " + appData);
            // Update cached appData
            mCachedAppData = appData;
        });
        // send request far all available Tiro voices
        streamTiroVoices("");
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
        assert(mCachedAppData.appDataId != 0);
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
        if (mTiroVoices == null) {
            mTiroVoiceController.streamQueryVoices(languageCode, new TiroVoiceQueryObserver());
        }
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
        mTiroSpeakController.streamAudio(request , new TiroAudioPlayObserver());
    }

    /**
     * Stops speaking current voice, if playing.
     */
    public void stopTiroSpeak() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
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
            final String SampleRate = "" + SAMPLE_RATE_WAV;
            SpeakRequest request = new SpeakRequest("standard", voice.languageCode,
                    "pcm", SampleRate, text, "text", voice.internalName);
            mTiroSpeakController.streamAudio(request , new TiroTtsObserver(synthCb));
        } else {
            Log.e(LOG_TAG, "startTiroTts: given voice is null ?!");
        }
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
     * Loads given voice, e.g. from disk. Can also to network request to see, if the voice is
     * available.
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

        if (availableVoicesList.isEmpty()) {
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

        if (mSelectedVoice != null) {
            Log.d(LOG_TAG, "getDefaultVoiceFor: selected voice not yet loaded");
            Long curVoiceId = mAppDataDao.getCurrentVoiceId();
            if (curVoiceId != 0) {
                for (final Voice voice : getCachedVoices()) {
                    if (voice.voiceId == curVoiceId) {
                        mSelectedVoice = voice;
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

    private void waitABit(long millis) {
        try            {
            Thread.sleep(millis);
        }
        catch(InterruptedException ex)            {
            Thread.currentThread().interrupt();
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

        updateVoicesAsyncTask(ApiDbUtil apiDbUtil) {  mApiDbUtil = apiDbUtil;  }

        @Override
        protected Void doInBackground(final List<VoiceResponse>... voices) {
            mApiDbUtil.updateModelVoices(voices[0]);
            return null;
        }
    }
}
