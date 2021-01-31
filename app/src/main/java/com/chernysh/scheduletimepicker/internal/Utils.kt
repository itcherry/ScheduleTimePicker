package com.chernysh.scheduletimepicker.internal

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

internal fun Int.dpToPx(context: Context): Float = (this * context.resources.displayMetrics.density)

internal fun Int.spToPx(): Float = (this * Resources.getSystem().displayMetrics.scaledDensity)

internal fun String.normaliseTime() = if (this.length < 2) "0$this" else this

internal fun Float.degreesToRadians() = this * Math.PI.toFloat() / 180.0f

internal fun Float.radiansToDegrees() = this * 180.0f / Math.PI.toFloat()

internal fun getAngleCoefficient(minutesPerDot: Int) =
    (minutesPerDot * DEGREES_IN_CIRLCE).toFloat() / (HOURS_IN_DAY * MINUTES_IN_HOUR).toFloat()

internal fun Int.minuteToAngle(minutesPerDot: Int) =
    this / minutesPerDot.toFloat() * getAngleCoefficient(
        minutesPerDot
    )

internal fun Float.angleToMinute(minutesPerDot: Int): Int {
    val minute = ((this * minutesPerDot) / getAngleCoefficient(
        minutesPerDot
    )).toInt()

    return if (minute % minutesPerDot != 0) {
        minute + minute % minutesPerDot
    } else {
        minute
    }
}

internal fun getAngleFromDecart(circleCenterPoint: PointF, x: Float, y: Float): Float {
    val degree =
        atan((y - circleCenterPoint.y) / (x - circleCenterPoint.x)).radiansToDegrees() + 90

    return if (x < circleCenterPoint.x) {
        degree + 180
    } else {
        degree
    }
}

internal fun Context.getDecartCoordinates(timeTextWith: Float, radius: Float, angle: Float): PointF =
    PointF(
        timeTextWith + 16.dpToPx(this) + radius + radius * cos(angle.degreesToRadians()),
        timeTextWith + 16.dpToPx(this) + radius + radius * sin(angle.degreesToRadians())
    )

internal fun RectF.containsClick(context: Context, x: Float, y: Float): Boolean {
    val clickAreaMargin = CLICK_AREA_MARGIN_DP.dpToPx(context)
    val biggerRectForBigFatFinger = RectF(
        left - clickAreaMargin, top - clickAreaMargin,
        right + clickAreaMargin, bottom + clickAreaMargin
    )
    return biggerRectForBigFatFinger.contains(x, y)
}
