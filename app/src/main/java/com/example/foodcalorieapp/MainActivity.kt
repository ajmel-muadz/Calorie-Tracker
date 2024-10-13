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
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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
                var returnCurrentDate = intent.getStringExtra("RETURN_CURRENT_DATE")
                if (returnCurrentDate != null) {
                    appViewModel.formattedDate = returnCurrentDate
                }
                MainApp(viewModel = appViewModel, dateWithFoodsDao = dao)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: AppViewModel, dateWithFoodsDao: DateWithFoodsDao) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR)
    ) {
        // Composable which contains day switcher
        DaySwitcher(viewModel, dateWithFoodsDao)

        // Button used to add food.
        SearchFoodButton(viewModel)
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
fun DaySwitcher(viewModel: AppViewModel, dateWithFoodsDao: DateWithFoodsDao) {
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(viewModel.formattedDate) {
        scope.launch {
            Log.d("Testing", viewModel.formattedDate)
            Log.d("Testing", dateWithFoodsDao.getFoodsWithDate(viewModel.formattedDate).toString())
        }
    }
}


private fun launchAddFoodActivity(context: Context, viewModel: AppViewModel) {
    val intent = Intent(context, AddFoodActivity::class.java)
    intent.putExtra("CURRENT_DATE", viewModel.formattedDate)
    context.startActivity(intent)
}

@Composable
fun SearchFoodButton(viewModel: AppViewModel) {
    val context = LocalContext.current

    Button(onClick = {
        launchAddFoodActivity(context, viewModel)
    }) {
        Text(text = "Search Food")
    }
}
