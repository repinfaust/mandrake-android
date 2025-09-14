package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.repinfaust.mandrake.data.entity.RiskBand
import com.repinfaust.mandrake.domain.ScreeningQuestion
import com.repinfaust.mandrake.domain.ScreeningQuestions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreeningQuestionnaire(
    questions: List<ScreeningQuestion>,
    title: String,
    subtitle: String,
    onComplete: (List<Int>) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var responses by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val isLastQuestion = currentQuestionIndex == questions.size - 1
    val canProceed = responses.containsKey(currentQuestionIndex)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // Privacy notice
                Text(
                    text = "This stays on your device. Skip anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Progress indicator
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1).toFloat() / questions.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            
            Text(
                text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            currentQuestion?.let { question ->
                // Question text
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Answer options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.options.forEachIndexed { optionIndex, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = responses[currentQuestionIndex] == question.scores[optionIndex],
                                    onClick = {
                                        responses = responses.toMutableMap().apply {
                                            put(currentQuestionIndex, question.scores[optionIndex])
                                        }
                                    }
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = responses[currentQuestionIndex] == question.scores[optionIndex],
                                onClick = {
                                    responses = responses.toMutableMap().apply {
                                        put(currentQuestionIndex, question.scores[optionIndex])
                                    }
                                }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back/Skip button
                if (currentQuestionIndex > 0) {
                    TextButton(
                        onClick = { 
                            currentQuestionIndex = maxOf(0, currentQuestionIndex - 1)
                        }
                    ) {
                        Text("Back")
                    }
                } else {
                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                }
                
                // Next/Complete button
                Button(
                    onClick = {
                        if (isLastQuestion) {
                            // Complete screening
                            val responseList = (0 until questions.size).map { index ->
                                responses[index] ?: 0
                            }
                            onComplete(responseList)
                        } else {
                            // Next question
                            currentQuestionIndex = minOf(questions.size - 1, currentQuestionIndex + 1)
                        }
                    },
                    enabled = canProceed
                ) {
                    Text(if (isLastQuestion) "Complete" else "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreeningResultCard(
    band: RiskBand,
    category: String,
    onDismiss: () -> Unit,
    onViewSupport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bandColor = when (band) {
        RiskBand.LOW -> MaterialTheme.colorScheme.tertiary
        RiskBand.ELEVATED -> MaterialTheme.colorScheme.secondary
        RiskBand.HIGH -> MaterialTheme.colorScheme.error
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Screening Complete",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = bandColor
            )
            
            Text(
                text = ScreeningQuestions.getBandDescription(band),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = ScreeningQuestions.getBandSuggestion(band),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (onViewSupport != null) 
                    Arrangement.SpaceBetween else Arrangement.End
            ) {
                if (onViewSupport != null && band == RiskBand.HIGH) {
                    OutlinedButton(onClick = onViewSupport) {
                        Text("View Support")
                    }
                }
                
                Button(onClick = onDismiss) {
                    Text("Continue")
                }
            }
            
            // Privacy reminder
            Text(
                text = "This result is stored locally on your device only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}