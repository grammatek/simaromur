package com.grammatek.simaromur;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/*
 This activity gets triggered, when Android tries to fill the list of provided voices. The
 user can then choose between different voices that should be used for TTS.
*/
public class CheckSimVoices extends Activity {
    private final static String LOG_TAG = "Simaromur_Java_" + CheckSimVoices.class.getSimpleName();

	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        AppRepository mAppRepository = App.getAppRepository();
        mAppRepository.streamNetworkVoices("");
        int result = TextToSpeech.Engine.CHECK_VOICE_DATA_PASS;
        Intent returnData = new Intent();

        List<com.grammatek.simaromur.db.Voice> voices = mAppRepository.getCachedVoices();
        if (voices == null || voices.isEmpty()) {
            Log.e(LOG_TAG, "no voices available yet ?!");
            // todo: maybe we can use direct db calls and blocking network requests, as we are
            //       probably not called here inside the UI thread ?
            result = TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL;
            setResult(result, returnData);
            finish();
            return;
        }

        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> unavailable = new ArrayList<>();

        for (final com.grammatek.simaromur.db.Voice voice:voices) {
            String entry = voice.iso3LangCountryName();
            if (voice.isAvailable()) {
                available.add(entry);
                Log.i(LOG_TAG, "available: " + voice);
            } else {
                unavailable.add(entry);
                Log.i(LOG_TAG, "unavailable: " + voice);
            }
        }

        returnData.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available);
        returnData.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES, unavailable);
        setResult(result, returnData);

        finish();
    }
}
