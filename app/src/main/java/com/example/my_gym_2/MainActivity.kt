package com.example.my_gym_2

import android.content.Intent
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.my_gym_2.ui.theme.My_Gym_2Theme
import com.example.my_gym_2.ui.controls.DoubleStepper
import com.example.my_gym_2.ui.controls.NumberStepper
import com.example.my_gym_2.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import com.example.my_gym_2.ui.theme.ProgressScreen
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color


val calendarInstance = Calendar.getInstance()
val currentDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK)
val myWorkoutOptionsPush = listOf("Push Day", "Pull Day", "Leg Day", "Rest Day", "X")
val myWorkoutOptionsPull = listOf( "Pull Day", "Leg Day", "Rest Day","Push Day","X")
val myWorkoutOptionsLeg = listOf( "Leg Day", "Rest Day","Push Day", "Pull Day","X")
val myWorkoutOptionsRest = listOf( "Rest Day","Push Day", "Pull Day", "Leg Day","x")

public var currentWorkout by mutableStateOf(
    if (currentDayOfWeek == 2 || currentDayOfWeek == 6) myWorkoutOptionsPush
    else if (currentDayOfWeek == 3 || currentDayOfWeek == 7) myWorkoutOptionsPull
    else if (currentDayOfWeek == 4) myWorkoutOptionsLeg // Assuming currentDay is defined elsewhere
    else myWorkoutOptionsRest)

val puchWorkoutList= listOf(Workout("Bench Press",50.0,6,"bench_press.jpg"),
    Workout("Incline Bench Press",50.0,6,"incline_bench_press.jpg"),
    Workout("Chest Butterfly",30.0,8,"chest_butterfly.jpg"),
    Workout("Dip",0.0,6,"dip.jpg"),
    Workout("Pushdown",30.0,8,"pushdown.jpg"),
    Workout("Shoulder Press",15.0,8,"shoulder_press.jpg"),
    Workout("Lateral Raise",15.0,8,"lateral_raise.jpg"),)

val pullWorkoutList= listOf(Workout("Pull Ups",0.0,6,"pull_up.jpg"),
    Workout("Dumbell Row",20.0,6,"dumbell_row.jpg"),
    Workout("Cable Pulldown",40.0,6,"cable_pulldown.jpg"),
    Workout("Shrugs",0.0,6,"shrugs.jpg"),
    Workout("Biceps Curl",0.0,6,"biceps_curl.jpg"),)

val legWorkoutList= listOf(Workout("Romenian Deadlift",70.0,6,"romanian_deadlift.jpg"),
    Workout("Leg Press",70.0,6,"leg_press.jpg"),
    Workout("Leg Extension",30.0,8,"leg_extention.jpg"),
    Workout("Calf Raise",0.0,15,"calf_raise.jpg"),
    Workout("Abs",0.0,0,"plank.jpg"),)

