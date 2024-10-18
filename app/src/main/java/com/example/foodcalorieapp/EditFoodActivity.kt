package com.example.foodcalorieapp

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_food_activity)

        // Get the data passed to this activity
        val foodName = intent.getStringExtra("FOOD_NAME") ?: ""
        val foodCalories = intent.getDoubleExtra("FOOD_CALORIES", 0.0)
        val foodFat = intent.getDoubleExtra("FOOD_FAT", 0.0)
        val foodProtein = intent.getDoubleExtra("FOOD_PROTEIN", 0.0)
        val foodCarbs = intent.getDoubleExtra("FOOD_CARBS", 0.0)


        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextName)
        caloriesEditText = findViewById(R.id.editTextCalories)
        fatEditText = findViewById(R.id.editTextFat)
        proteinEditText = findViewById(R.id.editTextProtein)
        carbsEditText = findViewById(R.id.editTextCarbs)
        saveButton = findViewById(R.id.saveButton)


        // Set the existing values
        nameEditText.setText(foodName)
        caloriesEditText.setText(foodCalories.toString())
        fatEditText.setText(foodFat.toString())
        proteinEditText.setText(foodProtein.toString())
        carbsEditText.setText(foodCarbs.toString())

        // Save the updated food details
        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString()
            val updatedCalories = caloriesEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedFat = fatEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedProtein = proteinEditText.text.toString().toDoubleOrNull() ?: 0.0
            val updatedCarbs = carbsEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Use ViewModel to save the updated food item
            val updatedFood = Food(
                name = updatedName,
                calories = updatedCalories,
                fat = updatedFat,
                protein = updatedProtein,
                carbs = updatedCarbs,
                dateString = "DateStringPlaceholder" // You might want to pass the actual date
            )

            // It should work once this below is done
//            lifecycleScope.launch {
//                appViewModel.updateFood(updatedFood)
//                Toast.makeText(this@EditFoodActivity, "Food updated!", Toast.LENGTH_SHORT).show()
//                finish() // Close the activity after saving
//            }
        }
    }
}
