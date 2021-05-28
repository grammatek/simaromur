package com.grammatek.simaromur.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.grammatek.simaromur.App;

import java.util.List;

@Database(entities = {Voice.class, AppData.class},
        version = 1, exportSchema = true)
public abstract class ApplicationDb extends RoomDatabase {
    private final static String LOG_TAG = "Simaromur_" + ApplicationDb.class.getSimpleName();
    private static ApplicationDb INSTANCE;

    public abstract AppDataDao appDataDao();
    public abstract VoiceDao voiceDao();

    public static ApplicationDb getDatabase(final Context context) {
        Log.v(LOG_TAG, "getDatabase");
        if (INSTANCE == null) {
            synchronized (ApplicationDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ApplicationDb.class, "application_db")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Override the onOpen method to populate the database.
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){

        @Override
        public void onOpen (@NonNull SupportSQLiteDatabase db){
            Log.v(LOG_TAG, "onOpen");
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Populate the database with initial voices in the background.
     * TODO(DS): this should be based on voice auto detection at the various URL's
     */
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDataDao mAppDataDao;
        private final VoiceDao mVoiceDao;

        PopulateDbAsync(ApplicationDb db) {
            mAppDataDao = db.appDataDao();
            mVoiceDao = db.voiceDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {

            AppData appData = mAppDataDao.getAppData();
            if (appData == null)
            {
                appData = new AppData();
                appData.fliteVoiceListPath = App.getDataPath();
                appData.simVoiceListPath = App.getVoiceDataPath();
                mAppDataDao.insert(appData);
            }

            List<Voice> voices = mVoiceDao.getAnyVoices();
            if (voices == null || voices.isEmpty())
            {
                Log.v(LOG_TAG, "PopulateDbAsync:  voices == null");
                // fill in initial list of voices, currently via Tíro TTS web service only
                Voice v1 = new Voice("Dóra", "Dora",
                        "is.IS", "is","clear", "tiro", "");
                Voice v2 = new Voice("Karl", "Karl",
                        "is.IS", "is", "clear", "tiro", "");
                Voice v3 = new Voice("Neural 1", "other",
                        "is.IS", "is", "clear", "tiro", "");
                mVoiceDao.insertVoices(v1, v2, v3);
                Voice selectedVoice = mVoiceDao.findVoice(v1.name, v1.internalName, v1.languageCode,
                        v1.languageName, v1.variant);
                mAppDataDao.selectCurrentVoice(selectedVoice);
            }
            return null;
        }
    }
}
