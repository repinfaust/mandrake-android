package com.repinfaust.mandrake.ui.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(nav: androidx.navigation.NavController) {
  Scaffold(topBar = { TopAppBar(title = { Text("Privacy") }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp)) {
      Text("Your data stays yours. Logs are private on your device unless you turn on sync. Export or delete anytime.")
      Text("We support self-management. This app does not diagnose, treat, or replace professional care.")
    }
  }
}
