package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentCard by remember { mutableStateOf(0) }
    
    val cards = listOf(
        OnboardingCard(
            header = "Private wins, right when it counts.",
            body = "A pocket tool for urge moments — fast, quiet, yours.",
            actions = listOf("Skip", "Show me")
        ),
        OnboardingCard(
            header = "Not a program. A moment.",
            body = "No labels, no streak pressure. Not therapy. Not clinical. Just self-management.",
            secondary = "You're always in control."
        ),
        OnboardingCard(
            header = "One tap. Two prompts.",
            bullets = listOf(
                "Tap \"I chose an alternative.\"",
                "Log what helped (Walk • Call/Text • Music • Jot • Other).",
                "Say how you feel now (emoji row).",
                "Get a subtle haptic + \"That was a win.\""
            ),
            footer = "See when urges spike and what works best."
        ),
        OnboardingCard(
            header = "Private by default.",
            body = "Logs stay on your device. Export or delete anytime.",
            body2 = "If patterns get heavy, we'll gently signpost options. Crisis help is one tap away.",
            disclaimer = "This app supports self-management. It does not diagnose or treat.",
            actions = listOf("Try a demo log", "Open app")
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mandrake logo
                Image(
                    painter = painterResource(id = context.resources.getIdentifier("mandrake_splash_logo", "drawable", context.packageName)),
                    contentDescription = "Mandrake",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentCard + 1).toFloat() / cards.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Card content
                val card = cards[currentCard]
                
                Text(
                    text = card.header,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                card.body?.let { body ->
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                card.secondary?.let { secondary ->
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                card.bullets?.let { bullets ->
                    Column(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        bullets.forEach { bullet ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = bullet,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                card.body2?.let { body2 ->
                    Text(
                        text = body2,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                card.footer?.let { footer ->
                    Text(
                        text = footer,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                card.disclaimer?.let { disclaimer ->
                    Text(
                        text = disclaimer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions
                if (card.actions != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (card.actions.size == 2) 
                            Arrangement.SpaceBetween 
                        else 
                            Arrangement.Center
                    ) {
                        card.actions.forEachIndexed { index, action ->
                            when {
                                // Skip button (first card)
                                action == "Skip" -> {
                                    TextButton(onClick = onComplete) {
                                        Text(action)
                                    }
                                }
                                // Show me button (first card)
                                action == "Show me" -> {
                                    Button(onClick = { currentCard = 1 }) {
                                        Text(action)
                                    }
                                }
                                // Try a demo log (last card)
                                action == "Try a demo log" -> {
                                    OutlinedButton(onClick = {
                                        // TODO: Show demo logging flow
                                        onComplete()
                                    }) {
                                        Text(action)
                                    }
                                }
                                // Open app (last card)
                                action == "Open app" -> {
                                    Button(onClick = onComplete) {
                                        Text(action)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Auto-advance for middle cards
                    LaunchedEffect(currentCard) {
                        delay(4000) // Show for 4 seconds
                        if (currentCard < cards.size - 1) {
                            currentCard++
                        }
                    }
                    
                    // Manual advance button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentCard > 0) {
                            TextButton(onClick = { currentCard-- }) {
                                Text("Back")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        
                        Button(onClick = {
                            if (currentCard < cards.size - 1) {
                                currentCard++
                            } else {
                                onComplete()
                            }
                        }) {
                            Text(if (currentCard < cards.size - 1) "Next" else "Continue")
                        }
                    }
                }
            }
        }
    }
}

data class OnboardingCard(
    val header: String,
    val body: String? = null,
    val secondary: String? = null,
    val bullets: List<String>? = null,
    val body2: String? = null,
    val footer: String? = null,
    val disclaimer: String? = null,
    val actions: List<String>? = null
)