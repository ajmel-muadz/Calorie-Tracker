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

class EditFoodActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()


    private lateinit var nameEditText: EditText
    private lateinit var caloriesEditText: EditText
    private lateinit var fatEditText: EditText
    private lateinit var proteinEditText: EditText
    private lateinit var carbsEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel.setContext(this)
        setContentView(R.layout.edit_food_activity)

        // Get the data passed to this activity
        val foodId = intent.getIntExtra("FOOD_ID", 0)
        val foodName = intent.getStringExtra("FOOD_NAME") ?: ""
        val foodCalories = intent.getDoubleExtra("FOOD_CALORIES", 0.0)
        val foodFat = intent.getDoubleExtra("FOOD_FAT", 0.0)
        val foodProtein = intent.getDoubleExtra("FOOD_PROTEIN", 0.0)
        val foodCarbs = intent.getDoubleExtra("FOOD_CARBS", 0.0)
        val currentDateString = intent.getStringExtra("CURRENT_DATE_STRING") ?: ""

        // Get the original date and time from the intent;
        val originalCalendarTimeMillis = intent.getLongExtra("CURRENT_DATE_TIME_IN_MILLIS", -1L)

        // A small check as I faced some issues with going back to the previous page.
        val calendarTimeMillis = if (originalCalendarTimeMillis != -1L) {
            originalCalendarTimeMillis
        } else {
            appViewModel.calendarDate.timeInMillis
        }

        nameEditText = findViewById(R.id.editTextName)
        caloriesEditText = findViewById(R.id.editTextCalories)
        fatEditText = findViewById(R.id.editTextFat)
        proteinEditText = findViewById(R.id.editTextProtein)
        carbsEditText = findViewById(R.id.editTextCarbs)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Set the existing values to my EditTexts
        nameEditText.setText(foodName)
        caloriesEditText.setText(foodCalories.toString())
        fatEditText.setText(foodFat.toString())
        proteinEditText.setText(foodProtein.toString())
        carbsEditText.setText(foodCarbs.toString())

        backButton.setOnClickListener {
            finish()
        }

        // Save the updated food details
        saveButton.setOnClickListener {


            val updatedName = nameEditText.text.toString()
            val updatedCalories = caloriesEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedFat = fatEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedProtein = proteinEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedCarbs = carbsEditText.text.toString().toDoubleOrNull() ?: 0.0

            val updatedFood = Food(
                id = foodId,
                name = updatedName,
                calories = updatedCalories,
                fat = updatedFat,
                protein = updatedProtein,
                carbs = updatedCarbs,
                dateString = currentDateString // should I keep the dateString to what it was before or change it to the time the food was updated?
            )

            // Updating food details in the database then going back to the previous page
            lifecycleScope.launch {
                appViewModel.updateFood(updatedFood, this@EditFoodActivity)

                Toast.makeText(this@EditFoodActivity, "Food updated!", Toast.LENGTH_SHORT).show()

                // Return to MainActivity with the exact original date and time
                launchMainActivityWithDate(currentDateString, calendarTimeMillis)
            }
        }
    }

    private fun launchMainActivityWithDate(returnCurrentDate: String, returnCurrentDateTimeInMillis: Long) {

        val intent = Intent(this@EditFoodActivity, MainActivity::class.java).apply {
            putExtra("RETURN_CURRENT_DATE", returnCurrentDate)
            putExtra("RETURN_CURRENT_DATE_TIME_IN_MILLIS", returnCurrentDateTimeInMillis)

        }
        startActivity(intent)
        finish()
    }
}
