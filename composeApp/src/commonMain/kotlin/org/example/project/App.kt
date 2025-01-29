package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import kmp_liquid_slider.composeapp.generated.resources.Res
import kmp_liquid_slider.composeapp.generated.resources.blush
import kmp_liquid_slider.composeapp.generated.resources.cold_sweat
import kmp_liquid_slider.composeapp.generated.resources.disappointed_relieved
import kmp_liquid_slider.composeapp.generated.resources.heart_eyes
import kmp_liquid_slider.composeapp.generated.resources.neutral_face
import org.example.project.library.LiquidSlider
import org.example.slider.library.LiquidSliderConfig
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            Spacer(Modifier.height(100.dp))
            Box(
                Modifier.fillMaxWidth()
            ) {
                val imageList = listOf<ImageBitmap>(
                    imageResource(Res.drawable.cold_sweat),
                    imageResource(Res.drawable.disappointed_relieved),
                    imageResource(Res.drawable.neutral_face),
                    imageResource(Res.drawable.blush),
                    imageResource(Res.drawable.heart_eyes),

                    )
                LiquidSlider(
                    modifier = Modifier.align(Alignment.Center),
                    liquidSliderConfig = LiquidSliderConfig(
                        imageList = imageList,
                        barColor = Color(0xFF5CB338),
                        bubbleColor = Color(0xFF5CB338)
                    ),
                    onValueChange = { newValue ->
                    },
                )
            }

            Spacer(Modifier.height(100.dp))
            Box(
                Modifier.fillMaxWidth()
            ) {
                LiquidSlider(
                    modifier = Modifier.align(Alignment.Center),

                    onValueChange = { newValue ->
                    },
                )

            }
        }
    }
}

