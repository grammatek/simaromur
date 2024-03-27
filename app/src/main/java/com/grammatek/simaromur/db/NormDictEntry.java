package com.grammatek.simaromur.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * The NormDictEntry class represents the normalization dictionary table in the database.
 * It contains the schema version, the search term and the replacement term.
 */
@Entity(tableName = "norm_dict_table",
        indices = {@Index(value = {"term"}, unique = true)})
public class NormDictEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String term;
    public String replacement;

    public NormDictEntry() {
        term = "";
        replacement = "";
    }

    @NonNull
    @Override
    public String toString() {
        return "NormDictEntry{" +
                "normDictId=" + id +
                ", searchTerm=" + term +
                ", replacement=" + replacement +
                '}';
    }
}
