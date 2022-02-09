package com.grammatek.simaromur.network.tiro;

import android.util.Log;

import com.grammatek.simaromur.audio.AudioObserver;
import com.grammatek.simaromur.device.TTSAudioControl;
import com.grammatek.simaromur.network.tiro.pojo.SpeakRequest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeakController implements Callback<ResponseBody> {
    private final static String LOG_TAG = "Simaromur_Tiro" + SpeakController.class.getSimpleName();
    private AudioObserver mAudioObserver;       // Audio observer given in streamAudio()
    private TTSAudioControl.AudioFinishedObserver mAudioFinishedObserver;
    private Call<ResponseBody> mCall;           // Caller object created in streamAudio(),
                                                // saved for being cancelable via stop().

    /**
     *  Starts streaming speech audio from Tiro API. This call is done asynchronously and can be
     *  cancelled via stop().
     *
     * @param request           the request to be sent to the Tiro Speak API
     * @param audioObserver     the audio observer to be used for available audio data / error
     */
    public synchronized void streamAudio(SpeakRequest request, AudioObserver audioObserver, TTSAudioControl.AudioFinishedObserver audioFinishedObserver) {
        Log.v(LOG_TAG, "streamAudio: request: " + request);
        if (mCall != null) {
            Log.w(LOG_TAG, "streamAudio: warning: stopping ongoing request: " + request);
            stop();
        }
        mCall = buildSpeakCall(request);
        mAudioObserver = audioObserver;
        mAudioFinishedObserver = audioFinishedObserver;
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
        if (mAudioObserver != null) {
            mAudioObserver.stop();
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
    public synchronized void onResponse(@NotNull Call<ResponseBody> call, Response<ResponseBody> response) {
        assert (mAudioObserver != null);
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            assert (body != null);
            try {
                // @todo: body.bytes() loads the whole response into memory. We should change this
                //        to body.byteStream() to support streamed responses without further
                //        network delays. Then we'd need to handle the end of response via a special
                //        done() call in the observer. (or e.g. mAudioFinishedObserver ?)
                byte[] data = body.bytes();
                Log.v(LOG_TAG, "API returned: " + data.length + " bytes");
                mAudioObserver.update(data);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Exception: " + e.getMessage());
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
        // not yet used when executed via TTS service
        if (mAudioFinishedObserver != null) {
            mAudioFinishedObserver.update();
        }
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
        mAudioObserver.error(t.getLocalizedMessage());
    }
}
