package com.example.my_gym_2.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.my_gym_2.data.DatabaseProvider
import com.example.my_gym_2.data.WorkoutProgressLogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProgressScreen(
    allExercises: List<String>, // base names from your app (push/pull/legs)
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // The final options shown in the dropdown
    var exerciseOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selected by remember { mutableStateOf("") }

    // Data for the selected exercise
    var logs by remember { mutableStateOf<List<WorkoutProgressLogs>>(emptyList()) }
    var maxWeight by remember { mutableStateOf<Double?>(null) }
    var max1RM by remember { mutableStateOf<Double?>(null) }

    // 1) Build the full list of exercise names (once)
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = DatabaseProvider.get(context).workoutLogDao()
            val fromDb = dao.getAllExerciseNames()                   // <- new DAO method
            // If you defined exerciseVariants map:
            // val fromVariants = exerciseVariants.values.flatten().map { it.name }
            // If not using variants map in this file, just keep it empty:
            val fromVariants = emptyList<String>()

            val unified = (allExercises + fromDb + fromVariants)
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            withContext(Dispatchers.Main) {
                exerciseOptions = unified
                selected = if ("Bench Press" in unified) {
                    "Bench Press"
                } else {
                    unified.firstOrNull().orEmpty()
                }
            }
        }
    }

    // 2) Whenever 'selected' changes, (re)load stats + logs
    LaunchedEffect(selected) {
        if (selected.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val dao = DatabaseProvider.get(context).workoutLogDao()
                val l = dao.getLogsForExercise(selected) // DESC in DAO
                val mw = dao.getMaxWeight(selected)
                val m1 = dao.getMaxOneRepMax(selected)
                withContext(Dispatchers.Main) {
                    logs = l
                    maxWeight = mw
                    max1RM = m1
                }
            }
        } else {
            logs = emptyList()
            maxWeight = null
            max1RM = null
        }
    }

    // Prepare points for the chart: (dateMillis, oneRm)
    val points: List<Pair<Long, Double>> = remember(logs) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        logs
            .asReversed() // oldest→newest for the chart
            .mapNotNull { log ->
                val r = log.reps
                val denom = 1.0278 - 0.0278 * r
                val oneRm = if (r in 1..10 && denom > 0) log.weight / denom else null
                val time = runCatching { sdf.parse(log.date)?.time }.getOrNull()
                if (time != null && oneRm != null) time to oneRm else null
            }
    }

    Surface(                       // 👈 add Surface
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    )
    {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Header with Back
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text("Progress", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(12.dp))

            // Exercise picker (uses the unified list)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selected,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exercise") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = exerciseOptions.isNotEmpty()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    exerciseOptions.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selected = name
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // PRs
            Text("Best weight: ${maxWeight?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Best est. 1RM: ${max1RM?.let { "%.1f kg".format(it) } ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // Chart
            Text("Estimated 1RM over time", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ProgressLineChart(
                points = points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                lineColor = MaterialTheme.colorScheme.primary,
                axisColor = MaterialTheme.colorScheme.onSurfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // History
            Text("History", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(logs) { log ->
                    Text("${log.date} • ${"%.1f".format(log.weight)} kg × ${log.reps} reps")
                    Divider()
                }
            }
        }
}
}




@Composable
fun ProgressLineChart(
    points: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier,
    padding: Dp = 24.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (points.isEmpty()) return

    val sortedPoints = points.sortedBy { it.first }

    // Ranges (0 .. max + 20)
    val xs = sortedPoints.map { it.first }
    val ys = sortedPoints.map { it.second }
    val minX = xs.minOrNull()!!
    val maxX = xs.maxOrNull()!!
    val minY = (ys.maxOrNull() ?: 0.0) -20.0
    val maxY = (ys.maxOrNull() ?: 0.0) + 10.0

    val xSpan = (maxX - minX).coerceAtLeast(1L)
    val ySpan = (maxY - minY).coerceAtLeast(1e-6)

    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

    Canvas(
        modifier = modifier
            .padding(horizontal = padding)
            .height(220.dp)
    ) {
        val w = size.width
        val h = size.height

        // paddings
        val leftPad = 40f
        val bottomPad = 24f
        val topPad = 8f
        val rightPad = 8f

        val chartW = w - leftPad - rightPad
        val chartH = h - topPad - bottomPad

        fun mapX(x: Long): Float =
            leftPad + ((x - minX).toFloat() / xSpan.toFloat()) * chartW
        fun mapY(y: Double): Float =
            topPad + ((maxY - y) / ySpan).toFloat() * chartH

        // THEME-AWARE paints created inside draw (pick up system theme instantly)
        val xTextPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val yTextPaint = android.graphics.Paint().apply {
            color = textColor.copy(alpha = 0.85f).toArgb()
            textSize = 26f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }
        val gridColor = axisColor.copy(alpha = 0.30f)

        // Axes
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad),
            end = Offset(leftPad, topPad + chartH),
            strokeWidth = 2f
        )
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad + chartH),
            end = Offset(leftPad + chartW, topPad + chartH),
            strokeWidth = 2f
        )

        // Line path
        val path = Path()
        sortedPoints.forEachIndexed { i, (x, y) ->
            val px = mapX(x)
            val py = mapY(y)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 3f))

        // Dots
        sortedPoints.forEach { (x, y) ->
            drawCircle(
                color = lineColor,
                radius = 5f,
                center = Offset(mapX(x), mapY(y))
            )
        }

        // Y ticks: 0, mid, max
        val yTicks = listOf(minY, (minY + maxY) / 2.0, maxY)
        yTicks.forEach { yVal ->
            val yPx = mapY(yVal)
            // grid
            drawLine(
                color = gridColor,
                start = Offset(leftPad, yPx),
                end = Offset(leftPad + chartW, yPx),
                strokeWidth = 1f
            )
            // label
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    String.format(java.util.Locale.US, "%.0f", yVal),
                    leftPad - 8f,
                    yPx + 8f,
                    yTextPaint
                )
            }
        }

        // X labels: first, mid, last
        val xLabels = when {
            sortedPoints.size <= 2 -> sortedPoints
            else -> listOf(sortedPoints.first(), sortedPoints[sortedPoints.size / 2], sortedPoints.last())
        }
        xLabels.forEach { (x, _) ->
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    sdf.format(java.util.Date(x)),
                    mapX(x),
                    topPad + chartH + 20f,
                    xTextPaint
                )
            }
        }
    }
}


