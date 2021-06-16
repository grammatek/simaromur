package com.grammatek.simaromur.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class AppDataDao {
    @Insert
    public abstract void insert(AppData app);

    @Update
    public abstract void update(AppData app);

    // TODO(DS): this should be called only for special circumstances
    @Delete
    public abstract void delete(AppData app);

    // get the single AppData object
    @Query("SELECT * FROM app_data_table ORDER BY ROWID ASC LIMIT 1")
    public abstract AppData getAppData();

    // get the single AppData object as LiveData
    @Query("SELECT * FROM app_data_table ORDER BY ROWID ASC LIMIT 1")
    public abstract LiveData<AppData> getLiveAppData();

    /**
     * Updates current voice in AppData table.
     *
     * @param voice the current voice to select, it needs to already exist in db
     */
    public void selectCurrentVoice(Voice voice) {
        if ((voice.voiceId == 0))
            throw new AssertionError("selectCurrentVoice: voiceId == 0");
        AppData appData = getAppData();
        appData.currentVoiceId = voice.voiceId;
        update(appData);
    }

    /**
     * Returns id of current voice from AppData table.
     *
     * @return voice id of the current selected voice
     */
    public Long getCurrentVoiceId() {
        AppData appData = getAppData();
        return appData.currentVoiceId;
    }
    // TODO(DS): To be continued ....
}
