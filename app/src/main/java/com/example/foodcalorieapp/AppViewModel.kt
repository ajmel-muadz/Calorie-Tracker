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


    // Anything contained in this code block is wholly responsible for API calls.
    /* -------------------------------------------------------------------------------------- */
    var name by mutableStateOf<String?>("<Empty>")
    var servingSize by mutableStateOf<Double?>(0.0)
    var calories by mutableStateOf<Double?>(0.0)
    var fat by mutableStateOf<Double?>(0.0)
    var protein by mutableStateOf<Double?>(0.0)
    var carbs by mutableStateOf<Double?>(0.0)

    var errorMessage by mutableStateOf<String?>("")
    var loading by mutableStateOf<Boolean>(false)

    private val apiService = RetrofitInstance.api

    // Function to perform the network call.
    fun fetchItems(searchQuery: String) {
        //_loading.value = true
        loading = true

        viewModelScope.launch {
            try {
                val searchResult = withContext(Dispatchers.IO) {
                    apiService.getItemsList(searchQuery)
                }

                name = searchResult.items.firstOrNull()?.name ?: "No item found"
                servingSize = searchResult.items.firstOrNull()?.servingSize ?: 0.0
                calories = searchResult.items.firstOrNull()?.calories ?: 0.0
                fat = searchResult.items.firstOrNull()?.fat ?: 0.0
                protein = searchResult.items.firstOrNull()?.protein ?: 0.0
                carbs = searchResult.items.firstOrNull()?.carbs ?: 0.0
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Error fetching name: ${e.message}"
                name = null
                servingSize = null
                calories = null
                fat = null
                protein = null
                carbs = null
            } finally {
                loading = false
            }
        }
    }
    /* -------------------------------------------------------------------------------------- */
}