const val PREF_LAST_RESET_DATE = "last_reset_date"
fun getCheckedPreferenceKey(workoutName: String) = "isChecked_$workoutName"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //val User = UsersParameters("Tymek", 20, 180.0, 71.0) // User is not used

        // Check if a new day has started and reset if necessary
        val sharedPreferences = getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE)
        val lastResetDate = sharedPreferences.getString(PREF_LAST_RESET_DATE, "")
        val currentDate = SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        if (lastResetDate != currentDate) {
            resetAllCheckedStates(this)
            sharedPreferences.edit().putString(PREF_LAST_RESET_DATE, currentDate).apply()
        }

        enableEdgeToEdge()
        setContent {
            My_Gym_2Theme {
                val workout = if (currentWorkout==myWorkoutOptionsPush)puchWorkoutList else if (currentWorkout==myWorkoutOptionsPull)pullWorkoutList else legWorkoutList
                val allExercises = (puchWorkoutList + pullWorkoutList + legWorkoutList).map { it.name }.distinct()
                var showProgressScreen by remember { mutableStateOf(false) }
                val context = LocalContext.current

                if (showProgressScreen) {
                    ProgressScreen(allExercises = allExercises, onBack = { showProgressScreen = false })

                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        LazyColumn(modifier = Modifier.padding(padding)) {
                            item {
                                MyButtonList(
                                    buttonLabels = currentWorkout,
                                    onButtonClick = { index, label -> Log.d("Button Click", "Index: $index, Label: $label") }
                                )
                            }
                            item {
                                Button(onClick = { showProgressScreen = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text("View Progress")
                                }
                            }
                            items(workout) {
                                if (currentWorkout == myWorkoutOptionsPush) {
                                    PushDay(it)
                                } else if (currentWorkout == myWorkoutOptionsPull) {
                                    PullDay(it)
                                } else if (currentWorkout == myWorkoutOptionsLeg) {
                                    LegDay(it)
                                } else {
                                    Spacer(Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun resetAllCheckedStates(context: Context) {
    val sharedPreferences = context.getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    puchWorkoutList.forEach { workout ->
        editor.putBoolean(getCheckedPreferenceKey(workout.name), false)
    }
    pullWorkoutList.forEach { workout ->
        editor.putBoolean(getCheckedPreferenceKey(workout.name), false)
    }
    legWorkoutList.forEach { workout ->
        editor.putBoolean(getCheckedPreferenceKey(workout.name), false)
    }
    editor.apply()
}





enum class WorkoutType { PUSH, PULL, LEG }

// --- 2) Generic composable used by all days ---
@Composable
fun WorkoutDay(
    workoutType: WorkoutType,
    workout: Workout
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    // --- Variant state ---
    val variants = exerciseVariants[workout.name].orEmpty()
    val baseKey = "variant_choice_${workout.name}"
    var currentIndex by rememberSaveable(workout.name) {
        mutableStateOf(prefs.getInt(baseKey, 0).coerceIn(0, (variants.size - 1).coerceAtLeast(0)))
    }
    val currentVariant = if (variants.isNotEmpty()) variants[currentIndex] else null
    var currentName by rememberSaveable(workout.name) { mutableStateOf(currentVariant?.name ?: workout.name) }
    var currentImage by rememberSaveable(workout.image) { mutableStateOf(currentVariant?.image ?: workout.image) }

    fun setVariant(newIndex: Int) {
        val safe = newIndex.coerceIn(0, (variants.size - 1).coerceAtLeast(0))
        currentIndex = safe
        val v = variants.getOrNull(safe)
        currentName = v?.name ?: workout.name
        currentImage = v?.image ?: workout.image
        prefs.edit().putInt(baseKey, safe).apply()
    }

    // --- Swipe gesture on the image ---
    // left swipe -> next variant, right swipe -> previous variant
    val SWIPE_THRESHOLD = 48f
    var accumDx by remember { mutableStateOf(0f) }

    val imageSwipeModifier = Modifier
        .pointerInput(variants, currentIndex) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    when {
                        accumDx <= -SWIPE_THRESHOLD && variants.size > 1 -> setVariant((currentIndex + 1) % variants.size)
                        accumDx >=  SWIPE_THRESHOLD && variants.size > 1 -> setVariant((currentIndex - 1 + variants.size) % variants.size)
                    }
                    accumDx = 0f
                },
                onHorizontalDrag = { _, dragAmount ->
                    accumDx += dragAmount
                }
            )
        }
        .then(Modifier) // keep chaining if you add more modifiers

    // --- Checkbox state keyed by *currentName* so each variant has its own check ---
    fun checkedKeyFor(name: String) = getCheckedPreferenceKey(name)
    var isChecked by rememberSaveable(currentName) {
        mutableStateOf(prefs.getBoolean(checkedKeyFor(currentName), false))
    }

    // --- Weight / reps state keyed by *currentName* to prefill per-variant ---
    var weight by rememberSaveable(currentName) { mutableStateOf(workout.weight) }
    var reps by rememberSaveable(currentName) { mutableStateOf(workout.reps) }

    // --- DAO, date, prefill last log for current variant ---
    val dao = remember { DatabaseProvider.get(context).workoutLogDao() }
    val date = remember { SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }

    LaunchedEffect(currentName) {
        withContext(Dispatchers.IO) {
            dao.getLastLogForExercise(currentName)?.let { last ->
                withContext(Dispatchers.Main) {
                    weight = last.weight
                    reps = last.reps
                }
            }
        }
    }

    // ====== UI (only the image line changes: add imageSwipeModifier; use currentName/currentImage everywhere) ======
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                // 👇 Swipeable image
                Box {
                    WorkoutImage(currentImage)

                    // swipe modifier layer (as you already had)
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .then(imageSwipeModifier)
                    )

                    // 👇 Dots indicator
                    if (variants.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            variants.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentIndex) MaterialTheme.colorScheme.primary
                                            else Color.LightGray
                                        )
                                )
                            }
                        }
                    }
                }


                Spacer(Modifier.width(8.dp))

                Column(Modifier.weight(1f)) {
                    Text(currentName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

                    DoubleStepper("Weight", weight, { weight = it })
                    Spacer(Modifier.height(8.dp))
                    NumberStepper("Reps", reps, {reps = it })
                }

                Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.TopEnd) {
                    Image(
                        painter = painterResource(id = if (isChecked) R.drawable.check_yes else R.drawable.check),
                        contentDescription = if (isChecked) "Checked" else "Unchecked",
                        modifier = Modifier
                            .clickable {
                                val newChecked = !isChecked
                                isChecked = newChecked
                                prefs.edit().putBoolean(checkedKeyFor(currentName), newChecked).apply()

                                // DB write/delete as before, but use currentName
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        if (newChecked) {
                                            val existing = dao.getLogByExerciseAndDate(currentName, date)
                                            if (existing != null && (existing.weight != weight || existing.reps != reps)) {
                                                dao.deleteLogByExerciseAndDate(existing.workoutName, existing.date)
                                            } else if (existing != null) return@launch
                                            dao.insertLog(
                                                WorkoutProgressLogs(
                                                    id = 0,
                                                    workoutName = currentName,
                                                    weight = weight,
                                                    reps = reps,
                                                    date = date
                                                )
                                            )
                                        } else {
                                            dao.deleteLogByExerciseAndDate(currentName, date)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e(workoutType.name, "DB error: ${e.message}")
                                    }
                                }
                            }
                            .padding(top = 8.dp)
                    )
                }
            }

        }
    }
}


