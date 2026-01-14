package com.example.honeywellfood.data.repository

import android.util.Log
import com.example.honeywellfood.data.local.ScanDao
import com.example.honeywellfood.data.network.FoodFactsApi
import com.example.honeywellfood.domain.model.ScanItem
import kotlinx.coroutines.flow.Flow
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

    suspend fun deleteScanById(id: Int) = scanDao.deleteById(id)

    suspend fun getProductNameFromBarcode(barcode: String): String? {
        productCache[barcode]?.let {
            Log.d("ScanRepository", "Cache hit for barcode: $barcode")
            return it
        }

        Log.d("ScanRepository", "Getting product for barcode: '$barcode'")

        return try {
            val response = foodFactsApi.getProductByBarcode(barcode)

            val productName = if (response.status == "success" && response.product != null) {
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
                Log.d("ScanRepository", "Found product: $productName")
            } else {
                Log.d("ScanRepository", "Product not found")
            }

            productName

        } catch (e: Exception) {
            Log.e("ScanRepository", "API call failed: ${e.message}")
            productCache[barcode] = null
            null
        }
    }
}