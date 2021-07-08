package com.grammatek.simaromur.network.tiro;

import android.util.Log;

import com.grammatek.simaromur.network.tiro.pojo.SpeakRequest;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeakController implements Callback<ResponseBody> {
    private final static String LOG_TAG = "Simaromur_Tiro" + SpeakController.class.getSimpleName();

    /**
     * Observer interface for async. speech audio playback
     */
    public interface AudioObserver {
        /**
         * Called when new data arrives from API.
         *
         * @param audioData     Audio data buffer - data format is dependent on requested
         *                      OutputFormat
         */
        void update(byte[] audioData);

        /**
         * Called when an error occurs. Currently only the error message is passed on.
         *
         * @param errorMsg  Error message
         */
        void error(String errorMsg);

        /**
         * Stop actions for Observer.
         */
        void stop();
    }

    private AudioObserver mAudioObserver;       // Audio observer given in streamAudio()
    private Call<ResponseBody> mCall;           // Caller object created in streamAudio(),
                                                // saved for being cancelable via stop().

    /**
     *  Starts streaming speech audio from Tiro API. This call is done asynchronously and can be
     *  cancelled via stop().
     *
     * @param request           the request to be sent to the Tiro Speak API
     * @param audioObserver     the audio observer to be used for available audio data / error
     */
    public synchronized void streamAudio(SpeakRequest request, AudioObserver audioObserver) {
        if (mCall != null) {
            stop();
        }
        Log.v(LOG_TAG, "streamAudio: request: " + request);
        mCall = buildSpeakCall(request);
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
        }
        mCall = null;
        if (mAudioObserver != null) {
            mAudioObserver.stop();
        }
        mAudioObserver = null;
    }

    /**
     * Executes a speak request to Tiro API synchronously. The resulting audio is returned in a
     * byte[] array.
     *
     * @param request   the request to be sent to the Tiro Speak API.
     *
     * @return  audio data buffer - data format is dependent on given request.OutputFormat
     *
     * @throws IOException
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
        TiroAPI tiroAPI = TiroServiceGenerator.createService(TiroAPI.class);
        return tiroAPI.postSpeakRequest(speakRequest);
    }

    @Override
    public synchronized void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        assert (mAudioObserver != null);
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            assert (body != null);
            try {
                mAudioObserver.update(body.bytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String errMsg = "";
            try {
                assert response.errorBody() != null;
                errMsg = response.errorBody().string();
                Log.e(LOG_TAG, "API Error: " + errMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mAudioObserver.error(errMsg);
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
    }
}
