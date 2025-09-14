package com.repinfaust.mandrake.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.repinfaust.mandrake.data.entity.TacticType
import com.repinfaust.mandrake.data.entity.CustomChip
import com.repinfaust.mandrake.data.entity.ChipType
import com.repinfaust.mandrake.data.repo.CustomChipRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TacticChips(
  selected: TacticType?, 
  onSelect: (TacticType?) -> Unit,
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
    customChips = customChipRepo.getChipsForType(ChipType.TACTIC)
  }
  
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Default tactic chips
    listOf(
      TacticType.WALK to "Walk",
      TacticType.CALL to "Call/Text",
      TacticType.MUSIC to "Music",
      TacticType.JOT to "Jot it down",
      TacticType.SHOWER to "Shower",
      TacticType.BREATH to "Breath"
    ).forEach { (type, label) ->
      val isSelected = selected == type && selectedCustomText == null
      AssistChip(
        onClick = { 
          onSelect(if (isSelected) null else type)
          onSelectCustom?.invoke(null)
        }, 
        label = { Text(label) },
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
    
    // Custom tactic chips
    customChips.forEach { customChip ->
      val isSelected = selected == TacticType.OTHER && selectedCustomText == customChip.text
      AssistChip(
        onClick = { 
          if (isSelected) {
            onSelect(null)
            onSelectCustom?.invoke(null)
          } else {
            onSelect(TacticType.OTHER)
            onSelectCustom?.invoke(customChip.text)
            // Increment usage count
            scope.launch {
              customChipRepo.createOrIncrementChip(customChip.text, ChipType.TACTIC)
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
    val isOtherSelected = selected == TacticType.OTHER && selectedCustomText == null
    AssistChip(
      onClick = { 
        onSelect(if (isOtherSelected) null else TacticType.OTHER) 
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
