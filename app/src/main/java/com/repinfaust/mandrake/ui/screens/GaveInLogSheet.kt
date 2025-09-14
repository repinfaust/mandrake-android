package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repinfaust.mandrake.data.entity.Mood
import com.repinfaust.mandrake.data.entity.TriggerType
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import com.repinfaust.mandrake.ui.components.EmojiRow
import com.repinfaust.mandrake.ui.components.IntensitySlider
import com.repinfaust.mandrake.ui.components.TriggerChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaveInLogSheet(
  onDismiss: () -> Unit,
  onSave: (UrgeEvent) -> Unit,
  navController: NavController,
  onJustLog: ((UrgeEvent) -> Unit)? = null
) {
  var trigger by remember { mutableStateOf<TriggerType?>(null) }
  var intensity by remember { mutableStateOf(5) }
  var moodIdx by remember { mutableStateOf<Int?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("It's okay. Learning isn't linear.")
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Thank you for being honest. This takes courage.",
          style = MaterialTheme.typography.bodyMedium,
          fontStyle = FontStyle.Italic,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        // ✅ Use named args so the lambda binds to onSelect, not a Modifier
        TriggerChips(
          selected = trigger,
          onSelect = { trigger = it }
        )
        Spacer(Modifier.height(16.dp))

        // ✅ Same here for the slider
        IntensitySlider(
          value = intensity,
          onValueChange = { intensity = it }
        )
        Spacer(Modifier.height(16.dp))

        Text("How are you feeling now?")
        Spacer(Modifier.height(8.dp))

        // ✅ And here for emoji picker
        EmojiRow(
          selected = moodIdx,
          onSelect = { moodIdx = it }
        )
        Spacer(Modifier.height(16.dp))

        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Remember:",
              style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            Text(
              text = "• One choice doesn't define your pattern\n" +
                      "• You're learning what triggers to watch for\n" +
                      "• Each moment is a fresh choice\n" +
                      "• You're brave for tracking this",
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Support link
        TextButton(
          onClick = {
            navController.navigate("crisis")
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Need support? Access crisis resources →",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TextButton(
          onClick = onDismiss
        ) {
          Text("Not now")
        }
        
        if (onJustLog != null) {
          TextButton(
            enabled = trigger != null,
            onClick = {
              onJustLog(
                UrgeEvent(
                  eventType = EventType.WENT_WITH_URGE,
                  tactic = null,
                  customTactic = null,
                  mood = moodIdx?.let { Mood.values()[it] },
                  usedWaveTimer = false,
                  durationSeconds = 0,
                  intensity = intensity,
                  trigger = trigger,
                  customUrgeAbout = null,
                  gaveIn = true
                )
              )
            }
          ) {
            Text("Just log it")
          }
        }
        
        TextButton(
          enabled = trigger != null,
          onClick = {
            onSave(
              UrgeEvent(
                eventType = EventType.WENT_WITH_URGE,
                tactic = null,
                customTactic = null,
                mood = moodIdx?.let { Mood.values()[it] },
                usedWaveTimer = false,
                durationSeconds = 0,
                intensity = intensity,
                trigger = trigger,
                customUrgeAbout = null,
                gaveIn = true
              )
            )
          }
        ) {
          Text("Add a repair step")
        }
      }
    },
    dismissButton = null
  )
}
