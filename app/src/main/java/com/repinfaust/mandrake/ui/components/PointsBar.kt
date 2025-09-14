package com.repinfaust.mandrake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repinfaust.mandrake.ui.theme.SageGreen
import com.repinfaust.mandrake.ui.theme.DarkForestGreen

@Composable
fun ProgressBar(urgesBypassed: Int, avoidedThingsCompleted: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Urges Bypassed
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$urgesBypassed",
                    fontSize = 18.sp,
                    color = SageGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Urges Bypassed",
                    fontSize = 12.sp,
                    color = DarkForestGreen,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Separator
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(DarkForestGreen.copy(alpha = 0.3f))
            )
            
            // Avoided Things
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$avoidedThingsCompleted",
                    fontSize = 18.sp,
                    color = SageGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Avoided Things",
                    fontSize = 12.sp,
                    color = DarkForestGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}