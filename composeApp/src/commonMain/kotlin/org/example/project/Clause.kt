package org.example.project

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.math.*

enum class SliderSize(val height: Dp, val width: Dp) {
    NORMAL(56.dp, 224.dp),
    SMALL(40.dp, 160.dp)
}

private const val TOP_SPREAD_FACTOR = 0.4f
private const val BOTTOM_START_SPREAD_FACTOR = 0.25f
private const val BOTTOM_END_SPREAD_FACTOR = 0.1f
private const val METABALL_HANDLER_FACTOR = 2.4f
private const val METABALL_MAX_DISTANCE = 100f
@Composable
fun FluidSlider(
    modifier: Modifier = Modifier,
    value: Float = 0.5f,
    onValueChange: (Float) -> Unit,
    size: SliderSize = SliderSize.NORMAL,
    startText: String = "0",
    endText: String = "100",
    bubbleText: String? = null,
    barColor: Color = Color(0xFF6168E7),
    bubbleColor: Color = Color.Green,
    barTextColor: Color = Color.Green,
    bubbleTextColor: Color = Color.Black,
    animationDuration: Int = 400,
    onBeginTracking: () -> Unit = {},
    onEndTracking: () -> Unit = {}
) {
    val density = LocalDensity.current
    val barHeightPx = with(density) { size.height.toPx() }
    val textMeasurer = rememberTextMeasurer()

    val desiredWidth = with(density) { size.width.toPx() }
    val desiredHeight = barHeightPx * 2.5f

    val touchDiameter = barHeightPx
    val labelDiameter = barHeightPx - 10f
    val metaballRiseDistance = barHeightPx * 1.1f
    val barVerticalOffset = barHeightPx * 1.5f

    var position by remember { mutableStateOf(value) }
    var touching by remember { mutableStateOf(false) }

    val labelOffset by animateFloatAsState(
        targetValue = if (touching) -metaballRiseDistance else 0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        )
    )

    val textStyle = TextStyle(
        color = barTextColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )

    fun getVectorLength(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    fun drawMetaball(
        drawScope: DrawScope,
        circle1: Offset,
        circle2: Offset,
        radius1: Float,
        radius2: Float,
        handleRate: Float,
        maxDistance: Float,
        topSpreadFactor: Float,
        bottomStartSpreadFactor: Float,
        bottomEndSpreadFactor: Float,
        riseRatio: Float
    ) {
        with(drawScope) {
            val d = getVectorLength(circle1.x, circle1.y, circle2.x, circle2.y)


            val riseProgress = (1 - riseRatio).coerceIn(0f, 1f)
            val bottomSpreadFactor = bottomStartSpreadFactor + (bottomEndSpreadFactor - bottomStartSpreadFactor) * riseProgress

            val u1 = atan2(circle2.y - circle1.y, circle2.x - circle1.x)
            val angle = acos((radius1 - radius2) / d)

            val angle1a = u1 + angle * bottomSpreadFactor
            val angle1b = u1 - angle * bottomSpreadFactor
            val angle2a = (u1 + PI.toFloat() - angle * topSpreadFactor)
            val angle2b = (u1 - PI.toFloat() + angle * topSpreadFactor)

            val p1a = Offset(
                x = circle1.x + cos(angle1a) * radius1,
                y = circle1.y + sin(angle1a) * radius1
            )
            val p1b = Offset(
                x = circle1.x + cos(angle1b) * radius1,
                y = circle1.y + sin(angle1b) * radius1
            )
            val p2a = Offset(
                x = circle2.x + cos(angle2a) * radius2,
                y = circle2.y + sin(angle2a) * radius2
            )
            val p2b = Offset(
                x = circle2.x + cos(angle2b) * radius2,
                y = circle2.y + sin(angle2b) * radius2
            )

            val totalRadius = radius1 + radius2
            val d2Base = minOf(
                maxOf(topSpreadFactor, bottomSpreadFactor) * handleRate,
                getVectorLength(p1a.x, p1a.y, p2a.x, p2a.y) / totalRadius
            )

            val d2 = d2Base * minOf(1f, d * 2 / (radius1 + radius2))

            val r1 = radius1 * d2
            val r2 = radius2 * d2

            val path = Path().apply {
                moveTo(p1a.x, p1a.y)
                cubicTo(
                    p1a.x + cos(angle1a - PI.toFloat()/2) * r1,
                    p1a.y + sin(angle1a - PI.toFloat()/2) * r1,
                    p2a.x + cos(angle2a + PI.toFloat()/2) * r2,
                    p2a.y + sin(angle2a + PI.toFloat()/2) * r2,
                    p2a.x, p2a.y
                )
                lineTo(circle2.x, circle2.y)
                lineTo(p2b.x, p2b.y)
                cubicTo(
                    p2b.x + cos(angle2b - PI.toFloat()/2) * r2,
                    p2b.y + sin(angle2b - PI.toFloat()/2) * r2,
                    p1b.x + cos(angle1b + PI.toFloat()/2) * r1,
                    p1b.y + sin(angle1b + PI.toFloat()/2) * r1,
                    p1b.x, p1b.y
                )
                close()
            }

            drawPath(path = path, color = bubbleColor)
            drawCircle(color = bubbleColor, radius = radius2, center = circle2)
        }
    }

    LaunchedEffect(value) {
        position = value
    }

    Canvas(
        modifier = modifier
            .width(with(density) { desiredWidth.toDp() })
            .height(with(density) { desiredHeight.toDp() })
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        touching = true
                        onBeginTracking()
                    },
                    onDragEnd = {
                        touching = false
                        onEndTracking()
                    },
                    onDragCancel = {
                        touching = false
                        onEndTracking()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newPosition = (position + dragAmount.x / (desiredWidth - touchDiameter))
                        .coerceIn(0f, 1f)
                    position = newPosition
                    onValueChange(newPosition)
                }
            }
    ) {
        // Draw base bar
        drawRoundRect(
            color = barColor,
            topLeft = Offset(x = 0f, y = barVerticalOffset),
            size = Size(width = desiredWidth, height = barHeightPx),
            cornerRadius = CornerRadius(x = 2.dp.toPx(), y = 2.dp.toPx())
        )

        // Draw start text
        drawText(
            textMeasurer = textMeasurer,
            text = startText,
            topLeft = Offset(x = 8.dp.toPx(), y = barVerticalOffset + barHeightPx/4),
            style = textStyle
        )

        // Draw end text
        val endTextResult = textMeasurer.measure(endText, textStyle)
        drawText(
            textMeasurer = textMeasurer,
            text = endText,
            topLeft = Offset(
                x = desiredWidth - endTextResult.size.width - 8.dp.toPx(),
                y = barVerticalOffset + barHeightPx/4
            ),
            style = textStyle
        )

        // Calculate bubble position
        val bubbleX = touchDiameter/2 + (desiredWidth - touchDiameter) * position
        val bubbleY = barVerticalOffset + labelOffset

        // Draw metaball and bubble
        val offset = Offset(size.width.toPx() / 2, size.height.toPx() / 2)
        withTransform({
            // Move the origin to the bottom-left corner of this canvas
            // so (0,0) is bottom-left instead of top-left.


        }) {
        drawMetaball(
            drawScope = this,
            circle1 = Offset(x = bubbleX, y = bubbleY),
            circle2 = Offset(x = bubbleX, y = bubbleY + metaballRiseDistance),
            radius1 = labelDiameter / 2,
            radius2 = touchDiameter / 2,
            handleRate = METABALL_HANDLER_FACTOR,
            maxDistance = METABALL_MAX_DISTANCE * density.density,
            topSpreadFactor = TOP_SPREAD_FACTOR,
            bottomStartSpreadFactor = BOTTOM_START_SPREAD_FACTOR,
            bottomEndSpreadFactor = BOTTOM_END_SPREAD_FACTOR,
            riseRatio = if (touching) 1f else 0f
        )}

        // Draw bubble text
        val bubbleTextContent = bubbleText ?: (position * 100).toInt().toString()
        val bubbleTextResult = textMeasurer.measure(bubbleTextContent, textStyle.copy(color = bubbleTextColor))
        drawText(
            textMeasurer = textMeasurer,
            text = bubbleTextContent,
            topLeft = Offset(
                x = bubbleX - bubbleTextResult.size.width / 2,
                y = bubbleY - bubbleTextResult.size.height / 2
            ),
            style = textStyle.copy(color = bubbleTextColor)
        )
    }
}