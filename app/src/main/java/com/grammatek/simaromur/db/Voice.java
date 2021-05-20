package com.grammatek.simaromur.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

// Create unique index on name, language, country, variant
@Entity(tableName = "voice_table",
        indices = {@Index(value = {"mName", "mLanguage", "mCountry", "mVariant"},
        unique = true)})
public class Voice {

    @PrimaryKey
    public long voiceId;

    // Voice name (mandatory)
    //  e.g. Karl, Dóra
    @NonNull
    public String mName;

    // Internal voice name (mandatory)
    //  e.g. "other", ""
    @NonNull
    public String mInternalName;

    // ISO-3 language code (not verified)
    @NonNull
    public String mLanguage;

    // ISO-3 country code (not verified)
    @NonNull
    public String mCountry;

    // language variant, e.g. "north-clear" (not verified)
    @NonNull
    public String mVariant;

    // Voice type (verified)
    // local voices: "clustergen", "clunits", "neural"
    // network voices: "tiro"
    public String mType;

    // creation date of DB entry
    @ColumnInfo(name = "update_time")
    @TypeConverters({TimestampConverter.class})
    public Date updateTime;

    // download date/time for non-network voice
    @ColumnInfo(name = "download_time")
    @TypeConverters({TimestampConverter.class})
    public Date downloadTime;

    // the http URL of a downloadable voice, empty for a network voice
    public String mUrl;

    // For local voices, the fully qualified local filename, or empty in case it's not yet
    // downloaded. For network voices: empty
    @ColumnInfo(name = "download_path") public String downloadPath;

    // Version of the voice, if available. Should be semantically versioned (major, minor, patch)
    public String version;

    // MD5 checksum of downloaded voice as String, empty if not yet downloaded or in case of a
    // network voice
    @ColumnInfo(name = "md5_sum") public String md5Sum;

    // file size if local voice file, 0 means not yet downloaded or network voice
    public long size;

    // Constructor for NonNull parameters
    public Voice(@NonNull String name,
                 @NonNull String internalName,
                 @NonNull String language,
                 @NonNull String country,
                 @NonNull String variant,
                 @NonNull String type,
                 @NonNull String url) {
        mName = name;
        mInternalName = internalName;
        mLanguage = language;
        mCountry = country;
        mVariant = variant;
        mType = type;
        mUrl = url;
    }
}
