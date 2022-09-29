package com.grammatek.simaromur;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.db.AppData;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.TTSAudioControl;
import com.grammatek.simaromur.device.TTSEngineController;

import java.util.List;
import java.util.Objects;

/**
 * View Model to keep a reference to the App repository and
 * an up-to-date list of all voices.
 */
public class VoiceViewModel extends AndroidViewModel {
    private final static String LOG_TAG = "Simaromur_" + VoiceViewModel.class.getSimpleName();

    private final AppRepository mRepository;
    private TTSEngineController.SpeakTask mDevSpeakTask = null;

    // these variables are for data caching
    private AppData mAppData;                       // application data
    private LiveData<List<Voice>> mAllVoices;       // our current voices model

    public VoiceViewModel(Application application) {
        super(application);
        mRepository = App.getAppRepository();
    }

    // Return application data
    public AppData getAppData() {
        if (mAppData == null) {
            mAppData = mRepository.getCachedAppData();
        }
        return mAppData;
    }

    // Return all voices
    public LiveData<List<Voice>> getAllVoices() {
        if (mAllVoices == null) {
            mAllVoices = mRepository.getAllVoices();
        }
        return mAllVoices;
    }

    // Return specific voice
    public Voice getVoiceWithId(long voiceId) {
        if (mAllVoices == null) {
            return null;
        }
        for(Voice voice: Objects.requireNonNull(mAllVoices.getValue())) {
            if (voice.voiceId == voiceId)
                return voice;
        }
        return null;
    }

    // Start speaking, i.e. make a speak request async.
    public void startSpeaking(Voice voice, CacheItem item, float speed, float pitch,
                              TTSAudioControl.AudioFinishedObserver finishedObserver) {
        switch (voice.type) {
            case Voice.TYPE_TIRO:
                mRepository.startNetworkSpeak(voice.internalName, item, voice.languageCode, finishedObserver);
                break;
            case Voice.TYPE_TORCH:
            case Voice.TYPE_FLITE:  // FALLTHROUGH
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
        if (voice.type.equals(Voice.TYPE_TIRO)) {
            mRepository.stopNetworkSpeak();
        } else if (voice.type.equals(Voice.TYPE_TORCH)) {
            mRepository.stopDeviceSpeak(mDevSpeakTask);
        } else {
            mRepository.stopDeviceSpeak(mDevSpeakTask);
        }
        mDevSpeakTask = null;
    }
}
