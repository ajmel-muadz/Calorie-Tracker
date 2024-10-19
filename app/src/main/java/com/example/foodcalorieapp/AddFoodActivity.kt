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

// Below link helped with calling suspend functions inside composables tied to a button click.
// 5. https://developer.android.com/develop/ui/compose/side-effects#:~:text=To%20perform%20work%20over%20the,if%20LaunchedEffect%20leaves%20the%20composition.

// Below link helped with adding data to Firebase Firestore.
//6. https://www.geeksforgeeks.org/android-jetpack-compose-add-data-to-firebase-firestore/
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme
import kotlinx.coroutines.launch
import java.io.File

class AddFoodActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val dao = AppDatabase.getInstance(this).dateWithFoodsDao
        super.onCreate(savedInstanceState)
        setContent {
            FoodCalorieAppTheme {
                val currentDate = intent.getStringExtra("CURRENT_DATE")
                val currentDateTimeInMillis = intent.getLongExtra("CURRENT_DATE_TIME_IN_MILLIS", 0)
                AddFoodScreen(viewModel = appViewModel, dateWithFoodsDao = dao, currentDate, currentDateTimeInMillis)
            }
        }
    }
}


@Composable
fun AddFoodScreen(
    viewModel: AppViewModel,
    dateWithFoodsDao: DateWithFoodsDao,
    currentDate: String?,
    currentDateTimeInMillis: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchKey by remember { mutableStateOf("") }  // Variable for search term

    val expand = remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var captureImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var newFile: File? = null

    var showImageBox by remember { mutableStateOf(false) }  // State variable to control image Box display

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val imageByteArray = newFile?.readBytes()

            if (imageByteArray != null) {
                viewModel.addMealToFirebase(imageByteArray, context)
            }
            showImageBox = true  // Show the image Box after capturing the image
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                cameraLauncher.launch(captureImageUri)
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

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

        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        viewModel.name?.let {
                            Text(
                                text = "Name: $it",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        viewModel.servingSize?.let {
                            Text(
                                text = "Serving size: $it",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        viewModel.calories?.let {
                            Text(
                                text = "Calories: $it",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        viewModel.fat?.let {
                            Text(
                                text = "Fat: $it",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        viewModel.protein?.let {
                            Text(
                                text = "Protein: $it",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        viewModel.carbs?.let {
                            Text(
                                text = "Carbs: $it",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        viewModel.errorMessage?.let {
                            if (it.isNotEmpty()) {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
            /* -------------------------------------------------------------------------- */

            // Display the image Box when an image is captured
            if (showImageBox) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(10.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    if (captureImageUri != Uri.EMPTY) {
                        Image(
                            painter = rememberAsyncImagePainter(captureImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "No image selected")
                    }
                }
            }

            // If no food item is found we launch an activity allowing for manual input.
            if (viewModel.name == "No item found") {
                launchAddNutritionActivity(
                    context,
                    searchKey,
                    currentDate,
                    currentDateTimeInMillis
                )
                viewModel.name = "<Empty>"
            }
        }

        IconButton(onClick = {
            focusManager.clearFocus()  // Clear cursor focus.
            keyboardController?.hide()

            showDialog = true
        }) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Edit a food's properties.",
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text(
                        text = "        Choose an Action",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    showDialog = false

                                    newFile = context.createImageFile(viewModel.name)
                                    captureImageUri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".provider",
                                        newFile!!
                                    )

                                    val permissionCheckResult = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    )
                                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                        cameraLauncher.launch(captureImageUri)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .padding(top = 20.dp)
                            ) {
                                Text("Open Camera")
                            }
                        }
                        Button(
                            onClick = {
                                // Action for "Select From Gallery" button
                            },
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        ) {
                            Text("Select From Gallery")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        // Show the 'Add Food' button if appropriate.
        if (viewModel.name != "No item found" && viewModel.name != "<Empty>") {
            Button(onClick = {
                val currentDateToAdd: String = currentDate!!  // Current date passed from intent
                val foodNameToAdd: String =
                    (viewModel.name!!).replaceFirstChar { it.uppercase() }  // Capitalize food name
                val foodCaloriesToAdd: Double = viewModel.calories!!
                val foodFatToAdd: Double = viewModel.fat!!
                val foodProteinToAdd: Double = viewModel.protein!!
                val foodCarbsToAdd: Double = viewModel.carbs!!

                // Add the corresponding data to the database.
                /* ------------------------------------------------------------------------------------------- */
                val dateToInsert = Date(currentDateToAdd)
                val foodToInsert = Food(
                    name = foodNameToAdd,
                    calories = foodCaloriesToAdd,
                    fat = foodFatToAdd,
                    protein = foodProteinToAdd,
                    carbs = foodCarbsToAdd,
                    dateString = currentDateToAdd
                )

                scope.launch {
                    dateWithFoodsDao.insertDate(dateToInsert)
                    dateWithFoodsDao.insertFood(foodToInsert)
                }
                /* ------------------------------------------------------------------------------------------- */

                // Go back to main screen when we add food.
                launchMainActivity(context, currentDateToAdd, currentDateTimeInMillis)

            }, modifier = Modifier.padding(bottom = 10.dp)) {
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


@Composable
fun imagePicker(onImageSelected: (Uri) -> Unit){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }
    )
    Button(
        onClick = { launcher.launch("image/*") }
    ) {
        Text("Select Image")
    }

}

@Composable
fun getImageFromCamera() {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current

    var captureImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val capturedImagesUriList = remember { mutableStateListOf<Uri>() }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ success ->
        if (success){
            capturedImagesUriList.add(captureImageUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(captureImageUri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun launchAddNutritionActivity(context: Context, searchKey: String, currentDate: String?, currentDateTimeInMillis: Long) {
    val intent = Intent(context, AddNutritionActivity::class.java)
    intent.putExtra("INVALID_FOOD_NAME", searchKey)
    intent.putExtra("CURRENT_DATE", currentDate)
    intent.putExtra("CURRENT_DATE_TIME_IN_MILLIS", currentDateTimeInMillis)
    context.startActivity(intent)
}

private fun launchMainActivity(context: Context, returnCurrentDate: String?, returnCurrentDateTimeInMillis: Long) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("RETURN_CURRENT_DATE", returnCurrentDate)
    intent.putExtra("RETURN_CURRENT_DATE_TIME_IN_MILLIS", returnCurrentDateTimeInMillis)
    context.startActivity(intent)
}

// Mock implementation for the preview, this should make it easier for us with using split screen
val mockDateWithFoodsDao = object : DateWithFoodsDao {
    override suspend fun getFoodsWithDate(dateString: String): List<Food> {
        return listOf(Food(name = "Sample Food", calories = 100.0, fat = 5.0, protein = 3.0, carbs = 12.0, dateString = dateString))
    }

    override suspend fun insertDate(date: Date) {
        // Do nothing
    }

    override suspend fun insertFood(food: Food) {
        // Do nothing
    }

    override suspend fun getFoodByIdAndDate(id: Int, dateString: String): Food? {
        TODO("Not yet implemented")
    }


    override suspend fun updateFood(food: Food) {
        // Do nothing
    }

    override suspend fun deleteFood(food: Food) {
        // Do nothing
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAddFoodScreen() {
    val mockViewModel = AppViewModel().apply {
        name = "Example Food"
        calories = 200.0
        fat = 10.0
        protein = 5.0
        carbs = 30.0
    }

    AddFoodScreen(
        viewModel = mockViewModel,
        dateWithFoodsDao = mockDateWithFoodsDao,
        currentDate = "2024-10-18",
        currentDateTimeInMillis = System.currentTimeMillis()
    )
}




