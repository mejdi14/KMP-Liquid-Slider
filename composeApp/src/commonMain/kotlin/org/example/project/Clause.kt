package org.example.project

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlin.math.*

// ------------------------------
//   1) Original "companion object" constants
//      (same names & values)
// ------------------------------
private const val BAR_CORNER_RADIUS = 12
private const val BAR_VERTICAL_OFFSET = 1.2f
private const val BAR_INNER_HORIZONTAL_OFFSET = 0f

private const val SLIDER_WIDTH = 4
private const val SLIDER_HEIGHT = 1 + BAR_VERTICAL_OFFSET

private const val TOP_CIRCLE_DIAMETER = 0.8f
private const val BOTTOM_CIRCLE_DIAMETER = 25f
private const val TOUCH_CIRCLE_DIAMETER = 1f
private const val LABEL_CIRCLE_DIAMETER = 1f

private const val ANIMATION_DURATION = 400
private const val TOP_SPREAD_FACTOR = 0.4f
private const val BOTTOM_START_SPREAD_FACTOR = 0.25f
private const val BOTTOM_END_SPREAD_FACTOR = 0.1f
private const val METABALL_HANDLER_FACTOR = 1.4f
private const val METABALL_MAX_DISTANCE = 15f
private const val METABALL_RISE_DISTANCE = 1.4f

private const val TEXT_SIZE = 12
private const val TEXT_OFFSET = 8
private const val TEXT_START = "0"
private const val TEXT_END = "100"

private const val INITIAL_POSITION = 0.5f

/**
 * Sizes that match the original:
 * - NORMAL = 56dp bar height
 * - SMALL = 40dp bar height
 */
enum class FluidSliderSize(val value: Int, val width: Int) {
    NORMAL(48, 300),
    SMALL(40, 300)
}

/**
 * Compose Multiplatform re‑implementation of the original FluidSlider.
 * It uses the exact same geometry + metaball math from the Java code.
 */
