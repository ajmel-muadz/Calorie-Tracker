/*
 * Module: EditFoodActivity
 * Description: Manages the activity for editing the details of a food item, including updating its nutritional values and handling UI interactions.
 * Author: Ahmed
 * ID: 21467369
 */

package com.example.foodcalorieapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/*
 * Class: EditFoodActivity
 * Description: Handles the user interface for editing existing food entries. Allows users to modify nutritional details and save changes.
 */
class EditFoodActivity : ComponentActivity() {

    // ViewModel for accessing application data
    private val appViewModel: AppViewModel by viewModels()

    // UI elements for user input and action buttons
    private lateinit var nameEditText: EditText
    private lateinit var caloriesEditText: EditText
    private lateinit var fatEditText: EditText
    private lateinit var proteinEditText: EditText
    private lateinit var carbsEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    /*
     * Method: onCreate
     * Description: Initializes the activity, retrieves data passed via intent, sets up UI components, and populates the fields with existing data.
     * Params:
     *   savedInstanceState - Bundle containing saved state data
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel.setContext(this)
        setContentView(R.layout.edit_food_activity)

        // Retrieve data passed from previous activity
        val foodId = intent.getLongExtra("FOOD_ID", 0)
        val foodName = intent.getStringExtra("FOOD_NAME") ?: ""
        val foodCalories = intent.getDoubleExtra("FOOD_CALORIES", 0.0)
        val foodFat = intent.getDoubleExtra("FOOD_FAT", 0.0)
        val foodProtein = intent.getDoubleExtra("FOOD_PROTEIN", 0.0)
        val foodCarbs = intent.getDoubleExtra("FOOD_CARBS", 0.0)
        val currentDateString = intent.getStringExtra("CURRENT_DATE_STRING") ?: ""

        // Original date and time in milliseconds
        val originalCalendarTimeMillis = intent.getLongExtra("CURRENT_DATE_TIME_IN_MILLIS", -1L)

        // Use the original date/time if valid, otherwise fallback to ViewModel's calendar time
        val calendarTimeMillis = if (originalCalendarTimeMillis != -1L) {
            originalCalendarTimeMillis
        } else {
            appViewModel.calendarDate.timeInMillis
        }

        // Initialize UI components
        nameEditText = findViewById(R.id.editTextName)
        caloriesEditText = findViewById(R.id.editTextCalories)
        fatEditText = findViewById(R.id.editTextFat)
        proteinEditText = findViewById(R.id.editTextProtein)
        carbsEditText = findViewById(R.id.editTextCarbs)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Set initial values to input fields
        nameEditText.setText(foodName)
        caloriesEditText.setText(foodCalories.toString())
        fatEditText.setText(foodFat.toString())
        proteinEditText.setText(foodProtein.toString())
        carbsEditText.setText(foodCarbs.toString())

        // Handle back button click to return to the previous activity
        backButton.setOnClickListener {
            finish()
        }

        // Save button click to update food details in the database
        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString()
            val updatedCalories = caloriesEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedFat = fatEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedProtein = proteinEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedCarbs = carbsEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Create updated food object
            val updatedFood = Food(
                id = foodId,
                name = updatedName,
                calories = updatedCalories,
                fat = updatedFat,
                protein = updatedProtein,
                carbs = updatedCarbs,
                dateString = currentDateString
            )

            // Update the food entry in the database asynchronously
            lifecycleScope.launch {
                appViewModel.updateFood(updatedFood, this@EditFoodActivity)

                Toast.makeText(this@EditFoodActivity, "Food updated!", Toast.LENGTH_SHORT).show()

                // Return to MainActivity with the original date and time
                launchMainActivityWithDate(currentDateString, calendarTimeMillis)
            }
        }
    }

    /*
     * Method: launchMainActivityWithDate
     * Description: Navigates back to the MainActivity, passing back the original date and time.
     * Params:
     *   returnCurrentDate - The original date string
     *   returnCurrentDateTimeInMillis - The original time in milliseconds
     */
    private fun launchMainActivityWithDate(returnCurrentDate: String, returnCurrentDateTimeInMillis: Long) {
        val intent = Intent(this@EditFoodActivity, MainActivity::class.java).apply {
            putExtra("RETURN_CURRENT_DATE", returnCurrentDate)
            putExtra("RETURN_CURRENT_DATE_TIME_IN_MILLIS", returnCurrentDateTimeInMillis)
        }
        startActivity(intent)
        finish()
    }
}
