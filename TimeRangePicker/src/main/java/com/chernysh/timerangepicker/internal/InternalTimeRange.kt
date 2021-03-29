package com.chernysh.timerangepicker.internal

import android.content.Context
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
internal data class InternalTimeRange(
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

  fun isCompletelyIntersectedBy(movingInternalTimeRange: InternalTimeRange?) =
    if (movingInternalTimeRange != null) {
      (this != movingInternalTimeRange) &&
        (this.endAngle <= movingInternalTimeRange.endAngle) &&
        (this.startAngle >= movingInternalTimeRange.startAngle)
    } else {
      false
    }

  companion object {
    fun createFrom(startMinute: Int,
                   endMinute: Int,
                   minutesPerDot: Int,
                   context: Context,
                   timePickerDataHolder: TimePickerDataHolder,
                   thumbRadius: Float): InternalTimeRange {
      val startAngle = startMinute.minuteToAngle(minutesPerDot)
      val endAngle = endMinute.minuteToAngle(minutesPerDot)

      return InternalTimeRange(
        startMinute,
        endMinute,
        startAngle,
        endAngle,
        context.getThumbRectFByAngle(startAngle, timePickerDataHolder, thumbRadius),
        context.getThumbRectFByAngle(endAngle, timePickerDataHolder, thumbRadius)
      )
    }
  }
}