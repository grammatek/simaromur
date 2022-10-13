package com.grammatek.simaromur.network.api;

import static com.grammatek.simaromur.utils.FileUtils.getAssetConfigProperty;

import com.grammatek.simaromur.App;
import com.grammatek.simaromur.network.api.pojo.SpeakRequest;
import com.grammatek.simaromur.network.api.pojo.VoiceResponse;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Endpoint {
    String SERVER = "api.grammatek.com";
    String URL = "https://" + SERVER + "/tts/v0/";

    @GET("voices")
    Call<List<VoiceResponse>> queryVoices(@Query("LanguageCode") String languageCode);

    @POST("speech")
    Call<ResponseBody> postSpeakRequest(@Body SpeakRequest request);
}
