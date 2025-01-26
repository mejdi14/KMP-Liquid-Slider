package org.example.project.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp

@Composable
fun FluidSlider2(
    modifier: Modifier = Modifier,
    size: FluidSliderSize = FluidSliderSize(48, 300),
    startText: String = SliderConstants.TEXT_START,
    endText: String = SliderConstants.TEXT_END,
    bubbleText: String? = null,
    barColor: Color = Color(0xFF6168E7),
    bubbleColor: Color = Color(0xFF6168E7),
    barTextColor: Color = Color.White,
    bubbleTextColor: Color = Color.Black,
    value: Float = SliderConstants.INITIAL_POSITION,
    onValueChange: (Float) -> Unit,
    onBeginTracking: () -> Unit = {},
    onEndTracking: () -> Unit = {},
    textSizeSp: Float = SliderConstants.TEXT_SIZE.toFloat(),
    durationMillis: Int = SliderConstants.ANIMATION_DURATION
) {
    val density = LocalDensity.current
    val barHeightPx = with(density) { size.height.dp.toPx() }
    val desiredWidthPx = with(density) { size.width.dp.toPx() }
    val desiredHeightPx = barHeightPx * SliderConstants.SLIDER_HEIGHT

    var sliderPosition by remember { mutableStateOf(value.coerceIn(0f, 1f)) }
    var isDragging by remember { mutableStateOf(false) }

    val topCircleAnimOffset by animateFloatAsState(
        targetValue = if (isDragging) -SliderConstants.METABALL_RISE_DISTANCE else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "topCircleY"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .width(with(density) { desiredWidthPx.toDp() })
            .height(with(density) { (desiredHeightPx * 1.4f).toDp() })
            .clip(RectangleShape)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            onBeginTracking()
                        },
                        onDragEnd = {
                            isDragging = false
                            onEndTracking()
                        },
                        onDragCancel = {
                            isDragging = false
                            onEndTracking()
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val actualWidth = desiredWidthPx
                        val touchDiameterPx = barHeightPx * SliderConstants.TOUCH_CIRCLE_DIAMETER
                        val maxMovement =
                            actualWidth - touchDiameterPx - SliderConstants.BAR_INNER_HORIZONTAL_OFFSET
                        val newPos = (sliderPosition + dragAmount.x / maxMovement).coerceIn(0f, 1f)
                        sliderPosition = newPos
                        onValueChange(newPos)
                    }
                }
        ) {
            val barVerticalOffsetPx = barHeightPx * SliderConstants.BAR_VERTICAL_OFFSET
            val rectBar = Rect(0f, barVerticalOffsetPx, desiredWidthPx, barVerticalOffsetPx + barHeightPx)

            val barCenterY = (rectBar.top + rectBar.bottom) / 2f

            drawRoundRect(
                color = barColor,
                topLeft = Offset(rectBar.left, rectBar.top),
                size = Size(rectBar.width, rectBar.height),
                cornerRadius = CornerRadius(SliderConstants.BAR_CORNER_RADIUS.toFloat())
            )

            // Draw text (start and end)
            drawTextWithMeasurer(
                textMeasurer, startText, rectBar.left + SliderConstants.TEXT_OFFSET, barCenterY, barTextColor, textSizeSp
            )
            drawTextWithMeasurer(
                textMeasurer, endText, rectBar.right - SliderConstants.TEXT_OFFSET, barCenterY, barTextColor, textSizeSp
            )
        }
    }
    LaunchedEffect(value) { sliderPosition = value.coerceIn(0f, 1f) }
}

private fun DrawScope.drawTextWithMeasurer(
    textMeasurer: TextMeasurer,
    text: String,
    x: Float,
    y: Float,
    color: Color,
    size: Float
) {
    val layout = textMeasurer.measure(AnnotatedString(text), style = TextStyle(fontSize = size.sp))
    drawText(
        textMeasurer,
        text,
        topLeft = Offset(x - layout.size.width / 2, y - layout.size.height / 2),
        style = TextStyle(color = color, fontSize = size.sp)
    )
}


