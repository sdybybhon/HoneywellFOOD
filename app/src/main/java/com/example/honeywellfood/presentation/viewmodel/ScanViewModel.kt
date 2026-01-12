package com.example.honeywellfood.presentation.viewmodel

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
    val isScanning: LiveData<Boolean> = _isScanning

    val scanHistory: Flow<List<ScanItem>> = repository.getAllScans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleScanning() {
        _isScanning.value = !(_isScanning.value ?: false)
    }

    fun addScan(data: String, symbology: String) {
        viewModelScope.launch {
            repository.addScan(ScanItem(data = data, symbology = symbology))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}