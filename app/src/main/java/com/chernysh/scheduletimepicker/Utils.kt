package com.chernysh.scheduletimepicker

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

fun Int.dpToPx(context: Context): Float = (this * context.resources.displayMetrics.density)

fun Int.spToPx(): Float = (this * Resources.getSystem().displayMetrics.scaledDensity)

fun String.normaliseTime() = if (this.length < 2) "0$this" else this

fun Float.degreesToRadians() = this * Math.PI.toFloat() / 180.0f

fun Float.radiansToDegrees() = this * 180.0f / Math.PI.toFloat()

fun getAngleCoefficient() =
    (DOT_EACH_N_MINUTES * DEGREES_IN_CIRLCE).toFloat() / (HOURS_IN_DAY * MINUTES_IN_HOUR).toFloat()

fun Int.minuteToAngle() =
    this / DOT_EACH_N_MINUTES.toFloat() * getAngleCoefficient()

fun Float.angleToMinute(): Int {
    val minute = ((this * DOT_EACH_N_MINUTES) / getAngleCoefficient()).toInt()

    return if (minute % DOT_EACH_N_MINUTES != 0) {
        minute + minute % DOT_EACH_N_MINUTES
    } else {
        minute
    }
}

fun getAngleFromDecart(circleCenterPoint: PointF, x: Float, y: Float): Float {
    val degree =
        atan((y - circleCenterPoint.y) / (x - circleCenterPoint.x)).radiansToDegrees() + 90

    return if (x < circleCenterPoint.x) {
        degree + 180
    } else {
        degree
    }
}

fun Context.getDecartCoordinates(timeTextWith: Float, radius: Float, angle: Float): PointF =
    PointF(
        timeTextWith + 8.dpToPx(this) + radius + radius * cos(angle.degreesToRadians()),
        timeTextWith + 8.dpToPx(this) + radius + radius * sin(angle.degreesToRadians())
    )
