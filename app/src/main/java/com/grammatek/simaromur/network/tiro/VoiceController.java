package com.grammatek.simaromur.network.tiro;

import static com.grammatek.simaromur.ApiDbUtil.NET_VOICE_SUFFIX;

import android.util.Log;

import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoiceController implements Callback<List<VoiceResponse>> {
    private final static String LOG_TAG = "Simaromur_Tiro" + VoiceController.class.getSimpleName();

    /**
     * Observer interface for async. voice list reception.
     */
    public interface VoiceObserver {
        /**
         * Called when new data arrives from API.
         *
         * @param voices     List of audio voices returned for streamQueryVoices()
         */
        void update(List<VoiceResponse> voices);

        /**
         * Called when an error occurs. Currently only an error message is passed on.
         *
         * @param errorMsg  Error message
         */
        void error(String errorMsg);
    }

    private VoiceObserver mVoiceListObserver;   // Voice list observer
    private Call<List<VoiceResponse>> mCall;    // Caller object created in streamQueryVoices(),
                                                // saved for being cancelable via stop().

    /**
     *  Stream or call queryVoices asynchronously.
     *
     * @param languageCode  Language Code filter to use for the voice list
     * @param voiceObserver the voice observer to be used for available voices / error
     */
    public synchronized void streamQueryVoices(String languageCode, VoiceObserver voiceObserver) {
        if (mCall != null) {
            stop();
        }
        mCall = buildQueryVoicesCall(languageCode);
        mVoiceListObserver = voiceObserver;
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
    }

    /**
     * Builds a Retrofit caller object for the Tiro API without calling its endpoint yet.
     *
     * @param languageCode   Language Code filter to use for the voice list
     *
     * @return  a caller object, still needs to be executed
     */
    private  Call<List<VoiceResponse>> buildQueryVoicesCall(String languageCode) {
        TiroAPI tiroAPI = TiroServiceGenerator.createService(TiroAPI.class);
        return tiroAPI.queryVoices(languageCode);
    }

    @Override
    public synchronized void onResponse(@NotNull Call<List<VoiceResponse>> call, Response<List<VoiceResponse>> response) {
        assert (mVoiceListObserver != null);
        if(response.isSuccessful()) {
            final List<VoiceResponse> netVoicesList = response.body();
            List<VoiceResponse> updateVoicesList = new ArrayList<>();
            assert (netVoicesList != null);
            for (VoiceResponse aVoice:netVoicesList) {
                aVoice.Name = aVoice.Name.concat(NET_VOICE_SUFFIX);
                updateVoicesList.add(aVoice);
            }
            mVoiceListObserver.update(updateVoicesList);
        }
        else {
            Log.e(LOG_TAG, "API Error: " + response.code() + " " + response.errorBody());
            mVoiceListObserver.error(response.message());
        }
    }

    @Override
    public void onFailure(@NotNull Call<List<VoiceResponse>> call, Throwable t) {
        Log.v(LOG_TAG, "onFailure: " + t.getLocalizedMessage());
        assert (mVoiceListObserver != null);
        if (t instanceof IOException) {
            if (call.isCanceled()) {
                return;
            }
        }
        mVoiceListObserver.error(t.getLocalizedMessage());
    }
}
