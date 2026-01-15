package com.example.honeywellfood.domain.model

import com.google.gson.annotations.SerializedName

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