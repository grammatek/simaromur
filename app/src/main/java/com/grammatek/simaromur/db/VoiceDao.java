package com.grammatek.simaromur.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VoiceDao {
    @Insert
    void insertVoices(Voice... voices);

    @Insert
    void insertVoice(Voice voice);

    @Update
    public void updateVoices(Voice... voices);

    @Delete
    void deleteVoices(Voice... voices);

    @Query("SELECT * FROM voice_table")
    public LiveData<List<Voice>> getAllVoices();

    @Query("SELECT * FROM voice_table WHERE mName LIKE :name ")
    public List<Voice> findVoiceWithName(String name);

    @Query("SELECT * FROM voice_table WHERE mType LIKE 'tiro' ")
    public List<Voice> findNetworkVoices();

    @Query("SELECT * FROM voice_table WHERE mType in ('clustergen', 'clunit', 'neural') ")
    public List<Voice> findLocalVoices();

    // TODO(DS): To be continued ....
}
