package com.example.foodcalorieapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AppViewModel : ViewModel() {
    var date by mutableStateOf("")

    fun setCurrentDateIfEmpty() {
        if (this.date == "") {
            this.date = SimpleDateFormat.getDateInstance().format(Date())
        }
    }
}