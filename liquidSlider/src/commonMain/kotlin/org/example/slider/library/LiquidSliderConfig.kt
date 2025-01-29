package org.example.slider.library

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import org.example.slider.library.SliderConstants.LIQUID_BALL_TEXT_BACKGROUND_FACTOR


data class LiquidSliderConfig(
    val barColor: Color = Color(0xFF6168E7),
    val bubbleColor: Color = Color(0xFF6168E7),
    val textColor: Color = Color.Black,
    val barTextColor: Color = Color.White,
    val imageList: List<ImageBitmap> = emptyList(),
    val bubbleImageSizeFactor: Float = 0.8f,
    val startText: String = SliderConstants.TEXT_START,
    val endText: String = SliderConstants.TEXT_END,
    val textSize: Float = SliderConstants.TEXT_SIZE,
    val bubbleText: String? = null,
    val showBubbleTextBackground: Boolean = true,
    val bubbleTextBackground: Color = Color.White,
    val bubbleTextBackgroundSizeFactor: Float = LIQUID_BALL_TEXT_BACKGROUND_FACTOR,
    val progressCount: Int = SliderConstants.PROGRESS_COUNT,
    val barCornerRadius: Float = SliderConstants.BAR_CORNER_RADIUS.toFloat(),
    val barVerticalOffset: Float = SliderConstants.BAR_VERTICAL_OFFSET,
    val barInnerHorizontalOffset: Float = SliderConstants.BAR_INNER_HORIZONTAL_OFFSET,
    val sliderWidth: Float = SliderConstants.SLIDER_WIDTH.toFloat(),
    val sliderHeight: Float = SliderConstants.SLIDER_HEIGHT,
    val topCircleDiameter: Float = SliderConstants.TOP_CIRCLE_DIAMETER,
    val bottomCircleDiameter: Float = SliderConstants.BOTTOM_CIRCLE_DIAMETER,
    val touchCircleDiameter: Float = SliderConstants.TOUCH_CIRCLE_DIAMETER,
    val labelCircleDiameter: Float = SliderConstants.LABEL_CIRCLE_DIAMETER,
    val animationDuration: Int = SliderConstants.ANIMATION_DURATION,
    val topSpreadFactor: Float = SliderConstants.TOP_SPREAD_FACTOR,
    val bottomStartSpreadFactor: Float = SliderConstants.BOTTOM_START_SPREAD_FACTOR,
    val bottomEndSpreadFactor: Float = SliderConstants.BOTTOM_END_SPREAD_FACTOR,
    val liquidBalHandlerFactor: Float = SliderConstants.LIQUID_BALL_HANDLER_FACTOR,
    val liquidBalMaxDistance: Float = SliderConstants.LIQUID_BALL_MAX_DISTANCE,
    val liquidBalRiseDistance: Float = SliderConstants.LIQUID_BALL_RISE_DISTANCE,
    val textOffset: Float = SliderConstants.TEXT_OFFSET.toFloat(),
    val initialPosition: Float = SliderConstants.INITIAL_POSITION
)