package com.grammatek.simaromur;

import com.grammatek.simaromur.frontend.NormalizationManager;
import com.grammatek.simaromur.network.ConnectionCheck;

import android.media.AudioFormat;
import android.provider.Settings;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import static android.speech.tts.TextToSpeech.ERROR_SERVICE;
import static com.grammatek.simaromur.audio.AudioManager.N_CHANNELS;
import static com.grammatek.simaromur.audio.AudioManager.SAMPLE_RATE_WAV;

/**
 * Implements the SIM Engine as a TextToSpeechService
 */
public class TTSService extends TextToSpeechService {
    private final static String LOG_TAG = "Simaromur_Java_" + TTSService.class.getSimpleName();
    private AppRepository mRepository;
    private static boolean playNetworkErrorOnce = true;

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate()");
        mRepository = App.getAppRepository();
        // This calls onIsLanguageAvailable() and must run after Initialization
        super.onCreate();
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
        // we will get speechrate and pitch from the settings,
        // but in case the retrieval of the values fails, let's get the values from the request first.
        int speechrate = request.getSpeechRate();
        int pitch = request.getPitch();
        try {
            speechrate = Settings.Secure.getInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE);
            pitch = Settings.Secure.getInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.v(LOG_TAG, "onSynthesizeText: (" + language + "/"+country+"/"+variant+"), voice: "
                + voiceName + " speed: " + speechrate + " pitch: " + pitch);
        String loadedVoiceName = mRepository.getLoadedVoiceName();
        if (loadedVoiceName.equals("")) {
            String voiceNameToLoad = voiceName != null ? voiceName : variant;
            if (TextToSpeech.SUCCESS == mRepository.loadVoice(voiceNameToLoad)) {
                Log.v(LOG_TAG, "onSynthesizeText: loaded voice ("+voiceNameToLoad+")");
                loadedVoiceName = mRepository.getLoadedVoiceName();
            } else {
                Log.w(LOG_TAG, "onSynthesizeText: couldn't load voice ("+voiceNameToLoad+")");
                callback.start(SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT, N_CHANNELS);
                callback.error(ERROR_SERVICE);
                if (callback.hasStarted() && ! callback.hasFinished()) {
                    callback.done();
                }
                return;
            }
        }

        if (!loadedVoiceName.equals(voiceName)) {
            Log.e(LOG_TAG, "onSynthesizeText: Loaded voice ("+loadedVoiceName+") and given voice ("+voiceName+") differ ?!");
        }

