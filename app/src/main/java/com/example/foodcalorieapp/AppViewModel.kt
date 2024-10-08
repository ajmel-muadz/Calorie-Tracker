package com.example.foodcalorieapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AppViewModel : ViewModel() {
    var calendarDate by mutableStateOf<Calendar>(Calendar.getInstance())
    var formattedDate by mutableStateOf<String>(SimpleDateFormat.getDateInstance().format(Date()))

    fun incrementDate() {
        val currentDate = this.calendarDate
        currentDate.add(Calendar.DAY_OF_MONTH, 1)
        this.formattedDate = SimpleDateFormat.getDateInstance().format(this.calendarDate.timeInMillis)
    }

    fun decrementDate() {
        val currentDate = this.calendarDate
        currentDate.add(Calendar.DAY_OF_MONTH, -1)
        this.formattedDate = SimpleDateFormat.getDateInstance().format(this.calendarDate.timeInMillis)
    }

    // --------------------------------------------------------------------------------- //

    // MutableStateFlow to hold the food name.
    private val _name = MutableStateFlow<String?>("<Empty>")
    val name: StateFlow<String?> get() = _name

    private val _servingSize = MutableStateFlow<Double?>(0.0)
    val servingSize: StateFlow<Double?> get() = _servingSize

    private val _calories = MutableStateFlow<Double?>(0.0)
    val calories: StateFlow<Double?> get() = _calories

    private val _fat = MutableStateFlow<Double?>(0.0)
    val fat: StateFlow<Double?> get() = _fat

    private val _protein = MutableStateFlow<Double?>(0.0)
    val protein: StateFlow<Double?> get() = _protein

    private val _carbs = MutableStateFlow<Double?>(0.0)
    val carbs: StateFlow<Double?> get() = _carbs

    // MutableStateFlow to hold error messages
    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // MutableStateFlow to hold loading state
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val apiService = RetrofitInstance.api

    // Function to perform the network call.
    fun fetchName(searchQuery: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val searchResult = withContext(Dispatchers.IO) {
                    apiService.getItemsList(searchQuery)
                }

                _name.value = searchResult.items.firstOrNull()?.name ?: "No item found"
                _servingSize.value = searchResult.items.firstOrNull()?.servingSize ?: 0.0
                _calories.value = searchResult.items.firstOrNull()?.calories ?: 0.0
                _fat.value = searchResult.items.firstOrNull()?.fat ?: 0.0
                _protein.value = searchResult.items.firstOrNull()?.protein ?: 0.0
                _carbs.value = searchResult.items.firstOrNull()?.carbs ?: 0.0
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching name: ${e.message}"
                _name.value = null
                _servingSize.value = null
                _calories.value = null
                _fat.value = null
                _protein.value = null
                _carbs.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}