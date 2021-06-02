package com.grammatek.simaromur;

import android.app.Application;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import java.util.List;

/**
 * Abstracted application repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
public class AppRepository {
    private final static String LOG_TAG = "Simaromur_" + AppRepository.class.getSimpleName();
    private AppDataDao mAppDataDao;
    private VoiceDao mVoiceDao;
    private AppData mAppData;
    private LiveData<List<com.grammatek.simaromur.db.Voice>> mAllVoices;
    private VoiceController mTiroVoiceController;
    private SpeakController mTiroSpeakController;
    private List<VoiceResponse> mTiroVoices;
    private ApiDbUtil mApiDbUtil;
    private MediaPlayer mMediaPlayer;

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
            new updateVoicesAsyncTask(mApiDbUtil).execute(voices);
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
                } catch (IOException ex) {
                    String s = ex.toString();
                    ex.printStackTrace();
                }
        }
        public void error(String errorMsg) {
            Log.e(LOG_TAG, "TiroAudioPlayObserver()::error: " + errorMsg);
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
    }

    /**
     * Returns AppData
     *
     * @return  single instance of the AppData
     */
    public AppData getAppData() {
        Log.v(LOG_TAG, "getAppData");
        if (mAppData == null) {
            mAppData = mAppDataDao.getAppData();
        }
        return mAppData;
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
     * Returns list of all Tiro voices from its API endpoint.
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
     * @todo Do this regularly via a timer
     */
    public void streamTiroVoices(String languageCode) {
        Log.v(LOG_TAG, "streamTiroVoices");
        if (mTiroVoices == null) {
            mTiroVoiceController.streamQueryVoices(languageCode, new TiroVoiceQueryObserver());
        }
    }

    public void streamTiroVoice(String voiceId, String text, float speed, float pitch) {
        final String KHZ_22 = "22050";
        SpeakRequest request = new SpeakRequest("standard", "is-IS",
                "mp3", KHZ_22, text, "text", voiceId);
        mTiroSpeakController.streamAudio(request , new TiroAudioPlayObserver());
    }

    public void stopTiroVoice() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
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
