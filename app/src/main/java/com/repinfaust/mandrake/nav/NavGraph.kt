package com.repinfaust.mandrake.nav

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.repinfaust.mandrake.auth.AuthService
import com.repinfaust.mandrake.ui.screens.AuthScreen
import com.repinfaust.mandrake.ui.screens.HomeScreen
import com.repinfaust.mandrake.ui.screens.JourneyScreen
import com.repinfaust.mandrake.ui.screens.RewardsScreen
import com.repinfaust.mandrake.ui.screens.ReviewScreen
import com.repinfaust.mandrake.ui.screens.MilestonesScreen
import com.repinfaust.mandrake.ui.screens.SettingsScreen
import com.repinfaust.mandrake.ui.screens.PrivacyScreen
import com.repinfaust.mandrake.ui.screens.CrisisScreen
import com.repinfaust.mandrake.ui.screens.OnboardingScreen
import com.repinfaust.mandrake.ui.screens.SubscriptionScreen
import com.repinfaust.mandrake.ui.components.EntitlementGate
import com.repinfaust.mandrake.data.prefs.UserPrefsDataStore
import androidx.compose.runtime.collectAsState

sealed class Routes(val route: String) {
  data object Auth : Routes("auth")
  data object Onboarding : Routes("onboarding")
  data object Home : Routes("home")
  data object Journey : Routes("journey")
  data object Rewards : Routes("rewards")
  data object Settings : Routes("settings")
  data object Privacy : Routes("privacy")
  data object Crisis : Routes("crisis")
  data object Subscription : Routes("subscription")
  // Legacy routes for compatibility
  data object Review : Routes("review")
  data object Milestones : Routes("milestones")
}

data class BottomNavItem(
  val route: String,
  val title: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val bottomNavItems = listOf(
  BottomNavItem(Routes.Home.route, "Home", Icons.Filled.Home),
  BottomNavItem(Routes.Journey.route, "Journey", Icons.Filled.List),
  BottomNavItem(Routes.Rewards.route, "Rewards", Icons.Filled.Star),
  BottomNavItem(Routes.Crisis.route, "Support", Icons.Filled.Phone),
  BottomNavItem(Routes.Settings.route, "Settings", Icons.Filled.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
  navController: NavHostController = rememberNavController()
) {
  val context = LocalContext.current
  val authService = remember { AuthService(context) }
  val userPrefsDataStore = remember { UserPrefsDataStore.getInstance(context) }
  val scope = rememberCoroutineScope()
  
  // Make auth state reactive
  var isSignedIn by remember { mutableStateOf(authService.isSignedIn) }
  var isSigningIn by remember { mutableStateOf(false) }
  
  // Check onboarding state - use null as initial to prevent flicker
  val onboardingCompleted by userPrefsDataStore.onboardingCompleted.collectAsState(initial = null)
  
  // Listen for auth state changes
  LaunchedEffect(authService.currentUser) {
    isSignedIn = authService.isSignedIn
  }

  // Auto sign in anonymously if not authenticated
  LaunchedEffect(Unit) {
    Log.d("MandrakeAuth", "LaunchedEffect: isSignedIn=$isSignedIn, isSigningIn=$isSigningIn")
    if (!authService.isSignedIn && !isSigningIn) {
      Log.d("MandrakeAuth", "Starting anonymous sign in...")
      isSigningIn = true
      authService.signInAnonymously().fold(
        onSuccess = { user ->
          Log.d("MandrakeAuth", "Anonymous sign in successful: ${user.uid}")
          isSignedIn = true
          isSigningIn = false
        },
        onFailure = { error ->
          Log.e("MandrakeAuth", "Anonymous sign in failed: ${error.message}")
          isSigningIn = false
        }
      )
    }
  }

  // Show loading, onboarding, or main app based on state
  when (onboardingCompleted) {
    null -> {
      // Loading state - show blank screen to prevent flicker
      Box(modifier = Modifier.fillMaxSize())
    }
    false -> {
      OnboardingScreen(
        navController = navController,
        onComplete = {
          scope.launch {
            userPrefsDataStore.setOnboardingCompleted(true)
          }
        }
      )
    }
    true -> {
    // Main app with persistent bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
      bottomBar = {
        NavigationBar(
          modifier = Modifier.fillMaxWidth()
        ) {
          bottomNavItems.forEach { item ->
            NavigationBarItem(
              icon = { Icon(item.icon, contentDescription = item.title) },
              label = { Text(item.title) },
              selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
              onClick = {
                navController.navigate(item.route) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            )
          }
        }
      }
    ) { paddingValues ->
      NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = Modifier.padding(paddingValues)
      ) {
        composable(Routes.Home.route) {
          EntitlementGate(
            onNavigateToSubscription = { navController.navigate(Routes.Subscription.route) },
            onNavigateBack = { navController.popBackStack() }
          ) {
            HomeScreen(navController)
          }
        }
        composable(Routes.Journey.route) {
          EntitlementGate(
            onNavigateToSubscription = { navController.navigate(Routes.Subscription.route) },
            onNavigateBack = { navController.popBackStack() }
          ) {
            JourneyScreen(navController)
          }
        }
        composable(Routes.Rewards.route) {
          EntitlementGate(
            onNavigateToSubscription = { navController.navigate(Routes.Subscription.route) },
            onNavigateBack = { navController.popBackStack() }
          ) {
            RewardsScreen(navController)
          }
        }
        // Legacy routes for compatibility
        composable(Routes.Review.route) {
          EntitlementGate(
            onNavigateToSubscription = { navController.navigate(Routes.Subscription.route) },
            onNavigateBack = { navController.popBackStack() }
          ) {
            JourneyScreen(navController) // Redirect to Journey
          }
        }
        composable(Routes.Milestones.route) {
          EntitlementGate(
            onNavigateToSubscription = { navController.navigate(Routes.Subscription.route) },
            onNavigateBack = { navController.popBackStack() }
          ) {
            JourneyScreen(navController) // Redirect to Journey
          }
        }
        composable(Routes.Settings.route) {
          SettingsScreen(navController)
        }
        composable(Routes.Privacy.route) {
          PrivacyScreen(navController)
        }
        composable(Routes.Crisis.route) {
          CrisisScreen(navController)
        }
        composable(Routes.Subscription.route) {
          SubscriptionScreen(
            onNavigateBack = { navController.popBackStack() }
          )
        }
        composable(Routes.Onboarding.route) {
          OnboardingScreen(
            navController = navController,
            onComplete = {
              scope.launch {
                userPrefsDataStore.setOnboardingCompleted(true)
                navController.navigate(Routes.Home.route) {
                  popUpTo(Routes.Onboarding.route) { inclusive = true }
                }
              }
            }
          )
        }
      }
    }
    }
  }
}
