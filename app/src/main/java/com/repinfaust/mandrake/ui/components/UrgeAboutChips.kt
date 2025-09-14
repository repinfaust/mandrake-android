package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.repinfaust.mandrake.data.entity.CustomChip
import com.repinfaust.mandrake.data.entity.ChipType
import com.repinfaust.mandrake.data.repo.CustomChipRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UrgeAboutChips(
  selected: String?,
  onSelect: (String?) -> Unit,
  modifier: Modifier = Modifier,
  selectedCustomText: String? = null,
  onSelectCustom: ((String?) -> Unit)? = null,
  refreshTrigger: Int = 0
) {
  val context = LocalContext.current
  val database = remember { AppDatabase.get(context) }
  val customChipRepo = remember { CustomChipRepository(database.customChipDao()) }
  val scope = rememberCoroutineScope()
  
  var customChips by remember { mutableStateOf<List<CustomChip>>(emptyList()) }
  
  LaunchedEffect(refreshTrigger) {
    customChips = customChipRepo.getChipsForType(ChipType.URGE_ABOUT)
  }
  
  val defaultUrgeAboutOptions = listOf(
    "Use", "Smoking", "Alcohol", "Porn", "Gambling", "Overspend", 
    "Scroll", "Lash out", "Avoid task"
  )

  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Default options
    defaultUrgeAboutOptions.forEach { option ->
      val isSelected = selected == option && selectedCustomText == null
      AssistChip(
        onClick = { 
          onSelect(if (isSelected) null else option)
          onSelectCustom?.invoke(null)
        },
        label = { Text(option) },
        colors = AssistChipDefaults.assistChipColors(
          containerColor = if (isSelected) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
          else MaterialTheme.colorScheme.surfaceVariant,
          labelColor = if (isSelected) 
            MaterialTheme.colorScheme.primary 
          else MaterialTheme.colorScheme.onSurface
        ),
        modifier = if (isSelected) {
          Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(8.dp)
          )
        } else Modifier
      )
    }
    
    // Custom urge about chips
    customChips.forEach { customChip ->
      val isSelected = selected == "Other" && selectedCustomText == customChip.text
      AssistChip(
        onClick = { 
          if (isSelected) {
            onSelect(null)
            onSelectCustom?.invoke(null)
          } else {
            onSelect("Other")
            onSelectCustom?.invoke(customChip.text)
            // Increment usage count
            scope.launch {
              customChipRepo.createOrIncrementChip(customChip.text, ChipType.URGE_ABOUT)
            }
          }
        },
        label = { Text(customChip.text) },
        colors = AssistChipDefaults.assistChipColors(
          containerColor = if (isSelected) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
          else MaterialTheme.colorScheme.surfaceVariant,
          labelColor = if (isSelected) 
            MaterialTheme.colorScheme.primary 
          else MaterialTheme.colorScheme.onSurface
        ),
        modifier = if (isSelected) {
          Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(8.dp)
          )
        } else Modifier
      )
    }
    
    // "Other" chip
    val isOtherSelected = selected == "Other" && selectedCustomText == null
    AssistChip(
      onClick = { 
        onSelect(if (isOtherSelected) null else "Other")
        onSelectCustom?.invoke(null)
      },
      label = { Text("Other") },
      colors = AssistChipDefaults.assistChipColors(
        containerColor = if (isOtherSelected) 
          MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surfaceVariant,
        labelColor = if (isOtherSelected) 
          MaterialTheme.colorScheme.primary 
        else MaterialTheme.colorScheme.onSurface
      ),
      modifier = if (isOtherSelected) {
        Modifier.border(
          width = 2.dp,
          color = MaterialTheme.colorScheme.primary,
          shape = RoundedCornerShape(8.dp)
        )
      } else Modifier
    )
  }
}