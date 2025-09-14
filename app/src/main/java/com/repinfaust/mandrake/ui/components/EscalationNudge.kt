package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.repinfaust.mandrake.data.entity.NudgeTier
import com.repinfaust.mandrake.domain.NudgeContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscalationNudgeCard(
    content: NudgeContent,
    onViewOptions: () -> Unit,
    onViewSupport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // UI patterns based on tier: Soft = inline, Firm = banner-like
    val cardColors = when (content.tier) {
        NudgeTier.SOFT -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        NudgeTier.FIRM -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
        NudgeTier.NONE -> CardDefaults.cardColors()
    }
    
    val titleColor = when (content.tier) {
        NudgeTier.SOFT -> MaterialTheme.colorScheme.primary
        NudgeTier.FIRM -> MaterialTheme.colorScheme.onErrorContainer
        NudgeTier.NONE -> MaterialTheme.colorScheme.onSurface
    }
    
    val elevation = when (content.tier) {
        NudgeTier.SOFT -> CardDefaults.cardElevation(defaultElevation = 2.dp)
        NudgeTier.FIRM -> CardDefaults.cardElevation(defaultElevation = 6.dp)
        NudgeTier.NONE -> CardDefaults.cardElevation()
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = cardColors,
        elevation = elevation
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            
            // Body text
            Text(
                text = content.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Action buttons based on tier
            when (content.tier) {
                NudgeTier.SOFT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Not now")
                        }
                        
                        Button(
                            onClick = onViewOptions,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Show options")
                        }
                    }
                }
                
                NudgeTier.FIRM -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onViewSupport,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View support options")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Later")
                            }
                            
                            OutlinedButton(
                                onClick = onViewOptions,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Quick tools")
                            }
                        }
                    }
                }
                
                NudgeTier.NONE -> {
                    // Should not render for NONE tier
                }
            }
        }
    }
}

@Composable
fun SupportResourceTile(
    title: String,
    description: String,
    contact: String,
    isPhoneNumber: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = contact,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isPhoneNumber) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CrisisBanner(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️ If there's risk to life, call 999 now.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}