package com.repinfaust.mandrake.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repinfaust.mandrake.data.repo.UrgeRepository
import com.repinfaust.mandrake.domain.SignpostRules
import com.repinfaust.mandrake.domain.StatsComputer
import com.repinfaust.mandrake.ui.components.Heatmap
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(nav: NavController) {
  val ctx = LocalContext.current
  val repo = remember { UrgeRepository(ctx) }
  val zone = ZoneId.systemDefault()
  var events by remember { mutableStateOf(emptyList<com.repinfaust.mandrake.data.entity.UrgeEvent>()) }

  LaunchedEffect(Unit) {
    val now = Instant.now()
    val weekAgo = now.minus(Duration.ofDays(7))
    repo.between(weekAgo.toEpochMilli(), now.toEpochMilli()).collect { events = it.reversed() }
  }

  val heat = StatsComputer.weeklyHeatmap(events, zone)
  val max = heat.maxOfOrNull { it.count } ?: 0
  val signpost = SignpostRules.evaluate(events)

  Scaffold(topBar = { TopAppBar(title = { Text("Patterns & Insights") }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp)) {
      Heatmap(heat, max)
      Spacer(Modifier.height(16.dp))
      Text("Best tactics this week")
      StatsComputer.topTactics(events).take(5).forEach { (name, count) ->
        Text("• $name — $count")
      }
      Spacer(Modifier.height(16.dp))
      if (signpost.firm) {
        ElevatedCard(colors = CardDefaults.elevatedCardColors()) {
          Column(Modifier.padding(12.dp)) {
            Text("You’ve carried a lot solo. One confidential option could help.")
            TextButton(onClick = { nav.navigate("crisis") }) { Text("See private options") }
          }
        }
      } else if (signpost.soft) {
        ElevatedCard {
          Column(Modifier.padding(12.dp)) {
            Text("This pattern looks heavy — want private options?")
            TextButton(onClick = { nav.navigate("crisis") }) { Text("See options") }
          }
        }
      }
    }
  }
}
