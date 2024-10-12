// Credits...
/* -------------------------------------------------------------------------------- */
// Below link helped with forcing input to be a number.
// 1. https://medium.com/@android-world/jetpack-compose-textfield-input-types-213cc2ec22f5
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme

class AddNutritionActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodCalorieAppTheme {
                val invalidFoodName = intent.getStringExtra("INVALID_FOOD_NAME")
                AddNutritionScreen(viewModel = appViewModel, invalidFoodName)
            }
        }
    }
}

@Composable
fun AddNutritionScreen(viewModel: AppViewModel, invalidFoodName: String?) {
    var caloriesInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                text = "Enter macros per serving for $invalidFoodName (Serving size is 100g)",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )

            TextField(
                value = caloriesInput,
                onValueChange = { caloriesInput = it },
                label = { Text(text = "Calories per 100g") },
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
                label = { Text(text = "Fat per 100g") },
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
                label = { Text(text = "Protein per 100g") },
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
                label = { Text(text = "Carbs per 100g") },
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
        }

        viewModel.name = invalidFoodName
        viewModel.servingSize = 100.0
        viewModel.calories = caloriesInput.toDoubleOrNull()
        viewModel.fat = fatInput.toDoubleOrNull()
        viewModel.protein = proteinInput.toDoubleOrNull()
        viewModel.carbs = carbsInput.toDoubleOrNull()
        
        if (viewModel.calories != null && viewModel.fat != null && viewModel.protein != null
            && viewModel.carbs != null) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.padding(bottom = 10.dp)) {
                Text(text = "Add New Food")
            }
        }
    }
}