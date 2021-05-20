package com.grammatek.simaromur.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "app_data_table")
public class AppData {
    @PrimaryKey
    public long appDataId;

    // Version string of the database schema
    @ColumnInfo(name = "schema_version")
    public String schemaVersion;

    // Currently chosen voice
    @ColumnInfo(name = "current_voice_id")
    public long currentVoiceId;

    //
    // FLite voices download info
    //

    // path to downloaded flite voice list, empty if not yet downloaded
    @ColumnInfo(name = "flite_voice_list_path")
    public String fliteVoiceListPath;

    // last download date/time for non-network FLite voice
    @ColumnInfo(name = "flite_voice_list_update_time")
    @TypeConverters({TimestampConverter.class})
    public Date fliteVoiceListUpdateTime;

    //
    // SIM voices download info
    // TODO(DS): contents of the voice list to be defined
    //

    // path to SIM voice list, empty if not yet downloaded
    @ColumnInfo(name = "sim_voice_list_path")
    public String simVoiceListPath;

    // last download date/time for non-network SIM voice
    @ColumnInfo(name = "sim_voice_list_update_time")
    @TypeConverters({TimestampConverter.class})
    public Date simVoiceListUpdateTime;
}