// --- 3) Thin wrappers for your screens ---
@Composable fun PushDay(workout: Workout) = WorkoutDay(WorkoutType.PUSH, workout)
@Composable fun PullDay(workout: Workout) = WorkoutDay(WorkoutType.PULL, workout)
@Composable fun LegDay(workout: Workout)  = WorkoutDay(WorkoutType.LEG,  workout)


@Composable
fun MyButtonList(
    buttonLabels: List<String>,
    onButtonClick: (index: Int, label: String) -> Unit // Our function type
) {
    var expanded by remember { mutableStateOf(false) }
    val itemsToShow = if (expanded) buttonLabels else buttonLabels.take(1)

    Box(
        modifier = Modifier.fillMaxSize(), // Fill the whole screen
        contentAlignment = Alignment.TopCenter // Align content to the top center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp) // Add some padding from the top
        ) {
            itemsToShow.forEachIndexed { index, label ->
                Button(onClick = {
                    if (!expanded) {
                        expanded = true
                    } else {
                        val originalIndex = buttonLabels.indexOf(label)
                        if (originalIndex != -1) {
                            onButtonClick(originalIndex, label)
                            expanded = false
                            when (label) {
                                "Push Day" -> currentWorkout = myWorkoutOptionsPush
                                "Pull Day" -> currentWorkout = myWorkoutOptionsPull
                                "Leg Day" -> currentWorkout = myWorkoutOptionsLeg
                                "Rest Day" -> currentWorkout = myWorkoutOptionsRest
                            }
                        }
                    }
                }) {
                    Text(label)
                }
            }
        }
    }
}
@Composable
fun WorkoutImage(name:String){
    val imageResource = when (name) {
        //Push
        "bench_press.jpg" -> R.drawable.bench_press
        "incline_bench_press.jpg" -> R.drawable.incline_bench_press
        "chest_butterfly.jpg" -> R.drawable.chest_butterfly
        "lateral_raise.jpg" -> R.drawable.lateral_raise
        "pushdown.jpg" -> R.drawable.pushdown
        "shoulder_press.jpg" -> R.drawable.shoulder_press
        "dip.jpg" -> R.drawable.dip
        "bench_press_dumbbell.jpg" -> R.drawable.bench_press_dumbbell
        //Legs
        "romanian_deadlift.jpg" -> R.drawable.romanian_deadlift
        "leg_press.jpg" -> R.drawable.leg_press
        "leg_extention.jpg" -> R.drawable.leg_extention
        "calf_raise.jpg" -> R.drawable.calf_raise
        "plank.jpg" -> R.drawable.plank
        //Pull
        "pull_up.jpg" -> R.drawable.pull_up
        "dumbell_row.jpg" -> R.drawable.dumbell_row
        "cable_pulldown.jpg" -> R.drawable.cable_pulldown
        "shrugs.jpg" -> R.drawable.shrugs
        "biceps_curl.jpg" -> R.drawable.biceps_curl
        else -> R.drawable.ic_launcher_background // Default image or handle error
    }
    Image(
        //painter = painterResource(id = imageResource as Int), // Unsafe cast
        painter = painterResource(id = imageResource ),
        contentDescription = null,
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
    )
}

