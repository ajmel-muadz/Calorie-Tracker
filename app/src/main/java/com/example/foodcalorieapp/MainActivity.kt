// Credits...
/* -------------------------------------------------------------------------------- */
// Below link helped with date and time picker in Jetpack Compose.
// 1. https://medium.com/@droidvikas/exploring-date-and-time-pickers-compose-bytes-120e75349797

// Below link helped in converting Java's time to date format.
// 2. https://www.javatpoint.com/getting-date-from-calendar-in-java

// Below link helped with updating title in Date Picker when using previous/next arrows.
// 3. https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DatePicker(androidx.compose.material3.DatePickerState,androidx.compose.ui.Modifier,androidx.compose.material3.DatePickerFormatter,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.material3.DatePickerColors)

// Below link helped with using suspend functions in composables not tied to a button click.
// 4. https://developer.android.com/develop/ui/compose/side-effects#launchedeffect

// Below link helped me with prompting the image app and returning the image uri, triggered after clicking the image icon.
// 5. https://medium.com/@dheerubhadoria/capturing-images-from-camera-in-android-with-jetpack-compose-a-step-by-step-guide-64cd7f52e5de
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import coil.annotation.ExperimentalCoilApi
import java.io.File
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import android.Manifest
import android.os.Environment
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context


// Constants are defined here.
/* ---------------------------------------------------------- */
val BACKGROUND_COLOUR = Color(0xff2b2b2b)
val LIGHTER_BACKGROUND_COLOUR = Color(0xff3b3b3b)
val DATE_ARROW_ICONS = R.drawable.white_next
/* ---------------------------------------------------------- */

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val dao = AppDatabase.getInstance(this).dateWithFoodsDao
        super.onCreate(savedInstanceState)
        setContent {
            FoodCalorieAppTheme {
                val returnCurrentDate = intent.getStringExtra("RETURN_CURRENT_DATE")
                val returnCurrentDateTimeInMillis = intent.getLongExtra("RETURN_CURRENT_DATE_TIME_IN_MILLIS", 0)
                if (returnCurrentDate != null) {
                    appViewModel.formattedDate = returnCurrentDate
                    appViewModel.calendarDate.timeInMillis = returnCurrentDateTimeInMillis
                }
                MainApp(viewModel = appViewModel, dateWithFoodsDao = dao)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: AppViewModel, dateWithFoodsDao: DateWithFoodsDao) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Composable which contains day switcher
        DaySwitcher(viewModel)
        
        Spacer(modifier = Modifier.height(5.dp))  // Add some space between date switcher and list.

        // This code block is responsible for displaying the foods in a list.
        /* ----------------------------------------------------------------------------- */
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            val foodsToDisplay = mutableListOf<FoodDisplay>()
            var foodsList by remember { mutableStateOf<List<Food>>(emptyList()) }
            LaunchedEffect(viewModel.formattedDate) {
                scope.launch {
                    val foodsRetrieved = dateWithFoodsDao.getFoodsWithDate(viewModel.formattedDate)
                    foodsList = foodsRetrieved
                }
            }
            for (food in foodsList) {
                val foodName = food.name
                val foodCalories = food.calories
                val foodFat = food.fat
                val foodProtein = food.protein
                val foodCarbs = food.carbs


                val foodToAdd = FoodDisplay(name = foodName, 100.0, calories = foodCalories,
                    fat = foodFat, protein = foodProtein, carbs = foodCarbs)

                foodsToDisplay.add(foodToAdd)
            }

            FoodList(foodDisplays = foodsToDisplay, modifier = Modifier.weight(1f))
        }
        /* ----------------------------------------------------------------------------- */

        // Button used to add food.
        SearchFoodButton(viewModel)
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun SingleFood(foodDisplay: FoodDisplay) {

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(20)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Display food name.
                Text(
                    text = foodDisplay.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Display food macros.
                Text(
                    text = "${foodDisplay.calories} kcal",
                    fontSize = 12.sp
                )
            }

            // Row for icons
            Row(
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit a food's properties.",
                    )
                }
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete a food from the day's log.",
                    )
                }
            }
        }
    }
}


@Composable
fun FoodList(foodDisplays: List<FoodDisplay>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(foodDisplays) { foodDisplay: FoodDisplay ->
            SingleFood(foodDisplay = foodDisplay)
        }
    }
}

