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
//
// Below link helped make the expandable card to display more details.
// 6. https://developer.android.com/codelabs/jetpack-compose-basics#6
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.ColorFilter


// Constants are defined here.
/* ---------------------------------------------------------- */
val BACKGROUND_COLOUR = Color(0xff2b2b2b)
val LIGHTER_BACKGROUND_COLOUR = Color(0xff3b3b3b)
val DATE_ARROW_ICONS = R.drawable.white_next
/* ---------------------------------------------------------- */

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting up the appViewModel with current Context
        appViewModel.setContext(this)

        // Setting up the database and Dao
        val dao = AppDatabase.getInstance(this).dateWithFoodsDao


        // Setting up the UI components for the app
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
    val context = LocalContext.current;

    viewModel.refreshDailySummary() // Refresh the daily summary
    var totalCalories by remember { mutableStateOf(0.0) } // State variable to hold total calories

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Composable which contains day switcher
        DaySwitcher(viewModel)

        Spacer(modifier = Modifier.height(5.dp))  // Add some space between date switcher and list.


        // This code block is responsible for updating the total calories displayed.
        /* ----------------------------------------------------------------------------- */
        LaunchedEffect(viewModel.formattedDate) {
            // Refresh the total calories displayed to match the current date's food
            scope.launch {
                val foodsForDate = dateWithFoodsDao.getFoodsWithDate(viewModel.formattedDate)
                totalCalories = foodsForDate.sumOf { it.calories }
            }
        }


        // This code block is responsible for displaying a summary of food details.

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
                val foodMealType = food.mealType
                val foodId = food.id


                Log.d("FoodLog", "Food Name: $foodName, Calories: $foodCalories, Fat: $foodFat, Protein: $foodProtein, Carbs: $foodCarbs, ID: $foodId")

                val foodToAdd = FoodDisplay(name = foodName, 100.0, calories = foodCalories,
                    fat = foodFat, protein = foodProtein, carbs = foodCarbs, mealType = foodMealType, id = foodId)

                foodsToDisplay.add(foodToAdd)
            }


            FoodList(
                foodDisplays = foodsToDisplay,
                modifier = Modifier.weight(1f),
                onEditClicked = { handleEditFood(it, context, dateWithFoodsDao, viewModel) },
                onDeleteClicked = { handleDeleteFood(it, context, scope, dateWithFoodsDao, viewModel) },
                viewModel
            )
        }
        /* ----------------------------------------------------------------------------- */



        SummaryCard(viewModel = viewModel) // Displaying the summary card then a spacer below it.

        Spacer(modifier = Modifier.height(5.dp))

        // Button used to add food.
        SearchFoodButton(viewModel)
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun SingleFood(foodDisplay: FoodDisplay,
               onEditClicked: (FoodDisplay) -> Unit,
               onDeleteClicked: (FoodDisplay) -> Unit,
               viewModel: AppViewModel) {

    var decodedImage by remember { mutableStateOf<Bitmap?>(null) } // State variable to hold decoded image
    var loadingImage by remember { mutableStateOf(false) } // State variable to control image loading

    val expanded = remember { mutableStateOf(false) } // State variable to control expanded state

    // Expand animation for the card.
    val extraPadding by animateDpAsState(
        if (expanded.value) 20.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(5)
    ) {
        Column(

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = extraPadding.coerceAtLeast(0.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp)
                ) {
                    if (!expanded.value) {
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
                    } else {
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

                        // Display food macros.
                        Text(
                            text = "${foodDisplay.fat}g fat",
                            fontSize = 12.sp
                        )

                        // Display food macros.
                        Text(
                            text = "${foodDisplay.protein}g protein",
                            fontSize = 12.sp
                        )

                        // Display food macros.
                        Text(
                            text = "${foodDisplay.carbs}g carbs",
                            fontSize = 12.sp
                        )

                        // Display meal type.
                        Text(
                            text = "Meal type: ${foodDisplay.mealType}",
                            fontSize = 12.sp
                        )
                    }
                }

                // Row for icons
                Row(
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { onEditClicked(foodDisplay) }, // Clickable icon for editing a food.
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit a food's properties.",
                            tint = Color(0xFF03738C)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { onDeleteClicked(foodDisplay) }, // Clickable icon for deleting a food.
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete a food from the day's log.",
                            tint = Color(0xFF03738C)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                    ) {
                        IconButton(onClick = {
                            if (expanded.value) {
                                expanded.value = false
                            } else {
                                expanded.value = true
                            }
                        }) {
                            // Icon for expanding the card.
                            if (expanded.value) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Search button for searching for food.",
                                    tint = Color(0xFF03738C)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Search button for searching for food.",
                                    tint = Color(0xFF03738C)
                                )
                            }
                        }
                    }
                }
            }
            if (expanded.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    LaunchedEffect(foodDisplay.id) {
                        Log.d("FoodLog", "Food ID: ${foodDisplay.id}")

                        loadingImage = true // Set loadingImage to true before decoding the image

                        val imageString: String? = viewModel.getMealImageById(foodDisplay.id) // Get the image string from the firebase storage

                        delay(1000)

                        // Decode the image string into a Bitmap
                        if (!imageString.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                            decodedImage =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        }

                        loadingImage = false // Set loadingImage to false after decoding the image
                    }
                    // Display a loading indicator while the image is being decoded
                     if (loadingImage) {
                            CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Display the image if it's not null
                        if (decodedImage != null) {
                            Image(
                                bitmap = decodedImage!!.asImageBitmap(),
                                contentDescription = "Captured Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(2f)
                            )
                        } else {
                            // Display a placeholder or loading indicator
                            Text(
                                text = "No Image Found",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun FoodList(foodDisplays: List<FoodDisplay>, modifier: Modifier = Modifier,
             onEditClicked: (FoodDisplay) -> Unit, onDeleteClicked: (FoodDisplay) -> Unit,
             viewModel: AppViewModel) {
    LazyColumn(modifier = modifier) {
        items(foodDisplays) { foodDisplay: FoodDisplay ->
            SingleFood(

                foodDisplay = foodDisplay,
                onEditClicked = onEditClicked,
                onDeleteClicked = onDeleteClicked,
                viewModel
            )
        }
    }
}


/* Method: handleEditFood
 * Description: Handles the editing of a food item.
 */
fun handleEditFood(foodDisplay: FoodDisplay, context: Context, dateWithFoodsDao: DateWithFoodsDao, viewModel: AppViewModel) {

    // Get the selected date
    val selectedDateString = viewModel.formattedDate

    // Launch a coroutine to perform the database operation in the background
    CoroutineScope(Dispatchers.Main).launch {

        // Retrieving the Food item using the name and the selected date
        val foodList = dateWithFoodsDao.getFoodsWithDate(selectedDateString)
        val foodToEdit = foodList.find { it.name == foodDisplay.name }

        // Handle the result of the database operation
        foodToEdit?.let {
            launchEditFoodActivity(context, it, viewModel)
        } ?: run {
            Toast.makeText(context, "Food not found!", Toast.LENGTH_SHORT).show()
        }
    }
}


/* Method: handleDeleteFood
 * Description: Handles the deletion of a food item.
 * Params: foodDisplay - The food item to be deleted.
 *         context - The application context.
 *         viewModelScope - The coroutine scope for the ViewModel.
 *         dateWithFoodsDao - The DAO for interacting with the database.
 *         viewModel - The ViewModel instance.
 */
fun handleDeleteFood(
    foodDisplay: FoodDisplay,
    context: Context,
    viewModelScope: CoroutineScope,
    dateWithFoodsDao: DateWithFoodsDao,
    viewModel: AppViewModel
) {
    // Get the selected date
    val selectedDateString = viewModel.formattedDate

    viewModelScope.launch {
        // Retrieve the list of foods for the selected date
        val foodList = dateWithFoodsDao.getFoodsWithDate(selectedDateString)
        val foodToDelete = foodList.find { it.id == foodDisplay.id }

        foodToDelete?.let {

            // Delete the image from Firebase Storage
            viewModel.deleteMealImageFromFirebase(it.id, context)

            // Delete the food from the database
            dateWithFoodsDao.deleteFood(it)
            Toast.makeText(context, "Food deleted!", Toast.LENGTH_SHORT).show()

            viewModel.refreshDailySummary()

            // Check if there are any remaining foods for this date
            val remainingFoods = dateWithFoodsDao.getFoodsWithDate(selectedDateString)
            if (remainingFoods.isEmpty()) {
                // If no more foods are left, delete the date entry as well
                dateWithFoodsDao.deleteDate(Date(selectedDateString))

                // Check if there are any remaining foods in the entire table
                val allFoods = dateWithFoodsDao.getAllFoods()
                if (allFoods.isEmpty()) {
                    // Reset the auto-increment ID counter
                    dateWithFoodsDao.resetFoodIdCounter()
                }
            }

            // Refresh the foods list
            refreshFoodsList(viewModel, dateWithFoodsDao, selectedDateString)
            restartMainActivity(context, selectedDateString, viewModel.calendarDate.timeInMillis)

            // Show a toast message for exceptions
        } ?: run {
            Toast.makeText(context, "Food not found!", Toast.LENGTH_SHORT).show()
        }
    }
}


/* Method: refreshFoodsList
 * Description: Refreshes the list of foods for a specific date.
 * Params: viewModel - The ViewModel instance.
 *         dateWithFoodsDao - The DAO for interacting with the database.
 *         date - The date for which to refresh the list.
 */
fun refreshFoodsList(viewModel: AppViewModel, dateWithFoodsDao: DateWithFoodsDao, date: String) {
    viewModel.viewModelScope.launch {
        // Refresh the foods list from the database
        val updatedFoods = dateWithFoodsDao.getFoodsWithDate(date)
        // Update the ViewModel's data
        viewModel.updateFoodsList(updatedFoods)
    }
}


/* Method: restartMainActivity
 * Description: Restarts the main activity with the updated date.
 * Params: context - The application context.
 *         date - The updated date.
 */
private fun restartMainActivity(context: Context, date: String, dateInMillis: Long) {

    // Create an intent to restart the main activity
    val intent = Intent(context, MainActivity::class.java).apply {
        // Adding the correct date and dateInMillis to the intent
        putExtra("RETURN_CURRENT_DATE", date)
        putExtra("RETURN_CURRENT_DATE_TIME_IN_MILLIS", dateInMillis)

    }
    context.startActivity(intent) // Start the activity
}



/* Method: launchEditFoodActivity
 * Description: Launches the edit food activity.
 * Params: context - The application context.
 *         food - The food item to be edited.
 *         viewModel - The ViewModel instance.
 */
private fun launchEditFoodActivity(context: Context, food: Food, viewModel: AppViewModel) {
    // Create an intent to launch the edit food activity
    val intent = Intent(context, EditFoodActivity::class.java).apply {
        // Adding the food details to the intent
        putExtra("FOOD_ID", food.id)
        putExtra("FOOD_NAME", food.name)
        putExtra("FOOD_CALORIES", food.calories)
        putExtra("FOOD_FAT", food.fat)
        putExtra("FOOD_PROTEIN", food.protein)
        putExtra("FOOD_CARBS", food.carbs)
        putExtra("CURRENT_DATE_STRING", food.dateString)
        putExtra("CURRENT_DATE_TIME_IN_MILLIS", viewModel.calendarDate.timeInMillis)
    }

    context.startActivity(intent) // Start the activity
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
            colorFilter = ColorFilter.tint(Color(0xFF03738C)),
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
            colorFilter = ColorFilter.tint(Color(0xFF03738C)),
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

private fun launchAddFoodActivity(context: Context, viewModel: AppViewModel) {
    val intent = Intent(context, AddFoodActivity::class.java)
    intent.putExtra("CURRENT_DATE", viewModel.formattedDate)
    intent.putExtra("CURRENT_DATE_TIME_IN_MILLIS", viewModel.calendarDate.timeInMillis)
    context.startActivity(intent)
}



/* Method: SummaryCard
 * Description: Displays a summary card with nutritional goals.
 */
@Composable
fun SummaryCard(viewModel: AppViewModel) {
    var isExpanded by remember { mutableStateOf(false) } // State to track expansion
    var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility
    val context = LocalContext.current

    val defaultTextColor = Color.Black
    val goalMetColor = Color(0xFF418341)

    // Card for displaying nutritional goals and summary
    ElevatedCard(
        // Styling
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded }, // Toggle expansion on click
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB9B9B9))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Daily Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = {
                    showDialog = true // Open the dialog when clicked
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Daily Goal",
                        tint = Color(0xFF03738C)
                    )
                }
            }

            // Display the main calories summary
            Text(
                text = "Calories: ${String.format("%.2f", viewModel.totalCalories)} / ${String.format("%.2f", viewModel.caloriesGoal)} kcal",
                fontSize = 16.sp,
                        color = if (viewModel.totalCalories >= viewModel.caloriesGoal) goalMetColor else defaultTextColor
            )


            if (isExpanded) {
                // Show expanded details
                Column {
                    Text(
                        text = "${String.format("%.2f", viewModel.totalFat)}g fat / ${String.format("%.2f", viewModel.fatGoal)}g",
                        fontSize = 14.sp,
                        color = if (viewModel.totalFat >= viewModel.fatGoal) goalMetColor else defaultTextColor
                    )
                    Text(
                        text = "${String.format("%.2f", viewModel.totalProtein)}g protein / ${String.format("%.2f", viewModel.proteinGoal)}g",
                        fontSize = 14.sp,
                        color = if (viewModel.totalProtein >= viewModel.proteinGoal) goalMetColor else defaultTextColor
                    )
                    Text(
                        text = "${String.format("%.2f", viewModel.totalCarbs)}g carbs / ${String.format("%.2f", viewModel.carbGoal)}g",
                        fontSize = 14.sp,
                        color = if (viewModel.totalCarbs >= viewModel.carbGoal) goalMetColor else defaultTextColor
                    )
                }
            }

            // Arrow icon to indicate expand/collapse state
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle More Info",
                tint = Color(0xFF03738C),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isExpanded = !isExpanded } // Toggle the state
                    .align(Alignment.CenterHorizontally)
            )
        }
    }

    // Show the edit dialog if showDialog is true (will be set to true when the edit icon is clicked)
    if (showDialog) {
        EditGoalDialog(
            // Pass the ViewModel to the dialog
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onSave = {
                showDialog = false
                viewModel.refreshDailySummary() // Refresh summary after saving goals
            }
        )
    }

}

