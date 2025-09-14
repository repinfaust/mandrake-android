package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntensitySlider(
  value: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text("Intensity: $value / 10")
    Slider(
      value = value.toFloat(),
      onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 10)) },
      valueRange = 0f..10f,
      // 0..10 inclusive -> 9 steps between ticks
      steps = 9,
      modifier = Modifier.fillMaxWidth()
    )
  }
}
