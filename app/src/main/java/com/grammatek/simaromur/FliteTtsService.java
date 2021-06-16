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
    private FrontendManager mFrontendManager;
    private AppRepository mRepository;

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate()");
        mRepository = App.getAppRepository();
        initializeFrontendManager(this);
        // This calls onIsLanguageAvailable() and must run after Initialization
        super.onCreate();
    }

    private void initializeFrontendManager(Context context) {
        if (mFrontendManager == null)
            mFrontendManager = new FrontendManager(context);
    }

    // mandatory
    @Override
    protected synchronized int onIsLanguageAvailable(String language, String country, String variant) {
        Log.i(LOG_TAG, "onIsLanguageAvailable("+language+","+country+","+variant+")");
        // @todo: we should return LANG_MISSING_DATA, for network voices without network
        return mRepository.isLanguageAvailable(language, country, variant);
    }

    // @todo: seems to be deprecated, but still it's mandatory to implement it ...
    @Override
    protected synchronized String[] onGetLanguage() {
        // @todo: return currently set language as selected from the settings menu
        Log.e(LOG_TAG, "onGetLanguage()");
        return new String[] {"isl", "ISL", ""};
    }

    // mandatory
    // @todo: What should we do here instead of checking the language ? Should we check
    //        for network connectivity ? And then return LANG_MISSING_DATA ?
    @Override
    protected synchronized int onLoadLanguage(String language, String country, String variant) {
        Log.i(LOG_TAG, "onLoadLanguage("+language+","+country+","+variant+")");
        return onIsLanguageAvailable(language, country, variant);
    }

    // mandatory
    @Override
    protected synchronized void onStop() {
        Log.i(LOG_TAG, "onStop");
        // @todo stop ongoing speec request, i.e. unregister observers
    }

    // mandatory
    @Override
    protected synchronized void onSynthesizeText(
            SynthesisRequest request, SynthesisCallback callback) {
        Log.i(LOG_TAG, "onSynthesizeText");

        String language = request.getLanguage();
        String country = request.getCountry();
        String variant = request.getVariant();
        String text = request.getCharSequenceText().toString();
        String voiceName = request.getVoiceName();
        int speechrate = request.getSpeechRate();
        int pitch = request.getPitch();
        Log.i(LOG_TAG, "onSynthesizeText: (" + language + "/"+country+"/"+variant+"), voice: "
                + voiceName);
        String loadedVoiceName = mRepository.getLoadedVoiceName();
        if (!loadedVoiceName.equals(voiceName)) {
            Log.e(LOG_TAG, "Loaded voice ("+loadedVoiceName+") and given voice ("+voiceName+") differ ?!");
        }

        com.grammatek.simaromur.db.Voice voice = mRepository.getVoiceForName(loadedVoiceName);
        if (voice != null) {
            String engineInput = mFrontendManager.process(text);
            Log.i(LOG_TAG, text + " => normalized =>" + engineInput);
            if (engineInput.isEmpty()) {
                // @todo: if there is nothing to speak, we don't need to access the API ... ?!
                text = " ";
            }

            mRepository.startTiroTts(callback, voice, text, speechrate, pitch/100.0f);
        }
        else {
            Log.e(LOG_TAG, "onSynthesizeText: unsupported voice ?!");
        }
    }

    @Override
    public synchronized String onGetDefaultVoiceNameFor(String language, String country, String variant)
    {
        Log.i(LOG_TAG, "onGetDefaultVoiceNameFor("+language+","+country+","+variant+")");
        String defaultVoice = mRepository.getDefaultVoiceFor(language, country, variant);
        Log.i(LOG_TAG, "onGetDefaultVoiceNameFor: " + defaultVoice);
        return defaultVoice;
    }

    @Override
    public synchronized  List<Voice> onGetVoices()
    {
        Log.i(LOG_TAG, "onGetVoices");
        ArrayList<Voice> announcedVoiceList = new ArrayList<>();

        for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
            int quality = Voice.QUALITY_NORMAL;
            int latency = Voice.LATENCY_LOW;
            boolean needsNetwork = false;
            Set<String> features = new HashSet<>();

            if (voice.type.equals("tiro")) {
                latency = Voice.LATENCY_VERY_HIGH;
                features.add(TextToSpeech.Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
                needsNetwork = true;
            }
            Voice ttsVoice = new Voice(voice.name, voice.getLocale(), quality, latency,
                    needsNetwork, features);
            announcedVoiceList.add(ttsVoice);
            Log.v(LOG_TAG, "onGetVoices: " + ttsVoice);
        }

        return announcedVoiceList;
    }

    @Override
    public synchronized int onIsValidVoiceName(String name)
    {
        Log.i(LOG_TAG, "onIsValidVoiceName("+name+")");
        for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
            if (voice.name.equals(name)) {
                Log.v(LOG_TAG, "SUCCESS");
                return TextToSpeech.SUCCESS;
            }
        }
        Log.e(LOG_TAG, "ERROR");
        return TextToSpeech.ERROR;
    }

    /**
     * onLoadVoice() has multiple purposes: it initializes the speech engine with the appropriate
     * voice and determines, which voice to use for the next synthesis.
     *
     * @param name  Name of the voice
     * @return  TextToSpeech.SUCCESS in case the call was successful, TextToSpeech.ERROR otherwise.
     */
    @Override
    public synchronized int onLoadVoice(String name)
    {
        Log.i(LOG_TAG, "onLoadVoice("+name+")");
        if (onIsValidVoiceName(name) == TextToSpeech.SUCCESS) {
            Log.v(LOG_TAG, "SUCCESS");
            if (TextToSpeech.SUCCESS == mRepository.loadVoice(name)) {
                return TextToSpeech.SUCCESS;
            }
        }
        Log.e(LOG_TAG, "ERROR");
        return TextToSpeech.ERROR;
    }
}
