package com.grammatek.simaromur.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.grammatek.simaromur.App;

import java.util.List;


@Database(
        version = ApplicationDb.LATEST_VERSION,
        entities = {Voice.class, AppData.class}
)
public abstract class ApplicationDb extends RoomDatabase {
    private final static String LOG_TAG = "Simaromur_" + ApplicationDb.class.getSimpleName();
    static final int LATEST_VERSION = 8;
    private static volatile ApplicationDb INSTANCE;

    public abstract AppDataDao appDataDao();
    public abstract VoiceDao voiceDao();

    static public final Migration MIGRATION_1_2 = new Migration(1, 2){ // From version 1 to version 2
        @Override
        public void migrate(SupportSQLiteDatabase database){
            Log.v(LOG_TAG, "MIGRATION_1_2");
            // Remove the table
            database.execSQL("DROP INDEX index_voice_table_name_gender_language_code_type");
            database.execSQL("DROP TABLE voice_table");
            database.execSQL("CREATE TABLE IF NOT EXISTS voice_table (`voiceId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `gender` TEXT NOT NULL, `internal_name` TEXT NOT NULL, `language_code` TEXT NOT NULL, `language_name` TEXT NOT NULL, `variant` TEXT NOT NULL, `type` TEXT, `update_time` TEXT, `download_time` TEXT, `url` TEXT, `download_path` TEXT, `version` TEXT, `md5_sum` TEXT, `local_size` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_voice_table_internal_name_gender_language_code_type` ON voice_table (`internal_name`, `gender`, `language_code`, `type`)");
        }
    };
    static public final Migration MIGRATION_2_3 = new Migration(2, 3){  // v2 => 3
        @Override
        public void migrate(SupportSQLiteDatabase database){
            Log.v(LOG_TAG, "MIGRATION_2_3");
            database.execSQL("ALTER TABLE app_data_table "
                    + " ADD COLUMN privacy_info_dialog_accepted INTEGER NOT NULL DEFAULT(0)");
        }
    };
    static public final Migration MIGRATION_3_4 = new Migration(3, 4){  // v3 => 4
        @Override
        public void migrate(SupportSQLiteDatabase database){
            Log.v(LOG_TAG, "MIGRATION_3_4");
            database.execSQL("ALTER TABLE app_data_table "
                    + " ADD COLUMN crash_lytics_user_consent_accepted INTEGER NOT NULL DEFAULT(0)");
        }
    };
    static public final Migration MIGRATION_4_5 = new Migration(4, 5){  // v4 => 5
        @Override
        public void migrate(SupportSQLiteDatabase database){
            Log.v(LOG_TAG, "MIGRATION_4_5");
            database.execSQL("DELETE FROM voice_table WHERE type = 'tiro'");
        }
    };
    static public final Migration MIGRATION_5_6 = new Migration(5, 6){  // v5 => 6
        @Override
        public void migrate(SupportSQLiteDatabase database){
            Log.v(LOG_TAG, "MIGRATION_5_6");
            // create a dummy version for all empty version info
            database.execSQL("UPDATE voice_table SET version = 'v0' WHERE version = ''");
        }
    };
    static public final Migration MIGRATION_6_7 = new Migration(6, 7){  // v6 => 7
        @Override
        public void migrate(SupportSQLiteDatabase database){
            Log.v(LOG_TAG, "MIGRATION_6_7");
            // create a dummy version for all empty version info
            database.execSQL("UPDATE voice_table SET url = 'https://api.grammatek.com/tts/v0' WHERE type = 'network'");
        }
    };

    static public final Migration MIGRATION_7_8 = new Migration(7, 8) {  // v7 => 8
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.v(LOG_TAG, "MIGRATION_7_8");

            // adapt voice_table to new schema: delete all but onnx voices and update schema version
            database.execSQL("DELETE FROM voice_table WHERE type != 'onnx'");
            database.execSQL("UPDATE app_data_table SET schema_version = '8'");

            // adapt app_data_table to new schema: drop unnecessary columns. This is not directly
            // supported in SQLite. We need to create a new table, copy the data over and finally
            // drop the old table. Please take into account the definitions of AppData class !
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS app_data_table_temp (`appDataId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `schema_version` TEXT NOT NULL, `current_voice_id` INTEGER NOT NULL, `voice_list_update_time` TEXT, `privacy_info_dialog_accepted` INTEGER NOT NULL DEFAULT 0, `crash_lytics_user_consent_accepted` INTEGER NOT NULL DEFAULT 0)");
            database.execSQL(
                    "INSERT INTO app_data_table_temp SELECT appDataId, schema_version, current_voice_id, sim_voice_list_update_time, privacy_info_dialog_accepted, crash_lytics_user_consent_accepted FROM app_data_table"
            );
            database.execSQL("DROP TABLE app_data_table");
            database.execSQL("ALTER TABLE app_data_table_temp RENAME TO app_data_table");

            // if the current_voice_id does not exist in voice_table or is NULL, set it to the first voice found in voice_table,
            // if there is no entry in voice_table, set it to -1
            database.execSQL(
                    "UPDATE app_data_table " +
                            "SET current_voice_id = CASE " +
                            "WHEN current_voice_id IN (SELECT voiceId FROM voice_table) THEN current_voice_id " +
                            "ELSE -1 " +
                            "END " +
                            "WHERE current_voice_id NOT IN (SELECT voiceId FROM voice_table) OR current_voice_id IS NULL;"
            );

            // clean up the database file: doesn't work inside a transaction
            //database.execSQL("VACUUM");
        }
    };

    public static ApplicationDb getDatabase(final Context context) {
        Log.v(LOG_TAG, "getDatabase");
        if (INSTANCE == null) {
            synchronized (ApplicationDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ApplicationDb.class, "application_db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
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
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){

        @Override
        public void onOpen (@NonNull SupportSQLiteDatabase db){
            Log.v(LOG_TAG, "onOpen");
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Populate the database with initial voices in the background.
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
            if (appData == null) {
                // initialize fresh app data table values. This is after a fresh install
                Log.i(LOG_TAG, "PopulateDbAsync: Fresh install, initializing App data table");
                appData = new AppData();
                mAppDataDao.insert(appData);
            } else {
                Log.v(LOG_TAG, "PopulateDbAsync: App data table found.");
            }

            List<Voice> voices = mVoiceDao.getAnyVoices();
            if (voices == null || voices.isEmpty()) {
                Log.d(LOG_TAG, "PopulateDbAsync: no voices yet");
            } else {
                Log.d(LOG_TAG, "PopulateDbAsync: Voices found.");
            }
            return null;
        }
    }
}
