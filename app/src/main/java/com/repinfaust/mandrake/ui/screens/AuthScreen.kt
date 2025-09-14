package com.repinfaust.mandrake.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.repinfaust.mandrake.auth.AuthService
import com.repinfaust.mandrake.nav.Routes
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    navController: NavController,
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authService = remember { AuthService(context) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = Identity.getSignInClient(context)
                    .getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                
                if (googleIdToken != null) {
                    scope.launch {
                        isLoading = true
                        val signInResult = authService.signInWithGoogle(googleIdToken)
                        isLoading = false
                        
                        signInResult.fold(
                            onSuccess = { onSignInSuccess() },
                            onFailure = { errorMessage = it.message }
                        )
                    }
                }
            } catch (e: ApiException) {
                isLoading = false
                errorMessage = "Sign in failed: ${e.message}"
            }
        } else {
            isLoading = false
        }
    }
    
    fun startSignIn() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                val signInRequest = authService.beginSignIn()
                val oneTapClient = Identity.getSignInClient(context)
                val result = oneTapClient.beginSignIn(signInRequest)
                
                result.addOnSuccessListener { beginSignInResult: BeginSignInResult ->
                    val intentSenderRequest = IntentSenderRequest.Builder(
                        beginSignInResult.pendingIntent.intentSender
                    ).build()
                    launcher.launch(intentSenderRequest)
                }.addOnFailureListener {
                    isLoading = false
                    errorMessage = "Sign in not available"
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mandrake Logo (lockup includes app name)
        Image(
            painter = painterResource(id = com.repinfaust.mandrake.R.drawable.ic_launcher_foreground),
            contentDescription = "Mandrake Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Build better habits, your way",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Anonymous Mode Button (Primary)
        Button(
            onClick = { 
                // Navigate to main app without sign-in
                onSignInSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue Privately",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Sign-in Button (Secondary)
        OutlinedButton(
            onClick = { startSignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🔐",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Anonymous: Your data stays on this device only.\nSign in: Sync across devices with Google account.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}