package com.grammatek.simaromur;

import android.util.Log;

import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.db.VoiceDao;
import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiDbUtil {
    // Appended to voice name to expose the network type in the voice settings screen
    public final static String NET_VOICE_SUFFIX = " net";

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
        modelVoice.name = apiVoice.Name.concat(NET_VOICE_SUFFIX);
        modelVoice.internalName = apiVoice.VoiceId;
        modelVoice.languageCode = apiVoice.LanguageCode;
        modelVoice.languageName = apiVoice.LanguageName;
        modelVoice.type = Voice.TYPE_TIRO;
        mVoiceDao.updateVoices(modelVoice);
    }

    /**
     * Updates all model voices from given Api voices of given type.
     *
     * @param apiVoices     Voice list from API endpoint
     * @param voiceType     Voice type to be used, only voices of that type are updated
     */
    public void updateApiVoices(List<VoiceResponse> apiVoices, String voiceType) {
        // we collect all potentially new voices here
        Set<VoiceResponse> newApiVoices = new HashSet<>(apiVoices);
        List<com.grammatek.simaromur.db.Voice> dbApiVoices = mVoiceDao.findNetworkVoices();

        // update existing voices, in case something has changed
        for (VoiceResponse apiVoice: apiVoices) {
            for (com.grammatek.simaromur.db.Voice dbVoice:dbApiVoices) {
                if (dbVoice.internalName.equals(apiVoice.VoiceId)
                        && dbVoice.type.equals(voiceType)) {
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
            Voice voice = new Voice(av.Name, av.VoiceId, av.Gender, av.LanguageCode, av.LanguageName,
                    "", Voice.TYPE_TIRO, "", "", "", "", 0);
            Log.v(LOG_TAG, "New contents: " + voice);
            try {
                mVoiceDao.insertVoice(voice);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Voice could not be added to db: " + voice);
                Log.e(LOG_TAG, "Reason: " + e.getMessage());
            }
        }

        // remove voices, that are not inside the provided API voices
        Set<String> newApiVoiceIds = new HashSet<>();
        for (VoiceResponse voice:apiVoices) {
            newApiVoiceIds.add(voice.VoiceId);
        }
        for (Voice voice: mVoiceDao.findNetworkVoices()) {
            if (voice.type.equals(voiceType)
                    && (! newApiVoiceIds.contains(voice.internalName))) {
                try {
                    Log.v(LOG_TAG, "Deleting existing voice from db: " + voice);
                    mVoiceDao.deleteVoices(voice);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Voice could not be deleted from db: " + voice);
                    Log.e(LOG_TAG, "Reason: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks, if the given Api Voice matches a given model voice.
     *
     * These are the currently used match criteria:
     *
     *      apiVoice.VoiceId        == modelVoice.internalName
     *      apiVoice.Name           == modelVoice.name
     *      apiVoice.LanguageCode   == modelVoice.languageCode
     *      apiVoice.LanguageName   == modelVoice.languageName
     *      apiVoice.Gender         == modelVoice.gender
     *      "tiro"                  == modelVoice.type
     *
     * @param apiVoice      API voice as returned from API endpoint
     * @param modelVoice    Model voice inside db
     *
     * @return  true in case given apiVoice matches given modelVoice, false otherwise
     */
    static private boolean apiVoiceEqualsModel(VoiceResponse apiVoice, Voice modelVoice) {
        assert (modelVoice.voiceId != 0);   // the modelVoice should already be inside db
        return (apiVoice.VoiceId.equals(modelVoice.internalName) &&
                apiVoice.Name.equals(modelVoice.name) &&
                apiVoice.LanguageCode.equals(modelVoice.languageCode) &&
                apiVoice.LanguageName.equals(modelVoice.languageName) &&
                apiVoice.Gender.equals(modelVoice.gender) &&
                Voice.TYPE_TIRO.equals(modelVoice.type));
    }
}
