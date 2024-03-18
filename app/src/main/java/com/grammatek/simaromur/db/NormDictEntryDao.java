package com.grammatek.simaromur.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NormDictEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NormDictEntry dictEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NormDictEntry... dictEntries);

    @Update
    void update(NormDictEntry... dictEntries);

    @Delete
    void delete(NormDictEntry... dictEntries);

    @Query("SELECT * FROM norm_dict_table as entries order by entries.term COLLATE NOCASE asc")
    LiveData<List<NormDictEntry>> getSortedEntries();

    @Query("SELECT * FROM norm_dict_table as entries order by entries.term COLLATE NOCASE asc")
    List<NormDictEntry> getEntries();
}