@Composable
fun PreviousDay(viewModel: AppViewModel) {
    Button(onClick = {
        viewModel.decrementDate()
    },
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = LIGHTER_BACKGROUND_COLOUR)
    ) {
        Image(
            painter = painterResource(id = DATE_ARROW_ICONS),
            contentDescription = "Button to go to the previous day",
            modifier = Modifier
                .size(25.dp)
                .scale(scaleX = -1f, scaleY = 1f)  // Responsible for 'mirroring' icon.
        )
    }
}

@Composable
fun NextDay(viewModel: AppViewModel) {
    Button(onClick = {
        viewModel.incrementDate()
    },
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = LIGHTER_BACKGROUND_COLOUR)
    ) {
        Image(
            painter = painterResource(id = DATE_ARROW_ICONS),
            contentDescription = "Button to go to the next day",
            modifier = Modifier
                .size(25.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySwitcher(viewModel: AppViewModel) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.calendarDate.timeInMillis)
    var showDatePicker by remember { mutableStateOf(false) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(LIGHTER_BACKGROUND_COLOUR)
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PreviousDay(viewModel)

        // Text indicating date. Can be clicked on to show date picker.
        /* -------------------------------------------------------------------------------- */
        Button(
            onClick = {
            showDatePicker = true
        },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = LIGHTER_BACKGROUND_COLOUR)
        ) {
            Text(text = viewModel.formattedDate, fontSize = 20.sp, color = Color.White)
        }
        /* -------------------------------------------------------------------------------- */

        NextDay(viewModel)

        // Date picker component
        /* -------------------------------------------------------------------------------- */
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.calendarDate.timeInMillis = datePickerState.selectedDateMillis!!
                        val formattedDate = SimpleDateFormat.getDateInstance().format(viewModel.calendarDate.timeInMillis)
                        viewModel.formattedDate = formattedDate
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        /* -------------------------------------------------------------------------------- */

        // This one line is responsible for allowing the date in the date picker to be
        // synchronised with when the arrow is clicked.
        datePickerState.selectedDateMillis = viewModel.calendarDate.timeInMillis
    }
}

fun Context.createImageFile(name: String?): File {
    // create or get a directory to store the images
    val imageDirectory = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "food_images")

    // If the directory doesn't exist, create it
    if (!imageDirectory.exists()) {
        imageDirectory.mkdirs()  // Create the directory
    }

    val image = File.createTempFile(
        name,
        ".jpg",
        externalCacheDir
    )
    return image
}

private fun launchAddFoodActivity(context: Context, viewModel: AppViewModel) {
    val intent = Intent(context, AddFoodActivity::class.java)
    intent.putExtra("CURRENT_DATE", viewModel.formattedDate)
    intent.putExtra("CURRENT_DATE_TIME_IN_MILLIS", viewModel.calendarDate.timeInMillis)
    context.startActivity(intent)
}

@Composable
fun SearchFoodButton(viewModel: AppViewModel) {
    val context = LocalContext.current

    Button(onClick = {
        launchAddFoodActivity(context, viewModel)
    }, modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = "Search Food")
    }
}



// Similarly to the other Previews, trying to make a mock preview for this part
@Composable
fun MockAppViewModel(): AppViewModel {
    return AppViewModel().apply {
        // Manually set any properties you want to simulate in the preview
        formattedDate = "2024-10-18"
        calendarDate.timeInMillis = System.currentTimeMillis()

        // Set mock data directly if needed
        name = "Sample Food"
        calories = 150.0
        fat = 10.0
        protein = 5.0
        carbs = 20.0
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainAppWithMockData() {
    // Create a mock ViewModel
    val mockViewModel = MockAppViewModel().apply {
        formattedDate = "2024-10-18"
    }

    // The Mock Dao does not work for some reason however this should be enough
    val mockDao = object : DateWithFoodsDao {
        override suspend fun getFoodsWithDate(dateString: String): List<Food> {
            return listOf(
                Food(name = "Apple", calories = 95.0, fat = 0.3, protein = 0.5, carbs = 25.0, dateString = dateString),
                Food(name = "Banana", calories = 105.0, fat = 0.4, protein = 1.3, carbs = 27.0, dateString = dateString)
            )
        }

        override suspend fun insertDate(date: Date) {
            // No operation needed for preview
        }

        override suspend fun insertFood(food: Food): Long {
            // No operation needed for preview
            return 1
        }
    }

     // Use the existing MainApp but inject mock data via the mock DAO
    MainApp(
        viewModel = mockViewModel,
        dateWithFoodsDao = mockDao
    )
}


