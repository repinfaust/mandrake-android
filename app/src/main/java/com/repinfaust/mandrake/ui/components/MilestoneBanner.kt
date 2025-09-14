package com.repinfaust.mandrake.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repinfaust.mandrake.data.entity.Milestone

@Composable
fun MilestoneBanner(
  milestone: Milestone,
  visible: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically { -it } + fadeIn(),
    exit = slideOutVertically { -it } + fadeOut(),
    modifier = modifier
  ) {
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Use proper vector drawable icons like in Journey screen
        milestone.iconResource?.let { iconName ->
          val iconResId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
          if (iconResId != 0) {
            Icon(
              painter = painterResource(id = iconResId),
              contentDescription = milestone.name,
              modifier = Modifier
                .size(64.dp)
                .padding(bottom = 8.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          } else {
            // Fallback to emoji only if icon not found
            Text(
              text = milestone.emoji,
              fontSize = 48.sp,
              modifier = Modifier.padding(bottom = 8.dp)
            )
          }
        } ?: Text(
          text = milestone.emoji,
          fontSize = 48.sp,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
          text = "Milestone Achieved!",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
          text = milestone.name,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(4.dp))
        
        Text(
          text = milestone.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        TextButton(onClick = onDismiss) {
          Text("Continue")
        }
      }
    }
  }
}