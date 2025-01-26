package org.example.project

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import org.example.project.library.Rect
import org.example.project.library.SliderConstants.ANIMATION_DURATION
import org.example.project.library.SliderConstants.BAR_CORNER_RADIUS
import org.example.project.library.SliderConstants.BAR_INNER_HORIZONTAL_OFFSET
import org.example.project.library.SliderConstants.BAR_VERTICAL_OFFSET
import org.example.project.library.SliderConstants.BOTTOM_CIRCLE_DIAMETER
import org.example.project.library.SliderConstants.BOTTOM_END_SPREAD_FACTOR
import org.example.project.library.SliderConstants.BOTTOM_START_SPREAD_FACTOR
import org.example.project.library.SliderConstants.INITIAL_POSITION
import org.example.project.library.SliderConstants.LABEL_CIRCLE_DIAMETER
import org.example.project.library.SliderConstants.METABALL_HANDLER_FACTOR
import org.example.project.library.SliderConstants.METABALL_MAX_DISTANCE
import org.example.project.library.SliderConstants.METABALL_RISE_DISTANCE
import org.example.project.library.SliderConstants.SLIDER_HEIGHT
import org.example.project.library.SliderConstants.TEXT_END
import org.example.project.library.SliderConstants.TEXT_OFFSET
import org.example.project.library.SliderConstants.TEXT_SIZE
import org.example.project.library.SliderConstants.TEXT_START
import org.example.project.library.SliderConstants.TOP_CIRCLE_DIAMETER
import org.example.project.library.SliderConstants.TOP_SPREAD_FACTOR
import org.example.project.library.SliderConstants.TOUCH_CIRCLE_DIAMETER
import org.example.project.library.drawFluidBall
import kotlin.math.*

data class FluidSliderSize(val height: Int = 60, val width: Int = 300) {

}


@Composable
fun FluidSlider(
    modifier: Modifier = Modifier,
    size: FluidSliderSize = FluidSliderSize(),

    startText: String = TEXT_START,
    endText: String = TEXT_END,
    bubbleText: String? = null,

    barColor: Color = Color(0xFF6168E7),
    bubbleColor: Color = Color(0xFF6168E7),
    barTextColor: Color = Color.White,
    bubbleTextColor: Color = Color.Black,

    value: Float = INITIAL_POSITION,
    onValueChange: (Float) -> Unit,
    onBeginTracking: () -> Unit = {},
    onEndTracking: () -> Unit = {},

    textSizeSp: Float = TEXT_SIZE.toFloat(),
    durationMillis: Int = ANIMATION_DURATION
) {
    val density = LocalDensity.current
    val barHeightPx = with(density) { size.height.dp.toPx() }

    val desiredWidthPx = with(density) { size.width.dp.toPx() }
    val desiredHeightPx = (barHeightPx * SLIDER_HEIGHT)

    var sliderPosition = remember { mutableStateOf(value.coerceIn(0f, 1f)) }

    var isDragging = remember { mutableStateOf(false) }
    val topCircleAnimOffset = animateFloatAsState(
        targetValue = if (isDragging.value) -METABALL_RISE_DISTANCE else 0f,
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
            .border(2.dp, Color.Red)
    ) {
        FluidSliderCanvas(
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
            barColor,
            barTextColor,
            textSizeSp,
            textMeasurer,
            startText,
            endText,
            bubbleColor,
            bubbleText,
            bubbleTextColor
        )
    }

    LaunchedEffect(value) {
        sliderPosition.value = value.coerceIn(0f, 1f)
    }
}













