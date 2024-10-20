// Credits...
/* -------------------------------------------------------------------------------- */
// Below link helped with forcing input to be a number.
// 1. https://medium.com/@android-world/jetpack-compose-textfield-input-types-213cc2ec22f5
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.ui.semantics.Role
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme
import kotlinx.coroutines.launch

class AddNutritionActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val dao = AppDatabase.getInstance(this).dateWithFoodsDao
        super.onCreate(savedInstanceState)
        setContent {
            FoodCalorieAppTheme {
                val invalidFoodName = intent.getStringExtra("INVALID_FOOD_NAME")
                val currentDate = intent.getStringExtra("CURRENT_DATE")
                val currentDateTimeInMillis = intent.getLongExtra("CURRENT_DATE_TIME_IN_MILLIS", 0)
                AddNutritionScreen(viewModel = appViewModel, dateWithFoodsDao = dao,
                    invalidFoodName, currentDate, currentDateTimeInMillis)
            }
        }
    }
}


@Composable
fun AddNutritionScreen(viewModel: AppViewModel, dateWithFoodsDao: DateWithFoodsDao,
                       invalidFoodName: String?, currentDate: String?, currentDateTimeInMillis: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // These variables are for storing value of input text.
    /* ---------------------------------------------------------- */
    var caloriesInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    /* ---------------------------------------------------------- */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter macros for $invalidFoodName",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )

            // Text fields are in this code block.
            /* ---------------------------------------------------------------------------- */
            TextField(
                value = caloriesInput,
                onValueChange = { caloriesInput = it },
                label = { Text(text = "Calories") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Search button for searching for food."
                        )
                    }
                },
                modifier = Modifier.padding(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = fatInput,
                onValueChange = { fatInput = it },
                label = { Text(text = "Fat") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Search button for searching for food."
                        )
                    }
                },
                modifier = Modifier.padding(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = proteinInput,
                onValueChange = { proteinInput = it },
                label = { Text(text = "Protein") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Search button for searching for food."
                        )
                    }
                },
                modifier = Modifier.padding(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = carbsInput,
                onValueChange = { carbsInput = it },
                label = { Text(text = "Carbs") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Search button for searching for food."
                        )
                    }
                },
                modifier = Modifier.padding(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Drop down menu allowing a user to choose their meal type.
            /* -------------------------------------------------------------------------- */
            val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack")
            val (selectedOption, onOptionSelected) = remember { mutableStateOf(mealOptions[0]) }
            // Modifier.selectableGroup() is recommended to use by Google's documentation. I am just following orders.
            Column(Modifier.selectableGroup()) {
                mealOptions.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (text == selectedOption), onClick = { onOptionSelected(text) })
                        Text(
                            text = text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            viewModel.mealType = selectedOption
            /* -------------------------------------------------------------------------- */
            /* ---------------------------------------------------------------------------- */
        }

        viewModel.name = invalidFoodName
        viewModel.servingSize = 100.0
        viewModel.calories = caloriesInput.toDoubleOrNull()
        viewModel.fat = fatInput.toDoubleOrNull()
        viewModel.protein = proteinInput.toDoubleOrNull()
        viewModel.carbs = carbsInput.toDoubleOrNull()

        // We only allowing for adding the food if all input fields are populated with numbers.
        if (viewModel.calories != null && viewModel.fat != null && viewModel.protein != null
            && viewModel.carbs != null) {
            Button(onClick = {
                val currentDateToAdd: String = currentDate!!  // Current date passed from intent
                val foodNameToAdd: String = (viewModel.name!!).replaceFirstChar { it.uppercase() }  // Capitalise food name
                val foodCaloriesToAdd: Double = viewModel.calories!!
                val foodFatToAdd: Double = viewModel.fat!!
                val foodProteinToAdd: Double = viewModel.protein!!
                val foodCarbsToAdd: Double = viewModel.carbs!!
                val foodMealTypeToAdd: String = viewModel.mealType

                // When clicking 'Add Food' we add the corresponding data to the database.
                /* ------------------------------------------------------------------------------------------- */
                val dateToInsert = Date(currentDateToAdd)
                val foodToInsert = Food(name = foodNameToAdd, calories = foodCaloriesToAdd, fat = foodFatToAdd,
                    protein = foodProteinToAdd, carbs = foodCarbsToAdd, mealType = foodMealTypeToAdd, dateString = currentDateToAdd)

                scope.launch {
                    dateWithFoodsDao.insertDate(dateToInsert)
                    dateWithFoodsDao.insertFood(foodToInsert)
                }
                /* ------------------------------------------------------------------------------------------- */

                // Go back to main screen when we add food.
                launchMainActivity(context, currentDateToAdd, currentDateTimeInMillis)

            }, modifier = Modifier.padding(bottom = 10.dp)) {
                Text(text = "Add New Food")
            }
        }
    }
}


private fun launchMainActivity(context: Context, returnCurrentDate: String?, returnCurrentDateTimeInMillis: Long) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("RETURN_CURRENT_DATE", returnCurrentDate)
    intent.putExtra("RETURN_CURRENT_DATE_TIME_IN_MILLIS", returnCurrentDateTimeInMillis)
    context.startActivity(intent)
}


