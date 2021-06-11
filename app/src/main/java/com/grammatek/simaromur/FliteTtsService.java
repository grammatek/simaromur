/*************************************************************************/
/*                                                                       */
/*                  Language Technologies Institute                      */
/*                     Carnegie Mellon University                        */
/*                         Copyright (c) 2010                            */
/*                        All Rights Reserved.                           */
/*                                                                       */
/*  Permission is hereby granted, free of charge, to use and distribute  */
/*  this software and its documentation without restriction, including   */
/*  without limitation the rights to use, copy, modify, merge, publish,  */
/*  distribute, sublicense, and/or sell copies of this work, and to      */
/*  permit persons to whom this work is furnished to do so, subject to   */
/*  the following conditions:                                            */
/*   1. The code must retain the above copyright notice, this list of    */
/*      conditions and the following disclaimer.                         */
/*   2. Any modifications must be clearly marked as such.                */
/*   3. Original authors' names are not deleted.                         */
/*   4. The authors' names are not used to endorse or promote products   */
/*      derived from this software without specific prior written        */
/*      permission.                                                      */
/*                                                                       */
/*  CARNEGIE MELLON UNIVERSITY AND THE CONTRIBUTORS TO THIS WORK         */
/*  DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING      */
/*  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT   */
/*  SHALL CARNEGIE MELLON UNIVERSITY NOR THE CONTRIBUTORS BE LIABLE      */
/*  FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES    */
/*  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN   */
/*  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,          */
/*  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF       */
/*  THIS SOFTWARE.                                                       */
/*                                                                       */
/*************************************************************************/
/*             Author:  Alok Parlikar (aup@cs.cmu.edu)                   */
/*               Date:  June 2012                                        */
/*************************************************************************/

package com.grammatek.simaromur;

import com.grammatek.simaromur.NativeFliteTTS.SynthReadyCallback;
import com.grammatek.simaromur.frontend.FrontendManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Implements the Flite Engine as a TextToSpeechService
 */
public class FliteTtsService extends TextToSpeechService {
    private final static String LOG_TAG = "Simaromur_Java_" + FliteTtsService.class.getSimpleName();
    private NativeFliteTTS mEngine;
    private FrontendManager mFrontendManager;

    private static final String DEFAULT_LANGUAGE = "isl";
    private static final String DEFAULT_COUNTRY = "ISL";
    private static final String DEFAULT_VARIANT = "Alfur";

    private String mCountry = DEFAULT_COUNTRY;
    private String mLanguage = DEFAULT_LANGUAGE;
    private String mVariant = DEFAULT_VARIANT;
    private SynthesisCallback mCallback;
    private AppRepository mRepository;

    @Override
    public void onCreate() {
        //mRepository = new AppRepository(App.getApplication());
        mRepository = App.getAppRepository();
        //initializeFliteEngine();
        initializeFrontendManager(this);
        // This calls onIsLanguageAvailable() and must run after Initialization
        super.onCreate();
    }

    /*
    private void initializeFliteEngine() {
        if (mEngine != null) {
            mEngine.stop();
            mEngine = null;
        }
        mEngine = new NativeFliteTTS(this, mSynthCallback);
    }
    */
    private void initializeFrontendManager(Context context) {
        if (mFrontendManager == null)
            mFrontendManager = new FrontendManager(context);
    }

    // mandatory for API < 18, but deprecated later on
    @Override
    protected String[] onGetLanguage()
    {
        Log.v(LOG_TAG, "onGetLanguage");
        return new String[] {
                mLanguage, mCountry, mVariant
        };
    }

    // mandatory
    @Override
    protected int onIsLanguageAvailable(String language, String country, String variant) {
        Log.v(LOG_TAG, "onIsLanguageAvailable("+language+","+country+","+variant+")");
        int retVal = mRepository.isLanguageAvailable(language, country, variant);
        if (retVal < 0) {
            // try engine
            //retVal = mEngine.isLanguageAvailable(language, country, variant);
        }
        return retVal;
    }

    // mandatory
    @Override
    protected int onLoadLanguage(String language, String country, String variant) {
        Log.v(LOG_TAG, "onLoadLanguage("+language+","+country+","+variant+")");
        mRepository.loadLanguages();
        return onIsLanguageAvailable(language, country, variant);
    }

