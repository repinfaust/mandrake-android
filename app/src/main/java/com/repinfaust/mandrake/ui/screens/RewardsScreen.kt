package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import com.repinfaust.mandrake.data.entity.Reward
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import com.repinfaust.mandrake.data.entity.Milestone
import com.repinfaust.mandrake.data.entity.MilestoneType
import com.repinfaust.mandrake.data.repo.FirestoreUrgeRepository
import com.repinfaust.mandrake.data.repo.RewardsRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import com.repinfaust.mandrake.domain.StatsComputer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(nav: NavController) {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val rewardsRepo = remember { RewardsRepository(database.rewardsDao(), database.redemptionsDao()) }
    val urgeRepo = remember { FirestoreUrgeRepository() }
    val scope = rememberCoroutineScope()
    
    var rewards by remember { mutableStateOf(emptyList<Reward>()) }
    var events by remember { mutableStateOf(emptyList<UrgeEvent>()) }
    var claimedRewardIds by remember { mutableStateOf(emptySet<String>()) }
    var rewardClaimCounts by remember { mutableStateOf(emptyMap<String, Int>()) }
    var achievedMilestones by remember { mutableStateOf(0) }
    var showAddReward by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showClaimResult by remember { mutableStateOf(false) }
    var claimResultMessage by remember { mutableStateOf("") }
    var isClaimSuccess by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Initialize template rewards
        rewardsRepo.initializeTemplateRewards()
        
        // Load data
        rewards = rewardsRepo.getAllActiveRewards()
        events = urgeRepo.getAllEvents()
        
        // Load claimed rewards
        val redemptions = rewardsRepo.getAllRedemptions()
        claimedRewardIds = redemptions.map { it.rewardId }.toSet()
        rewardClaimCounts = redemptions.groupingBy { it.rewardId }.eachCount()
        
        // Calculate achieved milestones for rewards eligibility using EventType
        val urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
        val avoidedTasksCompleted = events.count { it.eventType == EventType.AVOIDED_TASK }
        
        // Total milestones achieved
        val progressMilestones = Milestone.ALL_MILESTONES.filter { it.type == MilestoneType.PROGRESS }
        val progressAchieved = progressMilestones.count { urgesBypassed >= it.hoursRequired }
        
        val avoidanceMilestones = Milestone.ALL_MILESTONES.filter { it.type == MilestoneType.AVOIDANCE }
        val avoidanceAchieved = avoidanceMilestones.count { avoidedTasksCompleted >= it.hoursRequired }
        
        achievedMilestones = progressAchieved + avoidanceAchieved
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
        
        // Header with points balance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rewards",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "$achievedMilestones milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        // Description
        Text(
            text = "Celebrate your wins with rewards when you achieve milestones!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Tab row for Templates and Custom
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Templates") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Custom") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> TemplateRewardsContent(
                rewards = rewards.filter { it.template },
                claimedRewardIds = claimedRewardIds,
                achievedMilestones = achievedMilestones,
                getClaimCount = { rewardId -> rewardClaimCounts[rewardId] ?: 0 },
                onClaimReward = { reward ->
                    scope.launch {
                        val latestEvent = events.maxByOrNull { it.timestamp }
                        if (latestEvent != null) {
                            val result = rewardsRepo.claimReward(reward.id, latestEvent.id.toString(), "milestone")
                            if (result.isSuccess) {
                                claimResultMessage = "Successfully claimed \"${reward.title}\"!"
                                isClaimSuccess = true
                                // Refresh claimed rewards list
                                val redemptions = rewardsRepo.getAllRedemptions()
                                claimedRewardIds = redemptions.map { it.rewardId }.toSet()
                                rewardClaimCounts = redemptions.groupingBy { it.rewardId }.eachCount()
                            } else {
                                claimResultMessage = "Failed to claim reward. Please try again."
                                isClaimSuccess = false
                            }
                            showClaimResult = true
                        } else {
                            claimResultMessage = "No recent events found. Please log an urge first."
                            isClaimSuccess = false
                            showClaimResult = true
                        }
                    }
                }
            )
            1 -> CustomRewardsContent(
                rewards = rewards.filter { !it.template },
                claimedRewardIds = claimedRewardIds,
                achievedMilestones = achievedMilestones,
                getClaimCount = { rewardId -> rewardClaimCounts[rewardId] ?: 0 },
                onClaimReward = { reward ->
                    scope.launch {
                        val latestEvent = events.maxByOrNull { it.timestamp }
                        if (latestEvent != null) {
                            val result = rewardsRepo.claimReward(reward.id, latestEvent.id.toString(), "milestone")
                            if (result.isSuccess) {
                                claimResultMessage = "Successfully claimed \"${reward.title}\"!"
                                isClaimSuccess = true
                                // Refresh claimed rewards list
                                val redemptions = rewardsRepo.getAllRedemptions()
                                claimedRewardIds = redemptions.map { it.rewardId }.toSet()
                                rewardClaimCounts = redemptions.groupingBy { it.rewardId }.eachCount()
                            } else {
                                claimResultMessage = "Failed to claim reward. Please try again."
                                isClaimSuccess = false
                            }
                            showClaimResult = true
                        } else {
                            claimResultMessage = "No recent events found. Please log an urge first."
                            isClaimSuccess = false
                            showClaimResult = true
                        }
                    }
                },
                onAddReward = { showAddReward = true }
            )
        }
    }
    
    // Add reward dialog
    if (showAddReward) {
        AddRewardDialog(
            onDismiss = { showAddReward = false },
            onSave = { title, notes, costPoints, milestoneCount, milestoneType ->
                scope.launch {
                    rewardsRepo.createCustomRewardWithMilestones(title, notes, costPoints, milestoneCount, milestoneType)
                    rewards = rewardsRepo.getAllActiveRewards()
                    showAddReward = false
                }
            },
            usePoints = false
        )
    }
    
    // Claim result dialog
    if (showClaimResult) {
        AlertDialog(
            onDismissRequest = { showClaimResult = false },
            title = { 
                Text(if (isClaimSuccess) "Reward Claimed!" else "Claim Failed") 
            },
            text = { 
                Text(claimResultMessage)
            },
            confirmButton = {
                TextButton(onClick = { showClaimResult = false }) {
                    Text("OK")
                }
            },
            icon = if (isClaimSuccess) {
                { Icon(Icons.Default.CheckCircle, contentDescription = null) }
            } else null
        )
    }
}

