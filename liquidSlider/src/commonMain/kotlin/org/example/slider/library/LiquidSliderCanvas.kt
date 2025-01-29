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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.slider.library.LiquidSliderConfig
import kotlin.math.roundToInt

@Composable
internal fun BoxScope.LiquidSliderCanvas(
    density: Density,
    desiredHeightPx: Float,
    isDragging: MutableState<Boolean>,
    onBeginTracking: () -> Unit,
    onEndTracking: () -> Unit,
    size: LiquidSliderSize,
    desiredWidthPx: Float,
    barHeightPx: Float,
    sliderPosition: MutableState<Float>,
    onValueChange: (Float) -> Unit,
    topCircleAnimOffset: State<Float>,
    textMeasurer: TextMeasurer,
    liquidSliderConfig: LiquidSliderConfig,
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
                    val w = size.width.dp.toPx()
                    val actualWidth = desiredWidthPx
                    val touchDiameterPx = barHeightPx * liquidSliderConfig.touchCircleDiameter
                    val maxMovement =
                        actualWidth - touchDiameterPx - liquidSliderConfig.barInnerHorizontalOffset

                    val newPos =
                        (sliderPosition.value + dragAmount.x / maxMovement).coerceIn(0f, 1f)
                    sliderPosition.value = newPos
                    onValueChange(newPos)
                }
            }
    ) {
        val canvasWidth = size.width.dp.toPx().coerceAtLeast(desiredWidthPx)
        val canvasHeight = size.height.dp.toPx().coerceAtLeast(desiredHeightPx)

        val barVerticalOffsetPx = barHeightPx * liquidSliderConfig.barVerticalOffset
        val barRect = Rect(
            left = 0f,
            top = barVerticalOffsetPx,
            right = canvasWidth,
            bottom = barVerticalOffsetPx + barHeightPx
        )

        val topCircleDiameterPx = barHeightPx * liquidSliderConfig.topCircleDiameter
        val bottomCircleDiameterPx = barHeightPx * liquidSliderConfig.bottomCircleDiameter
        val touchDiameterPx = barHeightPx * liquidSliderConfig.touchCircleDiameter
        val labelDiameterPx = barHeightPx * liquidSliderConfig.labelCircleDiameter

        val liquidBallMaxDistPx = barHeightPx * liquidSliderConfig.liquidBalMaxDistance
        val liquidBallRiseDistPx = barHeightPx * liquidSliderConfig.liquidBalRiseDistance
        val cornerRadiusPx = liquidSliderConfig.barCornerRadius * density.density

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

        val maxMovement =
            canvasWidth - touchDiameterPx - liquidSliderConfig.barInnerHorizontalOffset * 2
        val xPos =
            liquidSliderConfig.barInnerHorizontalOffset + (touchDiameterPx / 2) + maxMovement * sliderPosition.value
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
            color = liquidSliderConfig.barColor,
            topLeft = Offset(barRect.left, barRect.top),
            size = Size(barRect.width, barRect.height),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )

        val textStyleBar = TextStyle(
            color = liquidSliderConfig.barTextColor,
            fontSize = ((liquidSliderConfig.textSize)).sp,
            textAlign = TextAlign.Center
        )
        val startTextLayout =
            textMeasurer.measure(
                AnnotatedString(liquidSliderConfig.startText),
                style = textStyleBar
            )
        drawText(
            textMeasurer,
            liquidSliderConfig.startText,
            topLeft = Offset(
                x = liquidSliderConfig.textOffset * density.density,
                y = barRect.top + (barRect.height - startTextLayout.size.height) / 2
            ),
            style = textStyleBar
        )
        val endTextLayout =
            textMeasurer.measure(AnnotatedString(liquidSliderConfig.endText), style = textStyleBar)
        drawText(
            textMeasurer,
            liquidSliderConfig.endText,
            topLeft = Offset(
                x = barRect.right - endTextLayout.size.width - liquidSliderConfig.textOffset * density.density,
                y = barRect.top + (barRect.height - endTextLayout.size.height) / 2
            ),
            style = textStyleBar
        )

        val path = Path()
        drawLiquidBall(
            liquidBallPath = path,
            bottomCircle = rectBottomCircle,
            topCircle = rectTopCircle,
            barTopBoundary = barRect.top,
            liquidBallRiseLimit = liquidBallRiseDistPx,
            maxDistanceBetweenCircles = liquidBallMaxDistPx,
            cornerRadiusPx = cornerRadiusPx,
            topCircleSpreadFactor = liquidSliderConfig.topSpreadFactor,
            bottomCircleStartSpreadFactor = liquidSliderConfig.bottomStartSpreadFactor,
            bottomCircleEndSpreadFactor = liquidSliderConfig.bottomEndSpreadFactor,
            handleRate = liquidSliderConfig.liquidBalHandlerFactor,
            liquidBallColor = liquidSliderConfig.barColor
        )

        drawCircle(
            color = liquidSliderConfig.bubbleColor,
            radius = labelDiameterPx / 2f,
            center = Offset(rectLabel.centerX, rectLabel.centerY)
        )

        if (liquidSliderConfig.imageList.isNotEmpty()) {
            val index = (sliderPosition.value * (liquidSliderConfig.imageList.size - 1))
                .roundToInt()
                .coerceIn(0, liquidSliderConfig.imageList.lastIndex)
            val imagePainter = liquidSliderConfig.imageList[index]
            val imageSize = labelDiameterPx * liquidSliderConfig.bubbleImageSizeFactor

            // Calculate the offset for the image
            val imageOffsetX = rectLabel.centerX - imageSize / 2
            val imageOffsetY = rectLabel.centerY - imageSize / 2

            // Use withTransform to position the image
            drawImage(
                image = imagePainter,
                dstSize = IntSize(imageSize.toInt(), imageSize.toInt()),
                dstOffset = IntOffset(imageOffsetX.toInt(), imageOffsetY.toInt()), // Positioning the image
            )
        } else {
            val labelString = liquidSliderConfig.bubbleText
                ?: ((sliderPosition.value * liquidSliderConfig.progressCount).toInt()).toString()
            val textLayoutLabel = textMeasurer.measure(
                AnnotatedString(labelString),
                style = textStyleBar.copy(color = liquidSliderConfig.textColor)
            )

            val backgroundDiameter = labelDiameterPx * liquidSliderConfig.bubbleTextBackgroundSizeFactor
            val backgroundRadius = backgroundDiameter / 2f

            if (liquidSliderConfig.showBubbleTextBackground)
                drawCircle(
                    color = liquidSliderConfig.bubbleTextBackground,
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
                style = textStyleBar.copy(color = liquidSliderConfig.textColor)
            )
        }
    }

}