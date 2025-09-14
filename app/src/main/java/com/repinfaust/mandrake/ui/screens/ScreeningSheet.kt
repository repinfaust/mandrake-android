package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.repinfaust.mandrake.data.entity.ScreeningType
import com.repinfaust.mandrake.data.repo.RiskAssessmentRepository
import com.repinfaust.mandrake.domain.ScreeningQuestions
import com.repinfaust.mandrake.ui.components.ScreeningQuestionnaire
import com.repinfaust.mandrake.ui.components.ScreeningResultCard
import kotlinx.coroutines.launch

@Composable
fun ScreeningSheet(
    category: String,
    screeningType: ScreeningType,
    repository: RiskAssessmentRepository,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onViewSupport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var showResult by remember { mutableStateOf(false) }
    var resultBand by remember { mutableStateOf(com.repinfaust.mandrake.data.entity.RiskBand.LOW) }
    
    val questions = when (screeningType) {
        ScreeningType.AUDIT_C -> ScreeningQuestions.getAuditCQuestions()
        ScreeningType.SDS -> ScreeningQuestions.getSdsQuestions(category)
    }
    
    val title = when (screeningType) {
        ScreeningType.AUDIT_C -> "Quick Check: Alcohol"
        ScreeningType.SDS -> "Quick Check: ${category.replaceFirstChar { it.uppercase() }}"
    }
    
    val subtitle = "This helps us tailor the app to work better for you"
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showResult) {
                ScreeningResultCard(
                    band = resultBand,
                    category = category,
                    onDismiss = {
                        onComplete()
                        onDismiss()
                    },
                    onViewSupport = onViewSupport,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ScreeningQuestionnaire(
                    questions = questions,
                    title = title,
                    subtitle = subtitle,
                    onComplete = { responses ->
                        scope.launch {
                            val result = repository.saveScreeningResult(screeningType, category, responses)
                            result.onSuccess { screening ->
                                resultBand = screening.band
                                showResult = true
                            }.onFailure {
                                // Handle error - for now just dismiss
                                onDismiss()
                            }
                        }
                    },
                    onSkip = {
                        scope.launch {
                            repository.skipScreening(screeningType, category)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RedFlagMiniCheck(
    category: String,
    repository: RiskAssessmentRepository,
    onComplete: (com.repinfaust.mandrake.data.entity.RedFlags) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questions = ScreeningQuestions.getRedFlagQuestions(category)
    var responses by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Safety Check",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "A few quick yes/no questions to help us understand patterns better",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            questions.forEachIndexed { questionIndex, question ->
                Column {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        question.options.forEachIndexed { optionIndex, option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = responses[questionIndex] == question.scores[optionIndex],
                                    onClick = { 
                                        responses = responses.toMutableMap().apply {
                                            put(questionIndex, question.scores[optionIndex])
                                        }
                                    }
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
                
                Button(
                    onClick = {
                        val redFlags = com.repinfaust.mandrake.data.entity.RedFlags(
                            morningUse = responses[0] == 1,
                            withdrawal = responses[1] == 1,
                            blackout = responses[2] == 1,
                            failedCutDown = responses[3] == 1
                        )
                        onComplete(redFlags)
                    },
                    enabled = responses.size == questions.size
                ) {
                    Text("Complete")
                }
            }
        }
    }
}