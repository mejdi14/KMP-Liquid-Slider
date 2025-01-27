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
import androidx.compose.ui.unit.dp
import org.example.project.library.LiquidSlider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            Spacer(Modifier.height(100.dp))
            Box(Modifier.fillMaxWidth()
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

