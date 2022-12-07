package com.example.truffo.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {
    @POST("send")
    Call<String> sendMessage(@HeaderMap Map<String, String> headers, @Body String messageBody);
}
