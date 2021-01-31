package com.chernysh.timerangepicker.internal

import android.graphics.PointF
import android.graphics.RectF
import com.chernysh.timerangepicker.TimeRange

internal data class TimePickerDataHolder(
    var center: PointF = PointF(),
    var left: Float = 0.0f,
    var right: Float = 0.0f,
    var top: Float = 0.0f,
    var bottom: Float = 0.0f,
    var circleRectF: RectF = RectF(),
    var radius: Float = 0.0f,
    var timeTextWidth: Float = 0.0f,
    var timeTextHeight: Float = 0.0f,
    val internalTimeRanges: MutableList<InternalTimeRange> = mutableListOf()
) {

    fun getMovingTimeRange() =
        this.internalTimeRanges.firstOrNull { it.isStartTimeMoving || it.isEndTimeMoving }

    fun resetLastMovedTime() = this.internalTimeRanges.forEach { it.lastMovedTime = -1 }

    fun resetTimeRangesMoveFlags() {
        internalTimeRanges.forEach {
            it.isStartTimeMoving = false
            it.isEndTimeMoving = false
        }
    }

    fun resetIntersectionFlags() = this.internalTimeRanges.forEach {
        it.isUnderIntersectionFromStart = false
        it.isUnderIntersectionFromEnd = false
    }

    fun makeMovingTimeRangeDrawOnTop() =
        internalTimeRanges.sortBy { it.isStartTimeMoving || it.isEndTimeMoving }

    fun canCreateTimeRange(minute: Int, threshold: Int) =
        internalTimeRanges.all {
            !it.isStartTimeMoving &&
                    !it.isEndTimeMoving &&
                    ((minute + threshold) < it.startTime || (minute - threshold) > it.endTime)
        }

    fun getLastMovedTime() =
        this.internalTimeRanges.firstOrNull { it.lastMovedTime > -1 }?.lastMovedTime

    fun mergeStartTimeIntersectionRange() {
        val intersectingStartTimeRange = getIntersectingStartTimeRange()
        val movingTimeRange = getMovingTimeRange()

        if (intersectingStartTimeRange != null && movingTimeRange != null) {
            movingTimeRange.startAngle = intersectingStartTimeRange.startAngle
            movingTimeRange.startTime = intersectingStartTimeRange.startTime
            movingTimeRange.startTimeRectF =
                intersectingStartTimeRange.startTimeRectF
            internalTimeRanges.remove(intersectingStartTimeRange)
        }
    }

    fun mergeEndTimeIntersectionRange() {
        val intersectingEndTimeRange = getIntersectingEndTimeRange()
        val movingTimeRange = getMovingTimeRange()

        if (intersectingEndTimeRange != null && movingTimeRange != null) {
            movingTimeRange.endAngle = intersectingEndTimeRange.endAngle
            movingTimeRange.endTime = intersectingEndTimeRange.endTime
            movingTimeRange.endTimeRectF = intersectingEndTimeRange.endTimeRectF
            internalTimeRanges.remove(intersectingEndTimeRange)
        }
    }

    fun getIntersectingStartTimeRange() = getMovingTimeRange()?.let { movingTimeRange ->
        internalTimeRanges.firstOrNull { timeRange ->
            (timeRange != movingTimeRange) &&
                    (timeRange.startAngle <= movingTimeRange.startAngle) &&
                    (timeRange.endAngle >= movingTimeRange.startAngle) &&
                    (timeRange.endAngle <= movingTimeRange.endAngle)
        }
    }

    fun getIntersectingEndTimeRange() = getMovingTimeRange()?.let { movingTimeRange ->
        internalTimeRanges.firstOrNull { timeRange ->
            (timeRange != movingTimeRange) &&
                    (timeRange.endAngle >= movingTimeRange.endAngle) &&
                    (timeRange.startAngle <= movingTimeRange.endAngle) &&
                    (timeRange.startAngle >= movingTimeRange.startAngle)
        }
    }

    fun removeMovingTimeRangeIfNoTimeSelected() {
        val movingTimeRange = getMovingTimeRange()
        if ((movingTimeRange != null) && (movingTimeRange.startAngle == movingTimeRange.endAngle)) {
            internalTimeRanges.remove(movingTimeRange)
        }
    }

    fun getCompletelyIntersectedRanges() =
        internalTimeRanges.filter { it.isCompletelyIntersectedBy(getMovingTimeRange()) }

    fun removeCompletelyIntersectedRanges() =
        internalTimeRanges.removeAll { timeRange ->
            timeRange.isCompletelyIntersectedBy(getMovingTimeRange())
        }

    fun getTimeRanges() = internalTimeRanges.map {
        TimeRange(
            it.getStartDate(),
            it.getEndDate()
        )
    }
}
