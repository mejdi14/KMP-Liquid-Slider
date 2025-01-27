package org.example.project.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import org.example.project.library.SliderConstants.SLIDER_HEIGHT
import org.example.slider.library.LiquidSliderConfig

@Composable
fun LiquidSlider(
    modifier: Modifier = Modifier,
    size: LiquidSliderSize = LiquidSliderSize(),
    liquidSliderConfig: LiquidSliderConfig = LiquidSliderConfig(),

    onValueChange: (Float) -> Unit,
    onBeginTracking: () -> Unit = {},
    onEndTracking: () -> Unit = {},

) {
    val density = LocalDensity.current
    val barHeightPx = with(density) { size.height.dp.toPx() }

    val desiredWidthPx = with(density) { size.width.dp.toPx() }
    val desiredHeightPx = (barHeightPx * SLIDER_HEIGHT)

    var sliderPosition = remember { mutableStateOf(liquidSliderConfig.initialPosition.coerceIn(0f, 1f)) }

    var isDragging = remember { mutableStateOf(false) }
    val topCircleAnimOffset = animateFloatAsState(
        targetValue = if (isDragging.value) -(liquidSliderConfig.liquidBalRiseDistance) else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "topCircleY"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .width(with(density) { desiredWidthPx.toDp() })
            .height(with(density) { (desiredHeightPx * 1.4f).toDp() })
            .clip(RectangleShape)
    ) {
        LiquidSliderCanvas(
            density,
            desiredHeightPx,
            isDragging,
            onBeginTracking,
            onEndTracking,
            size,
            desiredWidthPx,
            barHeightPx,
            sliderPosition,
            onValueChange,
            topCircleAnimOffset,
            textMeasurer,
            liquidSliderConfig
        )
    }

    LaunchedEffect(liquidSliderConfig.initialPosition) {
        sliderPosition.value = liquidSliderConfig.initialPosition.coerceIn(0f, 1f)
    }
}


