package com.grammatek.simaromur.network.tiro;

import com.grammatek.simaromur.network.tiro.pojo.SpeakRequest;
import com.grammatek.simaromur.network.tiro.pojo.VoiceResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TiroAPI {
    String SERVER = "tts.tiro.is";
    String URL = "https://" + SERVER + "/v0/";

    @GET("voices")
    Call<List<VoiceResponse>> queryVoices(@Query("LanguageCode") String languageCode);

    @POST("speech")
    Call<ResponseBody> postSpeakRequest(@Body SpeakRequest request);
}
