package org.example.project.library

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal fun getVector(radians: Float, length: Float): Pair<Float, Float> {
    val x = cos(radians) * length
    val y = sin(radians) * length
    return x to y
}

internal fun getVectorLength(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return sqrt(dx * dx + dy * dy)
}

internal fun offsetRectToPosition(centerX: Float, vararg rects: Rect) {
    rects.forEach { r ->
        val dx = centerX - (r.left + r.width / 2)
        r.translate(dx = dx, dy = 0f)
    }
}