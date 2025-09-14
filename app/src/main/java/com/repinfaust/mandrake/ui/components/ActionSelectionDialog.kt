package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.repinfaust.mandrake.domain.model.ActionDomain
import com.repinfaust.mandrake.domain.model.ActionTemplates
import com.repinfaust.mandrake.domain.model.NextRightActionItem
import com.repinfaust.mandrake.ui.theme.DarkForestGreen
import com.repinfaust.mandrake.ui.theme.SageGreen
import com.repinfaust.mandrake.data.entity.CustomChip
import com.repinfaust.mandrake.data.entity.ChipType
import com.repinfaust.mandrake.data.repo.CustomChipRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun ActionSelectionDialog(
    currentItem: NextRightActionItem,
    availableItems: List<NextRightActionItem>,
    onActionSelected: (NextRightActionItem) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customActionText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ActionDomain.ENVIRONMENT) }
    var showCreatePermanentDialog by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var customActions by remember { mutableStateOf<List<CustomChip>>(emptyList()) }
    
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val customChipRepo = remember { CustomChipRepository(database.customChipDao()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        customActions = customChipRepo.getChipsForType(ChipType.CUSTOM_ACTION)
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkForestGreen
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Choose an action",
                    fontSize = 18.sp,
                    color = SageGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group by category
                    ActionDomain.values().forEach { domain ->
                        val domainItems = availableItems.filter { it.category == domain }
                        val domainCustomActions = customActions.filter { 
                            it.actionCategory == domain.name 
                        }
                        
                        if (domainItems.isNotEmpty() || domainCustomActions.isNotEmpty()) {
                            item {
                                Text(
                                    text = ActionTemplates.getDomainLabel(domain),
                                    fontSize = 14.sp,
                                    color = SageGreen.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            
                            // Regular domain items
                            items(domainItems) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.id == currentItem.id) 
                                            SageGreen.copy(alpha = 0.2f) 
                                        else 
                                            Color.White.copy(alpha = 0.05f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {
                                        onActionSelected(item)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.text,
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                    }
                                }
                            }
                            
                            // Custom actions for this domain
                            items(domainCustomActions) { customAction ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.05f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {
                                        val customItem = NextRightActionItem(
                                            id = "custom_${customAction.id}",
                                            category = domain,
                                            text = customAction.text,
                                            defaultMinutes = 2,
                                            userMinutes = 2
                                        )
                                        scope.launch {
                                            customChipRepo.createOrIncrementChip(customAction.text, ChipType.CUSTOM_ACTION)
                                        }
                                        onActionSelected(customItem)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = customAction.text,
                                            fontSize = 14.sp,
                                            color = SageGreen,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                    }
                                }
                            }
                        }
                    }
                    
                    // Add custom action option
                    item {
                        if (showCustomInput) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Add Custom Action",
                                    fontSize = 14.sp,
                                    color = SageGreen.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                OutlinedTextField(
                                    value = customActionText,
                                    onValueChange = { customActionText = it },
                                    label = { Text("Custom action", color = Color.White.copy(alpha = 0.7f)) },
                                    placeholder = { Text("e.g., Organize drawer, Make bed...", color = Color.White.copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SageGreen,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = SageGreen
                                    ),
                                    maxLines = 1
                                )
                                
                                
                                Box {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.05f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        onClick = { showCategoryDropdown = true }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Category: ${ActionTemplates.getDomainLabel(selectedCategory)}",
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "▼",
                                                fontSize = 12.sp,
                                                color = SageGreen
                                            )
                                        }
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showCategoryDropdown,
                                        onDismissRequest = { showCategoryDropdown = false },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(DarkForestGreen)
                                    ) {
                                        ActionDomain.values().forEach { domain ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        ActionTemplates.getDomainLabel(domain), 
                                                        color = Color.White
                                                    ) 
                                                },
                                                onClick = { 
                                                    selectedCategory = domain
                                                    showCategoryDropdown = false
                                                },
                                                colors = MenuDefaults.itemColors(
                                                    textColor = Color.White,
                                                    leadingIconColor = Color.White,
                                                    trailingIconColor = Color.White,
                                                    disabledTextColor = Color.White.copy(alpha = 0.5f),
                                                    disabledLeadingIconColor = Color.White.copy(alpha = 0.5f),
                                                    disabledTrailingIconColor = Color.White.copy(alpha = 0.5f)
                                                ),
                                                modifier = Modifier.background(DarkForestGreen)
                                            )
                                        }
                                    }
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(
                                        onClick = { 
                                            showCustomInput = false
                                            customActionText = ""
                                        }
                                    ) {
                                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                                    }
                                    
                                    TextButton(
                                        onClick = { showCreatePermanentDialog = true },
                                        enabled = customActionText.isNotBlank()
                                    ) {
                                        Text("Save Permanently", color = SageGreen)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            if (customActionText.isNotBlank()) {
                                                val customItem = NextRightActionItem(
                                                    id = "custom_${System.currentTimeMillis()}",
                                                    category = selectedCategory,
                                                    text = customActionText.trim(),
                                                    defaultMinutes = 2,
                                                    userMinutes = 2
                                                )
                                                onActionSelected(customItem)
                                            }
                                        },
                                        enabled = customActionText.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SageGreen
                                        )
                                    ) {
                                        Text("Add", color = DarkForestGreen)
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                onClick = { showCustomInput = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add custom action",
                                        tint = SageGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Add custom action",
                                        fontSize = 14.sp,
                                        color = SageGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            "Cancel",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
    
    if (showCreatePermanentDialog) {
        Dialog(onDismissRequest = { showCreatePermanentDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkForestGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Permanent Action",
                        fontSize = 18.sp,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "This will save \"${customActionText}\" as a reusable action that will appear in this menu for future use.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showCreatePermanentDialog = false }
                        ) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (customActionText.isNotBlank()) {
                                    scope.launch {
                                        customChipRepo.createCustomAction(
                                            text = customActionText.trim(),
                                            category = selectedCategory,
                                            minutes = 2
                                        )
                                        customActions = customChipRepo.getChipsForType(ChipType.CUSTOM_ACTION)
                                    }
                                    
                                    val customItem = NextRightActionItem(
                                        id = "custom_${System.currentTimeMillis()}",
                                        category = selectedCategory,
                                        text = customActionText.trim(),
                                        defaultMinutes = 2,
                                        userMinutes = 2
                                    )
                                    
                                    showCreatePermanentDialog = false
                                    showCustomInput = false
                                    customActionText = ""
                                    onActionSelected(customItem)
                                }
                            },
                            enabled = customActionText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SageGreen
                            )
                        ) {
                            Text("Create & Use", color = DarkForestGreen)
                        }
                    }
                }
            }
        }
    }
}