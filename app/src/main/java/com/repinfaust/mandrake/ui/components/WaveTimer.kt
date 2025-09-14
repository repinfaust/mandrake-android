package com.repinfaust.mandrake.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun WaveTimer(
  totalSeconds: Int = 600,
  onTick: (Int) -> Unit,
  onFinish: () -> Unit,
  onSwitchTactic: () -> Unit,
) {
  var secondsLeft by remember { mutableStateOf(totalSeconds) }
  LaunchedEffect(Unit) {
    while (secondsLeft > 0) {
      delay(1000)
      secondsLeft--
      onTick(totalSeconds - secondsLeft)
    }
    onFinish()
  }
  Column(Modifier.fillMaxWidth()) {
    Text("Wave timer: ${secondsLeft/60}:${(secondsLeft%60).toString().padStart(2,'0')}")
    Spacer(Modifier.height(8.dp))
    Button(onClick = onSwitchTactic) { Text("Still surfing or switch tactic?") }
  }
}
