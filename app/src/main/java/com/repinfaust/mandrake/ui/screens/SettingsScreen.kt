package com.repinfaust.mandrake.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.navigation.NavController
import com.repinfaust.mandrake.auth.AuthService
import com.repinfaust.mandrake.data.repo.FirestoreUrgeRepository
import com.repinfaust.mandrake.data.db.AppDatabase
import com.repinfaust.mandrake.data.prefs.UserPrefsDataStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import com.repinfaust.mandrake.nav.Routes
import com.repinfaust.mandrake.data.repo.CustomChipRepository
import com.repinfaust.mandrake.data.entity.CustomChip
import com.repinfaust.mandrake.data.entity.ChipType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton

@Composable
fun SettingsScreen(nav: NavController) {
  val context = LocalContext.current
  val authService = remember { AuthService(context) }
  val userPrefsDataStore = remember { UserPrefsDataStore.getInstance(context) }
  val scope = rememberCoroutineScope()
  val database = remember { AppDatabase.get(context) }
  val urgeRepo = remember { FirestoreUrgeRepository() }
  val customChipRepo = remember { CustomChipRepository(database.customChipDao()) }
  
  var showResetDialog by remember { mutableStateOf(false) }
  var showCelebrations by remember { mutableStateOf(true) }
  var showRegionDialog by remember { mutableStateOf(false) }
  var showChipManagement by remember { mutableStateOf(false) }
  
  var tacticChips by remember { mutableStateOf<List<CustomChip>>(emptyList()) }
  var urgeAboutChips by remember { mutableStateOf<List<CustomChip>>(emptyList()) }
  
  LaunchedEffect(Unit) {
    tacticChips = customChipRepo.getAllChipsForType(ChipType.TACTIC)
    urgeAboutChips = customChipRepo.getAllChipsForType(ChipType.URGE_ABOUT)
  }
  
  val currentRegion by userPrefsDataStore.supportRegion.collectAsState(initial = "UK")
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // Mandrake logo
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(id = com.repinfaust.mandrake.R.drawable.vertical_primary_rounded_1024),
        contentDescription = "Mandrake Logo",
        modifier = Modifier.height(150.dp)
      )
    }
    
    Text(
      text = "Settings",
      style = MaterialTheme.typography.displaySmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(vertical = 16.dp)
    )
    
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = "Account",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
          text = "Signed in anonymously - no personal data collected",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Subscription section
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = "Subscription",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { nav.navigate(Routes.Subscription.route) }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Manage Subscription",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "View plans and manage your subscription",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Safety suggestions settings
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = "Safety & Support",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        
        var showSafetySuggestions by remember { mutableStateOf(true) }
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Show safety suggestions",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Get gentle nudges toward support when patterns look heavy",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = showSafetySuggestions,
            onCheckedChange = { showSafetySuggestions = it }
          )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Show milestone celebrations",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Celebrate achievements with confetti and messages",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = showCelebrations,
            onCheckedChange = { showCelebrations = it }
          )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showRegionDialog = true }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Region for support numbers: $currentRegion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
            contentDescription = "Change region",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // FAQ Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth()
      ) {
        Text(
          text = "FAQ",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Why don't you use Google Sign-In?
        Text(
          text = "Why don't you use Google Sign-In?",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
          text = "We keep things private by default. That means no email logins, no accounts, no personal identifiers. Your wins and logs stay yours.\n\nConvenience (like syncing across devices) is nice, but for this app, trust matters more than convenience. We'd rather you never worry about who can see your data.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Do you store any data in the cloud?
        Text(
          text = "Do you store any data in the cloud?",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
          text = "Yes — but only in the smallest, safest way. We use Firestore with a random anonymous ID (UID) to record your progress.\n\nNo names, no emails, no personal details.\n\nJust numbers: your wins, tactics, and moods.\n\nYou stay anonymous, even to us.\n\nThis helps the app run smoothly and show your progress, without tying it back to \"you\" as a person.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Can I back up or export my data?
        Text(
          text = "Can I back up or export my data?",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
          text = "Yes. You can export or delete everything anytime in Settings. Your data belongs to you — always.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Privacy & Data
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = "Privacy & Data",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
          text = "All screening results and risk assessments are stored locally on your device only. You can export or delete your data at any time.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              scope.launch {
                try {
                  val events = urgeRepo.getAllEvents()
                  val csvContent = buildString {
                    appendLine("timestamp,eventType,intensity,tactic,mood,trigger,urgeAbout,durationSeconds")
                    events.forEach { event ->
                      appendLine("${event.timestamp},${event.eventType},${event.intensity},${event.tactic?.name ?: ""},${event.mood?.name ?: ""},${event.trigger?.name ?: ""},${event.urgeAbout ?: ""},${event.durationSeconds}")
                    }
                  }
                  
                  val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, csvContent)
                    type = "text/plain"
                  }
                  context.startActivity(Intent.createChooser(shareIntent, "Export Mandrake Data"))
                } catch (e: Exception) {
                  android.util.Log.e("MandrakeExport", "Failed to export data", e)
                }
              }
            },
            modifier = Modifier.weight(1f)
          ) {
            Text("Export Data")
          }
          
          OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.error
            )
          ) {
            Text("Reset All Data")
          }
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Custom Chip Management
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Text(
          text = "Custom Chips",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
          text = "Manage your custom urge categories and alternative actions",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showChipManagement = true }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Manage Custom Chips",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "${tacticChips.size} alternative actions, ${urgeAboutChips.size} urge categories",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
    
    // Region selection dialog
    if (showRegionDialog) {
      AlertDialog(
        onDismissRequest = { showRegionDialog = false },
        title = { Text("Support Region") },
        text = {
          Column {
            Text(
              text = "Choose your region for emergency contacts and support numbers:",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(bottom = 16.dp)
            )
            
            listOf("UK", "USA").forEach { region ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    scope.launch {
                      userPrefsDataStore.setSupportRegion(region)
                      showRegionDialog = false
                    }
                  }
                  .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = currentRegion == region,
                  onClick = {
                    scope.launch {
                      userPrefsDataStore.setSupportRegion(region)
                      showRegionDialog = false
                    }
                  }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = region,
                  style = MaterialTheme.typography.bodyLarge
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showRegionDialog = false }) {
            Text("Done")
          }
        }
      )
    }
    
    // Reset confirmation dialog
    if (showResetDialog) {
      AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Reset All Data?") },
        text = { Text("This will permanently delete all your progress, events, and settings. This action cannot be undone.") },
        confirmButton = {
          TextButton(
            onClick = {
              scope.launch {
                try {
                  android.util.Log.d("MandrakeReset", "Starting data reset")
                  
                  // Clear local database on IO thread
                  withContext(Dispatchers.IO) {
                    database.clearAllTables()
                  }
                  android.util.Log.d("MandrakeReset", "Local database cleared")
                  
                  // Clear Firestore data
                  try {
                    val events = urgeRepo.getAllEvents()
                    android.util.Log.d("MandrakeReset", "Found ${events.size} Firestore events to delete")
                    
                    // Actually delete the Firestore documents
                    urgeRepo.deleteAllEvents().fold(
                      onSuccess = {
                        android.util.Log.d("MandrakeReset", "Successfully deleted all Firestore events")
                      },
                      onFailure = { e ->
                        android.util.Log.e("MandrakeReset", "Failed to delete Firestore events", e)
                      }
                    )
                  } catch (e: Exception) {
                    android.util.Log.e("MandrakeReset", "Failed to clear Firestore data", e)
                  }
                  
                  android.util.Log.d("MandrakeReset", "Reset completed")
                  showResetDialog = false
                } catch (e: Exception) {
                  android.util.Log.e("MandrakeReset", "Failed to reset data", e)
                  showResetDialog = false
                }
              }
            },
            colors = ButtonDefaults.textButtonColors(
              contentColor = MaterialTheme.colorScheme.error
            )
          ) {
            Text("Reset")
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
    
    // App Version at bottom
    Spacer(modifier = Modifier.height(24.dp))
    
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center
    ) {
      val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
      } catch (e: Exception) {
        "Unknown"
      }
      
      Text(
        text = "Version $versionName",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 16.dp)
      )
    }
    
    // Custom Chip Management Dialog
    if (showChipManagement) {
      AlertDialog(
        onDismissRequest = { showChipManagement = false },
        title = { Text("Manage Custom Chips") },
        text = {
          LazyColumn(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (tacticChips.isNotEmpty()) {
              item {
                Text(
                  text = "Alternative Actions:",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 4.dp)
                )
              }
              items(tacticChips) { chip ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = chip.text,
                      style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                      text = "Used ${chip.usageCount} times",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  IconButton(
                    onClick = {
                      scope.launch {
                        customChipRepo.deleteChip(chip.id)
                        tacticChips = customChipRepo.getAllChipsForType(ChipType.TACTIC)
                      }
                    }
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete chip",
                      tint = MaterialTheme.colorScheme.error
                    )
                  }
                }
              }
              item {
                Spacer(modifier = Modifier.height(16.dp))
              }
            }
            
            if (urgeAboutChips.isNotEmpty()) {
              item {
                Text(
                  text = "Urge Categories:",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 4.dp)
                )
              }
              items(urgeAboutChips) { chip ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = chip.text,
                      style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                      text = "Used ${chip.usageCount} times",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  IconButton(
                    onClick = {
                      scope.launch {
                        customChipRepo.deleteChip(chip.id)
                        urgeAboutChips = customChipRepo.getAllChipsForType(ChipType.URGE_ABOUT)
                      }
                    }
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete chip",
                      tint = MaterialTheme.colorScheme.error
                    )
                  }
                }
              }
            }
            
            if (tacticChips.isEmpty() && urgeAboutChips.isEmpty()) {
              item {
                Text(
                  text = "No custom chips yet. Create some by selecting 'Other' when logging urges and alternative actions.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(16.dp)
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showChipManagement = false }) {
            Text("Done")
          }
        }
      )
    }
  }
}
