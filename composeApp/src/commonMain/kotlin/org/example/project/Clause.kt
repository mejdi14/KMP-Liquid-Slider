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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
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

@Composable
private fun BoxScope.FluidSliderCanvas(
    density: Density,
    desiredHeightPx: Float,
    isDragging: MutableState<Boolean>,
    onBeginTracking: () -> Unit,
    onEndTracking: () -> Unit,
    size: FluidSliderSize,
    desiredWidthPx: Float,
    barHeightPx: Float,
    sliderPosition: MutableState<Float>,
    onValueChange: (Float) -> Unit,
    topCircleAnimOffset: State<Float>,
    barColor: Color,
    barTextColor: Color,
    textSizeSp: Float,
    textMeasurer: TextMeasurer,
    startText: String,
    endText: String,
    bubbleColor: Color,
    bubbleText: String?,
    bubbleTextColor: Color
) {
    Canvas(
        modifier = Modifier
            .matchParentSize()
            .offset(y = with(density) { (desiredHeightPx * 0.4f).toDp() })
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging.value = true
                        onBeginTracking()
                    },
                    onDragEnd = {
                        isDragging.value = false
                        onEndTracking()
                    },
                    onDragCancel = {
                        isDragging.value = false
                        onEndTracking()
                    }
                ) { change, dragAmount ->
                    change.consume()

                    size
                    val w = size.width.dp.toPx() // we have "size.value" is height...
                    val actualWidth = desiredWidthPx // our canvas width
                    val touchDiameterPx = barHeightPx * TOUCH_CIRCLE_DIAMETER
                    val maxMovement =
                        actualWidth - touchDiameterPx - BAR_INNER_HORIZONTAL_OFFSET

                    val newPos =
                        (sliderPosition.value + dragAmount.x / maxMovement).coerceIn(0f, 1f)
                    sliderPosition.value = newPos
                    onValueChange(newPos)
                }
            }
    ) {
        val canvasWidth = size.width.dp.toPx().coerceAtLeast(desiredWidthPx)
        val canvasHeight = size.height.dp.toPx().coerceAtLeast(desiredHeightPx)

        val barVerticalOffsetPx = barHeightPx * BAR_VERTICAL_OFFSET
        val barRect = Rect(
            left = 0f,
            top = barVerticalOffsetPx,
            right = canvasWidth,
            bottom = barVerticalOffsetPx + barHeightPx
        )

        val topCircleDiameterPx = barHeightPx * TOP_CIRCLE_DIAMETER
        val bottomCircleDiameterPx = barHeightPx * BOTTOM_CIRCLE_DIAMETER
        val touchDiameterPx = barHeightPx * TOUCH_CIRCLE_DIAMETER
        val labelDiameterPx = barHeightPx * LABEL_CIRCLE_DIAMETER

        val fluidballMaxDistPx = barHeightPx * METABALL_MAX_DISTANCE
        val fluidballRiseDistPx = barHeightPx * METABALL_RISE_DISTANCE
        val cornerRadiusPx = BAR_CORNER_RADIUS * density.density

        val rectBottomCircle = Rect(
            left = 0f,
            top = barVerticalOffsetPx,
            right = bottomCircleDiameterPx,
            bottom = barVerticalOffsetPx + bottomCircleDiameterPx
        )

        val topRising =
            topCircleAnimOffset.value * barHeightPx
        val rectTopCircle = Rect(
            left = 0f,
            top = barVerticalOffsetPx + topRising,
            right = topCircleDiameterPx,
            bottom = barVerticalOffsetPx + topCircleDiameterPx + topRising
        )

        val rectTouch = Rect(
            left = 0f,
            top = barVerticalOffsetPx,
            right = touchDiameterPx,
            bottom = barVerticalOffsetPx + touchDiameterPx
        )

        val maxMovement = canvasWidth - touchDiameterPx - BAR_INNER_HORIZONTAL_OFFSET * 2
        val xPos =
            BAR_INNER_HORIZONTAL_OFFSET + (touchDiameterPx / 2) + maxMovement * sliderPosition.value
        val labelOffsetY =
            barVerticalOffsetPx + (topCircleDiameterPx - labelDiameterPx) / 2f + topRising
        val rectLabel = Rect(
            left = xPos - labelDiameterPx / 2f,
            top = labelOffsetY,
            right = xPos + labelDiameterPx / 2f,
            bottom = labelOffsetY + labelDiameterPx
        )


        offsetRectToPosition(xPos, rectTouch, rectTopCircle, rectBottomCircle, rectLabel)

        drawRoundRect(
            color = barColor,
            topLeft = Offset(barRect.left, barRect.top),
            size = Size(barRect.width, barRect.height),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )

        val textStyleBar = TextStyle(
            color = barTextColor,
            fontSize = textSizeSp.sp,
            textAlign = TextAlign.Center
        )
        val startTextLayout =
            textMeasurer.measure(AnnotatedString(startText), style = textStyleBar)
        drawText(
            textMeasurer,
            startText,
            topLeft = Offset(
                x = TEXT_OFFSET * density.density,
                y = barRect.top + (barRect.height - startTextLayout.size.height) / 2
            ),
            style = textStyleBar
        )
        val endTextLayout = textMeasurer.measure(AnnotatedString(endText), style = textStyleBar)
        drawText(
            textMeasurer,
            endText,
            topLeft = Offset(
                x = barRect.right - endTextLayout.size.width - TEXT_OFFSET * density.density,
                y = barRect.top + (barRect.height - endTextLayout.size.height) / 2
            ),
            style = textStyleBar
        )

        val path = Path()
        drawFluidBall(
            fluidBallPath = path,
            bottomCircle = rectBottomCircle,
            topCircle = rectTopCircle,
            barTopBoundary = barRect.top,
            fluidBallRiseLimit = fluidballRiseDistPx,
            maxDistanceBetweenCircles = fluidballMaxDistPx,
            cornerRadiusPx = cornerRadiusPx,
            topCircleSpreadFactor = TOP_SPREAD_FACTOR,
            bottomCircleStartSpreadFactor = BOTTOM_START_SPREAD_FACTOR,
            bottomCircleEndSpreadFactor = BOTTOM_END_SPREAD_FACTOR,
            handleRate = METABALL_HANDLER_FACTOR,
            fluidBallColor = barColor
        )

        drawCircle(
            color = bubbleColor,
            radius = labelDiameterPx / 2f,
            center = Offset(rectLabel.centerX, rectLabel.centerY)
        )

        val labelString = bubbleText ?: ((sliderPosition.value * 100).toInt()).toString()
        val textLayoutLabel = textMeasurer.measure(
            AnnotatedString(labelString),
            style = textStyleBar.copy(color = bubbleTextColor)
        )

        val backgroundDiameter = labelDiameterPx * 0.8f
        val backgroundRadius = backgroundDiameter / 2f

        drawCircle(
            color = Color.White,
            radius = backgroundRadius,
            center = Offset(rectLabel.centerX, rectLabel.centerY)
        )

        drawText(
            textMeasurer,
            labelString,
            topLeft = Offset(
                x = rectLabel.centerX - textLayoutLabel.size.width / 2,
                y = rectLabel.centerY - textLayoutLabel.size.height / 2
            ),
            style = textStyleBar.copy(color = bubbleTextColor)
        )
    }

}


