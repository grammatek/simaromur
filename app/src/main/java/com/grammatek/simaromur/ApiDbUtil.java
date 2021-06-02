package com.grammatek.simaromur;

import android.util.Log;

import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.db.VoiceDao;
import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiDbUtil {
    private final static String LOG_TAG = "Simaromur_" + ApiDbUtil.class.getSimpleName();
    VoiceDao mVoiceDao;

    ApiDbUtil(VoiceDao voiceDao)
    {
        mVoiceDao = voiceDao;
    }

    /**
     * Updates the model voice according to given apiVoice. The model voice has to already exist
     * inside the db.
     *
     * @param apiVoice      API voice as returned from API endpoint
     * @param modelVoice    Model voice inside db
     */
    private void updateModelVoice(VoiceResponse apiVoice, Voice modelVoice) {
        Log.v(LOG_TAG, "Updating voice: " + modelVoice);
        Log.v(LOG_TAG, "With: " + apiVoice);
        assert (modelVoice.voiceId != 0);   // the modelVoice should already be inside db
        modelVoice.internalName = apiVoice.VoiceId;
        modelVoice.languageCode = apiVoice.LanguageCode;
        modelVoice.languageName = apiVoice.LanguageName;
        modelVoice.type = "tiro";
        mVoiceDao.updateVoices(modelVoice);
    }

    /**
     * Updates all model voices from given Api voices.
     *
     * @param apiVoices     Voice list from API endpoint
     */
    public void updateModelVoices(List<VoiceResponse> apiVoices) {
        // we collect all potentially new voices here
        Set<VoiceResponse> newApiVoices = new HashSet<>(apiVoices);
        List<com.grammatek.simaromur.db.Voice> dbApiVoices = mVoiceDao.findNetworkVoices();

        for (VoiceResponse apiVoice: apiVoices) {
            for (com.grammatek.simaromur.db.Voice dbVoice:dbApiVoices) {
                if (dbVoice.internalName.equals(apiVoice.VoiceId)) {
                    // remove known apiVoice from set
                    assert(newApiVoices.remove(apiVoice));

                    // the internal name matches and it's a network voice: examine any other
                    // important attribute
                    if (!apiVoiceEqualsModel(apiVoice, dbVoice)) {
                        // update voice
                        updateModelVoice(apiVoice, dbVoice);
                    }
                }
            }
        }

        // create new voices from our list of unknown voices
        for (VoiceResponse av:newApiVoices) {
            Log.v(LOG_TAG, "Creating new voice from " + av);
            Voice voice = new Voice(av.VoiceId, av.VoiceId, av.Gender, av.LanguageCode, av.LanguageName,
                    "", "tiro", "");
            Log.v(LOG_TAG, "New contents: " + voice);
            mVoiceDao.insertVoice(voice);
        }
    }

    /**
     * Checks, if the given Api Voice matches a given model voice.
     *
     * These are the currently used match criteria:
     *
     *      apiVoice.VoiceId        == modelVoice.mInternalName
     *      apiVoice.LanguageCode   == modelVoice.mLanguageCode
     *      apiVoice.LanguageName   == modelVoice.mLanguageName
     *      "tiro"                  == modelVoice.mType
     *
     * @param apiVoice      API voice as returned from API endpoint
     * @param modelVoice    Model voice inside db
     *
     * @return  true in case given apiVoice matches given modelVoice, false otherwise
     */
    static private boolean apiVoiceEqualsModel(VoiceResponse apiVoice, Voice modelVoice) {
        assert (modelVoice.voiceId != 0);   // the modelVoice should already be inside db
        return (apiVoice.VoiceId.equals(modelVoice.internalName) &&
                apiVoice.LanguageCode.equals(modelVoice.languageCode) &&
                apiVoice.LanguageName.equals(modelVoice.languageName) &&
                "tiro".equals(modelVoice.type));
    }
}
