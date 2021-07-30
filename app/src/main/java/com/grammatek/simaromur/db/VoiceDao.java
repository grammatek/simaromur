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

    @Query("SELECT * FROM voice_table as voices order by voices.internal_name asc")
    public LiveData<List<Voice>> getAllVoices();

    /**
     * Returns all voices without using LiveData container.
     * We need this e.g. for the initialization step to populate
     * the db.
     *
     * @return List of all voices or null in case there are no voices yet.
     */
    @Query("SELECT * FROM voice_table")
    public List<Voice> getAnyVoices();

    @Query("SELECT * FROM voice_table WHERE name LIKE :name ")
    public List<Voice> findVoiceWithName(String name);

    @Query("SELECT * FROM voice_table WHERE name IS :name AND internal_name IS :internalName" +
            " AND language_code IS :languageCode AND language_name IS :languageName AND variant IS :variant")
    public Voice findVoice(String name, String internalName, String languageCode,  String languageName,
                           String variant);

    @Query("SELECT * FROM voice_table WHERE type LIKE 'tiro' ")
    public List<Voice> findNetworkVoices();

    @Query("SELECT * FROM voice_table WHERE type in ('clustergen', 'clunit', 'neural') ")
    public List<Voice> findLocalVoices();

    // TODO(DS): To be continued ....
}
