package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.navigation.NavController
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import com.repinfaust.mandrake.data.entity.Milestone
import com.repinfaust.mandrake.data.entity.MilestoneType
import com.repinfaust.mandrake.data.entity.TacticType
import com.repinfaust.mandrake.data.repo.FirestoreUrgeRepository
import com.repinfaust.mandrake.domain.StatsComputer
// import com.repinfaust.mandrake.ui.components.Heatmap // TODO: Add when available
import com.repinfaust.mandrake.nav.Routes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(nav: NavController) {
    val context = LocalContext.current
    val repo = remember { FirestoreUrgeRepository() }
    var events by remember { mutableStateOf(emptyList<UrgeEvent>()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        events = repo.getAllEvents()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mandrake logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = context.resources.getIdentifier("mandrake_splash_logo", "drawable", context.packageName)),
                contentDescription = "Mandrake",
                modifier = Modifier.size(48.dp)
            )
        }
        
        Text(
            text = "Journey",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Tab row for Milestones, Timeline, Patterns
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Milestones") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Timeline") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Patterns") }
            )
        }
        
        when (selectedTab) {
            0 -> MilestonesContent(events = events)
            1 -> TimelineContent(events = events)
            2 -> PatternsContent(events = events)
        }
    }
}

@Composable
private fun TimelineContent(events: List<UrgeEvent>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (events.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Your journey starts here",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Log your first urge event to start tracking patterns and progress.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            items(events.sortedByDescending { it.timestamp }) { event ->
                TimelineEventCard(event = event)
            }
        }
    }
}

