package com.example.foodcalorieapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
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
}