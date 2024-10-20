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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AppViewModel : ViewModel() {

    // Dao for accessing and interacting with the database
    private var dateWithFoodsDao: DateWithFoodsDao? = null

    // Calender for managing date and time
    var calendarDate by mutableStateOf<Calendar>(Calendar.getInstance())
    var formattedDate by mutableStateOf<String>(SimpleDateFormat.getDateInstance().format(Date()))


    // Properties to track nutritional totals and Goals
    var totalCalories by mutableStateOf(0.0)
    var totalFat by mutableStateOf(0.0)
    var totalProtein by mutableStateOf(0.0)
    var totalCarbs by mutableStateOf(0.0)

    // Here is a default kind of Goals that will be set if the user has not set any.
    var fatGoal by mutableStateOf(70.0)
    var proteinGoal by mutableStateOf(50.0)
    var carbGoal by mutableStateOf(300.0)
    var caloriesGoal by mutableStateOf(2000.0)


    // LiveData to hold the list of foods for the current date
    private val _foodsList = MutableLiveData<List<Food>>()
    val foodList: LiveData<List<Food>> = _foodsList


    /*
     * Method: setContext
     * Description: Initializes the DAO using the provided application context and sets up default goals if not already present.
     * Params:
     *   context - Application context for initializing database
     */
    fun setContext(context: Context) {

        // Initialize the DAO using the provided context (in our case it is the application context)
        dateWithFoodsDao = AppDatabase.getInstance(context).dateWithFoodsDao

        // If the DAO is null, display a toast message indicating failure to initialize
        if (dateWithFoodsDao == null) {
            Toast.makeText(context, "Failed to Initialize DAO", Toast.LENGTH_SHORT).show()
        } else {
            // If the DAO is initialized, initialize default goals and refresh daily summary
            initializeDefaultGoals()
            refreshDailySummary()
            loadUserGoals()
        }
    }

    /*
     * Method: initializeDefaultGoals
     * Description: Initializes default nutritional goals if not already present in the database.
     */
    private fun initializeDefaultGoals() {

        // Check if default goals already exist in the database
        viewModelScope.launch {
            val existingGoals = dateWithFoodsDao?.getUserGoals()

            // If default goals do not exist, insert them into the database
            if (existingGoals == null) {
                val defaultGoals = UserGoals(
                    caloriesGoal = caloriesGoal,
                    fatGoal = fatGoal,
                    proteinGoal = proteinGoal,
                    carbGoal = carbGoal
                )

                // Insert the default goals into the database
                dateWithFoodsDao?.insertUserGoals(defaultGoals)
            }
        }
    }

    /*
    * Method: updateNutritionalGoals
    * Description: Updates the user's nutritional goals and stores them in the database.
    * Params:
    *   newCaloriesGoal - New caloric intake goal
    *   newFatGoal - New fat intake goal
    *   newProteinGoal - New protein intake goal
    *   newCarbGoal - New carbohydrate intake goal
    */
    fun updateNutritionalGoals(newCaloriesGoal: Double, newFatGoal: Double, newProteinGoal: Double, newCarbGoal: Double) {

        // Update the local variables with the new goals (not to do with the database)
        caloriesGoal = newCaloriesGoal
        fatGoal = newFatGoal
        proteinGoal = newProteinGoal
        carbGoal = newCarbGoal

        // then we update the database with the new goals
        viewModelScope.launch {
            val userGoals = UserGoals(
                caloriesGoal = newCaloriesGoal,
                fatGoal = newFatGoal,
                proteinGoal = newProteinGoal,
                carbGoal = newCarbGoal
            )
            dateWithFoodsDao?.insertUserGoals(userGoals)
        }
    }

    /*
     * Method: loadUserGoals
     * Description: Loads user's nutritional goals from the database.
     */
    private fun loadUserGoals() {

        // Load the user's goals from the database (used for when we display the goals on the screen)
        viewModelScope.launch {
            dateWithFoodsDao?.getUserGoals()?.let { savedGoals ->
                caloriesGoal = savedGoals.caloriesGoal
                fatGoal = savedGoals.fatGoal
                proteinGoal = savedGoals.proteinGoal
                carbGoal = savedGoals.carbGoal
            }
        }
    }


    /*
     * Method: incrementDate
     * Description: Increments the current date by one day and updates the daily summary.
     */
    fun incrementDate() {
        val currentDate = this.calendarDate

        // Increment the date by one day
        currentDate.add(Calendar.DAY_OF_MONTH, 1)
        // Update the formatted date string
        this.formattedDate = SimpleDateFormat.getDateInstance().format(this.calendarDate.timeInMillis)
        refreshDailySummary() // Refresh the daily summary to show the new date's summary
    }


    /*
     * Method: decrementDate
     * Description: Decrements the current date by one day and updates the daily summary.
     */
    fun decrementDate() {
        val currentDate = this.calendarDate

        // Decrement the date by one day
        currentDate.add(Calendar.DAY_OF_MONTH, -1)
        // Update the formatted date string
        this.formattedDate = SimpleDateFormat.getDateInstance().format(this.calendarDate.timeInMillis)
        refreshDailySummary() // Refresh the daily summary to show the new date's summary
    }


    /*
     * Method: updateFood
     * Description: Updates a food entry in the database.
     * Params:
     *   food - Food object to be updated
     *   context - Application context for displaying toast messages
     */
    fun updateFood(food: Food, context: Context) {

        // Update the food entry in the database asynchronously
        viewModelScope.launch {
            // Check if the DAO is initialized before attempting to update the food
            if (dateWithFoodsDao != null) {
                dateWithFoodsDao?.updateFood(food)
                Toast.makeText(context, "Food updated!", Toast.LENGTH_SHORT).show()
                refreshDailySummary() // Refresh the daily summary after updating the food

            } else {
                Toast.makeText(context, "DAO not initialized!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
     * Method: refreshDailySummary
     * Description: Refreshes the daily summary by retrieving food entries for the current date.
     */
    fun refreshDailySummary() {

        // Retrieve food entries for the current date from the database
        viewModelScope.launch {

            // Check if the DAO is initialized before attempting to retrieve foods
            dateWithFoodsDao?.let { dao ->
                val foods = dao.getFoodsWithDate(formattedDate)
                totalCalories = foods.sumOf { it.calories }
                totalFat = foods.sumOf { it.fat }
                totalProtein = foods.sumOf { it.protein }
                totalCarbs = foods.sumOf { it.carbs }
            }
        }
    }

    /*
     * Method: updateFoodsList
     * Description: Updates the list of foods displayed on the screen.
     * Params:
     *   updatedFoods - List of updated food entries
     */
    fun updateFoodsList(updatedFoods: List<Food>) {
        _foodsList.value = updatedFoods
        refreshDailySummary()
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

    // Add meal image to firebase
    fun addMealToFirebase(image: Bitmap?, context: Context, id: Long){

        // on below line creating an instance of firebase firestore.
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()

        //creating a collection reference for our Firebase Firestore database.
        val dbMeals: CollectionReference = db.collection("MealImagesMuadz")

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
        val db = FirebaseFirestore.getInstance() // Initialize Firebase Firestore
        var returnVal: String? = null

        try {
            val querySnapshot = db.collection("MealImagesMuadz")
                .whereEqualTo("id", inId)
                .get()
                .await()

            // Iterate through the query results and retrieve the image
            for (document in querySnapshot.documents) {
                val mealImage = document.toObject(MealImage::class.java) // Convert Firestore document to MealImage object
                returnVal = mealImage?.image // Return the image string
            }
        }catch(e: Exception){
            Log.e("FirestoreError", "Error getting meal image", e)
        }

        return returnVal
    }

    suspend fun deleteMealImageFromFirebase(id: Long, context: Context){
        val db = FirebaseFirestore.getInstance() // Initialize Firebase Firestore

        try{
            // Query Firestore for the meal image with the given ID
            val querySnapshot = db.collection("MealImagesMuadz")
                .whereEqualTo("id", id)
                .get()
                .await()

            // Iterate through the query results and delete the image
            for(document in querySnapshot.documents){
                document.reference.delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Your Meal has been deleted from Firebase Firestore",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener{ e ->
                        Toast.makeText(context, "Fail to delete course $e", Toast.LENGTH_SHORT).show()
                    }
            }
        }catch(e: Exception){
            Log.e("FirestoreError", "Error deleting meal image", e)
        }
    }
    /* -------------------------------------------------------------------------------------- */
}

