package com.grammatek.simaromur.network.tiro;

import android.util.Log;

import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
        mCall = null;
        mVoiceListObserver = null;
    }

    /**
     * Queries available voices at API for given Language Code. If given language code is empty,
     * all available voices are returned.
     *
     * @param languageCode   Language Code filter to use for the voice list
     *
     * @return  list of supported voices from API matching given language code.
     *
     * @throws IOException
     */
    public List<VoiceResponse> queryVoices(String languageCode) throws IOException {
        Log.v(LOG_TAG, "queryVoices (" + languageCode+ ")");
        Call<List<VoiceResponse>> call = buildQueryVoicesCall(languageCode);

        // synchronous request
        Response<List<VoiceResponse>> response = call.execute();

        List<VoiceResponse> voices = null;
        if (response.isSuccessful()) {
            voices = response.body();
            assert voices != null;
            Log.v(LOG_TAG, "API returned voice list of size: " + voices.size());
        }
        else {
            Log.e(LOG_TAG, "API Error: " + response.errorBody());
        }
        return voices;
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
    public synchronized void onResponse(Call<List<VoiceResponse>> call, Response<List<VoiceResponse>> response) {
        assert (mVoiceListObserver != null);
        if(response.isSuccessful()) {
            List<VoiceResponse> voicesList = response.body();
            assert (voicesList != null);
            mVoiceListObserver.update(voicesList);
        }
        else {
            Log.e(LOG_TAG, "API Error: " + response.code() + " " + response.errorBody());
            mVoiceListObserver.error(response.message());
        }
    }

    @Override
    public void onFailure(Call<List<VoiceResponse>> call, Throwable t) {
        String errMsg = "";
        if (t instanceof SocketTimeoutException) {
            errMsg = "Socket timeout";
        } else if (t instanceof IOException) {
            errMsg = "Timeout";
        } else {
            if (call.isCanceled()) {
                errMsg = "Call was cancelled";
            } else {
                errMsg = "Network Error :" + t.getLocalizedMessage();
            }
        }
        Log.e(LOG_TAG, "onFailure: " + errMsg);
        mVoiceListObserver.error(errMsg);
    }
}
