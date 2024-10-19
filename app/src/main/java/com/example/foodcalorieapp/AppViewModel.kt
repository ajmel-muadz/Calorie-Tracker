// Credits...
/* -------------------------------------------------------------------------------- */
// Used to help me create a firebase database.
// 1. https://www.geeksforgeeks.org/android-jetpack-compose-add-data-to-firebase-firestore/
//
// Used to help convert the bitmap image to an base64 string to store in the firebase firestore
// 2. https://medium.com/@reddytintaya/note-1-android-bitmap-to-base64-string-with-kotlin-552890c56b04
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.tasks.await
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
    var loading by mutableStateOf(false)

    private var _meal = MutableStateFlow<MealImage?>(null)
    var meal = _meal.asStateFlow()

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

    fun addMealToFirebase(image: Bitmap?, context: Context, id: Long){

        // on below line creating an instance of firebase firestore.
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()

        //creating a collection reference for our Firebase Firestore database.
        val dbMeals: CollectionReference = db.collection("MealImages")

        // convert the bitmap to base64 to store it as a string in the firestore
        val encodedImage: String?
        val baos = java.io.ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        encodedImage = android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT)

        //adding our data to our courses object class.
        val mealImages = MealImage(encodedImage, id)

        //below method is use to add data to Firebase Firestore.
        dbMeals.add(mealImages).addOnSuccessListener {
            Toast.makeText(
                context,
                "Your Meal has been added to Firebase Firestore",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e ->
            // this method is called when the data addition process is failed.
            // displaying a toast message when data addition is failed.
            Toast.makeText(context, "Fail to add course \n$e", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getMealImageById(inId: Long?): String?{
        val db = FirebaseFirestore.getInstance()
        var returnVal: String? = null

        try {
            val querySnapshot = db.collection("MealImages")
                .whereEqualTo("id", inId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val mealImage = document.toObject(MealImage::class.java)
                returnVal = mealImage?.image
            }

        }catch(e: Exception){
            Log.e("FirestoreError", "Error getting meal image", e)
        }

        return returnVal
    }
    /* -------------------------------------------------------------------------------------- */
}

