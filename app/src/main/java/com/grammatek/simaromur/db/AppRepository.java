package com.grammatek.simaromur.db;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.VoiceManager;

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
    private LiveData<List<Voice>> mAllVoices;

    // Note that in order to unit test the AppRepository, you have to remove the Application
    // dependency.
    public AppRepository(Application application) {
        ApplicationDb db = ApplicationDb.getDatabase(application);
        mAppDataDao = db.appDataDao();
        mVoiceDao = db.voiceDao();
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
    public LiveData<List<Voice>> getAllVoices() {
        Log.v(LOG_TAG, "getAllVoices");
        if (mAllVoices == null) {
            mAllVoices = mVoiceDao.getAllVoices();
        }
        return mAllVoices;
    }

    /**
     * Insert a voice into the db.
     *
     * @param voice Voice to be saved into db
     */
    public void insertVoice(Voice voice) {
        Log.v(LOG_TAG, "insertVoice");
        new insertVoiceAsyncTask(mVoiceDao).execute(voice);
    }

    private static class insertVoiceAsyncTask extends AsyncTask<Voice, Void, Void> {
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
}
