package com.grammatek.simaromur;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.db.AppData;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * View Model to keep a reference to the App repository and
 * an up-to-date list of all voices.
 */
public class VoiceViewModel extends AndroidViewModel {
    private final static String LOG_TAG = "Simaromur_" + VoiceViewModel.class.getSimpleName();

    private AppRepository mRepository;
    // these variables are for data caching
    private AppData mAppData;
    private LiveData<List<Voice>> mAllVoices;
    private List<VoiceResponse> mTiroVoices;

    public VoiceViewModel(Application application) {
        super(application);
        mRepository = new AppRepository(application);
    }

    // model accessors
    public AppData getAppData() {
        if (mAppData == null) {
            mAppData = mRepository.getAppData();
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

    // Return all voices
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

    // Query Tiro Voices: either return cashed version of voices or make a synchronous request
    public List<VoiceResponse> queryTiroVoices(String languageCode) {
        Log.v(LOG_TAG, "queryTiroVoices");
        if (mTiroVoices == null) {
            try {
                mTiroVoices = mRepository.queryTiroVoices(languageCode);
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "queryTiroVoices failed");
            }
        }
        return mTiroVoices;
    }

    // Start fetching new voices from api, if any updates are available, the voice model is updated
    public void startFetchingNetworkVoices(String languageCode) {
        mRepository.streamTiroVoices(languageCode);
    }

    // TODO(DS): To be continued ....
}
