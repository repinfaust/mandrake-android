package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.repinfaust.mandrake.data.repo.FirestoreUrgeRepository
import com.repinfaust.mandrake.domain.StatsComputer
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.Milestone
import com.repinfaust.mandrake.data.entity.MilestoneType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestonesScreen(nav: NavController) {
  val context = LocalContext.current
  val repo = remember { FirestoreUrgeRepository() }

  var events by remember { mutableStateOf<List<UrgeEvent>>(emptyList()) }

  LaunchedEffect(Unit) {
    events = repo.getAllEvents()
  }

  val progressMilestones = Milestone.ALL_MILESTONES.filter { it.type == MilestoneType.PROGRESS }
  val avoidanceMilestones = Milestone.ALL_MILESTONES.filter { it.type == MilestoneType.AVOIDANCE }
  
  // TODO: Update milestone achievement logic based on events and NextRightAction completions
  val achievedMilestones = StatsComputer.calculateMilestones(events)

  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    // Main title
    Text(
      text = "Your Journey",
      style = MaterialTheme.typography.displaySmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
    )
    
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Progress Milestones Section
      item {
        Text(
          text = "Progress Milestones",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(vertical = 16.dp)
        )
      }

      items(progressMilestones) { milestone ->
        MilestoneCard(
          milestone = milestone,
          context = context,
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Avoidance Milestones Section  
      item {
        Text(
          text = "Avoidance Milestones",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(vertical = 16.dp)
        )
      }

      items(avoidanceMilestones) { milestone ->
        MilestoneCard(
          milestone = milestone,
          context = context,
          modifier = Modifier.fillMaxWidth()
        )
      }

      item { Spacer(Modifier.height(16.dp)) }
    }
  }
}

@Composable
private fun MilestoneCard(
  milestone: Milestone,
  context: android.content.Context,
  modifier: Modifier = Modifier
) {
  val achieved = milestone.isAchieved

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = if (achieved)
        MaterialTheme.colorScheme.primaryContainer
      else
        MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Use custom icon if available, fallback to emoji
      milestone.iconResource?.let { iconName ->
        val iconResId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        if (iconResId != 0) {
          Icon(
            painter = painterResource(id = iconResId),
            contentDescription = milestone.name,
            modifier = Modifier
              .size(48.dp)
              .padding(end = 16.dp),
            tint = if (achieved) 
              MaterialTheme.colorScheme.primary 
            else 
              MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Text(
            text = milestone.emoji,
            fontSize = 32.sp,
            modifier = Modifier.padding(end = 16.dp)
          )
        }
      } ?: Text(
        text = milestone.emoji,
        fontSize = 32.sp,
        modifier = Modifier.padding(end = 16.dp)
      )

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = milestone.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (achieved)
            MaterialTheme.colorScheme.onPrimaryContainer
          else
            MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
          text = milestone.description,
          style = MaterialTheme.typography.bodyMedium,
          color = if (achieved)
            MaterialTheme.colorScheme.onPrimaryContainer
          else
            MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Removed milestone count to avoid gamification
      }

      if (achieved) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Achieved",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}
