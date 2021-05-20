package com.grammatek.simaromur.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AppDataDao {
    @Insert
    void insert(AppData app);

    @Update
    public void update(AppData app);

    // TODO(DS): this should be called only for special circumstances
    @Delete
    public void delete(AppData app);

    // get the single AppData object
    @Query("SELECT * FROM app_data_table ORDER BY ROWID ASC LIMIT 1")
    public AppData getAppData();

    /**
     * Updates current voice in AppData table.
     *
     * @param voice the current voice to select
     */
    @Update
    public void selectCurrentVoice(Voice voice);

    // TODO(DS): To be continued ....
}
