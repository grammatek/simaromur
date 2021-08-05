package com.grammatek.simaromur;

import androidx.room.AutoMigration;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.grammatek.simaromur.db.ApplicationDb;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * ERoom database migration tests
 *
 */
@RunWith(AndroidJUnit4.class)
public class TestRoomDbMigration {

    private final String TEST_DB_NAME="simaromur-test";

    @Rule
    public MigrationTestHelper testHelper;

    public TestRoomDbMigration() {
        testHelper = new MigrationTestHelper((InstrumentationRegistry.getInstrumentation()),
                ApplicationDb.class.getCanonicalName(), new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrationFrom1To2() throws IOException {
        // Create the database in version 1
        SupportSQLiteDatabase db =
                testHelper.createDatabase(TEST_DB_NAME, 1);
        db.execSQL("INSERT INTO app_data_table VALUES (1, '1', 2, '/flite/voices', 'today'," +
                " '/sim/voices', 'today')");

        db.execSQL("INSERT INTO voice_table VALUES (1, 'Álfur', 'male', 'Alfur', 'is-IS', 'Íslenska(icelandic)'," +
                " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
        db.execSQL("INSERT INTO voice_table VALUES (3, 'Dóra', 'female', 'Dora', 'is-IS', 'Íslenska(icelandic)'," +
                " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
        // index is just created on name, internal_name may be redundant. That's why schema v2 was created
        db.execSQL("INSERT INTO voice_table VALUES (2, 'Alfur', 'male', 'Alfur', 'is-IS', 'Íslenska(icelandic)'," +
                " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
        db.execSQL("INSERT INTO voice_table VALUES (4, 'Dora', 'female', 'Dora', 'is-IS', 'Íslenska(icelandic)'," +
                " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
        db.close();
        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, ApplicationDb.MIGRATION_1_2);
    }

    @Test
    public void TestDBV2() throws IOException {
        // Create DB in V2
        SupportSQLiteDatabase db =
                testHelper.createDatabase(TEST_DB_NAME, 2);
        db.execSQL("INSERT INTO app_data_table VALUES (1, '1', 2, '/flite/voices', 'today'," +
                " '/sim/voices', 'today')");

        db.execSQL("INSERT INTO voice_table VALUES (1, 'Álfur', 'male', 'Alfur', 'is-IS', 'Íslenska(icelandic)'," +
                " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
        db.execSQL("INSERT INTO voice_table VALUES (2, 'Dóra', 'female', 'Dora', 'is-IS', 'Íslenska(icelandic)'," +
                " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");

        try {
            // try to add a bunch of redundant entries: unique index constraint for internal name
            // is then violated
            db.execSQL("INSERT INTO voice_table VALUES (3, 'Alfur', 'male', 'Alfur', 'is-IS', 'Íslenska(icelandic)'," +
                    " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
            db.execSQL("INSERT INTO voice_table VALUES (4, 'Dora', 'female', 'Dora', 'is-IS', 'Íslenska(icelandic)'," +
                    " '', 'tiro', 'now', 'now', 'http://someurl', 'downloadpath', 'API1', 'nomd5sum', 0)");
            fail("Exception not thrown");
        }
        catch (Exception e) {
            // nothing
        }

        db.close();
    }
}
