package com.example.honeywellfood.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.honeywellfood.data.repository.ScanRepository
import com.example.honeywellfood.domain.model.ScanItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {
    private val _isScanning = MutableLiveData(false)
    private val _showProductDialog = MutableLiveData<Triple<String, String?, String>?>(null)

    val isScanning: LiveData<Boolean> = _isScanning
    val showProductDialog: LiveData<Triple<String, String?, String>?> = _showProductDialog

    val scanHistory: Flow<List<ScanItem>> = repository.getAllScans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleScanning() {
        _isScanning.value = !(_isScanning.value ?: false)
    }

    fun onBarcodeScanned(data: String, symbology: String) {
        Log.d("ScanViewModel", "Processing barcode: '$data' (length: ${data.length})")

        if (data.isBlank() || data.length < 8) {
            Log.e("ScanViewModel", "Invalid barcode: '$data'")
            return
        }

        viewModelScope.launch {
            val productName = try {
                Log.d("ScanViewModel", "Fetching product info for barcode: $data")
                val name = repository.getProductNameFromBarcode(data)
                Log.d("ScanViewModel", "Received product name: $name")
                name
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Error fetching product: ${e.message}")
                e.printStackTrace()
                null
            }

            Log.d("ScanViewModel", "Showing dialog with product name: $productName")
            _showProductDialog.postValue(Triple(data, productName, symbology))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteScan(scan: ScanItem) {
        viewModelScope.launch {
            repository.deleteScan(scan)
        }
    }

    fun addProductWithInfo(
        barcode: String,
        productName: String,
        category: String?,
        expiryDate: Long,
        symbology: String
    ) {
        viewModelScope.launch {
            repository.addScan(
                ScanItem(
                    barcode = barcode,
                    productName = productName,
                    category = category,
                    expiryDate = expiryDate,
                    symbology = symbology
                )
            )
            _showProductDialog.postValue(null)
        }
    }
}