package com.grammatek.simaromur.network.tiro;

import static com.grammatek.simaromur.cache.AudioFormat.AUDIO_FMT_MP3;
import static com.grammatek.simaromur.cache.AudioFormat.AUDIO_FMT_PCM;
import static com.grammatek.simaromur.cache.AudioFormat.INVALID_AUDIO_FMT;
import static com.grammatek.simaromur.cache.SampleRate.INVALID_SAMPLE_RATE;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_11KHZ;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_16KHZ;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_22KHZ;
import static com.grammatek.simaromur.cache.SampleRate.SAMPLE_RATE_44_1KHZ;

import android.util.Log;

import androidx.annotation.NonNull;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.AppRepository;
import com.grammatek.simaromur.TTSRequest;
import com.grammatek.simaromur.audio.AudioObserver;
import com.grammatek.simaromur.cache.AudioFormat;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.cache.PhonemeEntry;
import com.grammatek.simaromur.cache.SampleRate;
import com.grammatek.simaromur.cache.Utterance;
import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.cache.VoiceAudioDescription;
import com.grammatek.simaromur.network.tiro.pojo.SpeakRequest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeakController implements Callback<ResponseBody> {
    private final static String LOG_TAG = "Simaromur_Tiro" + SpeakController.class.getSimpleName();
    private AudioObserver mAudioObserver;       // Audio observer given in streamAudio()
    private Call<ResponseBody> mCall;           // Caller object created in streamAudio(),
                                                // saved for being cancelable via stop().
    private SpeakRequest mRequest;              // saved for audio parameter handling
    private CacheItem mItem;                    // the cache item to be used
    private TTSRequest mTTSRequest;             // the tts request that is being processed

    /**
     *  Starts streaming speech audio from Tiro API. This call is done asynchronously and can be
     *  cancelled via stop().
     *
     * @param request           the request to be sent to the Tiro Speak API
     * @param audioObserver     the audio observer to be used for available audio data / error
     */
    public synchronized void streamAudio(SpeakRequest request, AudioObserver audioObserver, CacheItem item, TTSRequest ttsRequest) {
        Log.v(LOG_TAG, "streamAudio: request: " + request);
        if (mCall != null) {
            Log.w(LOG_TAG, "streamAudio: warning: stopping ongoing request: " + request);
            stop();
        }
        mRequest = request;
        mItem = item;
        mTTSRequest = ttsRequest;
        mCall = buildSpeakCall(request);
        mRequest = request;
        mAudioObserver = audioObserver;
        // async request execution
        mCall.enqueue(this);
    }

    /**
     *  Stops currently ongoing speak request. Has no effect, if no current audio is
     *  streamed.
     */
    public synchronized void stop() {
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
        if (mAudioObserver != null) {
            mAudioObserver.stop(mTTSRequest);
        }
    }

    /**
     * Executes a speak request to Tiro API synchronously. The resulting audio is returned in a
     * byte[] array.
     *
     * @param request   the request to be sent to the Tiro Speak API.
     *
     * @return  audio data buffer - data format is dependent on given request.OutputFormat
     *
     * @throws IOException if an error occurs during the request execution.
     */
    public byte[] speak(SpeakRequest request) throws IOException {
        Log.v(LOG_TAG, "speak: " + request.Text);
        Call<ResponseBody> call = buildSpeakCall(request);

        // synchronous request
        Response<ResponseBody> response = call.execute();
        byte[] voiceAudio = null;

        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            assert body != null;
            voiceAudio = body.bytes();
            Log.v(LOG_TAG, "API returned data of size: " + voiceAudio.length);
        }
        else {
            Log.e(LOG_TAG, "API Error: " + response.errorBody());
        }
        return voiceAudio;
    }

    /**
     * Builds a Retrofit caller object for the Tiro API without calling its endpoint yet.
     *
     * @param speakRequest   The speak request to build
     *
     * @return  a caller object, still needs to be executed
     */
    private Call<ResponseBody> buildSpeakCall(SpeakRequest speakRequest) {
        TiroAPI tiroAPI = TiroServiceGenerator.createService(TiroAPI.class, mTTSRequest.serialize());
        return tiroAPI.postSpeakRequest(speakRequest);
    }

    @Override
    public synchronized void onResponse(@NotNull Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.v(LOG_TAG, "onResponse()");
        assert (mAudioObserver != null);
        final String xRequestId = response.headers().get("X-Request-Id");
        if (response.isSuccessful() && xRequestId != null) {
            Log.v(LOG_TAG, "onResponse: X-Request-Id: " + xRequestId);
            TTSRequest ttsRequest;
            try {
                ttsRequest = TTSRequest.restore(xRequestId);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                Log.e(LOG_TAG, "onResponse: ArrayIndexOutOfBoundsException: " + e.getMessage());
                TTSRequest dummyRequest = new TTSRequest(AudioObserver.DUMMY_CACHEITEM_UUID);
                mAudioObserver.error("Cannot deduct TTSRequest from network response", dummyRequest);
                return;
            }
            ResponseBody body = response.body();
            assert (body != null);
            try {
                // @note: body.bytes() loads the whole response into memory
                byte[] audioData = body.bytes();
                Log.v(LOG_TAG, "API returned: " + audioData.length + " bytes for "
                        + ttsRequest.serialize());
                if (saveSpeechDataToCache(audioData, ttsRequest.getCacheItemUuid())) {
                    mAudioObserver.update(audioData, ttsRequest);
                } else {
                    mAudioObserver.error("failed to save speech data", ttsRequest);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
                mAudioObserver.error(e.getMessage(), ttsRequest);
            }
        } else {
            String errMsg;
            try {
                assert response.errorBody() != null;
                errMsg = response.errorBody().string();
                Log.e(LOG_TAG, "API Error: " + errMsg);
            } catch (IOException e) {
                e.printStackTrace();
                errMsg = e.getMessage();
            }
            // we couldn't retrieve the corresponding uuid from the response header, therefore we
            // need to use a dummy cache item uuid
            TTSRequest dummyRequest = new TTSRequest(AudioObserver.DUMMY_CACHEITEM_UUID);
            Log.e(LOG_TAG, "onResponse: error occured: " + errMsg);
            mAudioObserver.error(errMsg, dummyRequest);
        }
        mCall = null;
    }

    /**
     * Saves given speech data to appropriate cache item in the speech utterance cache. The given
     * uuid must be valid.
     *
     * @note Currently we only support associating the data to the first phoneme entry of the cache
     *       item.
     *
     * @param data  data buffer to be saved to cache
     * @param uuid  the uuid of the cache item to associate the data to
     */
    private boolean saveSpeechDataToCache(byte[] data, String uuid) {
        AppRepository appRepo = App.getAppRepository();
        Optional<CacheItem> optItem = appRepo.getUtteranceCache().findItemByUuid(uuid);
        boolean rv = false;
        if (optItem.isPresent()) {
            CacheItem item = optItem.get();
            if (mItem.getUuid().equals(uuid)) {
                Utterance utterance = item.getUtterance();
                if (utterance.getPhonemesCount() > 0) {
                    // use first phoneme entry of the cache item
                    PhonemeEntry phonemeEntry = utterance.getPhonemesList().get(0);
                    // TODO:- voiceVersion parameter is not taken into account yet !
                    //      - is the internal name of the voice model the same as we use here ?
                    //          this should be consistent with the on-device voice handling
                    String voiceName = mRequest.VoiceId;
                    SampleRate sampleRate = getSampleRate();
                    AudioFormat audioFormat = getAudioFormat();
                    final VoiceAudioDescription vad =
                            UtteranceCacheManager.newAudioDescription(audioFormat, sampleRate,
                                    data.length, voiceName, "v1");
                    if (data.length == 0) {
                        Log.w(LOG_TAG, "synthesizeSpeech(): No audio generated ?!");
                    } else {
                        UtteranceCacheManager ucm = appRepo.getUtteranceCache();
                        if (ucm.addAudioToCacheItem(item.getUuid(), phonemeEntry, vad, data)) {
                            Log.v(LOG_TAG, "Cached speech audio ("
                                    + mRequest.SampleRate + "/"
                                    + mRequest.OutputFormat + ") "
                                    + item.getUuid());
                            rv = true;
                        } else {
                            Log.e(LOG_TAG, "Couldn't add audio to cache item " + item.getUuid());
                        }
                    }

                } else {
                    Log.e(LOG_TAG, "onResponse(): No phonemes found in cache item "
                            + uuid + " ?!");
                }
            } else {
                // we haven't got what we requested, so we shouldn't play it back from here
                Log.e(LOG_TAG, "onResponse(): not the audio for the text we have sent "
                        + uuid + " != " + mItem.getUuid() + "?!");
            }
        } else {
            Log.e(LOG_TAG, "onResponse(): No valid uuid provided from API, should be "
                    + mItem.getUuid() + " speech audio is NOT cached");
        }
        return rv;
    }

    @NonNull
    private AudioFormat getAudioFormat() {
        AudioFormat audioFormat = INVALID_AUDIO_FMT;
        switch (mRequest.OutputFormat) {
            case "mp3":
                audioFormat = AUDIO_FMT_MP3;
                break;
            case "pcm":
                audioFormat = AUDIO_FMT_PCM;
                break;
        }
        return audioFormat;
    }

    @NonNull
    private SampleRate getSampleRate() {
        SampleRate sampleRate = INVALID_SAMPLE_RATE;
        switch (mRequest.SampleRate) {
            case "44100":
                sampleRate = SAMPLE_RATE_44_1KHZ;
                break;
            case "22050":
                sampleRate = SAMPLE_RATE_22KHZ;
                break;
            case "16000":
                sampleRate = SAMPLE_RATE_16KHZ;
                break;
            case "11025":
                sampleRate = SAMPLE_RATE_11KHZ;
                break;
        }
        return sampleRate;
    }

    @Override
    public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
        Log.w(LOG_TAG, "onFailure: " + t.getLocalizedMessage());
        assert (mAudioObserver != null);
        if (t instanceof IOException) {
            if (call.isCanceled()) {
                return;
            }
        }
        TTSRequest dummyRequest = new TTSRequest(AudioObserver.DUMMY_CACHEITEM_UUID);
        mAudioObserver.error(t.getLocalizedMessage(), dummyRequest);
    }
}
