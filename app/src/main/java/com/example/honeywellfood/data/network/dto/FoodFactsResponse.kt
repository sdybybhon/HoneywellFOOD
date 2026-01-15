package com.example.honeywellfood.data.network.dto

import com.example.honeywellfood.domain.model.Product
import com.google.gson.annotations.SerializedName

data class FoodFactsResponse(
    @SerializedName("product")
    val product: Product? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("code")
    val code: String? = null
)
