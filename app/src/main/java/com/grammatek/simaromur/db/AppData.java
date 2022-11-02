package com.grammatek.simaromur.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "app_data_table")
public class AppData {
    @PrimaryKey(autoGenerate = true)
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
    //

    // path to SIM voice list, empty if not yet downloaded
    @ColumnInfo(name = "sim_voice_list_path")
    public String simVoiceListPath;

    // last download date/time for non-network SIM voice
    @ColumnInfo(name = "sim_voice_list_update_time")
    @TypeConverters({TimestampConverter.class})
    public Date simVoiceListUpdateTime;

    // boolean for privacy info dialog acceptance of the user
    @ColumnInfo(name = "privacy_info_dialog_accepted", defaultValue = "0")
    @NonNull
    public Boolean privacyInfoDialogAccepted = false;

    // boolean for CrashLytics user consent
    @ColumnInfo(name = "crash_lytics_user_consent_accepted", defaultValue = "0")
    @NonNull
    public Boolean crashLyticsUserConsentGiven = false;

    @NonNull
    @Override
    public String toString() {
        return "AppData{" +
                "appDataId=" + appDataId +
                ", schemaVersion='" + schemaVersion + '\'' +
                ", currentVoiceId=" + currentVoiceId +
                ", fliteVoiceListPath='" + fliteVoiceListPath + '\'' +
                ", fliteVoiceListUpdateTime=" + fliteVoiceListUpdateTime +
                ", simVoiceListPath='" + simVoiceListPath + '\'' +
                ", simVoiceListUpdateTime=" + simVoiceListUpdateTime +
                ", privacyInfoDialogAccepted=" + privacyInfoDialogAccepted +
                ", crashLyticsUserConsentGiven=" + crashLyticsUserConsentGiven +
                '}';
    }
}
