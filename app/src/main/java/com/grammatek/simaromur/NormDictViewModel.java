package com.grammatek.simaromur;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.db.NormDictEntry;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.TTSAudioControl;
import com.grammatek.simaromur.device.TTSEngineController;

import java.util.List;
import java.util.Objects;

public class NormDictViewModel extends AndroidViewModel {
    private final static String LOG_TAG = "Simaromur_" + NormDictViewModel.class.getSimpleName();

    private final AppRepository mRepository;
    private TTSEngineController.SpeakTask mDevSpeakTask = null;

    // these variables are for data caching
    private LiveData<List<NormDictEntry>> mAllEntries;

    public NormDictViewModel(Application application) {
        super(application);
        mRepository = App.getAppRepository();
    }

    // Return all entries
    public LiveData<List<NormDictEntry>> getEntries() {
        if (mAllEntries == null) {
            mAllEntries = mRepository.getUserDictEntries();
        }
        return mAllEntries;
    }

    // Return specific entry
    public NormDictEntry getEntryWithId(long entryId) {
        if (mAllEntries == null) {
            return null;
        }
        for(NormDictEntry entry: Objects.requireNonNull(mAllEntries.getValue())) {
            if (entry.id == entryId)
                return entry;
        }
        return null;
    }

    // Start speaking, i.e. make a speak request async.
    public void startSpeaking(Voice voice, CacheItem item, float speed, float pitch,
                              TTSAudioControl.AudioFinishedObserver finishedObserver) {
        switch (voice.type) {
            case Voice.TYPE_NETWORK:
                mRepository.startNetworkSpeak(voice.internalName, voice.version, item, voice.languageCode, finishedObserver);
                break;
            case Voice.TYPE_ONNX:
                mDevSpeakTask = mRepository.startDeviceSpeak(voice, item, speed, pitch, finishedObserver);
                break;
            default:
                // other voice types follow here ..
                break;
        }
    }

    /**
     * Stops any ongoing speak activity
     */
    public void stopSpeaking(Voice voice) {
        if (voice == null) {
            return;
        }
        if (voice.type.equals(Voice.TYPE_NETWORK)) {
            mRepository.stopNetworkSpeak();
        } else {
            mRepository.stopDeviceSpeak(mDevSpeakTask);
        }
        mDevSpeakTask = null;
    }

    public void createOrUpdate(NormDictEntry mEntry) {
        Log.v(LOG_TAG, "update: " + mEntry.term + " -> " + mEntry.replacement);
        mRepository.createOrUpdateUserDictEntry(mEntry);
    }

    public void delete(NormDictEntry mEntry) {
        Log.v(LOG_TAG, "delete: " + mEntry.term + " -> " + mEntry.replacement);
        mRepository.deleteUserDictEntry(mEntry);
    }
}
