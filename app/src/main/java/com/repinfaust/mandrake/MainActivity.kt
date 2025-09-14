package com.repinfaust.mandrake
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.repinfaust.mandrake.nav.NavGraph
import com.repinfaust.mandrake.ui.theme.UrgeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      UrgeTheme {
        MaterialTheme {
          NavGraph()
        }
      }
    }
  }
}
