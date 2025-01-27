package org.example.project.library

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

internal fun DrawScope.drawLiquidBall(
    liquidBallPath: Path,
    bottomCircle: Rect,
    topCircle: Rect,
    barTopBoundary: Float,
    liquidBallRiseLimit: Float,
    maxDistanceBetweenCircles: Float,
    cornerRadiusPx: Float,
    topCircleSpreadFactor: Float,
    bottomCircleStartSpreadFactor: Float,
    bottomCircleEndSpreadFactor: Float,
    handleRate: Float,
    liquidBallColor: Color
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

    val riseRatio = min(1f, max(0f, barTopBoundary - topCircle.top) / liquidBallRiseLimit)

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
    val bottomCircleSpreadFactor =
        bottomCircleStartSpreadFactor - bottomCircleSpreadFactorDiff * riseRatio

    val fullPi = PI.toFloat()
    val baseAngle = atan2(yDistance, xDistance)
    val angleDifference = acos((bottomCircleRadius - topCircleRadius) / centerDistance)

    val bottomCircleAngle1 =
        baseAngle + angleOffset1 + (angleDifference - angleOffset1) * bottomCircleSpreadFactor
    val bottomCircleAngle2 =
        baseAngle - angleOffset1 - (angleDifference - angleOffset1) * bottomCircleSpreadFactor
    val topCircleAngle1 =
        baseAngle + fullPi - angleOffset2 - (fullPi - angleOffset2 - angleDifference) * topCircleSpreadFactor
    val topCircleAngle2 =
        baseAngle - fullPi + angleOffset2 + (fullPi - angleOffset2 - angleDifference) * topCircleSpreadFactor

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
        getVectorLength(
            bottomCirclePoint1.x,
            bottomCirclePoint1.y,
            topCirclePoint1.x,
            topCirclePoint1.y
        ) / combinedRadius
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

    with(liquidBallPath) {
        reset()
        moveTo(adjustedBottomPoint1.x, adjustedBottomPoint1.y + cornerRadiusPx)
        lineTo(adjustedBottomPoint1.x, adjustedBottomPoint1.y)

        cubicTo(
            adjustedBottomPoint1.x + bottomHandle1.first,
            adjustedBottomPoint1.y + bottomHandle1.second,
            topCirclePoint1.x + topHandle1.first,
            topCirclePoint1.y + topHandle1.second,
            topCirclePoint1.x,
            topCirclePoint1.y
        )

        lineTo(topCircle.centerX, topCircle.centerY)
        lineTo(topCirclePoint2.x, topCirclePoint2.y)

        cubicTo(
            topCirclePoint2.x + topHandle2.first,
            topCirclePoint2.y + topHandle2.second,
            adjustedBottomPoint2.x + bottomHandle2.first,
            adjustedBottomPoint2.y + bottomHandle2.second,
            adjustedBottomPoint2.x,
            adjustedBottomPoint2.y
        )

        lineTo(adjustedBottomPoint2.x, adjustedBottomPoint2.y + cornerRadiusPx)
        close()
    }

    drawPath(path = liquidBallPath, color = liquidBallColor)
    drawOval(
        color = liquidBallColor,
        topLeft = Offset(topCircle.left, topCircle.top),
        size = Size(topCircle.width, topCircle.height)
    )
}
