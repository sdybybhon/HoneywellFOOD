package com.example.honeywellfood.data.network.dto

import com.google.gson.annotations.SerializedName

data class FoodFactsResponse(
    @SerializedName("product")
    val product: Product? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("code")
    val code: String? = null
)

data class Product(
    @SerializedName("product_name")
    val productName: String? = null,

    @SerializedName("product_name_ru")
    val productNameRu: String? = null,

    @SerializedName("generic_name")
    val genericName: String? = null,

    @SerializedName("brands")
    val brands: String? = null
)
