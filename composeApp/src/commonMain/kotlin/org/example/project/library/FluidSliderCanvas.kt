package org.example.project.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.FluidSliderSize
import org.example.project.library.SliderConstants.BAR_CORNER_RADIUS
import org.example.project.library.SliderConstants.BAR_INNER_HORIZONTAL_OFFSET
import org.example.project.library.SliderConstants.BAR_VERTICAL_OFFSET
import org.example.project.library.SliderConstants.BOTTOM_CIRCLE_DIAMETER
import org.example.project.library.SliderConstants.BOTTOM_END_SPREAD_FACTOR
import org.example.project.library.SliderConstants.BOTTOM_START_SPREAD_FACTOR
import org.example.project.library.SliderConstants.LABEL_CIRCLE_DIAMETER
import org.example.project.library.SliderConstants.METABALL_HANDLER_FACTOR
import org.example.project.library.SliderConstants.METABALL_MAX_DISTANCE
import org.example.project.library.SliderConstants.METABALL_RISE_DISTANCE
import org.example.project.library.SliderConstants.TEXT_OFFSET
import org.example.project.library.SliderConstants.TOP_CIRCLE_DIAMETER
import org.example.project.library.SliderConstants.TOP_SPREAD_FACTOR
import org.example.project.library.SliderConstants.TOUCH_CIRCLE_DIAMETER

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