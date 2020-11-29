package com.chernysh.scheduletimepicker

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.min


class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Math fields
    private val timePickerDataHolder: TimePickerDataHolder = TimePickerDataHolder()

    // Sizes
    private val smallDotRadius = SMALL_DOT_RADIUS.dpToPx(context)
    private val thumbRadius = THUMB_RADIUS.dpToPx(context)

    // Paints
    private val paintArcTimeRange: Paint
    private val paintArcTimeRangeIntersected: Paint
    private val paintThumbTimeRange: Paint
    private val paintThumbTimeRangeIntersected: Paint
    private val paintCircleSecondary: Paint
    private val paintCircleTime: Paint
    private val paintCenterTime: Paint

    private val centerTimeAnimator: ValueAnimator

    init {
        with(PaintsInitialiser(context)) {
            paintArcTimeRange = getArcTimeRangePaint()
            paintArcTimeRangeIntersected = getArcTimeRangeIntersectedPaint()
            paintThumbTimeRange = getThumbTimeRangePaint()
            paintThumbTimeRangeIntersected = getThumbTimeRangeIntersectedPaint()
            paintCircleSecondary = getCircleSecondaryPaint()
            paintCircleTime = getCircleTimePaint()
            paintCenterTime = getCenterTimePaint()
        }

        centerTimeAnimator = ValueAnimator.ofInt(255, 0)
            .apply {
                duration = SELECTED_TIME_ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    paintCenterTime.alpha = it.animatedValue as Int
                    invalidate()
                }
                doOnEnd {
                    paintCenterTime.alpha = 255
                    timePickerDataHolder.resetLastMovedTime()
                }
            }
    }

    /*-----------------------------------
     ------------ Measuring -------------
     -----------------------------------*/

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val measuredWidth = measureDimension(desiredWidth, widthMeasureSpec)

        setMeasuredDimension(
            measuredWidth,
            measuredWidth
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        val result = when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> min(desiredSize, specSize)
            else -> desiredSize
        }

        if (result < desiredSize) {
            Log.e("ScheduleTimePicker", "The view is too small, the content might get cut")
        }

        return result
    }

    /*-----------------------------------
     ----- Storing important values -----
     -----------------------------------*/

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val xpad = (paddingLeft + paddingRight).toFloat()
        val width = w.toFloat() - xpad

        val textRect = Rect()
        paintCircleTime.getTextBounds("24", 0, 2, textRect);

        timePickerDataHolder.apply {
            radius = width / 2 - textRect.width() - 8.dpToPx(context)
            top = paddingTop.toFloat()
            left = paddingLeft.toFloat()
            right = (w - paddingRight).toFloat()
            bottom = (h - paddingBottom).toFloat()
            center = PointF(
                (right + left) / 2.0f,
                (bottom + top) / 2.0f
            )
            circleRectF = RectF(
                left + textRect.width() + 8.dpToPx(context) + smallDotRadius,
                top + textRect.width() + 4.dpToPx(context) + smallDotRadius,
                right - textRect.width() - 8.dpToPx(context) - smallDotRadius,
                bottom - textRect.width() - 4.dpToPx(context) - smallDotRadius
            )
            timeTextWidth = textRect.width().toFloat()
            timeTextHeight = textRect.height().toFloat()
        }
    }

    /*-----------------------------------
    -------------- Draw -----------------
    -----------------------------------*/

    override fun onDraw(canvas: Canvas) {
        with(canvas) {
            drawSecondaryCircle()
            drawHourNumbers()
            drawSelectors()
            drawTimeText()
        }
    }

    private fun Canvas.drawSecondaryCircle() {
        val stepDegrees = getAngleCoefficient()
        var angle = 0.0f
        while (angle < DEGREES_IN_CIRLCE) { // While is using for Float step
            val circlePoint = context.getDecartCoordinates(
                timePickerDataHolder.timeTextWidth,
                timePickerDataHolder.radius,
                angle
            )
            drawCircle(
                circlePoint.x,
                circlePoint.y,
                smallDotRadius,
                paintCircleSecondary
            )
            angle += stepDegrees
        }
    }

    private fun Canvas.drawHourNumbers() {
        with(timePickerDataHolder) {
            drawText(
                "24",
                center.x - timeTextWidth / 2,
                timeTextHeight + 4.dpToPx(context),
                paintCircleTime
            )
            drawText("06", right - timeTextWidth, center.y + timeTextHeight / 2, paintCircleTime)
            drawText(
                "12",
                center.x - timeTextWidth / 2,
                bottom - 4.dpToPx(context),
                paintCircleTime
            )
            drawText("18", left, center.y + timeTextHeight / 2, paintCircleTime)
        }
    }

    private fun Canvas.drawSelectors() {
        timePickerDataHolder.timeRanges.forEach { timeRange ->
            val paintForThumbs =
                if (timeRange.isUnderIntersection()) paintThumbTimeRangeIntersected else paintThumbTimeRange
            val paintForArc =
                if (timeRange.isUnderIntersection()) paintArcTimeRangeIntersected else paintArcTimeRange

            // Start time thumb
            drawThumb(
                timeRange.startTimeRectF,
                timeRange.isUnderIntersectionFromEnd,
                paintForThumbs
            )

            // End time thumb
            drawThumb(
                timeRange.endTimeRectF,
                timeRange.isUnderIntersectionFromStart,
                paintForThumbs
            )

            drawArc(
                timePickerDataHolder.circleRectF,
                timeRange.startAngle - 90,
                timeRange.getSweepAngle(),
                false,
                paintForArc
            )
        }
    }

    private fun Canvas.drawThumb(
        rectF: RectF,
        isUnderIntersection: Boolean,
        paintForThumbs: Paint
    ) {
        if (!isUnderIntersection) {
            drawCircle(
                rectF.centerX(),
                rectF.centerY(),
                thumbRadius,
                paintForThumbs
            )
        }
    }

    private fun Canvas.drawTimeText() {
        timePickerDataHolder.getLastMovedTime()?.let { time ->
            val timeText = "${(time / 60).toString().normaliseTime()}:${
                (time % 60).toString().normaliseTime()
            }"

            val centerTextRect = Rect()
            getClipBounds(centerTextRect)
            val cHeight: Int = centerTextRect.height()
            val cWidth: Int = centerTextRect.width()
            paintCenterTime.getTextBounds(timeText, 0, timeText.length, centerTextRect)
            val centerTextX: Float = cWidth / 2f - centerTextRect.width() / 2f - centerTextRect.left
            val centerTextY: Float =
                cHeight / 2f + centerTextRect.height() / 2f - centerTextRect.bottom
            drawText(timeText, centerTextX, centerTextY, paintCenterTime)
        }
    }

    /*-----------------------------------
    -------- Touch processing -----------
    -----------------------------------*/

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var angle = getAngleFromDecart(timePickerDataHolder.center, event.x, event.y)
        var minute = angle.angleToMinute()

        if (minute % DOT_EACH_N_MINUTES != 0) {
            minute -= minute % DOT_EACH_N_MINUTES
            angle = minute.minuteToAngle()
        }

        return when (event.action) {
            ACTION_DOWN -> {
                selectTimeCircleToMove(event)
                createAndDrawNewTimeRange(minute)
                return true
            }
            ACTION_UP -> {
                mergeRangesAfterMoveIfNeeded()
                centerTimeAnimator.start()
                return true
            }
            ACTION_MOVE -> {
                moveTimeRange(angle, minute)
                return true
            }
            else -> {
                false
            }
        }
    }

    /* ----------------------------------------------
    --------- Handle finger DOWN gestures -----------
    ---------------------------------------------- */

    private fun selectTimeCircleToMove(event: MotionEvent) {
        var needToSort = false
        timePickerDataHolder.timeRanges.forEach {
            it.isStartTimeMoving = false
            it.isEndTimeMoving = false
            if (it.startTimeRectF.contains(event.x, event.y)) {
                it.isStartTimeMoving = true
                needToSort = true
                return@forEach
            } else if (it.endTimeRectF.contains(event.x, event.y)) {
                it.isEndTimeMoving = true
                needToSort = true
                return@forEach
            }
        }

        // In order to draw intersected time range below the one user actually move
        if (needToSort) {
            timePickerDataHolder.makeMovingTimeRangeDrawOnTop()
        }
    }

    private fun createAndDrawNewTimeRange(minute: Int) {
        val threshold = 6 * DOT_EACH_N_MINUTES
        if ((timePickerDataHolder.timeRanges.size < MAX_RANGES_COUNT) &&
            timePickerDataHolder.canCreateTimeRange(minute, threshold)
        ) {
            timePickerDataHolder.timeRanges.add(getNewTimeRange(minute))
            invalidate()
        }
    }

    private fun getNewTimeRange(minute: Int): TimeRange {
        val startMinute = minute - 3 * DOT_EACH_N_MINUTES
        val endMinute = minute + 3 * DOT_EACH_N_MINUTES

        val startAngle = startMinute.minuteToAngle()
        val endAngle = endMinute.minuteToAngle()

        return TimeRange(
            startMinute,
            endMinute,
            startAngle,
            endAngle,
            getThumbRectFByAngle(startAngle),
            getThumbRectFByAngle(endAngle)
        )
    }

    /* --------------------------------------------
    --------- Handle finger UP gestures -----------
    -------------------------------------------- */

    private fun mergeRangesAfterMoveIfNeeded() {
        timePickerDataHolder.apply {
            removeCompletelyIntersectedRanges()
            mergeStartTimeIntersectionRange()
            mergeEndTimeIntersectionRange()
            removeMovingTimeRangeIfNoTimeSelected()
            resetTimeRangesMoveFlags()
        }

        invalidate()
    }

    /* ---------------------------------------
    --------- Handle MOVE gestures -----------
    --------------------------------------- */

    private fun moveTimeRange(angle: Float, minute: Int) {
        timePickerDataHolder.getMovingTimeRange()?.apply {
            if (isStartTimeMoving) {
                moveStartTime(angle, minute)
            } else if (isEndTimeMoving) {
                moveEndTime(angle, minute)
            }

            timePickerDataHolder.getCompletelyIntersectedRanges()
                .forEach {
                    it.isUnderIntersectionFromStart = true
                    it.isUnderIntersectionFromEnd = true
                }

            invalidate()
        }
    }

    private fun TimeRange.moveStartTime(angle: Float, minute: Int) {
        if (angle in 0f..endAngle) {
            startAngle = angle
            startTime = minute
            startTimeRectF = getThumbRectFByAngle(angle)
            lastMovedTime = minute

            val intersectingRange = timePickerDataHolder.getIntersectingStartTimeRange()
            if (intersectingRange != null) {
                intersectingRange.isUnderIntersectionFromStart = true
                intersectingRange.isUnderIntersectionFromEnd = false
            } else {
                timePickerDataHolder.resetIntersectionFlags()
            }
        }
    }

    private fun TimeRange.moveEndTime(angle: Float, minute: Int) {
        if (angle in startAngle..360f) {
            endAngle = angle
            endTime = minute
            endTimeRectF = getThumbRectFByAngle(angle)
            lastMovedTime = minute

            val intersectingRange = timePickerDataHolder.getIntersectingEndTimeRange()
            if (intersectingRange != null) {
                intersectingRange.isUnderIntersectionFromStart = false
                intersectingRange.isUnderIntersectionFromEnd = true
            } else {
                timePickerDataHolder.resetIntersectionFlags()
            }
        }
    }

    private fun getThumbRectFByAngle(angle: Float): RectF {
        val startRangeCenterPointF =
            context.getDecartCoordinates(
                timePickerDataHolder.timeTextWidth,
                timePickerDataHolder.radius,
                angle - 90
            )

        return RectF(
            startRangeCenterPointF.x - thumbRadius,
            startRangeCenterPointF.y - thumbRadius,
            startRangeCenterPointF.x + thumbRadius,
            startRangeCenterPointF.y + thumbRadius
        )
    }
}
