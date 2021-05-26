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
    public LiveData<List<Voice>> getAllVoices() {
        if (mAllVoices == null) {
            mAllVoices = mRepository.getAllVoices();
        }
        return mAllVoices;
    }

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
    public void startFetchingNetworkVoices(String languageCode) {
        mRepository.streamTiroVoices(languageCode);
    }

    // TODO(DS): To be continued ....
}