@Composable
private fun PatternsContent(events: List<UrgeEvent>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Weekly Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (events.isNotEmpty()) {
                        // TODO: Implement heatmap when component is available
                        Text(
                            text = "Heatmap coming soon - ${events.size} events logged",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Log events to see patterns over time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        
        item {
            PatternInsightsCard(events = events)
        }
        
        item {
            TacticEffectivenessCard(events = events)
        }
    }
}

@Composable
private fun TimelineEventCard(event: UrgeEvent) {
    val date = java.time.Instant.ofEpochMilli(event.timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDateTime()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (event.gaveIn) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = when (event.eventType) {
                            EventType.WENT_WITH_URGE -> "Went with urge"
                            EventType.BYPASSED_URGE -> "Bypassed urge"
                            EventType.AVOIDED_TASK -> "Avoided task"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Show the actual task name for avoided tasks
                    if (event.eventType == EventType.AVOIDED_TASK && !event.customTactic.isNullOrBlank()) {
                        Text(
                            text = event.customTactic!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${date.hour.toString().padStart(2, '0')}:${date.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (event.intensity > 0) {
                Text(
                    text = "Intensity: ${event.intensity}/10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            event.tactic?.let { tactic ->
                val tacticText = if (tactic == TacticType.OTHER && !event.customTactic.isNullOrBlank()) {
                    event.customTactic!!
                } else {
                    when (tactic) {
                        TacticType.WALK -> "Walk"
                        TacticType.CALL -> "Call/Text"
                        TacticType.MUSIC -> "Music"
                        TacticType.JOT -> "Jot it down"
                        TacticType.SHOWER -> "Shower"
                        TacticType.BREATH -> "Breath"
                        TacticType.OTHER -> "Other"
                        else -> tactic.name.lowercase().replaceFirstChar { it.uppercase() }
                    }
                }
                Text(
                    text = "Used: $tacticText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Display custom urge about text if it exists
            if (event.urgeAbout == "Other" && !event.customUrgeAbout.isNullOrBlank()) {
                Text(
                    text = "About: ${event.customUrgeAbout}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else if (!event.urgeAbout.isNullOrBlank() && event.urgeAbout != "Other") {
                Text(
                    text = "About: ${event.urgeAbout}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PatternInsightsCard(events: List<UrgeEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Pattern Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (events.isNotEmpty()) {
                // Basic stats computation
                val bypassedUrges = events.count { it.eventType == EventType.BYPASSED_URGE }
                val avoidedTasks = events.count { it.eventType == EventType.AVOIDED_TASK }
                
                Text(
                    text = "• $bypassedUrges urges bypassed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Text(
                    text = "• $avoidedTasks tasks completed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                val avgIntensity = events.filter { it.intensity > 0 }.map { it.intensity }.average()
                if (!avgIntensity.isNaN()) {
                    Text(
                        text = "• Average urge intensity: ${"%.1f".format(avgIntensity)}/10",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                val mostCommonTactic = events.mapNotNull { it.tactic }.groupingBy { it }.eachCount().maxByOrNull { it.value }
                mostCommonTactic?.let { (tactic, count) ->
                    Text(
                        text = "• Most used tactic: ${tactic.name.lowercase()} ($count times)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Show most common avoided task category
                val avoidedTaskEvents = events.filter { it.eventType == EventType.AVOIDED_TASK && !it.customTactic.isNullOrBlank() }
                if (avoidedTaskEvents.isNotEmpty()) {
                    val taskCategories = avoidedTaskEvents.mapNotNull { event ->
                        event.customTactic?.split(":")?.firstOrNull()?.trim()
                    }.groupingBy { it }.eachCount().maxByOrNull { it.value }
                    
                    taskCategories?.let { (category, count) ->
                        Text(
                            text = "• Most completed tasks: $category ($count times)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Log events to see pattern insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TacticEffectivenessCard(events: List<UrgeEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Action Effectiveness",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            if (events.isNotEmpty()) {
                // Regular tactics for bypassed urges
                val tacticStats = events.filter { it.tactic != null && !it.gaveIn }
                    .groupBy { it.tactic }
                    .mapValues { (_, events) -> events.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(3)
                
                if (tacticStats.isNotEmpty()) {
                    Text(
                        text = "Urge Management:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    tacticStats.forEach { (tactic, count) ->
                        Text(
                            text = "• ${tactic!!.name.lowercase().replaceFirstChar { it.uppercase() }}: $count successes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 2.dp, start = 8.dp)
                        )
                    }
                }
                
                // Avoided tasks effectiveness
                val avoidedTaskStats = events.filter { it.eventType == EventType.AVOIDED_TASK && !it.customTactic.isNullOrBlank() }
                    .mapNotNull { event ->
                        event.customTactic?.substringAfter(":")?.trim()
                    }
                    .groupingBy { it }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .take(3)
                
                if (avoidedTaskStats.isNotEmpty()) {
                    Text(
                        text = "Task Completion:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    avoidedTaskStats.forEach { (task, count) ->
                        Text(
                            text = "• $task: $count times",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 2.dp, start = 8.dp)
                        )
                    }
                }
                
                if (tacticStats.isEmpty() && avoidedTaskStats.isEmpty()) {
                    Text(
                        text = "Keep logging to track what works best for you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "Track which tactics work best for you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
@Composable
private fun MilestonesContent(events: List<UrgeEvent>) {
    val context = LocalContext.current
    val progressMilestones = Milestone.ALL_MILESTONES.filter { it.type == MilestoneType.PROGRESS }
    val avoidanceMilestones = Milestone.ALL_MILESTONES.filter { it.type == MilestoneType.AVOIDANCE }
    
    // Calculate milestone achievement using EventType
    val urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
    val avoidedTasksCompleted = events.count { it.eventType == EventType.AVOIDED_TASK }
    
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Milestones Section
        item {
            Text(
                text = "Progress Milestones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(progressMilestones) { milestone ->
            MilestoneCard(
                milestone = milestone.copy(isAchieved = urgesBypassed >= milestone.hoursRequired),
                context = context,
                currentProgress = urgesBypassed,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Avoidance Milestones Section  
        item {
            Text(
                text = "Avoidance Milestones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(avoidanceMilestones) { milestone ->
            MilestoneCard(
                milestone = milestone.copy(isAchieved = avoidedTasksCompleted >= milestone.hoursRequired),
                context = context,
                currentProgress = avoidedTasksCompleted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    context: android.content.Context,
    currentProgress: Int,
    modifier: Modifier = Modifier
) {
    val achieved = milestone.isAchieved
    val progressText = "$currentProgress/${milestone.hoursRequired}"

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
            // Use proper vector drawable icons
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
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    // Fallback to emoji only if icon not found
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (achieved)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (achieved)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (achieved)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
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
