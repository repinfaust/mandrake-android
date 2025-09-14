package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiRow(
  selected: Int?,
  onSelect: (Int?) -> Unit,
  modifier: Modifier = Modifier
) {
  val emojis = listOf("😟", "😕", "😐", "🙂", "😄")

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
  ) {
    emojis.forEachIndexed { index, emoji ->
      val isSelected = selected == index
      AssistChip(
        onClick = { onSelect(if (isSelected) null else index) },
        label = {
          Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
          )
        },
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
        } else Modifier,
        leadingIcon = null
      )
    }
  }
}
