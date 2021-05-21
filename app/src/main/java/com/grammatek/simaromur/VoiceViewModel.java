package com.grammatek.simaromur;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.grammatek.simaromur.db.AppData;
import com.grammatek.simaromur.db.AppRepository;
import com.grammatek.simaromur.db.Voice;

import java.util.List;

/**
 * View Model to keep a reference to the App repository and
 * an up-to-date list of all voices.
 */
public class VoiceViewModel extends AndroidViewModel {
    private AppRepository mRepository;
    // these variables are for data caching
    private AppData mAppData;
    private LiveData<List<Voice>> mAllVoices;

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

    // TODO(DS): To be continued ....
}
