package com.repinfaust.mandrake.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.repinfaust.mandrake.data.entity.Mood
import com.repinfaust.mandrake.data.entity.TacticType
import com.repinfaust.mandrake.data.entity.TriggerType
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import com.repinfaust.mandrake.data.entity.ChipType
import com.repinfaust.mandrake.data.repo.CustomChipRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import kotlinx.coroutines.launch
import com.repinfaust.mandrake.ui.components.EmojiRow
import com.repinfaust.mandrake.ui.components.IntensitySlider
import com.repinfaust.mandrake.ui.components.TacticChips
import com.repinfaust.mandrake.ui.components.TriggerChips
import com.repinfaust.mandrake.ui.components.UrgeAboutChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(onDismiss: () -> Unit, onSave: (UrgeEvent) -> Unit) {
  var tactic by remember { mutableStateOf<TacticType?>(null) }
  var customTacticText by remember { mutableStateOf("") }
  var moodIdx by remember { mutableStateOf<Int?>(null) }
  var trigger by remember { mutableStateOf<TriggerType?>(null) }
  var urgeAbout by remember { mutableStateOf<String?>(null) }
  var customUrgeAboutText by remember { mutableStateOf("") }
  var selectedCustomUrgeText by remember { mutableStateOf<String?>(null) }
  var selectedCustomTacticText by remember { mutableStateOf<String?>(null) }
  var intensity by remember { mutableStateOf(5) }
  var duration by remember { mutableStateOf(0) }
  var showCreateTacticChipDialog by remember { mutableStateOf(false) }
  var showCreateUrgeChipDialog by remember { mutableStateOf(false) }
  var newChipText by remember { mutableStateOf("") }
  var refreshChips by remember { mutableStateOf(0) }
  val keyboardController = LocalSoftwareKeyboardController.current
  val context = LocalContext.current
  val database = remember { AppDatabase.get(context) }
  val customChipRepo = remember { CustomChipRepository(database.customChipDao()) }
  val scope = rememberCoroutineScope()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
        .verticalScroll(rememberScrollState())
        .imePadding()
    ) {
      // Header
      Text(
        text = "Nice catch — you noticed the urge.",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(Modifier.height(24.dp))
      
      // Urge Category Selection
      Text(
        text = "What was this urge about?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
      )
      Spacer(Modifier.height(8.dp))
      UrgeAboutChips(
        selected = urgeAbout, 
        onSelect = { urgeAbout = it; selectedCustomUrgeText = null },
        selectedCustomText = selectedCustomUrgeText,
        onSelectCustom = { selectedCustomUrgeText = it },
        refreshTrigger = refreshChips
      )
      
      // Custom urge text field when "Other" is selected and no custom chip is selected
      if (urgeAbout == "Other" && selectedCustomUrgeText == null) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
          value = customUrgeAboutText,
          onValueChange = { customUrgeAboutText = it },
          label = { Text("What was the urge about?") },
          placeholder = { Text("e.g., buying something expensive, eating junk food...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
        
        // Add "Create Chip" button
        if (customUrgeAboutText.isNotBlank()) {
          Spacer(Modifier.height(8.dp))
          TextButton(
            onClick = { showCreateUrgeChipDialog = true }
          ) {
            Text("+ Create permanent chip")
          }
        }
      }
      
      Spacer(Modifier.height(24.dp))
      
      // Trigger Selection
      Text(
        text = "What triggered this urge?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
      )
      Spacer(Modifier.height(8.dp))
      TriggerChips(selected = trigger, onSelect = { trigger = it })
      Spacer(Modifier.height(24.dp))
      
      // Intensity Slider
      IntensitySlider(value = intensity, onValueChange = { intensity = it })
      Spacer(Modifier.height(24.dp))
      
      // Tactic Selection
      Text(
        text = "What helped you choose differently?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
      )
      Spacer(Modifier.height(8.dp))
      TacticChips(
        selected = tactic, 
        onSelect = { tactic = it; selectedCustomTacticText = null },
        selectedCustomText = selectedCustomTacticText,
        onSelectCustom = { selectedCustomTacticText = it },
        refreshTrigger = refreshChips
      )
      
      // Custom tactic text field when "Other" is selected and no custom chip is selected
      if (tactic == TacticType.OTHER && selectedCustomTacticText == null) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
          value = customTacticText,
          onValueChange = { customTacticText = it },
          label = { Text("Describe what helped") },
          placeholder = { Text("e.g., took a cold shower, called a friend...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
        
        // Add "Create Chip" button
        if (customTacticText.isNotBlank()) {
          Spacer(Modifier.height(8.dp))
          TextButton(
            onClick = { showCreateTacticChipDialog = true }
          ) {
            Text("+ Create permanent chip")
          }
        }
      }
      
      Spacer(Modifier.height(24.dp))
      
      // Mood Selection
      Text(
        text = "How do you feel now?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
      )
      Spacer(Modifier.height(8.dp))
      EmojiRow(selected = moodIdx, onSelect = { moodIdx = it })
      Spacer(Modifier.height(24.dp))
      
      Spacer(Modifier.height(32.dp))
      
      // Action Buttons
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
      ) {
        OutlinedButton(
          onClick = {
            keyboardController?.hide()
            onDismiss()
          },
          modifier = Modifier.weight(1f)
        ) { 
          Text("Cancel") 
        }
        
        Button(
          onClick = {
            keyboardController?.hide()
            onSave(
              UrgeEvent(
                eventType = if (urgeAbout == "Avoid task") EventType.AVOIDED_TASK else EventType.BYPASSED_URGE,
                tactic = tactic,
                customTactic = when {
                  selectedCustomTacticText != null -> selectedCustomTacticText
                  tactic == TacticType.OTHER && customTacticText.isNotBlank() -> customTacticText
                  else -> null
                },
                mood = if (moodIdx != null) Mood.values()[moodIdx!!] else null,
                usedWaveTimer = false,
                durationSeconds = duration,
                intensity = intensity,
                trigger = trigger,
                urgeAbout = urgeAbout,
                customUrgeAbout = when {
                  selectedCustomUrgeText != null -> selectedCustomUrgeText
                  urgeAbout == "Other" && customUrgeAboutText.isNotBlank() -> customUrgeAboutText
                  else -> null
                }
              )
            )
          },
          enabled = urgeAbout != null && trigger != null && 
                   (tactic != TacticType.OTHER || selectedCustomTacticText != null || customTacticText.isNotBlank()) &&
                   (urgeAbout != "Other" || selectedCustomUrgeText != null || customUrgeAboutText.isNotBlank()),
          modifier = Modifier.weight(1f)
        ) { 
          Text("Save") 
        }
      }
    }
  }
  
  // Create Tactic Chip Dialog
  if (showCreateTacticChipDialog) {
    AlertDialog(
      onDismissRequest = { showCreateTacticChipDialog = false },
      title = { Text("Create Permanent Chip") },
      text = {
        Column {
          Text("Create a permanent chip for \"$customTacticText\"?")
          Text(
            "This will be available for future logging sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            scope.launch {
              customChipRepo.createOrIncrementChip(customTacticText, ChipType.TACTIC)
              selectedCustomTacticText = customTacticText
              tactic = TacticType.OTHER // Ensure tactic is set to OTHER
              customTacticText = ""
              showCreateTacticChipDialog = false
              refreshChips++ // Trigger refresh of chip components
            }
          }
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateTacticChipDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
  
  // Create Urge About Chip Dialog
  if (showCreateUrgeChipDialog) {
    AlertDialog(
      onDismissRequest = { showCreateUrgeChipDialog = false },
      title = { Text("Create Permanent Chip") },
      text = {
        Column {
          Text("Create a permanent chip for \"$customUrgeAboutText\"?")
          Text(
            "This will be available for future logging sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            scope.launch {
              customChipRepo.createOrIncrementChip(customUrgeAboutText, ChipType.URGE_ABOUT)
              selectedCustomUrgeText = customUrgeAboutText
              urgeAbout = "Other" // Ensure urgeAbout is set to "Other"
              customUrgeAboutText = ""
              showCreateUrgeChipDialog = false
              refreshChips++ // Trigger refresh of chip components
            }
          }
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateUrgeChipDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
