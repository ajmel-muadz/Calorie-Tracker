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
// 6. https://www.geeksforgeeks.org/android-jetpack-compose-add-data-to-firebase-firestore/
//
// Below link helped me retrieve an image from the gallery
// 7. https://medium.com/@daniel.atitienei/picking-images-from-gallery-using-jetpack-compose-a18c11d93e12
//
// Below link helped me convert the image retrieved from gallery to a bitmap using content resolver
// 8. https://www.geeksforgeeks.org/content-providers-in-android-with-example/
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Log.d
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.compose.material3.Text as Text

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
    // The default stats prior changing serving size
    var cals by remember { mutableDoubleStateOf(0.0) }
    var fats by remember { mutableDoubleStateOf(0.0) }
    var carb by remember { mutableDoubleStateOf(0.0) }
    var proteins by remember { mutableDoubleStateOf(0.0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasSearched by remember { mutableStateOf(false) }

    var searchKey by remember { mutableStateOf("") }  // Variable for search term

    var showDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showImageBox by remember { mutableStateOf(false) }  // State variable to control image Box display
    /* ---------------------------------------------------------------------------------------- */

    var image by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher for taking a picture with the camera.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? -> // Callback function to handle the captured image.
        if (bitmap != null) {
            d("AddFoodActivity", "Image captured:" )
            image = bitmap
        }else{
            Log.e("Camera Capture ","Failed to capture image")
        }
    }

    // Launcher for selecting an image from the gallery.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let{
                val contentResolver = context.contentResolver // Get the content resolver
                try {
                    val inputStream = contentResolver.openInputStream(it) // Open the input stream for the selected image
                    image = android.graphics.BitmapFactory.decodeStream(inputStream) // Decode the image
                }catch (e: Exception){
                    Log.e("AddFoodActivity", "Failed to load image", e)
                }
            }
        }
    )

    // Request permission to use camera.
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean->
            if (isGranted) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                cameraLauncher.launch(null) // Launch the camera
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    /* ---------------------------------------------------------------------------------------- */

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

                    // Trim leading and trailing whitespaces
                    searchKey = searchKey.trimStart().trimEnd()
                    if (searchKey == "") {
                        hasSearched = false
                    } else {
                        hasSearched = true
                        viewModel.fetchItems(searchKey)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search button for searching for food.",
                        tint = Color(0xFF03738C)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        /* --------------------------------------------------------------------------- */
        if(viewModel.name != "<Empty>" && viewModel.name != "No item found"){
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
                //Set standard metrics
                LaunchedEffect(viewModel.name) {
                    cals = viewModel.calories!!
                    fats = viewModel.fat!!
                    proteins = viewModel.protein!!
                    carb = viewModel.carbs!!
                }

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

                /* ----------------------{ Modify Serving Size Input }----------------------- */
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    TextField(
                        value = viewModel.servingSize.toString(), // Convert Double to String
                        onValueChange = { text ->
                            // Convert String input back to Double, only if valid
                            val number = text.toDoubleOrNull()
                            if (number != null) {
                                // Update the food values according to the input serving size (by weight)
                                viewModel.servingSize = number
                                viewModel.carbs = BigDecimal(carb * (number / 100)).setScale(2, RoundingMode.HALF_UP).toDouble()
                                viewModel.fat = BigDecimal(fats * (number / 100)).setScale(2, RoundingMode.HALF_UP).toDouble()
                                viewModel.protein = BigDecimal(proteins * (number / 100)).setScale(2, RoundingMode.HALF_UP).toDouble()
                                viewModel.calories = BigDecimal(cals * (number / 100)).setScale(2, RoundingMode.HALF_UP).toDouble()
                            }},
                        label = { Text(text = "Serving size (g)") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(20.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                /* -------------------------------------------------------------------------- */

                /* -------------------------------------------------------------------------- */

                // Display the image Box when an image is captured
                if (showImageBox) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        image?.let { bitmap -> // If an image is captured, display it.
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } ?: run{ // If no image is captured, display a message.
                            Text (
                                text = "No image captured",
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }

                    }
                }

            // If no food item is found we launch an activity allowing for manual input.
            if (viewModel.name == "No item found" && searchKey != "" && hasSearched) {
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

                showDialog = true // Show the dialog that allows for selecting an image.
            },
                modifier = Modifier
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFF03738C)
                )
            }

            // Show dialog when user clicks on the image icon.
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
                                        showImageBox = true

                                        // Check for camera permission.
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            cameraLauncher.launch(null) // Launch the camera
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA) // Request permission
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
                                    showDialog = false
                                    showImageBox = true

                                    galleryLauncher.launch("image/*") // Launch the gallery
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
        }

        // Show the 'Add Food' button if appropriate.
        if (viewModel.name != "No item found" && viewModel.name != "<Empty>") {
            Button(onClick = {
                d("AddFoodActivity", "Button clicked")
                val currentDateToAdd: String = currentDate!!  // Current date passed from intent
                val foodNameToAdd: String =
                    (viewModel.name!!).replaceFirstChar { it.uppercase() }  // Capitalize food name
                val foodCaloriesToAdd: Double = viewModel.calories!!
                val foodFatToAdd: Double = viewModel.fat!!
                val foodProteinToAdd: Double = viewModel.protein!!
                val foodCarbsToAdd: Double = viewModel.carbs!!
                val foodServingsToAdd: Double = viewModel.servingSize!!

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
                    val id: Long = dateWithFoodsDao.insertFood(foodToInsert)

                    // Add image to the firebase database
                    viewModel.addMealToFirebase(image, context, id)
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