@Composable
fun FluidSlider(
    modifier: Modifier = Modifier,
    // The "size" determines barHeight in the same way as the original constructor:
    size: FluidSliderSize = FluidSliderSize.NORMAL,

    // Start/End/Bubble text
    startText: String = TEXT_START,
    endText: String = TEXT_END,
    bubbleText: String? = null,

    // Colors
    barColor: Color = Color(0xFF6168E7),
    bubbleColor: Color = Color(0xFF6168E7),
    barTextColor: Color = Color.White,
    bubbleTextColor: Color = Color.Black,

    // Position
    value: Float = INITIAL_POSITION,
    onValueChange: (Float) -> Unit,
    onBeginTracking: () -> Unit = {},
    onEndTracking: () -> Unit = {},

    // Animations & text
    textSizeSp: Float = TEXT_SIZE.toFloat(), // default 12sp
    durationMillis: Int = ANIMATION_DURATION
) {
    // The original code uses barHeight = size.value * density
    // We'll do exactly that:
    val density = LocalDensity.current
    val barHeightPx = with(density) { size.value.dp.toPx() }

    // "desiredWidth" = barHeight * SLIDER_WIDTH
    val desiredWidthPx = with(density) { size.width.dp.toPx() }
    // "desiredHeight" = barHeight * SLIDER_HEIGHT
    val desiredHeightPx = (barHeightPx * SLIDER_HEIGHT)

    // We'll track "position" just like the original, from 0..1
    var sliderPosition by remember { mutableStateOf(value.coerceIn(0f, 1f)) }

    // We replicate "showLabel" / "hideLabel": top circle rises by "metaballRiseDistance"
    // We do a simple animateFloatAsState with a spring, or a tween if desired
    var isDragging by remember { mutableStateOf(false) }
    val topCircleAnimOffset by animateFloatAsState(
        targetValue = if (isDragging) -METABALL_RISE_DISTANCE else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "topCircleY"
    )

    // We'll define textMeasurer to replicate "drawText" logic
    val textMeasurer = rememberTextMeasurer()

    // This Box is sized to the "desired" width/height from the original code
    Box(
        modifier = modifier
            .width(with(density) { desiredWidthPx.toDp() })
            .height(with(density) { (desiredHeightPx * 1.4f).toDp() })
            .clip(RectangleShape)
            .border(2.dp, Color.Red)
    ) {
        // A Canvas to do the drawing
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .offset(y = with(density) { (desiredHeightPx * 0.4f).toDp() })
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

                        // The original code uses:
                        //   maxMovement = width - touchRectDiameter - barInnerOffset * 2
                        // Let's compute:
                        size
                        val w = size.width.dp.toPx() // we have "size.value" is height...
                        val actualWidth = desiredWidthPx // our canvas width
                        val touchDiameterPx = barHeightPx * TOUCH_CIRCLE_DIAMETER
                        val maxMovement =
                            actualWidth - touchDiameterPx - BAR_INNER_HORIZONTAL_OFFSET

                        // update position
                        val newPos = (sliderPosition + dragAmount.x / maxMovement).coerceIn(0f, 1f)
                        sliderPosition = newPos
                        onValueChange(newPos)
                    }
                }
        ) {
            // The canvas is "desiredWidthPx x desiredHeightPx" in size
            val canvasWidth = size.width.dp.toPx().coerceAtLeast(desiredWidthPx)
            val canvasHeight = size.value.dp.toPx().coerceAtLeast(desiredHeightPx)

            // Exactly like onSizeChanged:
            // rectBar.set(0f, barVerticalOffset, width, barVerticalOffset + barHeight)
            // rectTopCircle, rectBottomCircle, etc.
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

            // We'll keep rectTopCircle, rectBottomCircle
            // This is their "un-offset" position: (0, barVerticalOffset.. diameter)
            // Then we shift them horizontally with offsetRectToPosition
            // and also shift the top circle up/down by the "show/hide" animation
            val rectBottomCircle = Rect(
                left = 0f,
                top = barVerticalOffsetPx,
                right = bottomCircleDiameterPx,
                bottom = barVerticalOffsetPx + bottomCircleDiameterPx
            )

            // For the top circle, we also shift it vertically by topCircleAnimOffset * barHeightPx
            // The original code: showLabel => rectTopCircle offset up by metaballRiseDistance
            // We'll replicate that factor in real px:
            val topRising =
                topCircleAnimOffset * barHeightPx // or just multiply by barHeightPx if we want
            val rectTopCircle = Rect(
                left = 0f,
                top = barVerticalOffsetPx + topRising,
                right = topCircleDiameterPx,
                bottom = barVerticalOffsetPx + topCircleDiameterPx + topRising
            )

            // For the "touch rect" we won't strictly need it for drawing, but let's replicate:
            val rectTouch = Rect(
                left = 0f,
                top = barVerticalOffsetPx,
                right = touchDiameterPx,
                bottom = barVerticalOffsetPx + touchDiameterPx
            )

            // Then we do "maxMovement = width - touchRectDiameter - barInnerOffset * 2"
            // and offset them horizontally by: x = barInnerOffset + touchRectDiameter/2 + maxMovement * sliderPosition
            val maxMovement = canvasWidth - touchDiameterPx - BAR_INNER_HORIZONTAL_OFFSET * 2
            val xPos =
                BAR_INNER_HORIZONTAL_OFFSET + (touchDiameterPx / 2) + maxMovement * sliderPosition
            // rectLabel (the small bubble on top):
            val labelOffsetY = barVerticalOffsetPx + (topCircleDiameterPx - labelDiameterPx) / 2f + topRising
            val rectLabel = Rect(
                left = xPos - labelDiameterPx / 2f, // Center horizontally
                top = labelOffsetY,
                right = xPos + labelDiameterPx / 2f,
                bottom = labelOffsetY + labelDiameterPx
            )


            offsetRectToPosition(xPos, rectTouch, rectTopCircle, rectBottomCircle, rectLabel)

            // 1) Draw bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(barRect.left, barRect.top),
                size = Size(barRect.width, barRect.height),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )

            // 2) Draw bar texts (start & end)
            val textStyleBar = TextStyle(
                color = barTextColor,
                fontSize = textSizeSp.sp,
                textAlign = TextAlign.Center
            )
            // start text: left
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
            // end text: right
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

            // 3) Draw the metaball bridging the bottom circle & top circle
            //    (like drawMetaball(canvas, paintBar, pathMetaball, rectBottomCircle, rectTopCircle, rectBar.top) in original)
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

            // 4) Draw label circle (small oval)
            // original: `canvas.drawOval(rectLabel, paintLabel)`
            // replicate exactly:
            drawCircle(
                color = bubbleColor,
                radius = labelDiameterPx / 2f,
                center = Offset(rectLabel.centerX, rectLabel.centerY)
            )

            // label text: original uses bubbleText ?: (position*100).toInt()
            val labelString = bubbleText ?: ((sliderPosition * 100).toInt()).toString()
            val textLayoutLabel = textMeasurer.measure(
                AnnotatedString(labelString),
                style = textStyleBar.copy(color = bubbleTextColor)
            )
            // center in rectLabel
            // Inside Canvas block:

