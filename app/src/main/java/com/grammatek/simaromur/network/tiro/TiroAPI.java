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
    public static final String URL = "https://tts.tiro.is/v0/";

    @GET("voices/")
    Call<List<VoiceResponse>> queryVoices(@Query("LanguageCode") String languageCode);

    @POST("speak/")
    Call<ResponseBody> postSpeakRequest(@Body SpeakRequest request);
}
