package com.grammatek.simaromur.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

// Create unique index on name, language, country, variant
@Entity(tableName = "voice_table",
        indices = {@Index(value = {"name", "gender", "language_code", "type"}, unique = true)})
public class Voice {

    static final List<String> voiceTypes = Arrays.asList("tiro", "clustergen", "clunits", "neural");

    @PrimaryKey(autoGenerate = true)
    public long voiceId;

    // Voice name (mandatory)
    //  e.g. Karl, Dóra
    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    // Voice Gender (mandatory)
    //  e.g. Male, Female
    @NonNull
    @ColumnInfo(name = "gender")
    public String gender;

    // Internal voice name (mandatory)
    //  e.g. "other", "Dora",
    @NonNull
    @ColumnInfo(name = "internal_name")
    public String internalName;

    // language code (mandatory)
    //  e.g. "is-IS"
    @NonNull
    @ColumnInfo(name = "language_code")
    public String languageCode;

    // language name (mandatory)
    //  e.g. "Íslenska"
    @NonNull
    @ColumnInfo(name = "language_name")
    public String languageName;

    // language variant, e.g. "north-clear" (optional, not used for network voices)
    @NonNull
    @ColumnInfo(name = "variant")
    public String variant;

    // Voice type (verified)
    // local voices: "clustergen", "clunits", "neural"
    // network voices: "tiro"
    @ColumnInfo(name = "type")
    public String type;

    // creation date of DB entry
    @ColumnInfo(name = "update_time")
    @TypeConverters({TimestampConverter.class})
    public Date updateTime;

    // download date/time for non-network voice
    @ColumnInfo(name = "download_time")
    @TypeConverters({TimestampConverter.class})
    public Date downloadTime;

    // the http URL of a downloadable voice, empty for a network voice
    @ColumnInfo(name = "url")
    public String url;

    // For local voices, the fully qualified local filename, or empty in case it's not yet
    // downloaded. For network voices: empty
    @ColumnInfo(name = "download_path")
    public String downloadPath;

    // Version of the voice, if available. Should be semantically versioned (major, minor, patch)
    @ColumnInfo(name = "version")
    public String version;

    // MD5 checksum of downloaded voice as String, empty if not yet downloaded or in case of a
    // network voice
    @ColumnInfo(name = "md5_sum") public String md5Sum;

    // file size if local voice file, 0 means not yet downloaded or network voice
    @ColumnInfo(name = "local_size")
    public long size;

    // Constructor for NonNull parameters
    public Voice(@NonNull String name,
                 @NonNull String internalName,
                 @NonNull String gender,
                 @NonNull String languageCode,
                 @NonNull String languageName,
                 @NonNull String variant,
                 @NonNull String type,
                 @NonNull String url) {
        this.name = name;
        this.internalName = internalName;
        this.gender = gender;
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.variant = variant;
        // @todo: check correctness of type
        if (voiceTypes.contains(type)) {
            this.type = type;
        }
        else {
            throw new AssertionError("Given type not valid !");
        }
        this.url = url;
        this.updateTime = new Date();
    }

    @Override
    public String toString() {
        return "Voice{" +
                "voiceId=" + voiceId +
                ", name='" + name + '\'' +
                ", internalName='" + internalName + '\'' +
                ", languageCode='" + languageCode + '\'' +
                ", languageName='" + languageName + '\'' +
                ", variant='" + variant + '\'' +
                ", type='" + type + '\'' +
                ", updateTime=" + updateTime +
                ", downloadTime=" + downloadTime +
                ", url='" + url + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", version='" + version + '\'' +
                ", md5Sum='" + md5Sum + '\'' +
                ", size=" + size +
                '}';
    }
}
