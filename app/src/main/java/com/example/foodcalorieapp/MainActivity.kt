// Credits...
/* -------------------------------------------------------------------------------- */
// Below link helped with date and time picker in Jetpack Compose.
// 1. https://medium.com/@droidvikas/exploring-date-and-time-pickers-compose-bytes-120e75349797

// Below link helpedd in converting Java's time to date format.
// 2. https://www.javatpoint.com/getting-date-from-calendar-in-java
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorieapp.ui.theme.FoodCalorieAppTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar


// Constants are defined here.
/* ---------------------------------------------------------- */
val BACKGROUND_COLOUR = Color(0xff2b2b2b)
val LIGHTER_BACKGROUND_COLOUR = Color(0xff3b3b3b)
val DATE_ARROW_ICONS = R.drawable.white_next
/* ---------------------------------------------------------- */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodCalorieAppTheme {
                val appViewModel = AppViewModel()
                MainApp(viewModel = appViewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOUR)
    ) {
        // Composable which contains day switcher
        DaySwitcher(viewModel)
    }
}

@Composable
fun PreviousButton() {
    Button(onClick = { /*TODO*/ },
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
fun NextButton() {
    Button(onClick = { /*TODO*/ },
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
    val context = LocalContext.current

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(LIGHTER_BACKGROUND_COLOUR)
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PreviousButton()

        // Text indicating date. Can be clicked on to show date picker.
        /* -------------------------------------------------------------------------------- */
        Button(
            onClick = {
            showDatePicker = true
        },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = LIGHTER_BACKGROUND_COLOUR)
        ) {
            viewModel.setCurrentDateIfEmpty()  // Default date is current date when app starts.
            Text(text = viewModel.date, fontSize = 20.sp, color = Color.White)
            Toast.makeText(context, "Selected date: ${viewModel.date}", Toast.LENGTH_SHORT).show()
        }
        /* -------------------------------------------------------------------------------- */

        NextButton()

        // Date picker component
        /* -------------------------------------------------------------------------------- */
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { /*TODO*/ },
                confirmButton = { 
                    TextButton(onClick = {
                        val selectedDate = Calendar.getInstance().apply {
                            timeInMillis = datePickerState.selectedDateMillis!!
                        }
                        val date = selectedDate.time
                        val dateFormat = SimpleDateFormat.getDateInstance()
                        val formattedDate = dateFormat.format(date)
                        showDatePicker = false
                        viewModel.date = formattedDate
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
    }
}