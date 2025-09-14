package com.repinfaust.mandrake.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.repinfaust.mandrake.domain.StatsComputer

@Composable
fun Heatmap(cells: List<StatsComputer.HeatCell>, max: Int) {
  val cellSize = 18f
  val padding = 4f
  Canvas(Modifier.height(7.dp * 22).fillMaxWidth()) {
    cells.forEach { c ->
      val x = c.hour * (cellSize + padding)
      val y = (c.dayOfWeek) * (cellSize + padding)
      val intensity = if (max == 0) 0f else c.count.toFloat() / max
      drawRect(
        color = Color(0f, 0.7f, 0.6f, alpha = 0.2f + 0.8f*intensity),
        topLeft = Offset(x, y),
        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
      )
    }
  }
  Text("Weekly heatmap")
}