private fun offsetRectToPosition(centerX: Float, vararg rects: Rect) {
    rects.forEach { r ->
        val dx = centerX - (r.left + r.width / 2)
        r.translate(dx = dx, dy = 0f)
    }
}

private data class Rect(
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f

    fun translate(dx: Float, dy: Float) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }
}


private fun DrawScope.drawFluidBall(
    fluidBallPath: Path,
    bottomCircle: Rect,
    topCircle: Rect,
    barTopBoundary: Float,
    fluidBallRiseLimit: Float,
    maxDistanceBetweenCircles: Float,
    cornerRadiusPx: Float,
    topCircleSpreadFactor: Float,
    bottomCircleStartSpreadFactor: Float,
    bottomCircleEndSpreadFactor: Float,
    handleRate: Float,
    fluidBallColor: Color
) {
    val bottomCircleRadius = bottomCircle.width / 2f
    val topCircleRadius = topCircle.width / 2f

    if (bottomCircleRadius == 0f || topCircleRadius == 0f) return

    val centerDistance = getVectorLength(
        bottomCircle.centerX, bottomCircle.centerY,
        topCircle.centerX, topCircle.centerY
    )

    if (centerDistance > maxDistanceBetweenCircles || centerDistance <= abs(bottomCircleRadius - topCircleRadius)) {
        return
    }

    val riseRatio = min(1f, max(0f, barTopBoundary - topCircle.top) / fluidBallRiseLimit)

    val angleOffset1: Float
    val angleOffset2: Float

    if (centerDistance < bottomCircleRadius + topCircleRadius) {
        angleOffset1 = acos(
            (bottomCircleRadius * bottomCircleRadius + centerDistance * centerDistance - topCircleRadius * topCircleRadius) /
                    (2 * bottomCircleRadius * centerDistance)
        )
        angleOffset2 = acos(
            (topCircleRadius * topCircleRadius + centerDistance * centerDistance - bottomCircleRadius * bottomCircleRadius) /
                    (2 * topCircleRadius * centerDistance)
        )
    } else {
        angleOffset1 = 0f
        angleOffset2 = 0f
    }

    val xDistance = topCircle.centerX - bottomCircle.centerX
    val yDistance = topCircle.centerY - bottomCircle.centerY

    val bottomCircleSpreadFactorDiff = bottomCircleStartSpreadFactor - bottomCircleEndSpreadFactor
    val bottomCircleSpreadFactor = bottomCircleStartSpreadFactor - bottomCircleSpreadFactorDiff * riseRatio

    val fullPi = PI.toFloat()
    val baseAngle = atan2(yDistance, xDistance)
    val angleDifference = acos((bottomCircleRadius - topCircleRadius) / centerDistance)

    val bottomCircleAngle1 = baseAngle + angleOffset1 + (angleDifference - angleOffset1) * bottomCircleSpreadFactor
    val bottomCircleAngle2 = baseAngle - angleOffset1 - (angleDifference - angleOffset1) * bottomCircleSpreadFactor
    val topCircleAngle1 = baseAngle + fullPi - angleOffset2 - (fullPi - angleOffset2 - angleDifference) * topCircleSpreadFactor
    val topCircleAngle2 = baseAngle - fullPi + angleOffset2 + (fullPi - angleOffset2 - angleDifference) * topCircleSpreadFactor

    val bottomCirclePoint1 = getVector(bottomCircleAngle1, bottomCircleRadius).let {
        Offset(it.first + bottomCircle.centerX, it.second + bottomCircle.centerY)
    }
    val bottomCirclePoint2 = getVector(bottomCircleAngle2, bottomCircleRadius).let {
        Offset(it.first + bottomCircle.centerX, it.second + bottomCircle.centerY)
    }

    val topCirclePoint1 = getVector(topCircleAngle1, topCircleRadius).let {
        Offset(it.first + topCircle.centerX, it.second + topCircle.centerY)
    }
    val topCirclePoint2 = getVector(topCircleAngle2, topCircleRadius).let {
        Offset(it.first + topCircle.centerX, it.second + topCircle.centerY)
    }

    val combinedRadius = bottomCircleRadius + topCircleRadius
    val handleOffset = min(
        max(topCircleSpreadFactor, bottomCircleSpreadFactor) * handleRate,
        getVectorLength(bottomCirclePoint1.x, bottomCirclePoint1.y, topCirclePoint1.x, topCirclePoint1.y) / combinedRadius
    )

    val bottomHandleRadius = bottomCircleRadius * handleOffset
    val topHandleRadius = topCircleRadius * handleOffset

    val quarterPi = fullPi / 2
    val bottomHandle1 = getVector(bottomCircleAngle1 - quarterPi, bottomHandleRadius)
    val topHandle1 = getVector(topCircleAngle1 + quarterPi, topHandleRadius)
    val topHandle2 = getVector(topCircleAngle2 - quarterPi, topHandleRadius)
    val bottomHandle2 = getVector(bottomCircleAngle2 + quarterPi, bottomHandleRadius)

    val verticalOffset = abs(barTopBoundary - bottomCirclePoint1.y) * riseRatio - 1
    val adjustedBottomPoint1 = Offset(bottomCirclePoint1.x, bottomCirclePoint1.y - verticalOffset)
    val adjustedBottomPoint2 = Offset(bottomCirclePoint2.x, bottomCirclePoint2.y - verticalOffset)

    with(fluidBallPath) {
        reset()
        moveTo(adjustedBottomPoint1.x, adjustedBottomPoint1.y + cornerRadiusPx)
        lineTo(adjustedBottomPoint1.x, adjustedBottomPoint1.y)

        cubicTo(
            adjustedBottomPoint1.x + bottomHandle1.first, adjustedBottomPoint1.y + bottomHandle1.second,
            topCirclePoint1.x + topHandle1.first, topCirclePoint1.y + topHandle1.second,
            topCirclePoint1.x, topCirclePoint1.y
        )

        lineTo(topCircle.centerX, topCircle.centerY)
        lineTo(topCirclePoint2.x, topCirclePoint2.y)

        cubicTo(
            topCirclePoint2.x + topHandle2.first, topCirclePoint2.y + topHandle2.second,
            adjustedBottomPoint2.x + bottomHandle2.first, adjustedBottomPoint2.y + bottomHandle2.second,
            adjustedBottomPoint2.x, adjustedBottomPoint2.y
        )

        lineTo(adjustedBottomPoint2.x, adjustedBottomPoint2.y + cornerRadiusPx)
        close()
    }

    drawPath(path = fluidBallPath, color = fluidBallColor)
    drawOval(
        color = fluidBallColor,
        topLeft = Offset(topCircle.left, topCircle.top),
        size = Size(topCircle.width, topCircle.height)
    )
}


private fun getVector(radians: Float, length: Float): Pair<Float, Float> {
    val x = cos(radians) * length
    val y = sin(radians) * length
    return x to y
}

private fun getVectorLength(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return sqrt(dx * dx + dy * dy)
}