    // mandatory
    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop");
        mEngine.stop();
    }

    // mandatory
    @Override
    protected synchronized void onSynthesizeText(
            SynthesisRequest request, SynthesisCallback callback) {
        Log.v(LOG_TAG, "onSynthesizeText");

        String language = request.getLanguage();
        String country = request.getCountry();
        String variant = request.getVariant();
        String text = request.getCharSequenceText().toString();
        int speechrate = request.getSpeechRate();
        String voice = request.getVoiceName();

        Log.i(LOG_TAG, "onSynthesizeText: (" + language + "/"+country+"/"+variant+"), voice: " + voice);
        String engineInput = mFrontendManager.process(text);
        Log.i(LOG_TAG, text + " => normalized =>" + engineInput);
        if (engineInput.isEmpty())
        {
            // if there is nothing to speak, we don't need to access
            //callback.done();
            //return;
            text = " ";
        }
        mRepository.startTiroTts(callback, "Bjartur", text, "is-IS", speechrate, 1.0f);
/*
        if (! ((mLanguage.equals(language)) &&
                (mCountry.equals(country)) &&
                (mVariant.equals(variant)))) {
            result = mEngine.setLanguage(language, country, variant);
            mLanguage = language;
            mCountry = country;
            mVariant = variant;
        }

        if (!result) {
            Log.e(LOG_TAG, "Could not set language for synthesis");
            return;
        }

        mEngine.setSpeechRate(speechrate);

        mCallback = callback;
        Integer rate = mEngine.getSampleRate();
        Log.e(LOG_TAG, rate.toString());
        mCallback.start(mEngine.getSampleRate(), AudioFormat.ENCODING_PCM_16BIT, 1);
        mEngine.synthesize(text);

 */
    }
/*
    private final NativeFliteTTS.SynthReadyCallback mSynthCallback = new SynthReadyCallback() {
        @Override
        public void onSynthDataReady(byte[] audioData) {
            if ((audioData == null) || (audioData.length == 0)) {
                onSynthDataComplete();
                return;
            }

            final int maxBytesToCopy = mCallback.getMaxBufferSize();

            int offset = 0;

            while (offset < audioData.length) {
                final int bytesToWrite = Math.min(maxBytesToCopy, (audioData.length - offset));
                mCallback.audioAvailable(audioData, offset, bytesToWrite);
                offset += bytesToWrite;
            }
        }

        @Override
        public void onSynthDataComplete() {
            mCallback.done();
        }
    };
*/
    /**
     * Listens for language update broadcasts and initializes the flite engine.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //initializeFliteEngine();
        }
    };

    @Override
    public String onGetDefaultVoiceNameFor(String language, String country, String variant)
    {
        Log.v(LOG_TAG, "onGetDefaultVoiceNameFor("+language+","+country+","+variant+")");
        if (mRepository.isLanguageAvailable(language, country, variant) >= 0) {
            return mRepository.getDefaultVoiceFor(language, country, variant);
        }
        return "";
    }

    @Override
    public List<Voice> onGetVoices()
    {
        Log.v(LOG_TAG, "onGetVoices");
        ArrayList<Voice> announcedVoiceList = new ArrayList<>();

        for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
        	// languageCode is a string like "langugae-Country", so we need to regenerate language
			// Country code
			String[] localeParts = voice.languageCode.split("-");
            Locale locale = new Locale(localeParts[0], localeParts[1]);
            int quality = Voice.QUALITY_NORMAL;
            int latency = Voice.LATENCY_LOW;
            boolean needsNetwork = false;
            /*
            @todo: Features for various Speech Engine things. Will keep this list to a minimum,
                   as the implementation can be quite complex for many of these
                   :
                   TextToSpeech.Engine.FEATURE, where FEATURE is any of those values:
                        - ACTION_CHECK_TTS_DATA
                        - ACTION_GET_SAMPLE_TEXT
                        - ACTION_INSTALL_TTS_DATA
                        - ACTION_TTS_DATA_INSTALLED
                        - CHECK_VOICE_DATA_FAIL
                        - CHECK_VOICE_DATA_PASS
                        - DEFAULT_STREAM
                        - EXTRA_AVAILABLE_VOICES
                        - EXTRA_SAMPLE_TEXT
                        - EXTRA_UNAVAILABLE_VOICES
                        - INTENT_ACTION_TTS_SERVICE
                        - KEY_FEATURE_NETWORK_RETRIES_COUNT
                        - KEY_FEATURE_NETWORK_TIMEOUT_MS
                        - KEY_FEATURE_NOT_INSTALLED
                        - KEY_PARAM_PAN
                        - KEY_PARAM_SESSION_ID
                        - KEY_PARAM_STREAM
                        - KEY_PARAM_UTTERANCE_ID
                        - KEY_PARAM_VOLUME
                        - SERVICE_META_DATA
             */
            Set<String> features = new HashSet<>();

            if (voice.type.equals("tiro")) {
                latency = Voice.LATENCY_VERY_HIGH;
                needsNetwork = true;
            }
            Voice ttsVoice = new Voice(voice.name, locale, quality, latency, needsNetwork, features);
            announcedVoiceList.add(ttsVoice);
        }

        return announcedVoiceList;
    }

    @Override
    public int onIsValidVoiceName(String name)
    {
        Log.v(LOG_TAG, "onIsValidVoiceName("+name+")");
		for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
			if (voice.name.equals(name)) {
				Log.v(LOG_TAG, "SUCCESS");
				return TextToSpeech.SUCCESS;
			}
		}
		Log.v(LOG_TAG, "ERROR");
        return TextToSpeech.ERROR;
    }

    @Override
    public int onLoadVoice(String name)
    {
        Log.v(LOG_TAG, "onLoadVoice("+name+")");
        if (onIsValidVoiceName(name) == TextToSpeech.SUCCESS) {
            return mRepository.loadVoice(name);
        }
        return TextToSpeech.ERROR;
    }
}
