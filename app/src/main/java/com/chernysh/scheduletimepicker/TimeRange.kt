package com.chernysh.scheduletimepicker

import android.graphics.RectF

/**
 * Entity for selection one of time ranges.
 * All fields are in minutes.
 * Range on possible values: 0-1440 (minutes in 24 hours)
 *
 * @param startTime - time in minutes when range is starting
 * @param endTime - time in minutes when range finishing
 */
data class TimeRange(
    var startTime: Int,
    var endTime: Int,
    var startAngle: Float,
    var endAngle: Float,
    var startTimeRectF: RectF,
    var endTimeRectF: RectF,
    var isStartTimeMoving: Boolean = false,
    var isEndTimeMoving: Boolean = false,
    var lastMovedTime: Int = -1,
    var isUnderIntersectionFromStart: Boolean = false,
    var isUnderIntersectionFromEnd: Boolean = false
) {
    fun isUnderIntersection() = isUnderIntersectionFromStart || isUnderIntersectionFromEnd

    fun getSweepAngle() = if (endAngle >= startAngle) {
        endAngle - startAngle
    } else {
        endAngle + 360 - startAngle
    }

    fun isCompletelyIntersectedBy(movingTimeRange: TimeRange?) =
        if (movingTimeRange != null) {
            (this != movingTimeRange) &&
                    (this.endAngle <= movingTimeRange.endAngle) &&
                    (this.startAngle >= movingTimeRange.startAngle)
        } else {
            false
        }
}