// 1. Calculate background circle size (70% of main label circle)
            val backgroundDiameter = labelDiameterPx * 0.8f
            val backgroundRadius = backgroundDiameter / 2f

// 2. Draw white background circle
            drawCircle(
                color = Color.White,
                radius = backgroundRadius,
                center = Offset(rectLabel.centerX, rectLabel.centerY)
            )

// 3. Then draw the text on top (existing code)
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

    // Keep external "value" in sync
    LaunchedEffect(value) {
        sliderPosition = value.coerceIn(0f, 1f)
    }
}

/**
 * Exactly matches `offsetRectToPosition(position, vararg rects: RectF)` from the original code:
 * For each rect, shift horizontally so its centerX becomes `position`.
 */
private fun offsetRectToPosition(centerX: Float, vararg rects: Rect) {
    rects.forEach { r ->
        val dx = centerX - (r.left + r.width / 2)
        // Shift horizontally, keep top/bottom same
        r.translate(dx = dx, dy = 0f)
    }
}

/**
 * In Jetpack Compose, we don't have "RectF" directly,
 * but we do have `Rect`, which can be mutated with [translate].
 */
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

/**
 * Direct translation of the original `drawMetaball(...)` method from the Java code.
 * We keep the same steps & variable names. This draws the path + the top circle
 * onto the current [DrawScope].
 */
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

    // distance between centers
    val d = getVectorLength(circle1.centerX, circle1.centerY, circle2.centerX, circle2.centerY)
    if (d > maxDistance || d <= abs(radius1 - radius2)) {
        // The original immediately returns if too far or one engulfs the other
        return
    }

    // riseRatio = how much the top circle has "risen" from bar top to top circle
    val riseRatio = min(1f, max(0f, topBorder - circle2.top) / riseDistance)

    // "case circles are overlapping"
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

    // p1a, p1b => points on circle1's circumference
    val p1a = getVector(angle1a, radius1).let {
        Offset(it.first + circle1.centerX, it.second + circle1.centerY)
    }
    val p1b = getVector(angle1b, radius1).let {
        Offset(it.first + circle1.centerX, it.second + circle1.centerY)
    }

    // p2a, p2b => points on circle2
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

    // "case circles are overlapping"
    val d2 = d2Base * min(1f, d * 2 / totalRadius)
    val r1 = radius1 * d2
    val r2 = radius2 * d2

    val pi2 = fPI / 2
    val sp1 = getVector(angle1a - pi2, r1)  // handle offset for p1a
    val sp2 = getVector(angle2a + pi2, r2)  // handle offset for p2a
    val sp3 = getVector(angle2b - pi2, r2)  // handle offset for p2b
    val sp4 = getVector(angle1b + pi2, r1)  // handle offset for p1b

    // "move bottom point to bar top border"
    // The code does:
    //  val yOffset = (abs(topBorder - p1a[1]) * riseRatio) - 1
    //  val fp1a = p1a.let { ... }
    val yOffset = (abs(topBorder - p1a.y) * riseRatio) - 1
    val fp1a = Offset(p1a.x, p1a.y - yOffset)
    val fp1b = Offset(p1b.x, p1b.y - yOffset)

    // Now replicate the path commands:
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

    // Draw the path
    drawPath(path = path, color = color)
    // Then "drawOval(circle2, paint)" => the top circle rect
    // We'll replicate "drawOval(rect, paint)"
    drawOval(
        color = color,
        topLeft = Offset(circle2.left, circle2.top),
        size = Size(circle2.width, circle2.height)
    )
}

/** From the original code: getVector(radians, length) -> Pair(x, y). */
private fun getVector(radians: Float, length: Float): Pair<Float, Float> {
    val x = cos(radians) * length
    val y = sin(radians) * length
    return x to y
}

/** Euclidean distance between (x1,y1) and (x2,y2). */
private fun getVectorLength(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return sqrt(dx * dx + dy * dy)
}

