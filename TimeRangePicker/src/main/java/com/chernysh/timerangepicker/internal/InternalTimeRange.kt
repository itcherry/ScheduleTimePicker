package com.chernysh.timerangepicker.internal

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

  fun getStartDate() = getDateOnlyMinutes(startTime)

  fun getEndDate() = getDateOnlyMinutes(endTime)

  private fun getDateOnlyMinutes(minutes: Int) =
    Calendar.getInstance().apply {
      set(0, 0, 0, 0, 0, 0)
      add(Calendar.MINUTE, minutes)
    }.time
}