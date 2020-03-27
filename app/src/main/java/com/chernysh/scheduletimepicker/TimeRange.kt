package com.chernysh.scheduletimepicker

import android.graphics.Rect
import android.graphics.RectF
import java.util.*

/**
 * Entity for selection one of time ranges.
 * All fields are in minutes.
 * Range on possible values: 0-1440 (minutes in 24 hours)
 *
 * @param startTime - time in minutes when range is starting
 * @param endTime - time in minutes when range finishing
 */
data class TimeRange(var startTime: Int,
                     var endTime: Int,
                     var startAngle: Float,
                     var endAngle: Float,
                     var startTimeRectF: RectF,
                     var endTimeRectF: RectF,
                     var isStartTimeMoving: Boolean = false,
                     var isEndTimeMoving: Boolean = false) {

    fun getStartTimeDate() = Calendar.getInstance().apply {
        clear()
        set(Calendar.HOUR_OF_DAY, startTime / 24)
        set(Calendar.MINUTE, startTime % 24)
    }

    fun getEndTimeDate() = Calendar.getInstance().apply {
        clear()
        set(Calendar.HOUR_OF_DAY, endTime / 24)
        set(Calendar.MINUTE, endTime % 24)
    }
}