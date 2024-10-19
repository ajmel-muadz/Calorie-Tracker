// Credits...
/* -------------------------------------------------------------------------------- */
// Used to help me create a firebase database.
// 1. https://www.geeksforgeeks.org/android-jetpack-compose-add-data-to-firebase-firestore/
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AppViewModel : ViewModel() {


    private var dateWithFoodsDao: DateWithFoodsDao? = null
    var calendarDate by mutableStateOf<Calendar>(Calendar.getInstance())
    var formattedDate by mutableStateOf<String>(SimpleDateFormat.getDateInstance().format(Date()))

    public var _carList = MutableStateFlow<List<Food>>(emptyList())
    var carList = _carList.asStateFlow()

    var selectedImageUri by mutableStateOf<Uri?>(null)


    fun setContext(context: Context) {
        dateWithFoodsDao = AppDatabase.getInstance(context).dateWithFoodsDao
        if (dateWithFoodsDao == null) {

            Toast.makeText(context, "Failed to Initialize DAO", Toast.LENGTH_SHORT).show()
        }
    }



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

    fun updateFood(food: Food, context: Context) {
        viewModelScope.launch {
            if (dateWithFoodsDao != null) {
                dateWithFoodsDao?.updateFood(food)
                Toast.makeText(context, "Food updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "DAO not initialized!", Toast.LENGTH_SHORT).show()
            }
        }
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
    var loading by mutableStateOf(false)

    private val apiService = RetrofitInstance.api

    // Function to perform the network call.
    fun fetchItems(searchQuery: String) {
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

    fun addMealToFirebase(imageByteArray: ByteArray, context: Context){
        // on below line creating an instance of firebase firestore.
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()
        //creating a collection reference for our Firebase Firestore database.
        val dbMeals: CollectionReference = db.collection("MealImage")
        //adding our data to our courses object class.
        val mealImage = MealImage(imageByteArray)

        //below method is use to add data to Firebase Firestore.
        dbMeals.add(mealImage).addOnSuccessListener {
            Toast.makeText(
                context,
                "Your Meal has been added to Firebase Firestore",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

//    fun getMealsList(){
//
//
//
//    }
    /* -------------------------------------------------------------------------------------- */
}