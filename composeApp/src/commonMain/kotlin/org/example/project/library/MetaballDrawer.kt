package org.example.project.library

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

fun DrawScope.drawMetaball(
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

    if (radius1 == 0f || radius2 == 0f) return

    // Calculate the centers manually
    val circle1CenterX = (circle1.left + circle1.right) / 2
    val circle1CenterY = (circle1.top + circle1.bottom) / 2
    val circle2CenterX = (circle2.left + circle2.right) / 2
    val circle2CenterY = (circle2.top + circle2.bottom) / 2

    val distance = SliderUtils.getVectorLength(circle1CenterX, circle1CenterY, circle2CenterX, circle2CenterY)
    if (distance > maxDistance || distance <= abs(radius1 - radius2)) return

    val riseRatio = min(1f, max(0f, topBorder - circle2.top) / riseDistance)

    val angle1 = atan2(circle2CenterY - circle1CenterY, circle2CenterX - circle1CenterX)
    val angle2a = angle1 + acos((radius1 - radius2) / distance)
    val angle2b = angle1 - acos((radius1 - radius2) / distance)

    val p1a = SliderUtils.getVector(angle2a, radius1).let {
        Offset(it.first + circle1CenterX, it.second + circle1CenterY)
    }
    val p1b = SliderUtils.getVector(angle2b, radius1).let {
        Offset(it.first + circle1CenterX, it.second + circle1CenterY)
    }

    val p2a = SliderUtils.getVector(angle2a, radius2).let {
        Offset(it.first + circle2CenterX, it.second + circle2CenterY)
    }
    val p2b = SliderUtils.getVector(angle2b, radius2).let {
        Offset(it.first + circle2CenterX, it.second + circle2CenterY)
    }

    with(path) {
        reset()
        moveTo(p1a.x, p1a.y)
        cubicTo(p1a.x, p1a.y, p2a.x, p2a.y, p2a.x, p2a.y)
        lineTo(circle2CenterX, circle2CenterY)
        lineTo(p2b.x, p2b.y)
        cubicTo(p2b.x, p2b.y, p1b.x, p1b.y, p1b.x, p1b.y)
        close()
    }

    drawPath(path, color)
}