@Composable
private fun TemplateRewardsContent(
    rewards: List<Reward>,
    claimedRewardIds: Set<String>,
    achievedMilestones: Int,
    onClaimReward: (Reward) -> Unit,
    getClaimCount: (String) -> Int = { 0 }
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rewards) { reward ->
            RewardCard(
                reward = reward,
                canAfford = achievedMilestones >= reward.requiredMilestoneCount,
                onClaim = { onClaimReward(reward) },
                achievedMilestones = achievedMilestones,
                isClaimed = claimedRewardIds.contains(reward.id),
                claimCount = getClaimCount(reward.id)
            )
        }
    }
}

@Composable
private fun CustomRewardsContent(
    rewards: List<Reward>,
    claimedRewardIds: Set<String>,
    achievedMilestones: Int,
    onClaimReward: (Reward) -> Unit,
    onAddReward: () -> Unit,
    getClaimCount: (String) -> Int = { 0 }
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onAddReward,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add reward",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Add custom reward",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        items(rewards) { reward ->
            RewardCard(
                reward = reward,
                canAfford = achievedMilestones >= reward.requiredMilestoneCount,
                onClaim = { onClaimReward(reward) },
                achievedMilestones = achievedMilestones,
                isClaimed = claimedRewardIds.contains(reward.id),
                claimCount = getClaimCount(reward.id)
            )
        }
    }
}

@Composable
private fun RewardCard(
    reward: Reward,
    canAfford: Boolean,
    onClaim: () -> Unit,
    achievedMilestones: Int = 0,
    isClaimed: Boolean = false,
    claimCount: Int = 0
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    reward.notes?.let { notes ->
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Show milestone requirement
                    val requirementText = when {
                        reward.specificMilestoneId != null -> "Requires specific milestone: ${reward.specificMilestoneId}"
                        reward.milestoneType != null -> "Requires ${reward.requiredMilestoneCount} ${reward.milestoneType.name.lowercase()} milestone${if (reward.requiredMilestoneCount > 1) "s" else ""}"
                        else -> "Requires ${reward.requiredMilestoneCount} milestone${if (reward.requiredMilestoneCount > 1) "s" else ""}"
                    }
                    
                    Text(
                        text = requirementText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (canAfford) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    // Show claim count if claimed
                    if (isClaimed && claimCount > 0) {
                        Text(
                            text = "Claimed $claimCount time${if (claimCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Button(
                    onClick = onClaim,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Claim")
                }
            }
        }
    }
}

@Composable
private fun AddRewardDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?, Int?, Int, MilestoneType?) -> Unit,
    usePoints: Boolean
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var costPoints by remember { mutableStateOf("") }
    var requiredMilestones by remember { mutableStateOf("1") }
    var selectedMilestoneType by remember { mutableStateOf<MilestoneType?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Reward") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reward title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = requiredMilestones,
                    onValueChange = { requiredMilestones = it.filter { char -> char.isDigit() } },
                    label = { Text("Milestones required") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Milestone type selector
                Text(
                    text = "Milestone Type (optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        onClick = { selectedMilestoneType = if (selectedMilestoneType == MilestoneType.PROGRESS) null else MilestoneType.PROGRESS },
                        label = { Text("Progress") },
                        selected = selectedMilestoneType == MilestoneType.PROGRESS
                    )
                    FilterChip(
                        onClick = { selectedMilestoneType = if (selectedMilestoneType == MilestoneType.AVOIDANCE) null else MilestoneType.AVOIDANCE },
                        label = { Text("Avoidance") },
                        selected = selectedMilestoneType == MilestoneType.AVOIDANCE
                    )
                }
                
                if (usePoints) {
                    OutlinedTextField(
                        value = costPoints,
                        onValueChange = { costPoints = it.filter { char -> char.isDigit() } },
                        label = { Text("Points cost (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && requiredMilestones.isNotBlank()) {
                        val cost = if (usePoints && costPoints.isNotBlank()) costPoints.toIntOrNull() else null
                        val milestoneCount = requiredMilestones.toIntOrNull() ?: 1
                        onSave(title, notes.ifBlank { null }, cost, milestoneCount, selectedMilestoneType)
                    }
                },
                enabled = title.isNotBlank() && requiredMilestones.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}