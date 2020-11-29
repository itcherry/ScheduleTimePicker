package com.chernysh.scheduletimepicker

import android.graphics.PointF
import android.graphics.RectF

data class TimePickerDataHolder(
    var center: PointF = PointF(),
    var left: Float = 0.0f,
    var right: Float = 0.0f,
    var top: Float = 0.0f,
    var bottom: Float = 0.0f,
    var circleRectF: RectF = RectF(),
    var radius: Float = 0.0f,
    var timeTextWidth: Float = 0.0f,
    var timeTextHeight: Float = 0.0f,
    val timeRanges: MutableList<TimeRange> = mutableListOf()
) {

    fun getMovingTimeRange() =
        this.timeRanges.firstOrNull { it.isStartTimeMoving || it.isEndTimeMoving }

    fun resetLastMovedTime() = this.timeRanges.forEach { it.lastMovedTime = -1 }

    fun resetTimeRangesMoveFlags() {
        timeRanges.forEach {
            it.isStartTimeMoving = false
            it.isEndTimeMoving = false
        }
    }

    fun resetIntersectionFlags() = this.timeRanges.forEach {
        it.isUnderIntersectionFromStart = false
        it.isUnderIntersectionFromEnd = false
    }

    fun makeMovingTimeRangeDrawOnTop() =
        timeRanges.sortBy { it.isStartTimeMoving || it.isEndTimeMoving }

    fun canCreateTimeRange(minute: Int, threshold: Int) =
        timeRanges.all {
            !it.isStartTimeMoving &&
                    !it.isEndTimeMoving &&
                    ((minute + threshold) < it.startTime || (minute - threshold) > it.endTime)
        }

    fun getLastMovedTime() = this.timeRanges.firstOrNull { it.lastMovedTime > -1 }?.lastMovedTime

    fun mergeStartTimeIntersectionRange() {
        val intersectingStartTimeRange = getIntersectingStartTimeRange()
        val movingTimeRange = getMovingTimeRange()

        if (intersectingStartTimeRange != null && movingTimeRange != null) {
            movingTimeRange.startAngle = intersectingStartTimeRange.startAngle
            movingTimeRange.startTime = intersectingStartTimeRange.startTime
            movingTimeRange.startTimeRectF =
                intersectingStartTimeRange.startTimeRectF
            timeRanges.remove(intersectingStartTimeRange)
        }
    }

    fun mergeEndTimeIntersectionRange() {
        val intersectingEndTimeRange = getIntersectingEndTimeRange()
        val movingTimeRange = getMovingTimeRange()

        if (intersectingEndTimeRange != null && movingTimeRange != null) {
            movingTimeRange.endAngle = intersectingEndTimeRange.endAngle
            movingTimeRange.endTime = intersectingEndTimeRange.endTime
            movingTimeRange.endTimeRectF = intersectingEndTimeRange.endTimeRectF
            timeRanges.remove(intersectingEndTimeRange)
        }
    }

    fun getIntersectingStartTimeRange() = getMovingTimeRange()?.let { movingTimeRange ->
        timeRanges.firstOrNull { timeRange ->
            (timeRange != movingTimeRange) &&
                    (timeRange.startAngle <= movingTimeRange.startAngle) &&
                    (timeRange.endAngle >= movingTimeRange.startAngle) &&
                    (timeRange.endAngle <= movingTimeRange.endAngle)
        }
    }

    fun getIntersectingEndTimeRange() = getMovingTimeRange()?.let { movingTimeRange ->
        timeRanges.firstOrNull { timeRange ->
            (timeRange != movingTimeRange) &&
                    (timeRange.endAngle >= movingTimeRange.endAngle) &&
                    (timeRange.startAngle <= movingTimeRange.endAngle) &&
                    (timeRange.startAngle >= movingTimeRange.startAngle)
        }
    }

    fun removeMovingTimeRangeIfNoTimeSelected() {
        val movingTimeRange = getMovingTimeRange()
        if ((movingTimeRange != null) && (movingTimeRange.startAngle == movingTimeRange.endAngle)) {
            timeRanges.remove(movingTimeRange)
        }
    }

    fun getCompletelyIntersectedRanges() =
        timeRanges.filter { it.isCompletelyIntersectedBy(getMovingTimeRange()) }

    fun removeCompletelyIntersectedRanges() =
        timeRanges.removeAll { timeRange ->
            timeRange.isCompletelyIntersectedBy(getMovingTimeRange())
        }

}
