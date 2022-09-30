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
    void updateVoices(Voice... voices);

    @Delete
    void deleteVoices(Voice... voices);

    @Query("SELECT * FROM voice_table as voices order by voices.name glob '[A-Za-z]*' asc")
    LiveData<List<Voice>> getAllVoices();

    /**
     * Returns all voices without using LiveData container.
     * We need this e.g. for the initialization step to populate
     * the db.
     *
     * @return List of all voices or null in case there are no voices yet.
     */
    @Query("SELECT * FROM voice_table")
    List<Voice> getAnyVoices();

    @Query("SELECT * FROM voice_table WHERE name LIKE :name ")
    List<Voice> findVoiceWithName(String name);

    @Query("SELECT * FROM voice_table WHERE name IS :name AND internal_name IS :internalName" +
            " AND language_code IS :languageCode AND language_name IS :languageName AND variant IS :variant")
    Voice findVoice(String name, String internalName, String languageCode, String languageName,
                    String variant);

    @Query("SELECT * FROM voice_table WHERE type LIKE 'tiro' ")
    List<Voice> findNetworkVoices();

    // Return voices registered in Assets
    @Query("SELECT * FROM voice_table WHERE url in ('assets') ")
    List<Voice> getAssetVoices();

    // Return voices that can or have been downloaded
    @Query("SELECT * FROM voice_table WHERE url LIKE ('network:') OR url LIKE ('disk') ")
    List<Voice> getDownloadableVoices();

    // Return voices according to list of given types
    @Query("SELECT * FROM voice_table WHERE type in (:localTypeNames)")
    List<Voice> getVoicesForType(List<String> localTypeNames);

    // Return voice belonging to given voice id
    @Query("SELECT * FROM voice_table WHERE voiceId in (:voiceId)")
    Voice findVoiceWithId(long voiceId);
}
