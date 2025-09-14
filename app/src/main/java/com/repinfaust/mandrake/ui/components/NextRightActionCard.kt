package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repinfaust.mandrake.domain.model.ActionTemplates
import com.repinfaust.mandrake.domain.model.NextRightActionItem
import com.repinfaust.mandrake.ui.theme.DarkForestGreen
import com.repinfaust.mandrake.ui.theme.SageGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextRightActionCard(
    actionItem: NextRightActionItem,
    availableItems: List<NextRightActionItem> = emptyList(),
    onDoItNow: () -> Unit,
    onDoneIt: () -> Unit,
    onSnooze: () -> Unit,
    onTimeChange: () -> Unit,
    onActionSelected: (NextRightActionItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showActionDialog by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkForestGreen.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Do one avoided thing",
                    fontSize = 14.sp,
                    color = SageGreen,
                    modifier = Modifier.weight(1f)
                )
                
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = ActionTemplates.getDomainLabel(actionItem.category),
                fontSize = 12.sp,
                color = SageGreen.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Action text with clickable visual indicator
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .then(
                        if (availableItems.isNotEmpty()) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    SageGreen.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { showActionDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = actionItem.text,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (availableItems.isNotEmpty()) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Change action",
                            tint = SageGreen.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Button(
                onClick = onDoneIt,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Done it",
                    color = DarkForestGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Action selection dialog
        if (showActionDialog && availableItems.isNotEmpty()) {
            ActionSelectionDialog(
                currentItem = actionItem,
                availableItems = availableItems,
                onActionSelected = { selectedItem ->
                    onActionSelected(selectedItem)
                    showActionDialog = false
                },
                onDismiss = {
                    showActionDialog = false
                }
            )
        }
    }
}