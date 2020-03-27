package com.chernysh.scheduletimepicker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Math fields
    private val drawParamHolder: DrawParamHolder = DrawParamHolder()

    // Sizes
    private val smallDotRadius = 2.dpToPx(context)
    private val thumbRadius = 16.dpToPx(context)

    // Paints
    private val paintArcTimeRange: Paint
    private val paintThumbTimeRange: Paint
    private val paintCircleSecondary: Paint
    private val paintCircleTime: Paint

    init {
        paintArcTimeRange = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.berry)
            style = Paint.Style.STROKE
            strokeWidth = 8.dpToPx(context)
        }

        paintThumbTimeRange = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.berry)
            style = Paint.Style.FILL_AND_STROKE
        }

        paintCircleSecondary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.midnight)
            style = Paint.Style.FILL_AND_STROKE
        }

        paintCircleTime = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.midnight)
            textSize = 20.spToPx()
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

        drawParamHolder.apply {
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

        /*drawParamHolder.timeRanges.add(createTimeRange(180.0f))
        drawParamHolder.timeRanges.add(createTimeRange(90.0f))*/
    }

    /*-----------------------------------
    -------------- Draw -----------------
    -----------------------------------*/
    override fun onDraw(canvas: Canvas) {
        with(canvas) {
            drawSecondaryCircle()
            drawHourNumbers()
            drawSelectors()
        }
    }

    private fun Canvas.drawSecondaryCircle() {
        val stepDegrees = getAngleCoefficient()
        var angle = 0.0f
        while (angle < DEGREES_IN_CIRLCE) { // While is using for Float step
            val circlePoint = getDecartCoordinates(drawParamHolder.radius, angle)
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
        with(drawParamHolder) {
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
        drawParamHolder.timeRanges.forEach { timeRange ->
            drawCircle(
                timeRange.startTimeRectF.centerX(),
                timeRange.startTimeRectF.centerY(),
                thumbRadius,
                paintThumbTimeRange
            )

            drawCircle(
                timeRange.endTimeRectF.centerX(),
                timeRange.endTimeRectF.centerY(), thumbRadius, paintThumbTimeRange
            )

            drawArc(
                drawParamHolder.circleRectF,
                timeRange.startAngle - 90,
                timeRange.endAngle - timeRange.startAngle,
                false,
                paintArcTimeRange
            )
        }
    }

    /*-----------------------------------
    -------- Touch processing -----------
    -----------------------------------*/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var angle = getAngleFromDecart(event.x, event.y)
        var minute = angle.angleToMinute()

        if(minute % DOT_EACH_N_MINUTES != 0) {
            minute -= minute % DOT_EACH_N_MINUTES
            angle = minute.minuteToAngle()
        }

        return when (event.action) {
            ACTION_DOWN -> {
                drawParamHolder.timeRanges.forEach {
                    it.isStartTimeMoving = false
                    it.isEndTimeMoving = false
                    if (it.startTimeRectF.contains(event.x, event.y)) {
                        it.isStartTimeMoving = true
                        return@forEach
                    } else if (it.endTimeRectF.contains(event.x, event.y)) {
                        it.isEndTimeMoving = true
                        return@forEach
                    }
                }
                if ((drawParamHolder.timeRanges.size < MAX_RANGES_COUNT) &&
                    drawParamHolder.timeRanges.all { !it.isStartTimeMoving && !it.isEndTimeMoving && (minute < it.startTime || minute > it.endTime) }) {
                    drawParamHolder.timeRanges.add(createTimeRange(minute))
                    invalidate()
                }

                return true
            }
            ACTION_UP -> {
                drawParamHolder.timeRanges.forEach {
                    it.isStartTimeMoving = false
                    it.isEndTimeMoving = false
                }
                return true
            }
            ACTION_MOVE -> {
                val movingTimeRange =
                    drawParamHolder.timeRanges.firstOrNull { it.isStartTimeMoving || it.isEndTimeMoving }
                movingTimeRange?.let { timeRange ->
                    Log.d("TAG", "angle: $angle, minute: $minute, x: ${event.x}, y: ${event.y}")
                    if (timeRange.isStartTimeMoving) {
                        if ((minute < timeRange.endTime - 3 * DOT_EACH_N_MINUTES) &&
                            drawParamHolder.timeRanges.filter { (it != timeRange) && (timeRange.startTime > it.endTime) }
                                .all { minute > it.endTime + 3 * DOT_EACH_N_MINUTES }
                        ) {
                            timeRange.startAngle = angle
                            timeRange.startTime = minute
                            timeRange.startTimeRectF = getThumbRectFByAngle(angle)
                        }
                    } else if (timeRange.isEndTimeMoving) {
                        if ((minute > timeRange.startTime + 3 * DOT_EACH_N_MINUTES) &&
                            drawParamHolder.timeRanges.filter { (it != timeRange) && (it.startTime > timeRange.endTime) }
                                .all { minute < (it.startTime - 3 * DOT_EACH_N_MINUTES) }
                        ) {
                            timeRange.endAngle = angle
                            timeRange.endTime = minute
                            timeRange.endTimeRectF = getThumbRectFByAngle(angle)
                        }
                    }
                    invalidate()
                }
                return true
            }
            else -> {
                false
            }
        }
    }

    private fun createTimeRange(minute: Int): TimeRange {
        val startMinute = if (minute - 3 * DOT_EACH_N_MINUTES >= 0) {
            minute - 3 * DOT_EACH_N_MINUTES
        } else {
            0
        }

        val endMinute = if (minute + 3 * DOT_EACH_N_MINUTES <= MINUTES_IN_HOUR * HOURS_IN_DAY) {
            minute + 3 * DOT_EACH_N_MINUTES
        } else {
            MINUTES_IN_HOUR * HOURS_IN_DAY
        }

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

    private fun getThumbRectFByAngle(angle: Float): RectF {
        val startRangeCenterPointF =
            getDecartCoordinates(drawParamHolder.radius, angle - 90)

        Log.d(
            "TAG",
            "getThumbRectFByAngle(x: ${startRangeCenterPointF.x}, y: ${startRangeCenterPointF.y})"
        )
        return RectF(
            startRangeCenterPointF.x - thumbRadius,
            startRangeCenterPointF.y - thumbRadius,
            startRangeCenterPointF.x + thumbRadius,
            startRangeCenterPointF.y + thumbRadius
        )
    }

    /*-----------------------------------
     ----------- Math utils -------------
     -----------------------------------*/
    fun getAngleFromDecart(x: Float, y: Float): Float {
        val degree =
            atan((y - drawParamHolder.center.y) / (x - drawParamHolder.center.x)).radiansToDegrees() + 90

        return if (x < drawParamHolder.center.x) {
            degree + 180
        } else {
            degree
        }
    }

    private fun Float.degreesToRadians() = this * Math.PI.toFloat() / 180.0f

    private fun Float.radiansToDegrees() = this * 180.0f / Math.PI.toFloat()

    private fun Int.minuteToAngle() =
        this / DOT_EACH_N_MINUTES.toFloat() * getAngleCoefficient()

    private fun Float.angleToMinute(): Int {
        val minute = ((this * DOT_EACH_N_MINUTES) / getAngleCoefficient()).toInt()

        return if (minute % DOT_EACH_N_MINUTES != 0) {
            minute + minute % DOT_EACH_N_MINUTES
        } else {
            minute
        }
    }

    private fun getAngleCoefficient() =
        (DOT_EACH_N_MINUTES * DEGREES_IN_CIRLCE).toFloat() / (HOURS_IN_DAY * MINUTES_IN_HOUR).toFloat()

    private fun getDecartCoordinates(radius: Float, angle: Float): PointF =
        PointF(
            drawParamHolder.timeTextWidth + 8.dpToPx(context) + radius + radius * cos(angle.degreesToRadians()),
            drawParamHolder.timeTextWidth + 8.dpToPx(context) + radius + radius * sin(angle.degreesToRadians())
        )

    companion object {
        private const val HOURS_IN_DAY = 24
        private const val MINUTES_IN_HOUR = 60
        private const val DEGREES_IN_CIRLCE = 360
        private const val DOT_EACH_N_MINUTES = 15
        private const val MAX_RANGES_COUNT = 4
    }
}
