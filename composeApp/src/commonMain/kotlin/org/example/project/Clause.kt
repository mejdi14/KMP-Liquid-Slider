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

        val metaballMaxDistPx = barHeightPx * METABALL_MAX_DISTANCE
        val metaballRiseDistPx = barHeightPx * METABALL_RISE_DISTANCE
        val cornerRadiusPx = BAR_CORNER_RADIUS * density.density

        val rectBottomCircle = Rect(
            left = 0f,
            top = barVerticalOffsetPx,
            right = bottomCircleDiameterPx,
            bottom = barVerticalOffsetPx + bottomCircleDiameterPx
        )

        val topRising =
            topCircleAnimOffset.value * barHeightPx // or just multiply by barHeightPx if we want
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
            left = xPos - labelDiameterPx / 2f, // Center horizontally
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
        drawMetaball(
            path = path,
            circle1 = rectBottomCircle,
            circle2 = rectTopCircle,
            topBorder = barRect.top,
            riseDistance = metaballRiseDistPx,
            maxDistance = metaballMaxDistPx,
            cornerRadius = cornerRadiusPx,
            topSpreadFactor = TOP_SPREAD_FACTOR,
            bottomStartSpreadFactor = BOTTOM_START_SPREAD_FACTOR,
            bottomEndSpreadFactor = BOTTOM_END_SPREAD_FACTOR,
            handleRate = METABALL_HANDLER_FACTOR,
            color = barColor
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


private fun DrawScope.drawMetaball(
    path: Path,
    circle1: Rect,
    circle2: Rect,
    topBorder: Float,
    riseDistance: Float,
    maxDistance: Float,
    cornerRadius: Float,
    topSpreadFactor: Float,
    bottomStartSpreadFactor: Float,
    bottomEndSpreadFactor: Float,
    handleRate: Float,
    color: Color
) {
    val radius1 = circle1.width / 2f
    val radius2 = circle2.width / 2f

    if (radius1 == 0f || radius2 == 0f) {
        return
    }

    val d = getVectorLength(circle1.centerX, circle1.centerY, circle2.centerX, circle2.centerY)
    if (d > maxDistance || d <= abs(radius1 - radius2)) {
        return
    }

    val riseRatio = min(1f, max(0f, topBorder - circle2.top) / riseDistance)

    val u1: Float
    val u2: Float
    if (d < radius1 + radius2) {
        u1 = acos((radius1 * radius1 + d * d - radius2 * radius2) / (2 * radius1 * d))
        u2 = acos((radius2 * radius2 + d * d - radius1 * radius1) / (2 * radius2 * d))
    } else {
        u1 = 0f
        u2 = 0f
    }

    val centerXMin = circle2.centerX - circle1.centerX
    val centerYMin = circle2.centerY - circle1.centerY

    val bottomSpreadDiff = (bottomStartSpreadFactor - bottomEndSpreadFactor)
    val bottomSpreadFactor = bottomStartSpreadFactor - bottomSpreadDiff * riseRatio

    val fPI = PI.toFloat()
    val angle1 = atan2(centerYMin, centerXMin)
    val angle2 = acos((radius1 - radius2) / d)
    val angle1a = angle1 + u1 + (angle2 - u1) * bottomSpreadFactor
    val angle1b = angle1 - u1 - (angle2 - u1) * bottomSpreadFactor
    val angle2a = angle1 + fPI - u2 - (fPI - u2 - angle2) * topSpreadFactor
    val angle2b = angle1 - fPI + u2 + (fPI - u2 - angle2) * topSpreadFactor

    val p1a = getVector(angle1a, radius1).let {
        Offset(it.first + circle1.centerX, it.second + circle1.centerY)
    }
    val p1b = getVector(angle1b, radius1).let {
        Offset(it.first + circle1.centerX, it.second + circle1.centerY)
    }

    val p2a = getVector(angle2a, radius2).let {
        Offset(it.first + circle2.centerX, it.second + circle2.centerY)
    }
    val p2b = getVector(angle2b, radius2).let {
        Offset(it.first + circle2.centerX, it.second + circle2.centerY)
    }

    val totalRadius = (radius1 + radius2)
    val d2Base = min(
        max(topSpreadFactor, bottomSpreadFactor) * handleRate,
        getVectorLength(p1a.x, p1a.y, p2a.x, p2a.y) / totalRadius
    )

    val d2 = d2Base * min(1f, d * 2 / totalRadius)
    val r1 = radius1 * d2
    val r2 = radius2 * d2

    val pi2 = fPI / 2
    val sp1 = getVector(angle1a - pi2, r1)  // handle offset for p1a
    val sp2 = getVector(angle2a + pi2, r2)  // handle offset for p2a
    val sp3 = getVector(angle2b - pi2, r2)  // handle offset for p2b
    val sp4 = getVector(angle1b + pi2, r1)  // handle offset for p1b


    val yOffset = (abs(topBorder - p1a.y) * riseRatio) - 1
    val fp1a = Offset(p1a.x, p1a.y - yOffset)
    val fp1b = Offset(p1b.x, p1b.y - yOffset)

    with(path) {
        reset()
        moveTo(fp1a.x, fp1a.y + cornerRadius)
        lineTo(fp1a.x, fp1a.y)

        cubicTo(
            fp1a.x + sp1.first, fp1a.y + sp1.second,
            p2a.x + sp2.first, p2a.y + sp2.second,
            p2a.x, p2a.y
        )

        lineTo(circle2.centerX, circle2.centerY)
        lineTo(p2b.x, p2b.y)
        cubicTo(
            p2b.x + sp3.first, p2b.y + sp3.second,
            fp1b.x + sp4.first, fp1b.y + sp4.second,
            fp1b.x, fp1b.y
        )

        lineTo(fp1b.x, fp1b.y + cornerRadius)
        close()
    }

    drawPath(path = path, color = color)
    drawOval(
        color = color,
        topLeft = Offset(circle2.left, circle2.top),
        size = Size(circle2.width, circle2.height)
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

