package com.grammatek.simaromur.db;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;

@Dao
public abstract class AppDataDao {
    private final static String LOG_TAG = "Simaromur_" + AppDataDao.class.getSimpleName();

    @Insert
    public abstract void insert(AppData app);

    @Update
    public abstract void update(AppData app);

    @Delete
    public abstract void delete(AppData app);

    // get the single AppData object
    @Query("SELECT * FROM app_data_table ORDER BY ROWID ASC LIMIT 1")
    public abstract AppData getAppData();

    // get the single AppData object as LiveData
    @Query("SELECT * FROM app_data_table ORDER BY ROWID ASC LIMIT 1")
    public abstract LiveData<AppData> getLiveAppData();

    // Return boolean, if privacy notice has been accepted
    public Boolean hasAcceptedPrivacyNotice() {
        return getAppData().privacyInfoDialogAccepted;
    }

    // Sets boolean for privacy notice acceptance
    public void doAcceptPrivacyNotice(Boolean setter) {
        AppData appData = getAppData();
        appData.privacyInfoDialogAccepted = setter;
        update(appData);
    }

    // Return boolean, if CrashLytics user consent has been given
    public Boolean hasGivenCrashLyticsUserConsent() {
        return getAppData().crashLyticsUserConsentGiven;
    }

    // Sets boolean for CrashLytics user consent
    public void doGiveCrashLyticsUserConsent(Boolean setter) {
        AppData appData = getAppData();
        appData.crashLyticsUserConsentGiven = setter;
        update(appData);
    }

    /**
     * Updates current voice in AppData table.
     *
     * @param voice the current voice to select, it needs to already exist in db
     */
    public void selectCurrentVoice(Voice voice) {
        if ((voice.voiceId <= 0))
            throw new AssertionError("selectCurrentVoice: voiceId <= 0");
        AppData appData = getAppData();
        appData.currentVoiceId = voice.voiceId;
        update(appData);
    }

    /**
     * Returns id of current voice from AppData table.
     *
     * @return voice id of the current selected voice or -1 if no voice is selected
     */
    public Long getCurrentVoiceId() {
        AppData appData = getAppData();
        return appData.currentVoiceId;
    }

    /**
     * Update voice list update date & time to now
     */
    public void updateVoiceListTimestamp() {
        AppData appData = getAppData();
        appData.voiceListUpdateTime = new java.util.Date();
        Log.v(LOG_TAG, "updateVoiceListTimestamp: " + appData.voiceListUpdateTime);
        update(appData);
    }

    public boolean voiceListUpdateTimeOlderThan(java.util.Date date) {
        AppData appData = getAppData();
        if (appData == null) {
            return true;
        }
        Date lastUpdate = appData.voiceListUpdateTime;
        if (lastUpdate == null) {
            return true;
        }
        return (appData.voiceListUpdateTime.before(date));
    }
}