/* Method: EditGoalDialog
 * Description: Displays a dialog for editing nutritional goals.
 * Params: viewModel - The ViewModel instance.
 *         onDismiss - Callback to be invoked when the dialog is dismissed.
 *         onSave - Callback to be invoked when the save button is clicked.
 */
@Composable
fun EditGoalDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {

    // State variables for the dialog's text fields
    var newCalorieGoal by remember { mutableStateOf(viewModel.caloriesGoal.toString()) }
    var newFatGoal by remember { mutableStateOf(viewModel.fatGoal.toString()) }
    var newProteinGoal by remember { mutableStateOf(viewModel.proteinGoal.toString()) }
    var newCarbGoal by remember { mutableStateOf(viewModel.carbGoal.toString()) }

    // AlertDialog for editing nutritional goals
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Nutritional Goals") },
        text = {
            Column {
                TextField(
                    value = newCalorieGoal,
                    onValueChange = { newCalorieGoal = it },
                    label = { Text("Calorie Goal (kcal)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = newFatGoal,
                    onValueChange = { newFatGoal = it },
                    label = { Text("Fat Goal (g)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = newProteinGoal,
                    onValueChange = { newProteinGoal = it },
                    label = { Text("Protein Goal (g)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = newCarbGoal,
                    onValueChange = { newCarbGoal = it },
                    label = { Text("Carb Goal (g)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { // Save button
            Button(onClick = {
                // Save to ViewModel and Database
                viewModel.updateNutritionalGoals(
                    newCalorieGoal.toDoubleOrNull() ?: viewModel.caloriesGoal,
                    newFatGoal.toDoubleOrNull() ?: viewModel.fatGoal,
                    newProteinGoal.toDoubleOrNull() ?: viewModel.proteinGoal,
                    newCarbGoal.toDoubleOrNull() ?: viewModel.carbGoal
                )
                onSave() // Close dialog and refresh UI
            }) {
                Text("Save")
            }
        },
        // Cancel button
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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