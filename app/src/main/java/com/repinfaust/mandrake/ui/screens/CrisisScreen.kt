package com.repinfaust.mandrake.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import com.repinfaust.mandrake.ui.components.SupportResourceTile
import com.repinfaust.mandrake.ui.components.CrisisBanner
import com.repinfaust.mandrake.data.prefs.UserPrefsDataStore

data class SupportResource(
    val title: String,
    val description: String,
    val contact: String,
    val isPhoneNumber: Boolean = false,
    val url: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisScreen(nav: NavController) {
    val context = LocalContext.current
    val userPrefsDataStore = remember { UserPrefsDataStore.getInstance(context) }
    val currentRegion by userPrefsDataStore.supportRegion.collectAsState(initial = "UK")
    
    val ukSupportResources = listOf(
        SupportResource(
            title = "NHS 111 Mental Health",
            description = "24/7 urgent mental health support. Free and confidential.",
            contact = "Call 111",
            isPhoneNumber = true
        ),
        SupportResource(
            title = "Samaritans",
            description = "24/7 emotional support for anyone in distress or despair.",
            contact = "116 123",
            isPhoneNumber = true
        ),
        SupportResource(
            title = "Shout Crisis Text",
            description = "Free 24/7 text support for mental health crises.",
            contact = "Text SHOUT to 85258",
            isPhoneNumber = false
        ),
        SupportResource(
            title = "FRANK Drug Support",
            description = "Honest information about drugs. Confidential helpline.",
            contact = "0300 123 6600",
            isPhoneNumber = true
        ),
        SupportResource(
            title = "Alcoholics Anonymous",
            description = "Fellowship of people sharing experience to recover from alcoholism.",
            contact = "0800 917 7650",
            isPhoneNumber = true,
            url = "https://www.alcoholics-anonymous.org.uk"
        ),
        SupportResource(
            title = "Narcotics Anonymous",
            description = "Fellowship for people recovering from drug addiction.",
            contact = "0300 999 1212",
            isPhoneNumber = true,
            url = "https://www.ukna.org"
        ),
        SupportResource(
            title = "GamCare",
            description = "Support for anyone affected by gambling problems.",
            contact = "0808 8020 133",
            isPhoneNumber = true,
            url = "https://www.gamcare.org.uk"
        ),
        SupportResource(
            title = "NHS Service Finder",
            description = "Find local mental health services and support groups.",
            contact = "Visit NHS website",
            url = "https://www.nhs.uk/service-search/find-a-psychological-therapies-service"
        )
    )
    
    val usaSupportResources = listOf(
        SupportResource(
            title = "Emergency",
            description = "Call if life is at risk.",
            contact = "911",
            isPhoneNumber = true
        ),
        SupportResource(
            title = "988 Suicide & Crisis Lifeline",
            description = "24/7 connects to trained crisis counselors across the U.S.",
            contact = "988",
            isPhoneNumber = true,
            url = "https://988lifeline.org"
        ),
        SupportResource(
            title = "Crisis Text Line",
            description = "24/7 crisis support via text message.",
            contact = "Text HELLO to 741741",
            isPhoneNumber = false,
            url = "https://www.crisistextline.org"
        ),
        SupportResource(
            title = "SAMHSA National Helpline",
            description = "24/7 confidential treatment referral and information (English & Spanish).",
            contact = "1-800-662-HELP (4357)",
            isPhoneNumber = true,
            url = "https://www.samhsa.gov/find-help/national-helpline"
        ),
        SupportResource(
            title = "NAMI HelpLine",
            description = "National Alliance on Mental Illness support (Mon–Fri, 10am–10pm ET).",
            contact = "1-800-950-NAMI (6264)",
            isPhoneNumber = true,
            url = "https://www.nami.org/help"
        ),
        SupportResource(
            title = "The Trevor Project",
            description = "LGBTQ+ crisis & suicide prevention, 24/7.",
            contact = "1-866-488-7386",
            isPhoneNumber = true,
            url = "https://www.thetrevorproject.org"
        ),
        SupportResource(
            title = "Veterans Crisis Line",
            description = "24/7 support for veterans and their families.",
            contact = "988, then press 1",
            isPhoneNumber = true,
            url = "https://www.veteranscrisisline.net"
        )
    )
    
    val supportResources = if (currentRegion == "USA") usaSupportResources else ukSupportResources
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Support") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Crisis banner at top
            item {
                CrisisBanner()
            }
            
            // Introduction text
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Private Support Options",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "All contacts below are confidential. You're never blocked from using the app - these are additional resources when you need them.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Support resource tiles
            items(supportResources) { resource ->
                SupportResourceTile(
                    title = resource.title,
                    description = resource.description,
                    contact = resource.contact,
                    isPhoneNumber = resource.isPhoneNumber,
                    onClick = {
                        try {
                            when {
                                // Handle text messages
                                resource.contact.contains("Text SHOUT") -> {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:85258"))
                                    intent.putExtra("sms_body", "SHOUT")
                                    context.startActivity(intent)
                                }
                                resource.contact.contains("Text HELLO") -> {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:741741"))
                                    intent.putExtra("sms_body", "HELLO")
                                    context.startActivity(intent)
                                }
                                // Handle URLs
                                resource.url != null -> {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                    context.startActivity(intent)
                                }
                                // Handle phone numbers
                                resource.isPhoneNumber -> {
                                    // Extract numbers from contact string, handling various formats
                                    val phoneNumber = when {
                                        resource.contact.contains("Call ") -> 
                                            resource.contact.replace("Call ", "").replace(" ", "")
                                        resource.contact == "911" || resource.contact == "988" -> 
                                            resource.contact
                                        resource.contact.contains("988, then press 1") ->
                                            "988"
                                        else -> {
                                            // Simple extraction of numbers and common phone characters
                                            resource.contact.filter { it.isDigit() || it in listOf('-', '(', ')', ' ') }
                                                .replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
                                        }
                                    }
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                    context.startActivity(intent)
                                }
                            }
                        } catch (e: Exception) {
                            // If intent fails, try to open URL if available
                            resource.url?.let { url ->
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e2: Exception) {
                                    // Silently fail - better than crashing
                                }
                            }
                        }
                    }
                )
            }
            
            // Footer text
            item {
                Text(
                    text = "All data in this app stays on your device unless you choose to sync. You can export or delete your data at any time from Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
