package com.repinfaust.mandrake.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.repinfaust.mandrake.ui.viewmodels.BillingViewModel
import java.util.concurrent.TimeUnit

@Composable
fun EntitlementGate(
    onNavigateToSubscription: () -> Unit,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val billingViewModel: BillingViewModel = viewModel()
    val uiState by billingViewModel.uiState.collectAsState()
    
    // Check if user has active entitlement or is in free trial
    val hasAccess = remember(uiState.entitlement) {
        val now = System.currentTimeMillis()
        val installTime = getInstallTime(context)
        val trialDurationMs = TimeUnit.DAYS.toMillis(7) // 7-day free trial
        val isInTrial = (now - installTime) < trialDurationMs
        
        uiState.entitlement.active || isInTrial
    }
    
    if (hasAccess) {
        content()
    } else {
        PaywallScreen(
            onNavigateToSubscription = onNavigateToSubscription,
            onNavigateBack = onNavigateBack
        )
    }
}

private fun getInstallTime(context: android.content.Context): Long {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.firstInstallTime
    } catch (e: Exception) {
        // Fallback to current time if we can't get install time
        System.currentTimeMillis()
    }
}