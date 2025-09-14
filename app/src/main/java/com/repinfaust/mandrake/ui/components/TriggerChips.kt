package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.repinfaust.mandrake.data.entity.TriggerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerChips(
  selected: TriggerType?,
  onSelect: (TriggerType?) -> Unit,
  modifier: Modifier = Modifier
) {
  val triggers = TriggerType.values()

  Row(
    modifier = modifier
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 2.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    triggers.forEach { t ->
      val isSelected = selected == t
      FilterChip(
        selected = isSelected,
        onClick = {
          // tap again to clear
          onSelect(if (isSelected) null else t)
        },
        label = {
          Text(
            text = t.name
              .lowercase()
              .replace('_', ' ')
              .capitalize(Locale.current),
            style = MaterialTheme.typography.labelLarge
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
          selectedLabelColor = MaterialTheme.colorScheme.primary
        )
      )
    }
  }
}