        com.grammatek.simaromur.db.Voice voice = mRepository.getVoiceForName(loadedVoiceName);
        if (voice != null) {
            // check if network voice && for network availability
            if (voice.type.equals("tiro")) {
                if (! testForAndHandleNetworkVoiceIssues(callback, text, voice)) {
                    return;
                }
            }
            NormalizationManager normalizationManager = App.getApplication().getNormalizationManager();
            String normalizedText = normalizationManager.process(text);

            Log.v(LOG_TAG, "onSynthesizeText: original (\"" + text + "\"), normalized (\""
                    + normalizedText + "\")");
            if (text.isEmpty() && normalizedText.isEmpty()) {
                Log.i(LOG_TAG, "onSynthesizeText: End of TTS session");
                playSilence(callback);
                return;
            } else if (normalizedText.isEmpty()) {
                Log.i(LOG_TAG, "onSynthesizeText: normalization failed ?");
                playSilence(callback);
                return;
            }
            mRepository.startTiroTts(callback, voice, normalizedText, speechrate/100.0f, pitch/100.0f);
        }
        else {
            Log.e(LOG_TAG, "onSynthesizeText: unsupported voice ?!");
        }
    }

    /**
     * Test for network and TTS service issues. If issues were found, an appropriate message is
     * played instead of the given text. This message is prerecorded and taken from the asset
     * directory. No network request is done. Therefore, the given voice is ignored for playing
     * back the message.
     *
     * @param callback  TTS service callback.
     * @param text      Text as received from TTS client
     * @param voice     The voice that is about to be used for speaking of text if no issues are found
     *                  Parameter is used to find out if we get end of TTS session.
     *
     * @return  true in case no network voice issues have been found, false otherwise
     */
    private boolean testForAndHandleNetworkVoiceIssues(SynthesisCallback callback, String text,
                                                       com.grammatek.simaromur.db.Voice voice)
    {
        String assetFileName = "";
        if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
            Log.w(LOG_TAG, "onSynthesizeText: tiro voice " + voice.name +
                    ": Network problems detected");
            if (!ConnectionCheck.isTTSServiceReachable()) {
                assetFileName = "audio/service_not_available_dora.pcm";
            } else {
                assetFileName = "audio/connection_problem_dora.pcm";
            }
            if (playNetworkErrorOnce) {
                // Toggle playing network notification. The idea is to just play once the
                // network warning notification for an ongoing TTS session and ignore any
                // following utterances as long as the network problem exists.
                playNetworkErrorOnce = false;
                App.getAppRepository().speakAssetFile(callback, assetFileName);
            } else {
                signalTtsError(callback, TextToSpeech.ERROR_NETWORK);
            }
            if (text.isEmpty()) {
                Log.v(LOG_TAG, "onSynthesizeText: End of TTS session");
                // toggle playing network notification
                playNetworkErrorOnce = true;
            }
            return false;
        } else {
            // toggle playing network notification
            playNetworkErrorOnce = true;
        }
        return true;
    }

    /**
     * Signal TTS client a TTS error with given error code.
     *
     * The sequence for signalling an error seems to be important: callback.start(),
     * callback.error(), callback.done(). Any callback.audioAvailable() call after a callback.error()
     * is ignored.
     *
     * @param callback      TTS Service callback, given in onSynthesizeText()
     * @param errorCode     Error Code to return to TTS client
     */
    private void signalTtsError(SynthesisCallback callback, int errorCode) {
        callback.start(SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT, N_CHANNELS);
        callback.error(errorCode);
        callback.done();
    }

    /**
     * Plays silence. This seems to be sometimes needed for TTS clients to work correctly, instead of
     * signalling synthesis errors.
     *
     * @param callback  TTS callback provided in the onSynthesizeText() callback
     */
    public static void playSilence(SynthesisCallback callback) {
        Log.v(LOG_TAG, "playSilence() ...");
        callback.start(SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT, 1);
        byte[] silenceData = new byte[callback.getMaxBufferSize()/2];
        callback.audioAvailable(silenceData, 0, silenceData.length);
        if (callback.hasStarted() && ! callback.hasFinished()) {
            callback.done();
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
        List<Voice> announcedVoiceList = new ArrayList<>();

        for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
            int quality = Voice.QUALITY_NORMAL;
            int latency = Voice.LATENCY_LOW;
            boolean needsNetwork = false;
            Set<String> features = new HashSet<>();

            if (voice.type.equals(com.grammatek.simaromur.db.Voice.TYPE_TIRO)) {
                latency = Voice.LATENCY_VERY_HIGH;
                features.add(TextToSpeech.Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
                needsNetwork = true;
            }
            Voice ttsVoice = new Voice(voice.name, voice.getLocale(), quality, latency,
                    needsNetwork, features);
            String voiceLanguage = "";
            try {
                voiceLanguage = voice.getLocale().getISO3Language();
            } catch (MissingResourceException e) {
                Log.w(LOG_TAG, "Couldn't retrieve ISO 639-2/T language code for locale: "
                        + voice.getLocale(), e);
            }

            String voiceCountry = "";
            try {
                voiceCountry = voice.getLocale().getISO3Country();
            } catch (MissingResourceException e) {
                Log.w(LOG_TAG, "Couldn't retrieve ISO 3166 country code for locale: "
                        + voice.getLocale(), e);
            }
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
