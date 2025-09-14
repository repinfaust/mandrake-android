package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.roundToInt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.navigation.NavController
import com.repinfaust.mandrake.data.entity.Milestone
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import com.repinfaust.mandrake.data.entity.ScreeningType
import com.repinfaust.mandrake.data.entity.RiskAssessment
import com.repinfaust.mandrake.data.entity.RedFlags
import com.repinfaust.mandrake.data.repo.FirestoreUrgeRepository
import com.repinfaust.mandrake.data.repo.RiskAssessmentRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import com.repinfaust.mandrake.domain.StatsComputer
import com.repinfaust.mandrake.nav.Routes
import com.repinfaust.mandrake.ui.components.MilestoneBanner
import com.repinfaust.mandrake.ui.components.ProgressBar
import com.repinfaust.mandrake.ui.components.NextRightActionCard
import com.repinfaust.mandrake.ui.components.NextRightActionCompleteSheet
import com.repinfaust.mandrake.ui.screens.QuickLogSheet
import com.repinfaust.mandrake.ui.screens.GaveInLogSheet
import com.repinfaust.mandrake.domain.model.ActionDomain
import com.repinfaust.mandrake.domain.model.ActionTemplates
import com.repinfaust.mandrake.domain.model.NextRightActionItem
import com.repinfaust.mandrake.domain.logic.NextRightActionManager
import com.repinfaust.mandrake.util.subtleHaptic
import com.repinfaust.mandrake.ui.components.EscalationNudgeCard
import com.repinfaust.mandrake.ui.screens.ScreeningSheet
import com.repinfaust.mandrake.domain.RiskRules
import kotlinx.coroutines.launch
import android.widget.Toast
// import java.time.LocalDateTime - replaced with System.currentTimeMillis()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val context = LocalContext.current
    val repo = remember { FirestoreUrgeRepository() }

    var showLog by remember { mutableStateOf(false) }
    var showGaveInLog by remember { mutableStateOf(false) }
    var urgesBypassed by remember { mutableStateOf(0) }
    var events by remember { mutableStateOf(emptyList<UrgeEvent>()) }
    var showMilestoneBanner by remember { mutableStateOf(false) }
    var currentMilestone by remember { mutableStateOf<Milestone?>(null) }
    var currentUrgeIntensity by remember { mutableStateOf(5f) }
    var currentMood by remember { mutableStateOf(3) } // 0-4 scale
    
    // NextRightAction state
    var currentActionItem by remember { mutableStateOf<NextRightActionItem?>(null) }
    var availableActionItems by remember { mutableStateOf<List<NextRightActionItem>>(emptyList()) }
    var showActionCompleteSheet by remember { mutableStateOf(false) }
    var completedAction by remember { mutableStateOf("") }
    var lastUrgeFlowTime by remember { mutableStateOf<Long?>(null) }
    var completedActionsTotal by remember { mutableStateOf(0) }
    var showRepairModal by remember { mutableStateOf(false) }
    
    val nextRightActionManager = remember { NextRightActionManager() }
    val scope = rememberCoroutineScope()
    
    // Risk assessment state
    val database = remember { AppDatabase.get(context) }
    val riskRepo = remember { RiskAssessmentRepository(database.screeningDao(), database.riskAssessmentDao()) }
    var showScreening by remember { mutableStateOf(false) }
    var screeningCategory by remember { mutableStateOf("alcohol") }
    var screeningType by remember { mutableStateOf(ScreeningType.AUDIT_C) }
    var latestRiskAssessment by remember { mutableStateOf<RiskAssessment?>(null) }
    var shouldShowNudge by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Refresh data when screen loads and when returning from other screens
    LaunchedEffect(nav.currentBackStackEntry) {
        // Reload data every time HomeScreen becomes visible
        android.util.Log.d("HomeScreen", "Refreshing data")
    }
    
    LaunchedEffect(refreshTrigger) {
        // Load events and milestones
        events = repo.getAllEvents()
        val latestMilestone = StatsComputer.getLatestAchievedMilestone(events)
        currentMilestone = latestMilestone
        
        // Calculate counts from events using EventType
        urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
        val wentWithUrges = events.count { it.eventType == EventType.WENT_WITH_URGE }
        completedActionsTotal = events.count { it.eventType == EventType.AVOIDED_TASK }
        
        // Initialize current action on first load
        if (currentActionItem == null) {
            currentActionItem = nextRightActionManager.pickNextRightAction(
                recentTriggers = emptyList() // TODO: Get from recent events
            )
        }
        
        // Load all available action items for selection dialog
        val allItems = ActionDomain.values().flatMap { domain ->
            ActionTemplates.getTemplates(domain).map { action ->
                NextRightActionItem(
                    id = "${domain.name.lowercase()}_${action.hashCode()}",
                    category = domain,
                    text = action,
                    defaultMinutes = when {
                        action.contains("call") -> 3
                        action.contains("15-min") -> 15
                        action.contains("10-min") -> 10
                        action.contains("email") -> 2
                        action.contains("photograph") -> 1
                        action.contains("breath") -> 1
                        action.contains("water") -> 1
                        else -> 2
                    },
                    userMinutes = when {
                        action.contains("call") -> 3
                        action.contains("15-min") -> 15
                        action.contains("10-min") -> 10
                        action.contains("email") -> 2
                        action.contains("photograph") -> 1
                        action.contains("breath") -> 1
                        action.contains("water") -> 1
                        else -> 2
                    }
                )
            }
        }
        availableActionItems = allItems.filter { it.canShowOnHome() }
        
        // Check if screening should be triggered
        val recentHighIntensity = events.takeLast(7).count { it.intensity >= 7 }
        if (riskRepo.shouldTriggerScreening(screeningCategory, recentHighIntensity)) {
            showScreening = true
        }
        
        // Perform behavioral risk assessment and check for nudges
        riskRepo.performBehavioralRiskAssessment(events).onSuccess { assessment ->
            latestRiskAssessment = assessment
            shouldShowNudge = riskRepo.shouldShowNudge()
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        // Main content
        val mainScrollState = rememberScrollState()
        Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(mainScrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
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
                // Current Status Section
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
                            text = "How are you feeling right now?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Urge Intensity Slider
                        Text(
                            text = "Urge Intensity: ${currentUrgeIntensity.roundToInt()}/10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Slider(
                            value = currentUrgeIntensity,
                            onValueChange = { currentUrgeIntensity = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mood Selection
                        Text(
                            text = "Current Mood",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val moods = listOf("😫", "😔", "😐", "🙂", "😊")
                            moods.forEachIndexed { index, emoji ->
                                TextButton(
                                    onClick = { currentMood = index },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (currentMood == index) 
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else 
                                                Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (currentMood == index)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = if (currentMood == index) 
                                                MaterialTheme.typography.headlineMedium.fontSize * 1.1f
                                            else 
                                                MaterialTheme.typography.headlineMedium.fontSize
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showLog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "I chose an alternative",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedButton(
                        onClick = { 
                            showGaveInLog = true
                            lastUrgeFlowTime = System.currentTimeMillis()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "I went with the urge",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // NextRightAction tertiary CTA
                if (nextRightActionManager.shouldShowNextRightAction(
                    urgeCardOpen = showLog || showGaveInLog,
                    urgeIntensity = currentUrgeIntensity.roundToInt(),
                    waveTimerRunning = false, // TODO: Add wave timer state when implemented
                    lastUrgeFlowTime = null // Simplified - removed LocalDateTime dependency
                ) && currentActionItem != null) {
                    NextRightActionCard(
                        actionItem = currentActionItem!!,
                        availableItems = availableActionItems,
                        onDoItNow = {
                            completedAction = currentActionItem!!.text
                            showActionCompleteSheet = true
                            
                            // Save to Firestore and update counts
                            scope.launch {
                                val completionEvent = UrgeEvent(
                                    id = 0,
                                    timestamp = System.currentTimeMillis(),
                                    eventType = EventType.AVOIDED_TASK,
                                    tactic = null,
                                    customTactic = if (currentActionItem!!.id.startsWith("custom_")) {
                                        "${currentActionItem!!.category.name}: ${currentActionItem!!.text}"
                                    } else {
                                        currentActionItem!!.text
                                    },
                                    mood = null,
                                    usedWaveTimer = false,
                                    durationSeconds = currentActionItem!!.userMinutes * 60,
                                    intensity = 0,
                                    trigger = null,
                                    customUrgeAbout = null
                                )
                                repo.logEvent(completionEvent).fold(
                                    onSuccess = {
                                        val oldMilestone = currentMilestone
                                        
                                        // Reload events and update counts
                                        events = repo.getAllEvents()
                                        urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
                                        completedActionsTotal = events.count { it.eventType == EventType.AVOIDED_TASK }
                                        
                                        // Check for new milestone achievement
                                        val newMilestone = StatsComputer.getLatestAchievedMilestone(events)
                                        android.util.Log.d("MilestoneCelebration", "Old: ${oldMilestone?.name}, New: ${newMilestone?.name}")
                                        
                                        if (newMilestone != null && newMilestone != oldMilestone) {
                                            currentMilestone = newMilestone
                                            showMilestoneBanner = true
                                            
                                            android.util.Log.d("MilestoneCelebration", "Showing celebration for: ${newMilestone.name}")
                                            
                                            // Show celebration toast
                                            Toast.makeText(
                                                context, 
                                                "🎉 Milestone achieved: ${newMilestone.name}!", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        
                                        // Get next available action AFTER successful save
                                        currentActionItem = nextRightActionManager.pickNextRightAction(
                                            recentTriggers = emptyList()
                                        )
                                    },
                                    onFailure = { 
                                        // Get next available action even if save failed
                                        currentActionItem = nextRightActionManager.pickNextRightAction(
                                            recentTriggers = emptyList()
                                        )
                                    }
                                )
                            }
                            
                            // Mark as completed - simplified without LocalDateTime
                            // currentActionItem = currentActionItem!!.withCompleted(System.currentTimeMillis())
                            
                            subtleHaptic(context)
                        },
                        onDoneIt = {
                            // Record completion - same flow as "Do it now"
                            completedAction = currentActionItem!!.text
                            showActionCompleteSheet = true
                            
                            // Save to Firestore and update counts
                            scope.launch {
                                val completionEvent = UrgeEvent(
                                    id = 0,
                                    timestamp = System.currentTimeMillis(),
                                    eventType = EventType.AVOIDED_TASK,
                                    tactic = null,
                                    customTactic = if (currentActionItem!!.id.startsWith("custom_")) {
                                        "${currentActionItem!!.category.name}: ${currentActionItem!!.text}"
                                    } else {
                                        currentActionItem!!.text
                                    },
                                    mood = null,
                                    usedWaveTimer = false,
                                    durationSeconds = currentActionItem!!.userMinutes * 60,
                                    intensity = 0,
                                    trigger = null,
                                    customUrgeAbout = null
                                )
                                repo.logEvent(completionEvent).fold(
                                    onSuccess = {
                                        val oldMilestone = currentMilestone
                                        
                                        // Reload events and update counts
                                        events = repo.getAllEvents()
                                        urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
                                        completedActionsTotal = events.count { it.eventType == EventType.AVOIDED_TASK }
                                        
                                        // Check for new milestone achievement
                                        val newMilestone = StatsComputer.getLatestAchievedMilestone(events)
                                        android.util.Log.d("MilestoneCelebration", "Old: ${oldMilestone?.name}, New: ${newMilestone?.name}")
                                        
                                        if (newMilestone != null && newMilestone != oldMilestone) {
                                            currentMilestone = newMilestone
                                            showMilestoneBanner = true
                                            
                                            android.util.Log.d("MilestoneCelebration", "Showing celebration for: ${newMilestone.name}")
                                            
                                            // Show celebration toast
                                            Toast.makeText(
                                                context, 
                                                "🎉 Milestone achieved: ${newMilestone.name}!", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        
                                        // Get next available action AFTER successful save
                                        currentActionItem = nextRightActionManager.pickNextRightAction(
                                            recentTriggers = emptyList()
                                        )
                                    },
                                    onFailure = { 
                                        // Get next available action even if save failed
                                        currentActionItem = nextRightActionManager.pickNextRightAction(
                                            recentTriggers = emptyList()
                                        )
                                    }
                                )
                            }
                            
                            subtleHaptic(context)
                        },
                        onSnooze = {
                            // Get next available action - simplified
                            currentActionItem = nextRightActionManager.pickNextRightAction(
                                recentTriggers = emptyList()
                            )
                        },
                        onTimeChange = {
                            // TODO: Implement time customization dialog
                        },
                        onActionSelected = { selectedItem ->
                            android.util.Log.d("MandrakeAction", "Action selected: ${selectedItem.text}, id: ${selectedItem.id}, category: ${selectedItem.category}")
                            currentActionItem = selectedItem
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Escalation nudge based on risk assessment
                if (shouldShowNudge && latestRiskAssessment != null && latestRiskAssessment!!.nudgeTier != com.repinfaust.mandrake.data.entity.NudgeTier.NONE) {
                    val nudgeContent = RiskRules.getNudgeContent(latestRiskAssessment!!.nudgeTier)
                    EscalationNudgeCard(
                        content = nudgeContent,
                        onViewOptions = {
                            // TODO: Show education sheet with urge surfing, wave timer info
                        },
                        onViewSupport = {
                            nav.navigate(Routes.Crisis.route)
                        },
                        onDismiss = {
                            shouldShowNudge = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Scroll indicator - shows when progress counters are below the fold  
                var shouldBlink by remember { mutableStateOf(false) }
                val hasProgress = urgesBypassed > 0 || completedActionsTotal > 0
                
                // Control blinking animation
                LaunchedEffect(urgesBypassed, completedActionsTotal) {
                    if (urgesBypassed > 0 || completedActionsTotal > 0) {
                        shouldBlink = true
                        // Stop blinking after 10 seconds
                        kotlinx.coroutines.delay(10000)
                        shouldBlink = false
                    }
                }
                
                if (hasProgress) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                scope.launch {
                                    // Calculate scroll position to show progress counters
                                    // Rough estimate based on screen layout
                                    val targetScroll = 800 // Adjust based on your layout height
                                    mainScrollState.animateScrollTo(targetScroll)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val alpha = if (shouldBlink) {
                            val infiniteTransition = rememberInfiniteTransition(label = "scroll_indicator")
                            infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scroll_alpha"
                            ).value
                        } else {
                            0.6f // Static opacity when not blinking
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Your progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll to see progress",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Only show progress after first win to avoid early demotivation
                if (urgesBypassed > 0 || completedActionsTotal > 0) {
                    ProgressBar(urgesBypassed, completedActionsTotal)
                }
        }
        
        // Overlays
        if (showLog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showLog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.clickable(enabled = false) { }
                ) {
                    QuickLogSheet(
                        onDismiss = { showLog = false },
                        onSave = { event ->
                            scope.launch {
                                val oldMilestone = currentMilestone
                                repo.logEvent(event).fold(
                                    onSuccess = {
                                        events = repo.getAllEvents()
                                        val newMilestone =
                                            StatsComputer.getLatestAchievedMilestone(events)

                                        if (newMilestone != null && newMilestone != oldMilestone) {
                                            currentMilestone = newMilestone
                                            showMilestoneBanner = true
                                            
                                            // Show celebration toast
                                            Toast.makeText(
                                                context, 
                                                "🎉 Milestone achieved: ${newMilestone.name}!", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
                                        completedActionsTotal = events.count { it.eventType == EventType.AVOIDED_TASK }
                                        lastUrgeFlowTime = System.currentTimeMillis()
                                        subtleHaptic(context)
                                        showLog = false
                                        
                                        // Trigger behavioral risk assessment after logging event
                                        riskRepo.performBehavioralRiskAssessment(events).onSuccess { assessment ->
                                            latestRiskAssessment = assessment
                                            shouldShowNudge = riskRepo.shouldShowNudge()
                                        }
                                    },
                                    onFailure = {
                                        showLog = false
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
        
        if (showGaveInLog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showGaveInLog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.clickable(enabled = false) { }
                ) {
                    GaveInLogSheet(
                        onDismiss = { showGaveInLog = false },
                        onSave = { event ->
                            scope.launch {
                                repo.logEvent(event).fold(
                                    onSuccess = {
                                        events = repo.getAllEvents()
                                        lastUrgeFlowTime = System.currentTimeMillis()
                                        subtleHaptic(context)
                                        showGaveInLog = false
                                        
                                        // Show NextRightAction selection after repair step
                                        currentActionItem = nextRightActionManager.pickNextRightAction(
                                            recentTriggers = emptyList()
                                        )
                                        showRepairModal = true
                                        
                                        // Trigger behavioral risk assessment after gave-in event
                                        riskRepo.performBehavioralRiskAssessment(events).onSuccess { assessment ->
                                            latestRiskAssessment = assessment
                                            shouldShowNudge = riskRepo.shouldShowNudge()
                                        }
                                    },
                                    onFailure = {
                                        showGaveInLog = false
                                    }
                                )
                            }
                        },
                        onJustLog = { event ->
                            scope.launch {
                                repo.logEvent(event).fold(
                                    onSuccess = {
                                        events = repo.getAllEvents()
                                        lastUrgeFlowTime = System.currentTimeMillis()
                                        subtleHaptic(context)
                                        showGaveInLog = false
                                        
                                        // Trigger behavioral risk assessment after gave-in event
                                        riskRepo.performBehavioralRiskAssessment(events).onSuccess { assessment ->
                                            latestRiskAssessment = assessment
                                            shouldShowNudge = riskRepo.shouldShowNudge()
                                        }
                                    },
                                    onFailure = {
                                        showGaveInLog = false
                                    }
                                )
                            }
                        },
                        navController = nav
                    )
                }
            }
        }

        // NextRightAction completion sheet
        if (showActionCompleteSheet) {
            NextRightActionCompleteSheet(
                completedAction = completedAction,
                onDismiss = {
                    showActionCompleteSheet = false
                }
            )
        }
        
        // Screening dialog
        if (showScreening) {
            ScreeningSheet(
                category = screeningCategory,
                screeningType = screeningType,
                repository = riskRepo,
                onDismiss = { showScreening = false },
                onComplete = {
                    showScreening = false
                    // Refresh behavioral risk assessment after screening
                    scope.launch {
                        riskRepo.performBehavioralRiskAssessment(events).onSuccess { assessment ->
                            latestRiskAssessment = assessment
                            shouldShowNudge = riskRepo.shouldShowNudge()
                        }
                    }
                },
                onViewSupport = {
                    showScreening = false
                    nav.navigate(Routes.Crisis.route)
                }
            )
        }
        
        // Repair step modal
        if (showRepairModal && currentActionItem != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showRepairModal = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clickable(enabled = false) { }
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Repair Step",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Try this to help rebuild your confidence:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            NextRightActionCard(
                                actionItem = currentActionItem!!,
                                availableItems = availableActionItems,
                                onDoItNow = {
                                    completedAction = currentActionItem!!.text
                                    showActionCompleteSheet = true
                                    showRepairModal = false
                                    
                                    // Save to Firestore and update counts
                                    scope.launch {
                                        val completionEvent = UrgeEvent(
                                            id = 0,
                                            timestamp = System.currentTimeMillis(),
                                            eventType = EventType.AVOIDED_TASK,
                                            tactic = null,
                                            mood = null,
                                            usedWaveTimer = false,
                                            durationSeconds = currentActionItem!!.userMinutes * 60,
                                            intensity = 0,
                                            trigger = null
                                        )
                                        repo.logEvent(completionEvent).fold(
                                            onSuccess = {
                                                val oldMilestone = currentMilestone
                                                
                                                // Reload events and update counts
                                                events = repo.getAllEvents()
                                                urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
                                                completedActionsTotal = events.count { it.eventType == EventType.AVOIDED_TASK }
                                                
                                                // Check for new milestone achievement
                                                val newMilestone = StatsComputer.getLatestAchievedMilestone(events)
                                                if (newMilestone != null && newMilestone != oldMilestone) {
                                                    currentMilestone = newMilestone
                                                    showMilestoneBanner = true
                                                    Toast.makeText(
                                                        context, 
                                                        "🎉 Milestone achieved: ${newMilestone.name}!", 
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            },
                                            onFailure = { }
                                        )
                                    }
                                    
                                    subtleHaptic(context)
                                },
                                onDoneIt = {
                                    completedAction = currentActionItem!!.text
                                    showActionCompleteSheet = true
                                    showRepairModal = false
                                    
                                    // Save to Firestore and update counts
                                    scope.launch {
                                        val completionEvent = UrgeEvent(
                                            id = 0,
                                            timestamp = System.currentTimeMillis(),
                                            eventType = EventType.AVOIDED_TASK,
                                            tactic = null,
                                            mood = null,
                                            usedWaveTimer = false,
                                            durationSeconds = currentActionItem!!.userMinutes * 60,
                                            intensity = 0,
                                            trigger = null
                                        )
                                        repo.logEvent(completionEvent).fold(
                                            onSuccess = {
                                                val oldMilestone = currentMilestone
                                                
                                                // Reload events and update counts
                                                events = repo.getAllEvents()
                                                urgesBypassed = events.count { it.eventType == EventType.BYPASSED_URGE }
                                                completedActionsTotal = events.count { it.eventType == EventType.AVOIDED_TASK }
                                                
                                                // Check for new milestone achievement
                                                val newMilestone = StatsComputer.getLatestAchievedMilestone(events)
                                                if (newMilestone != null && newMilestone != oldMilestone) {
                                                    currentMilestone = newMilestone
                                                    showMilestoneBanner = true
                                                    Toast.makeText(
                                                        context, 
                                                        "🎉 Milestone achieved: ${newMilestone.name}!", 
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            },
                                            onFailure = { }
                                        )
                                    }
                                    
                                    // Get next available action
                                    currentActionItem = nextRightActionManager.pickNextRightAction(
                                        recentTriggers = emptyList()
                                    )
                                    
                                    subtleHaptic(context)
                                },
                                onSnooze = {
                                    showRepairModal = false
                                },
                                onTimeChange = {
                                    // TODO: Implement time customization dialog
                                },
                                onActionSelected = { selectedItem ->
                                    currentActionItem = selectedItem
                                }
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showRepairModal = false }) {
                                    Text("Skip for now")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Milestone banner overlays content (BoxScope allows align)
        currentMilestone?.let { milestone ->
            MilestoneBanner(
                milestone = milestone,
                visible = showMilestoneBanner,
                onDismiss = { showMilestoneBanner = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
    }
}
