package com.example.healthcal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcal.data.model.FoodLog
import com.example.healthcal.data.repository.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DashboardUiState(
    val loading: Boolean = true,
    val foods: List<FoodLog> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false
) {
    val calories: Int get() = foods.sumOf { it.kalori }.toInt()
}

class DashboardViewModel(private val repository: FoodRepository = FoodRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching { withContext(Dispatchers.IO) { repository.getToday() } }
                .onSuccess { _uiState.value = _uiState.value.copy(loading = false, foods = it) }
                .onFailure { _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "Terjadi kesalahan") }
        }
    }

    fun addFood(name: String, meal: String, calories: Double, portion: String, carbs: Double?, protein: Double?, fat: Double?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, error = null, saved = false)
            runCatching { withContext(Dispatchers.IO) { repository.add(name, meal, calories, portion, carbs, protein, fat) } }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(saving = false, saved = true)
                    refresh()
                }
                .onFailure { _uiState.value = _uiState.value.copy(saving = false, error = it.message ?: "Gagal menyimpan") }
        }
    }

    fun clearFeedback() { _uiState.value = _uiState.value.copy(error = null, saved = false) }
}
