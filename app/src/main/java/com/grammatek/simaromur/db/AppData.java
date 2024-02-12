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
    @NonNull
    public String schemaVersion;

    // Currently chosen voice
    @ColumnInfo(name = "current_voice_id")
    public long currentVoiceId;

    // last download date/time for non-network FLite voice
    @ColumnInfo(name = "voice_list_update_time")
    @TypeConverters({TimestampConverter.class})
    public Date voiceListUpdateTime;

    // boolean for privacy info dialog acceptance of the user
    @ColumnInfo(name = "privacy_info_dialog_accepted", defaultValue = "0")
    @NonNull
    public Boolean privacyInfoDialogAccepted;

    // boolean for CrashLytics user consent
    @ColumnInfo(name = "crash_lytics_user_consent_accepted", defaultValue = "0")
    @NonNull
    public Boolean crashLyticsUserConsentGiven;

    public AppData() {
        schemaVersion = "8";
        currentVoiceId = -1;
        privacyInfoDialogAccepted = false;
        crashLyticsUserConsentGiven = false;
    }

    @NonNull
    @Override
    public String toString() {
        return "AppData{" +
                "appDataId=" + appDataId +
                ", schemaVersion='" + schemaVersion + '\'' +
                ", currentVoiceId=" + currentVoiceId +
                ", voiceListUpdateTime=" + voiceListUpdateTime +
                ", privacyInfoDialogAccepted=" + privacyInfoDialogAccepted +
                ", crashLyticsUserConsentGiven=" + crashLyticsUserConsentGiven +
                '}';
    }
}
