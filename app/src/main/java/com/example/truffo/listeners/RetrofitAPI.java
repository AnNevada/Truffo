package com.example.truffo.listeners;

import com.example.truffo.models.BotMessage;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitAPI {
    @GET
    Call<BotMessage> getMessage(@Url String url);
}
