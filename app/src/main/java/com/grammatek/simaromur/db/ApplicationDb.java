package com.grammatek.simaromur.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;

@Database(entities = {Voice.class, AppData.class},
        version = 1, exportSchema = false)
public abstract class ApplicationDb extends RoomDatabase {
    private static ApplicationDb INSTANCE;

    public abstract AppDataDao appDataDao();
    public abstract VoiceDao voiceDao();

    public static ApplicationDb getDatabase(final Context context) {
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
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Populate the database in the background.
     * If you want to start with more words, just add them.
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

            List<Voice> voices = mVoiceDao.getAllVoices().getValue();
            if (voices == null || voices.isEmpty())
            {
                // fill in initial list of voices, currently via Tíro TTS web service only
                Voice tiroDoraVoice = new Voice("Dóra", "Dora",
                        "is.IS", "is","clear", "tiro", "");
                Voice tiroKarlVoice = new Voice("Karl", "Karl",
                        "is.IS", "is", "clear", "tiro", "");
                Voice tiroNeuroVoice = new Voice("Neural 1", "other",
                        "is.IS", "is", "clear", "tiro", "");

                mAppDataDao.selectCurrentVoice(tiroDoraVoice);
            }
            return null;
        }
    }
}
