package com.example.googlemaps;

import com.example.googlemaps.model.ModelApi;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiCall {
    @GET("json?lat=36.7201600&lng=-4.4203400")
    Call<ModelApi> getData();
}
