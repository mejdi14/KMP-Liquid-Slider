package org.example.project.library

internal data class Rect(
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