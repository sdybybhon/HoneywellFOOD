package com.example.honeywellfood.data.repository

import android.util.Log
import com.example.honeywellfood.data.constants.ScannerConstants
import com.example.honeywellfood.data.local.ScanDao
import com.example.honeywellfood.data.network.FoodFactsApi
import com.example.honeywellfood.domain.model.ExpiryDistribution
import com.example.honeywellfood.domain.model.ScanItem
import com.example.honeywellfood.domain.model.StatisticsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val scanDao: ScanDao,
    private val foodFactsApi: FoodFactsApi
) {
    private val productCache = mutableMapOf<String, String?>()

    fun getAllScans(): Flow<List<ScanItem>> = scanDao.getAll()

    suspend fun addScan(scan: ScanItem) = scanDao.insert(scan)

    suspend fun clearHistory() = scanDao.clearAll()

    suspend fun deleteScan(scan: ScanItem) = scanDao.delete(scan)

    suspend fun getProductNameFromBarcode(barcode: String): String? {
        productCache[barcode]?.let {
            Log.d(ScannerConstants.LogTags.SCAN_REPOSITORY, "Cache hit for barcode: $barcode")
            return it
        }

        Log.d(ScannerConstants.LogTags.SCAN_REPOSITORY, "Getting product for barcode: '$barcode'")

        return try {
            val response = foodFactsApi.getProductByBarcode(barcode)

            val productName = if (response.status == ScannerConstants.Api.STATUS_SUCCESS && response.product != null) {
                response.product?.let { product ->
                    product.productName?.takeIf { it.isNotBlank() }
                        ?: product.productNameRu?.takeIf { it.isNotBlank() }
                        ?: product.brands?.takeIf { it.isNotBlank() }
                        ?: product.genericName?.takeIf { it.isNotBlank() }
                }
            } else {
                null
            }

            productCache[barcode] = productName

            if (productName != null) {
                Log.d(ScannerConstants.LogTags.SCAN_REPOSITORY, "Found product: $productName")
            } else {
                Log.d(ScannerConstants.LogTags.SCAN_REPOSITORY, "Product not found")
            }

            productName

        } catch (e: Exception) {
            Log.e(ScannerConstants.LogTags.SCAN_REPOSITORY, "API call failed: ${e.message}")
            productCache[barcode] = null
            null
        }
    }

    fun getStatisticsFlow(): Flow<StatisticsData> {
        return scanDao.getAll().map { scans ->
            val now = Calendar.getInstance()
            val expiryDistribution = calculateExpiryDistribution(scans, now)
            val categoryDistribution = calculateCategoryDistribution(scans)
            StatisticsData(expiryDistribution, categoryDistribution)
        }
    }
    private fun calculateExpiryDistribution(
        scans: List<ScanItem>,
        now: Calendar
    ): ExpiryDistribution {
        var expired = 0
        var lessThan7Days = 0
        var lessThan30Days = 0
        var moreThan30Days = 0

        scans.forEach { scan ->
            scan.expiryDate?.let { expiry ->
                val expiryCal = Calendar.getInstance().apply {
                    timeInMillis = expiry
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val normalizedNow = Calendar.getInstance().apply {
                    time = now.time
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val diff = expiryCal.timeInMillis - normalizedNow.timeInMillis
                val days = diff / ScannerConstants.Time.MILLISECONDS_PER_DAY

                when {
                    days < ScannerConstants.Expiry.EXPIRED -> expired++
                    days <= ScannerConstants.Expiry.LESS_THAN_7_DAYS -> lessThan7Days++
                    days <= ScannerConstants.Expiry.LESS_THAN_30_DAYS -> lessThan30Days++
                    else -> moreThan30Days++
                }
            }
        }

        return ExpiryDistribution(expired, lessThan7Days, lessThan30Days, moreThan30Days)
    }

    private fun calculateCategoryDistribution(scans: List<ScanItem>): Map<String, Int> {
        val categoryMap = mutableMapOf<String, Int>()

        scans.forEach { scan ->
            val category = scan.category ?: ScannerConstants.UI.NO_CATEGORY
            categoryMap[category] = categoryMap.getOrDefault(category, 0) + 1
        }

        return categoryMap
    }
}