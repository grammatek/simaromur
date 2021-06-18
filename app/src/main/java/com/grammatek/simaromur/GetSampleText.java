package com.grammatek.simaromur;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/*
 * Intent for returning a sample text for passed language
 */
public class GetSampleText extends Activity {
    private final static String LOG_TAG = "Simaromur_Java_" + GetSampleText.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        final String iso3Language = getLocaleFromIntent(getIntent()).getISO3Language();

        Intent returnData = new Intent();
        String sampleText = "";
        int rv = TextToSpeech.LANG_AVAILABLE;
        if (iso3Language.equals("eng")) {
          sampleText = getString(R.string.eng_sample);
        } else if (iso3Language.equals("isl")) {
            sampleText = getString(R.string.icelandic_sample);
        } else {
            Log.v(LOG_TAG, "Language not supported: " + iso3Language);
            rv = TextToSpeech.LANG_NOT_SUPPORTED;
        }
        Log.v(LOG_TAG, "sampleText: " + sampleText);
        returnData.putExtra(TextToSpeech.Engine.EXTRA_SAMPLE_TEXT, sampleText);
        setResult(rv, returnData);
        finish();
    }

    private static Locale getLocaleFromIntent(Intent intent) {
        if (intent != null) {
            final String language = intent.getStringExtra("language");

            if (language != null) {
                return new Locale(language);
            }
        }

        return Locale.getDefault();
    }
}
