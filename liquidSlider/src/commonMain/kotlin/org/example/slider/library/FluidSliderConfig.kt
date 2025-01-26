package org.example.project.library

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Size configuration


// Style configuration
data class FluidSliderConfig(
    val barColor: Color = Color(0xFF6168E7),
    val bubbleColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val startText: String = "0",
    val endText: String = "100",
    val textSize: Dp = 12.dp
)