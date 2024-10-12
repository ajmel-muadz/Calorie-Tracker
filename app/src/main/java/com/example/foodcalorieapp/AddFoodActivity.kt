// Credits...
/* -------------------------------------------------------------------------------- */
// Below link helped with removing weird phantom borders in rounded TextField.
// 1. https://www.youtube.com/watch?v=cIK7ILpApGE

// Below link helped with adding search icon to TextField.
// 2. https://www.youtube.com/watch?v=6w4l-3jC21E

// Below link helped with hiding keyboard when search icon is clicked.
// 3. https://stackoverflow.com/questions/59133100/how-to-close-the-virtual-keyboard-from-a-jetpack-compose-textfield

// Below link helped with clearing TextField focus when search icon is clicked.
// 4. https://stackoverflow.com/questions/67058630/how-clear-focus-for-basictextfield-in-jetpack-compose
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme

class AddFoodActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodCalorieAppTheme {
                AddFoodScreen(viewModel = appViewModel)
            }
        }
    }
}


@Composable
fun AddFoodScreen(viewModel: AppViewModel) {
    val context = LocalContext.current

    var searchKey by remember { mutableStateOf("") }  // Variable for search term
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TextField for searching for food.
        /* --------------------------------------------------------------------------- */
        TextField(
            value = searchKey, 
            onValueChange = { searchKey = it },
            label = { Text(text = "Search Food") },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()  // Clear cursor focus.
                    keyboardController?.hide()  // Hide keyboard.

                    viewModel.fetchItems(searchKey)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search button for searching for food."
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        /* --------------------------------------------------------------------------- */

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Show loading indicator when searching for food.
            if (viewModel.loading) {
                CircularProgressIndicator()
            }

            // Display the values retrieved from the API call.
            /* -------------------------------------------------------------------------- */
            if (viewModel.name != "<Empty>" && viewModel.name != "No item found") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = LIGHTER_BACKGROUND_COLOUR)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        viewModel.name?.let { Text(text = "Name: $it", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        viewModel.servingSize?.let { Text(text = "Serving size: $it", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        viewModel.calories?.let { Text(text = "Calories: $it", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        viewModel.fat?.let { Text(text = "Fat: $it", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        viewModel.protein?.let { Text(text = "Protein: $it", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        viewModel.carbs?.let { Text(text = "Carbs: $it", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }

                        viewModel.errorMessage?.let {
                            if (it.isNotEmpty()) {
                                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            }
            /* -------------------------------------------------------------------------- */

            // If no food item is found we launch an activity allowing for manually'
            // inputting the food yeah.
            if (viewModel.name == "No item found") {
                launchAddNutritionActivity(context, searchKey)
                viewModel.name = "<Empty>"
            }
        }

        // If API query does not result in "No item found" and it is not empty,
        // we show the 'Add food' button allowing for adding to the log.
        if (viewModel.name != "No item found" && viewModel.name != "<Empty>") {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.padding(bottom = 10.dp)) {
                Text(text = "Add Food")
            }
        }

        // Show error message if there is any.
        viewModel.errorMessage?.let {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


private fun launchAddNutritionActivity(context: Context, searchKey: String) {
    val intent = Intent(context, AddNutritionActivity::class.java)
    intent.putExtra("INVALID_FOOD_NAME", searchKey)
    context.startActivity(intent)
}