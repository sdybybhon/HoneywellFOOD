package com.example.honeywellfood.data.network

import com.example.honeywellfood.data.network.dto.FoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FoodFactsApi {
    @GET("/api/v3/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): FoodFactsResponse
}

//В будущем продумать несколько запросов к api с получением разных данных