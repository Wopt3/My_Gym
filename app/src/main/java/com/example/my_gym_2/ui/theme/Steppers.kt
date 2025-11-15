package com.example.my_gym_2.ui.controls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberStepper(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    step: Int = 1,
    min: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text("$label: $value")
        Spacer(Modifier.width(8.dp))
        Row {
            Button(onClick = { onChange((value - step).coerceAtLeast(min)) }) { Text("–") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onChange(value + step) }) { Text("+") }
        }
    }

}

@Composable
fun DoubleStepper(
    label: String,
    value: Double,
    onChange: (Double) -> Unit,
    step: Double = 2.5,
    min: Double = 0.0
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text("$label: ${"%.1f".format(value)}kg")
        Spacer(Modifier.width(8.dp))
        Row {
            Button(onClick = { onChange((value - step).coerceAtLeast(min)) }) { Text("–") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onChange(value + step) }) { Text("+") }
        }
    }
}
