package com.example.honeywellfood.domain.model

data class StatisticsData(
    val expiryDistribution: ExpiryDistribution,
    val categoryDistribution: Map<String, Int>
)

data class ExpiryDistribution(
    val expired: Int,
    val lessThan7Days: Int,
    val lessThan30Days: Int,
    val moreThan30Days: Int
) {
    val total: Int get() = expired + lessThan7Days + lessThan30Days + moreThan30Days
}