package com.grammatek.simaromur;

import android.media.AudioFormat;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.speech.tts.Voice;
import android.util.Log;

import com.grammatek.simaromur.audio.AudioManager;
import com.grammatek.simaromur.audio.AudioObserver;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.network.ConnectionCheck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Implements the SIM Engine as a TextToSpeechService
 */
public class TTSService extends TextToSpeechService {
    private final static String LOG_TAG = "Simaromur_Java_" + TTSService.class.getSimpleName();
    private AppRepository mRepository;
    private boolean mRmCacheItemAfterPlaying = false;
    private boolean mRmCacheItemForFastVoices = false;

    // This flag saves the state of a very little state machine that gets activated, if we are not
    // connected to the internet and should play a network voice: either we have not yet played a
    // network error warning already, then this value is true, or we have already played it
    // in which case this value is false and will be reset to true when a new TTS session starts.
    private static boolean mShouldPlayNetworkError = true;

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate()");

        mRepository = App.getAppRepository();
        // This calls onIsLanguageAvailable() and must run after Initialization
        super.onCreate();
        mRepository.streamNetworkVoices("");
        String val = mRepository.getAssetConfigValueFor("rm_cache_item_after_playing");
        if (val.equals("true")) {
            mRmCacheItemAfterPlaying = true;
        }
        val = mRepository.getAssetConfigValueFor("rm_cache_item_for_fast_voices");
        if (val.equals("true")) {
            mRmCacheItemForFastVoices = true;
        }
    }

    // mandatory
    @Override
    protected int onIsLanguageAvailable(String language, String country, String variant) {
        Log.v(LOG_TAG, "onIsLanguageAvailable("+language+","+country+","+variant+")");
        if (variant.endsWith(ApiDbUtil.NET_VOICE_SUFFIX)) {
            if (!ConnectionCheck.isTTSServiceReachable()) {
                Log.v(LOG_TAG, "onIsLanguageAvailable: TTS API NOT reachable");
                return TextToSpeech.LANG_MISSING_DATA;
            }
        }
        int rv = mRepository.isLanguageAvailable(language, country, variant);
        Log.v(LOG_TAG, "onIsLanguageAvailable("+language+","+country+","+variant+"): " + rv);
        return rv;
    }

    // @todo: seems to be deprecated, but still it's mandatory to implement it ...
    @Override
    protected String[] onGetLanguage() {
        // @todo: return currently set language as selected from the settings menu
        Log.e(LOG_TAG, "onGetLanguage()");
        return new String[] {"isl", "ISL", ""};
    }

    // mandatory
    @Override
    protected int onLoadLanguage(String language, String country, String variant) {
        Log.i(LOG_TAG, "onLoadLanguage("+language+","+country+","+variant+")");
        if (variant.endsWith(ApiDbUtil.NET_VOICE_SUFFIX)) {
            if (!ConnectionCheck.isTTSServiceReachable()) {
                Log.v(LOG_TAG, "onLoadLanguage: TTS API NOT reachable");
                return TextToSpeech.LANG_MISSING_DATA;
            }
        }
        int rv = onIsLanguageAvailable(language, country, variant);
        Log.i(LOG_TAG, "onLoadLanguage: returns " + rv);
        return rv;
    }

    /**
     * The TTS engine calls this method directly after onSynthesizeText() in case the user wants
     * to stop the current utterance. In case the audio is already playing, this method is strictly
     * not necessary any more, because the callback discards the audio data anyway. In case the audio
     * is currently being prepared, be it via the network or the on-device models, this method stops the
     * waiting for a TTSProcessingResult inside onSynthesizeText(). If afterwards the audio processing is
     * finished, the processing result is received and discarded, because the current utterance is
     * already finished and has changed.
     *
     * Note:  mandatory, don't synchronize this method !
     */
    @Override
    protected void onStop() {
        TTSProcessingResult stoppedProcessingResult =
                new TTSProcessingResult(mRepository.getCurrentTTsRequest());
        stoppedProcessingResult.setToStopped();
        Log.i(LOG_TAG, "onStop: stopping (" + stoppedProcessingResult.getTTSRequest().serialize() + ")");
        mRepository.enqueueTTSProcessingResult(stoppedProcessingResult);
    }

    // mandatory, don't synchronize this method
    @Override
    protected void onSynthesizeText(SynthesisRequest request,
                                                 SynthesisCallback callback) {
        String language = request.getLanguage();
        String country = request.getCountry();
        String variant = request.getVariant();
        String text = request.getCharSequenceText().toString();
        String voiceName = request.getVoiceName();
        int callerUid = request.getCallerUid();
        Bundle params = request.getParams();
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
        Log.v(LOG_TAG, "onSynthesizeText: " + text);
        Log.v(LOG_TAG, "onSynthesizeText: (" + language + "/" + country + "/" + variant
                + "), callerUid: " + callerUid + " speed: " + speechrate + " pitch: " + pitch
                + " bundle: " + params);

        String loadedVoiceName = mRepository.getLoadedVoiceName();
        if (loadedVoiceName.equals("")) {
            // This happens the first time the service comes up
            String voiceNameToLoad = voiceName != null ? voiceName : variant;
            if (TextToSpeech.SUCCESS == mRepository.loadVoice(voiceNameToLoad)) {
                Log.v(LOG_TAG, "onSynthesizeText: loaded voice ("+voiceNameToLoad+")");
                loadedVoiceName = mRepository.getLoadedVoiceName();
            } else {
                Log.w(LOG_TAG, "onSynthesizeText: couldn't load voice ("+voiceNameToLoad+")");
                callback.start(AudioManager.SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT,
                        AudioManager.N_CHANNELS);
                callback.error(TextToSpeech.ERROR_SERVICE);
                if (callback.hasStarted() && ! callback.hasFinished()) {
                    callback.done();
                }
                return;
            }
        }

        if (!loadedVoiceName.equals(voiceName)) {
            // Here we bump into the following problem: Android TTS doesn't officially support multiple voices for
            // the same locale, nor voices that return a different locale than Locale.getAvailableLocales(). Nevertheless
            // we return multiple voices for is_IS, by using the voice name as the variant of the assigned locale. To stick
            // to a voice given us in onLoadVoice(), we need to persist this as our default voice for is_IS. The method
            // onLoadVoice() seems to only be called when the user chooses a voice via the TTS settings, not if the service
            // starts. By persisting the loaded voice name, we can use it immediately without onLoadVoice() being called.
            Log.w(LOG_TAG, "onSynthesizeText: Loaded voice ("+loadedVoiceName+") and given voice ("+voiceName+") differ ?!");
        }

        com.grammatek.simaromur.db.Voice voice = mRepository.getVoiceForName(loadedVoiceName);
        if (voice == null) {
            Log.e(LOG_TAG, "onSynthesizeText: unsupported voice ?!");
            return;
        }
        if (text.isEmpty()) {
            Log.i(LOG_TAG, "onSynthesizeText: End of TTS session");
            playSilence(callback);
            mShouldPlayNetworkError = true;
            return;
        }
        // if cache item for text already exists: retrieve it, otherwise create a new cache
        // item and save it into cache, then test one-by-one availability of every single
        // requested utterance component and eventually add the missing pieces
        CacheItem item = mRepository.getUtteranceCache().addUtterance(text);
        item = mRepository.executeFrontendAndSaveIntoCache(text, item, voice);
        if ((item.getUtterance().getPhonemesCount() == 0) ||
                item.getUtterance().getPhonemesList().get(0).getSymbols().isEmpty()) {
            Log.w(LOG_TAG, "onSynthesizeText: No phonemes to speak");
            playSilence(callback);
            return;
        }

        TTSRequest ttsRequest = new TTSRequest(item.getUuid());
        mRepository.setCurrentTTSRequest(ttsRequest);

        // check if network voice && for network availability
        switch (voice.type) {
            case com.grammatek.simaromur.db.Voice.TYPE_NETWORK:
                if (!testForAndHandleNetworkVoiceIssues(callback, voice)) {
                    Log.v(LOG_TAG, "onSynthesizeText: finished (" + item.getUuid() + ")");
                    return;
                }
                if (item.getUtterance().getNormalized().isEmpty()) {
                    Log.i(LOG_TAG, "onSynthesizeText: normalization failed ?");
                    playSilence(callback);
                    Log.v(LOG_TAG, "onSynthesizeText: finished (" + item.getUuid() + ")");
                    return;
                }
                startSynthesisCallback(callback, AudioManager.SAMPLE_RATE_WAV, true);
                setSpeechMarksToBeginning(callback);
                mRepository.startNetworkTTS(voice, item, ttsRequest, speechrate / 100.0f, pitch / 100.0f);
                break;
            case com.grammatek.simaromur.db.Voice.TYPE_TORCH:
                startSynthesisCallback(callback, AudioManager.SAMPLE_RATE_TORCH, false);
                setSpeechMarksToBeginning(callback);
                mRepository.startDeviceTTS(voice, item, ttsRequest, speechrate / 100.0f, pitch / 100.0f);
                break;
            case com.grammatek.simaromur.db.Voice.TYPE_FLITE:
                startSynthesisCallback(callback, AudioManager.SAMPLE_RATE_FLITE, false);
                setSpeechMarksToBeginning(callback);
                mRepository.startDeviceTTS(voice, item, ttsRequest, speechrate / 100.0f, pitch / 100.0f);
                break;
            default:
                Log.e(LOG_TAG, "Voice type currently unsupported: " + voice.type);
                break;
        }
        handleProcessingResult(callback, item, ttsRequest, voice);
        Log.v(LOG_TAG, "onSynthesizeText: finished (" + item.getUuid() + ")");
    }

    /**
     * Wait for the processing result and handle it.
     * @param callback  the callback to use for the result
     * @param item     the cache item to use for the result
     * @param voice   the voice to use for the result
     */
    private void handleProcessingResult(SynthesisCallback callback, CacheItem item, TTSRequest ttsRequest, com.grammatek.simaromur.db.Voice voice) {
        Log.v(LOG_TAG, "handleProcessingResult for (" + item.getUuid() + ")");
        try {
            // get the current time for caclulating the amount of time that we have waited
            long startTime = System.currentTimeMillis();
            boolean isCached = item.containsVoiceAudioEntries(UtteranceCacheManager.buildVoiceKey(voice.internalName, voice.version));
            // here we wait for the response of the speak request. The result is sent via the queue
            // and then we need to feed the callback with the audio data from here
            boolean isHandled = false;
            do {
                // todo: we need to handle timeout errors here, e.g. processing
                //       timeouts, some error, e.g. network timeouts are already taken care of
                TTSProcessingResult elem = mRepository.dequeueTTSProcessingResult();
                float rtf = estimateRTF(startTime, System.currentTimeMillis(), item, elem);
                Log.v(LOG_TAG, "estimateRTF: rtf=" + rtf);
                if (rtf > 500.0f && !isCached) {
                    Log.w(LOG_TAG, "handleProcessingResult: rtf > 500.0f, something went wrong for the estimation");
                    // don't delete the item
                    rtf = 1.0f;
                }

                TTSRequest rcvdTtsRequest = elem.getTTSRequest();
                Log.v(LOG_TAG, "handleProcessingResult: received result for (" + rcvdTtsRequest.serialize() + ")");
                // if the received element is not meant for this utterance, we ignore it and wait
                // until we get the next one. If the received elements uuid is empty, we play an
                // asset file, for which there is no uuid
                if (elem.isStopped() && ! rcvdTtsRequest.equals(ttsRequest)) {
                    Log.w(LOG_TAG, "handleProcessingResult: discard (" + rcvdTtsRequest.serialize() + ")");
                    continue;
                }
                if (rcvdTtsRequest.equals(ttsRequest) ||
                        rcvdTtsRequest.getCacheItemUuid().equals(AudioObserver.DUMMY_CACHEITEM_UUID)) {
                    if (elem.isOk()) {
                        if (elem.isStopped()) {
                            Log.v(LOG_TAG, "handleProcessingResult: stop " + rcvdTtsRequest.serialize());
                        } else if (elem.getAudio() == null) {
                            Log.w(LOG_TAG, "handleProcessingResult: No audio data received ?!");
                        } else {
                            // everything is fine, feed the Android TTS callback
                            feedTtsCallback(callback, elem);
                            if (mRmCacheItemAfterPlaying) {
                                Log.v(LOG_TAG, "rm_cache_item_after_playing: delete cache item "
                                        + rcvdTtsRequest.serialize());
                                mRepository.getUtteranceCache().deleteCacheItem(item.getUuid());
                            } else if (mRmCacheItemForFastVoices && !isCached) {
                                // if the voice is fast, we can delete the cache item after playing
                                if (rtf > 20.0f) {
                                    Log.v(LOG_TAG, "rm_cache_item_for_fast_voices: delete cache item "
                                            + rcvdTtsRequest.serialize());
                                    mRepository.getUtteranceCache().deleteCacheItem(item.getUuid());
                                }
                            }
                        }
                    } else {
                        // todo: we should handle more errors here
                        Log.e(LOG_TAG, "onSynthesizeText: error during TTS processing");
                        callback.error(TextToSpeech.ERROR_SERVICE);
                    }
                    // we have handled the element, stop the loop
                    isHandled = true;
                }
            } while (!isHandled);
        } catch (InterruptedException e) {
            // thread has been interrupted
            e.printStackTrace();
        }
        if (callback.hasStarted() && ! callback.hasFinished()) {
            callback.done();
        }
    }

    /**
     * Estimate the real time factor for the given cache item and the processing result.
     * We use the real time factor to determine if we can delete the cache item after playing.
     * Some of the necessary parameters for calculation are not yet available here, so we assume
     * some conservative default values for now.
     *
     * @param startTimeMillis   time when the processing started
     * @param stopTimeMillis    time when the processing stopped
     * @param item              cache item
     * @param elem              processing result
     * @return the real time factor
     */
    private float estimateRTF(long startTimeMillis, long stopTimeMillis, CacheItem item, TTSProcessingResult elem) {
        String uuid = elem.getTTSRequest().getCacheItemUuid();
        Log.v(LOG_TAG, "estimateRTF for: " + uuid);

        if (elem.getAudio() == null) {
            Log.e(LOG_TAG, "estimateRTF: no audio data received ?!");
            return 1.0f;
        }

        // assume currently slowest used sample rate, i.e. 16kHz and 16 bit with 1 channel
        // TODO: we should use the real sample rate here, but this needs to be passed via the
        //       TTSProcessingResult
        final int sampleRate = AudioManager.SAMPLE_RATE_WAV;
        final int bytesPerSample = 2;
        final int channels = 1;

        // calculate the real time factor
        float utteranceDurationInSecs = (float) elem.getAudio().length / (sampleRate * bytesPerSample * channels);
        float measuredDurationInMsec = Math.max(stopTimeMillis - startTimeMillis, 1);
        return utteranceDurationInSecs * 1000 / measuredDurationInMsec;
    }

    /**
     * Play Audio via the callback for feeding the audio data.
     *
     * @param callback  TTS callback
     * @param elem    TTS processing result element received from the queue
     */
    private void feedTtsCallback(SynthesisCallback callback, TTSProcessingResult elem) {
        String uuid = elem.getTTSRequest().getCacheItemUuid();
        Log.v(LOG_TAG, "feedTtsCallback: " + uuid);

        if (elem.getAudio() == null) {
            Log.e(LOG_TAG, "feedTtsCallback: no audio data received");
            return;
        }

        Optional<CacheItem> optItem = mRepository.getUtteranceCache().findItemByUuid(uuid);
        if (!optItem.isPresent()) {
            Log.e(LOG_TAG, "feedTtsCallback: no cache item found for uuid: " + uuid);
            return;
        }
        final String rawText =  optItem.get().getUtterance().getText();
        AppRepository.feedBytesToSynthesisCallback(callback, elem.getAudio(), rawText);
    }

    /**
     * Set the speech marks to the beginning of the current utterance.
     * @param callback TTS callback
     */
    private static void setSpeechMarksToBeginning(SynthesisCallback callback) {
        Log.v(LOG_TAG, "setSpeechMarksToBeginning()");
        callback.rangeStart(0, 0, 1);
        byte[] silenceData = new byte[callback.getMaxBufferSize()];
        callback.audioAvailable(silenceData, 0, silenceData.length);
    }

    /**
     * Initialize the synthesis process.
     * @param mSynthCb  TTS callback
     * @param sampleRate    Sample rate
     * @param usesNetwork   True if the synthesis is done via the network
     */
    private static void startSynthesisCallback(SynthesisCallback mSynthCb, int sampleRate, boolean usesNetwork) {
        if (usesNetwork && !ConnectionCheck.isTTSServiceReachable()) {
            Log.e(LOG_TAG, "TTSObserver error: Service is not reachable ?!");
            mSynthCb.error(TextToSpeech.ERROR_NETWORK);
            return;
        }
        if (! mSynthCb.hasStarted()) {
            mSynthCb.start(sampleRate, AudioFormat.ENCODING_PCM_16BIT, AudioManager.N_CHANNELS);
        }
    }

    /**
     * Test for network and TTS service issues. If issues were found, an appropriate message is
     * played instead of the given text. This message is prerecorded and taken from the asset
     * directory. No network request is done. Therefore, the given voice is ignored for playing
     * back the message.
     *
     * @param callback  TTS service callback.
     * @param voice     The voice that is about to be used for speaking of text if no issues are found
     *                  Parameter is used to find out if we get end of TTS session.
     *
     * @return  true in case no network voice issues have been found, false otherwise
     */
    private boolean testForAndHandleNetworkVoiceIssues(SynthesisCallback callback,
                                                       com.grammatek.simaromur.db.Voice voice)
    {
        String assetFileName;
        if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
            Log.w(LOG_TAG, "onSynthesizeText: network voice " + voice.name +
                    ": Network problems detected");
            if (!ConnectionCheck.isTTSServiceReachable()) {
                assetFileName = "audio/service_not_available_dora.pcm";
            } else {
                assetFileName = "audio/connection_problem_dora.pcm";
            }
            if (mShouldPlayNetworkError) {
                // Toggle playing network notification. The idea is to just play once the
                // network warning notification for an ongoing TTS session and ignore any
                // following utterances as long as the network problem exists.
                mShouldPlayNetworkError = false;
                App.getAppRepository().speakAssetFile(callback, assetFileName);
            } else {
                signalTtsError(callback, TextToSpeech.ERROR_NETWORK);
            }
            return false;
        } else {
            // toggle playing network notification
            mShouldPlayNetworkError = true;
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
        Log.w(LOG_TAG, "signalTtsError(): errorCode = " + errorCode);
        callback.start(AudioManager.SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT,
                AudioManager.N_CHANNELS);
        callback.error(errorCode);
        callback.done();
    }

    /**
     * Plays silence. This seems to be sometimes needed for TTS clients to work correctly, instead of
     * signalling synthesis errors.
     *
     * @param callback  TTS callback provided in the onSynthesizeText() callback
     */
    private static void playSilence(SynthesisCallback callback) {
        Log.v(LOG_TAG, "playSilence() ...");
        callback.start(AudioManager.SAMPLE_RATE_WAV, AudioFormat.ENCODING_PCM_16BIT,
                    AudioManager.N_CHANNELS);
        setSpeechMarksToBeginning(callback);
        byte[] silenceData = new byte[callback.getMaxBufferSize()/2];
        callback.audioAvailable(silenceData, 0, silenceData.length);
        if (! callback.hasFinished() && callback.hasStarted()) {
            callback.done();
        }
    }

    @Override
    public String onGetDefaultVoiceNameFor(String language, String country, String variant)
    {
        Log.i(LOG_TAG, "onGetDefaultVoiceNameFor("+language+","+country+","+variant+")");
        String defaultVoice = mRepository.getDefaultVoiceFor(language, country, variant);
        Log.i(LOG_TAG, "onGetDefaultVoiceNameFor: voice name is " + defaultVoice);
        return defaultVoice;
    }

    @Override
    public List<Voice> onGetVoices()
    {
        Log.i(LOG_TAG, "onGetVoices");
        List<Voice> announcedVoiceList = new ArrayList<>();

        for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
            int quality = Voice.QUALITY_VERY_LOW;
            int latency = Voice.LATENCY_LOW;
            boolean needsNetwork = false;
            Set<String> features = new HashSet<>();

            if (voice.type.equals(com.grammatek.simaromur.db.Voice.TYPE_NETWORK)) {
                latency = Voice.LATENCY_VERY_HIGH;
                quality = Voice.QUALITY_HIGH;
                features.add(TextToSpeech.Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
                needsNetwork = true;
            } else if (voice.type.equals(com.grammatek.simaromur.db.Voice.TYPE_TORCH)) {
                quality = Voice.QUALITY_VERY_HIGH;
                latency = Voice.LATENCY_VERY_HIGH;
            }
            if (voice.needsDownload()) {
                features.add(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED);
            }
            Voice ttsVoice = new Voice(voice.name, voice.getLocale(), quality, latency,
                    needsNetwork, features);
            announcedVoiceList.add(ttsVoice);
            Log.v(LOG_TAG, "onGetVoices: " + ttsVoice);
        }
        return announcedVoiceList;
    }

    @Override
    public int onIsValidVoiceName(String name)
    {
        Log.i(LOG_TAG, "onIsValidVoiceName("+name+")");
        for (final com.grammatek.simaromur.db.Voice voice : mRepository.getCachedVoices()) {
            if (voice.name.equals(name)) {
                Log.v(LOG_TAG, "voice name is valid");
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
    public int onLoadVoice(String name)
    {
        Log.i(LOG_TAG, "onLoadVoice("+name+")");
        if (onIsValidVoiceName(name) == TextToSpeech.SUCCESS) {
            if (TextToSpeech.SUCCESS == mRepository.loadVoice(name)) {
                Log.v(LOG_TAG, "voice loading successful");
                return TextToSpeech.SUCCESS;
            }
        }
        Log.e(LOG_TAG, "Voice loading FAILED");
        return TextToSpeech.ERROR;
    }
}
