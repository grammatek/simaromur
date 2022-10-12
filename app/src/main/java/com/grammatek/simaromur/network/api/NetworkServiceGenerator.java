package com.grammatek.simaromur.network.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Generate NetworkService accessor.
 * All members of the class are statically initialized and therefore cached for the lifetime of
 * the application. This way, we use the same Http client for all further requests to Network API.
 * Furthermore explicitly use the order of protocols http_2, http_1_1. okhttp
 * will use automatically http2, if server supports it.
 */
public class NetworkServiceGenerator {

    private static final Gson gson = new GsonBuilder().setLenient().create();

    private static final Retrofit.Builder builder
            = new Retrofit.Builder()
            .baseUrl(Endpoint.URL)
            .addConverterFactory(GsonConverterFactory.create(gson));

    private static Retrofit retrofit = builder.build();

    private static final OkHttpClient.Builder httpClient
            = new OkHttpClient.Builder().protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1));

    private static final HttpLoggingInterceptor logging
            = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC);

    public static <S> S createService(Class<S> serviceClass) {
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            builder.client(httpClient.build());
            retrofit = builder.build();
        }
        return retrofit.create(serviceClass);
    }

    /**
     * Creates the service and adds given request id to the request header
     *
     * @param serviceClass the service class
     * @param requestId request Id to identify response
     * @param <S>   service class type
     * @return created service
     */
    public static <S> S createService(Class<S> serviceClass, final String requestId) {
        if (requestId != null) {
            httpClient.interceptors().clear();
            httpClient.addInterceptor( chain -> {
                Request original = chain.request();
                Request.Builder builder1 = original.newBuilder()
                        .header("x-request-id", requestId);
                Request request = builder1.build();
                return chain.proceed(request);
            });
            if (!httpClient.interceptors().contains(logging)) {
                httpClient.addInterceptor(logging);
            }
            builder.client(httpClient.build());
            retrofit = builder.build();
        }
        return retrofit.create(serviceClass);
    }
}
