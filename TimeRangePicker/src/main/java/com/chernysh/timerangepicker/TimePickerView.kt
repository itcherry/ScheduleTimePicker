package com.chernysh.timerangepicker

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.chernysh.timerangepicker.internal.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.math.min


class TimePickerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  // Math fields
  private val timePickerDataHolder: TimePickerDataHolder = TimePickerDataHolder()

  // Sizes
  var smallDotRadius: Float = SMALL_DOT_RADIUS.dpToPx(context)
  var thumbRadius = THUMB_RADIUS.dpToPx(context)
  var minutesPerDot = DOT_EACH_N_MINUTES_DEFAULT
  var maxRangesCount = MAX_RANGES_COUNT_DEFAULT
  var isAmPmTextFormat = IS_AM_PM_TIME_FORMAT_DEFAULT
  private var isAllDayEnabled = false

  // Paints
  private val paintArcTimeRange: Paint
  private val paintArcTimeRangeIntersected: Paint
  private val paintThumbTimeRange: Paint
  private val paintThumbTimeRangeIntersected: Paint
  private val paintCircleSecondary: Paint
  private val paintCircleTime: Paint
  private val paintCenterTime: Paint

  // Animators
  private val centerTimeAnimator: ValueAnimator

  // Dynamic styles
  private val arcColorStyleable = R.styleable.TimePickerView_tpv_range_arc_color
  private val arcThicknessStyleable = R.styleable.TimePickerView_tpv_range_arc_thickness

  private val centerTimeTextSizeStyleable = R.styleable.TimePickerView_tpv_center_time_text_size
  private val centerTimeTextColorStyleable = R.styleable.TimePickerView_tpv_center_time_text_color
  private val centerTimeTextFontStyleable = R.styleable.TimePickerView_tpv_center_time_text_font
  private val centerTimeAnimationDurationStyleable =
    R.styleable.TimePickerView_tpv_center_time_animation_duration_millis

  private val circleTimeTextSizeStyleable = R.styleable.TimePickerView_tpv_circle_time_text_size
  private val circleTimeTextColorStyleable = R.styleable.TimePickerView_tpv_circle_time_text_color
  private val circleTimeTextFontStyleable = R.styleable.TimePickerView_tpv_circle_time_text_font

  private val dotColorStyleable = R.styleable.TimePickerView_tpv_dot_color
  private val minutesPerDotStyleable = R.styleable.TimePickerView_tpv_minutes_per_dot
  private val dotRadiusStyleable = R.styleable.TimePickerView_tpv_dot_radius

  private val thumbRadiusStyleable = R.styleable.TimePickerView_tpv_thumb_radius
  private val thumbColorStyleable = R.styleable.TimePickerView_tpv_thumb_color

  private val maxRangesCountStyleable = R.styleable.TimePickerView_tpv_max_ranges_count
  private val isAmPmTimeFormatStyleable = R.styleable.TimePickerView_tpv_use_am_pm_time_format
  private val rangeIntersectionColorStyleable =
    R.styleable.TimePickerView_tpv_range_intersection_color

  // Listeners
  var timeRangesSelected: ((List<TimeRange>) -> Unit) = { }
  private val timeRangesSelectedSubject = PublishSubject.create<List<TimeRange>>()

  init {
    val midnightColor = ContextCompat.getColor(context, R.color.midnight)
    val brickColor = ContextCompat.getColor(context, R.color.brick)
    val berryColor = ContextCompat.getColor(context, R.color.berry)

    with(context.obtainStyledAttributes(attrs, R.styleable.TimePickerView, defStyleAttr, 0)) {
      with(PaintsInitialiser) {
        val circleTimeTypeface = if (hasValue(circleTimeTextFontStyleable)) {
          val circleTimeFontId = getResourceId(circleTimeTextFontStyleable, -1)
          ResourcesCompat.getFont(context, circleTimeFontId)
        } else null

        val centerTimeTypeface = if (hasValue(centerTimeTextFontStyleable)) {
          val circleTimeFontId = getResourceId(centerTimeTextFontStyleable, -1)
          ResourcesCompat.getFont(context, circleTimeFontId)
        } else null

        paintArcTimeRange = getArcTimeRangePaint(
          getColor(arcColorStyleable, berryColor),
          getDimensionPixelSize(arcThicknessStyleable, 8.dpToPx(context).toInt()).toFloat()
        )
        paintArcTimeRangeIntersected = getArcTimeRangeIntersectedPaint(
          getColor(rangeIntersectionColorStyleable, brickColor),
          getDimensionPixelSize(arcThicknessStyleable, 8.dpToPx(context).toInt()).toFloat()
        )
        paintCircleTime = getCircleTimePaint(
          getColor(circleTimeTextColorStyleable, midnightColor),
          getDimensionPixelSize(circleTimeTextSizeStyleable, 20.spToPx().toInt()).toFloat(),
          circleTimeTypeface ?: Typeface.DEFAULT
        )
        paintCenterTime = getCenterTimePaint(
          getColor(centerTimeTextColorStyleable, midnightColor),
          getDimensionPixelSize(centerTimeTextSizeStyleable, 56.spToPx().toInt()).toFloat(),
          centerTimeTypeface ?: Typeface.DEFAULT
        )
        paintThumbTimeRangeIntersected = getThumbTimeRangeIntersectedPaint(
          getColor(rangeIntersectionColorStyleable, brickColor)
        )
        paintThumbTimeRange = getThumbTimeRangePaint(getColor(thumbColorStyleable, berryColor))
        paintCircleSecondary = getCircleSecondaryPaint(getColor(dotColorStyleable, midnightColor))

        smallDotRadius = getDimensionPixelSize(
          dotRadiusStyleable, SMALL_DOT_RADIUS.dpToPx(context).toInt()
        ).toFloat()
        thumbRadius = getDimensionPixelSize(
          thumbRadiusStyleable, THUMB_RADIUS.dpToPx(context).toInt()
        ).toFloat()
        minutesPerDot = getInt(minutesPerDotStyleable, DOT_EACH_N_MINUTES_DEFAULT)
        maxRangesCount = getInt(maxRangesCountStyleable, MAX_RANGES_COUNT_DEFAULT)
        isAmPmTextFormat = getBoolean(isAmPmTimeFormatStyleable, IS_AM_PM_TIME_FORMAT_DEFAULT)
      }

      centerTimeAnimator = getCenterTimeAnimator()

      recycle()
    }
  }

  fun setArcColor(@ColorRes color: Int) {
    paintArcTimeRange.color = ContextCompat.getColor(context, color)
  }

  fun setArcThickness(thickness: Float) {
    paintArcTimeRange.strokeWidth = thickness
  }

  fun setArcIntersectionColor(@ColorRes color: Int) {
    paintArcTimeRangeIntersected.color = ContextCompat.getColor(context, color)
  }

  fun setArcIntersectionThickness(thickness: Float) {
    paintArcTimeRangeIntersected.strokeWidth = thickness
  }

  fun setCenterTextColor(@ColorRes color: Int) {
    paintCenterTime.color = ContextCompat.getColor(context, color)
  }

  fun setCenterTextSize(size: Float) {
    paintCenterTime.textSize = size
  }

  fun setCircleTextColor(@ColorRes color: Int) {
    paintCircleTime.color = ContextCompat.getColor(context, color)
  }

  fun setCircleTextSize(size: Float) {
    paintCircleTime.textSize = size
  }

  fun setThumbIntersectedColor(@ColorRes color: Int) {
    paintThumbTimeRangeIntersected.color = ContextCompat.getColor(context, color)
  }

  fun setThumbColor(@ColorRes color: Int) {
    paintThumbTimeRange.color = ContextCompat.getColor(context, color)
  }

  fun setDotsColor(@ColorRes color: Int) {
    paintCircleSecondary.color = ContextCompat.getColor(context, color)
  }

  /**
   *   When enable all-day mode it will draw ring for all circle without thumbs.
   *   Use it carefully. When this mode enabled - you won't have touch handler enabled.
   */
  fun enableAllDay(doEnable: Boolean) {
    isAllDayEnabled = doEnable
    invalidate()
  }

  private fun TypedArray.getCenterTimeAnimator() =
    ValueAnimator.ofInt(255, 0)
      .apply {
        duration =
          getInt(centerTimeAnimationDurationStyleable, SELECTED_TIME_ANIMATION_DURATION).toLong()
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

  /*-----------------------------------
   ------------ Measuring -------------
   -----------------------------------*/

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
    val measuredWidth = measureDimension(desiredWidth, widthMeasureSpec)

    setMeasuredDimension(
      measuredWidth,
      measuredWidth
    )
  }

  private fun measureDimension(
    desiredSize: Int,
    measureSpec: Int
  ): Int {
    val specMode = MeasureSpec.getMode(measureSpec)
    val specSize = MeasureSpec.getSize(measureSpec)

    val result = when (specMode) {
      MeasureSpec.EXACTLY -> specSize
      MeasureSpec.AT_MOST -> min(desiredSize, specSize)
      else -> desiredSize
    }

    if (result < desiredSize) {
      Log.e("TimeRangePicker", "The view is too small, the content might get cut")
    }

    return result
  }

  /*-----------------------------------
   ----- Storing important values -----
   -----------------------------------*/

  override fun onSizeChanged(
    w: Int,
    h: Int,
    oldw: Int,
    oldh: Int
  ) {
    super.onSizeChanged(w, h, oldw, oldh)

    val xpad = (paddingLeft + paddingRight).toFloat()
    val width = w.toFloat() - xpad

    val textRect = Rect()
    paintCircleTime.getTextBounds("24", 0, 2, textRect);

    timePickerDataHolder.apply {
      radius = width / 2 - textRect.width() - 16.dpToPx(context)
      top = paddingTop.toFloat()
      left = paddingLeft.toFloat()
      right = (w - paddingRight).toFloat()
      bottom = (h - paddingBottom).toFloat()
      center = PointF((right + left) / 2.0f, (bottom + top) / 2.0f)
      circleRectF = RectF(
        left + textRect.width() + 16.dpToPx(context) + smallDotRadius,
        top + textRect.width() + 16.dpToPx(context) + smallDotRadius,
        right - textRect.width() - 16.dpToPx(context) - smallDotRadius,
        bottom - textRect.width() - 16.dpToPx(context) - smallDotRadius
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

      if(isAllDayEnabled) {
        drawAllDaySelectors()
      } else {
        drawSelectors()
      }

      drawTimeText()
    }
  }

  private fun Canvas.drawSecondaryCircle() {
    val stepDegrees = getAngleCoefficient(minutesPerDot)
    var angle = 0.0f
    while (angle < DEGREES_IN_CIRLCE) { // While is using for Float step
      val circlePoint = context.getDecartCoordinates(
        timePickerDataHolder.timeTextWidth,
        timePickerDataHolder.radius,
        angle
      )
      drawCircle(circlePoint.x, circlePoint.y, smallDotRadius, paintCircleSecondary)
      angle += stepDegrees
    }
  }

  private fun Canvas.drawHourNumbers() {
    with(timePickerDataHolder) {
      drawText(
        if (isAmPmTextFormat) "12am" else "24",
        center.x - timeTextWidth / 2,
        timeTextHeight + 4.dpToPx(context),
        paintCircleTime
      )
      drawText(
        if (isAmPmTextFormat) "06\nam" else "06",
        right - timeTextWidth,
        center.y + timeTextHeight / 2,
        paintCircleTime
      )
      drawText(
        if (isAmPmTextFormat) "12pm" else "12",
        center.x - timeTextWidth / 2,
        bottom - 4.dpToPx(context),
        paintCircleTime
      )
      drawText(
        if (isAmPmTextFormat) "06\npm" else "18",
        left,
        center.y + timeTextHeight / 2,
        paintCircleTime
      )
    }
  }

  private fun Canvas.drawSelectors() {
    timePickerDataHolder.internalTimeRanges.forEach { timeRange ->
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

  private fun Canvas.drawAllDaySelectors() {
      drawCircle(
        timePickerDataHolder.center.x,
        timePickerDataHolder.center.y,
        timePickerDataHolder.radius,
        paintArcTimeRange
      )
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
      val timeText =
        "${(time / 60).toString().normaliseTime()}:${(time % 60).toString().normaliseTime()}"

      val centerTextRect = Rect()
      getClipBounds(centerTextRect)
      val cHeight: Int = centerTextRect.height()
      val cWidth: Int = centerTextRect.width()

      paintCenterTime.getTextBounds(timeText, 0, timeText.length, centerTextRect)

      val centerTextX: Float = cWidth / 2f - centerTextRect.width() / 2f - centerTextRect.left
      val centerTextY: Float = cHeight / 2f + centerTextRect.height() / 2f - centerTextRect.bottom

      drawText(timeText, centerTextX, centerTextY, paintCenterTime)
    }
  }

  /*-----------------------------------
  -------- Touch processing -----------
  -----------------------------------*/

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if(!isEnabled || isAllDayEnabled) return true

    var angle = getAngleFromDecart(timePickerDataHolder.center, event.x, event.y)
    var minute = angle.angleToMinute(minutesPerDot)

    if (minute % minutesPerDot != 0) {
      minute -= minute % minutesPerDot
      angle = minute.minuteToAngle(minutesPerDot)
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
        timeRangesSelected.invoke(timePickerDataHolder.getTimeRanges())
        timeRangesSelectedSubject.onNext(timePickerDataHolder.getTimeRanges())
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
    timePickerDataHolder.internalTimeRanges.forEach {
      it.isStartTimeMoving = false
      it.isEndTimeMoving = false
      if (it.startTimeRectF.containsClick(context, event.x, event.y)) {
        it.isStartTimeMoving = true
        needToSort = true
        return@forEach
      } else if (it.endTimeRectF.containsClick(context, event.x, event.y)) {
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
    val threshold = 6 * minutesPerDot
    if ((timePickerDataHolder.internalTimeRanges.size < maxRangesCount) &&
      timePickerDataHolder.canCreateTimeRange(minute, threshold)
    ) {
      timePickerDataHolder.addInternalTimeRangeWith(minute, minutesPerDot, context, thumbRadius)
      invalidate()
    }
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

  private fun moveTimeRange(
    angle: Float,
    minute: Int
  ) {
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

  private fun InternalTimeRange.moveStartTime(
    angle: Float,
    minute: Int
  ) {
    if (angle in 0f..endAngle) {
      startAngle = angle
      startTime = minute
      startTimeRectF = context.getThumbRectFByAngle(angle, timePickerDataHolder, thumbRadius)
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

  private fun InternalTimeRange.moveEndTime(
    angle: Float,
    minute: Int
  ) {
    if (angle in startAngle..360f) {
      endAngle = angle
      endTime = minute
      endTimeRectF = context.getThumbRectFByAngle(angle, timePickerDataHolder, thumbRadius)
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

  /* ---------------------------------------
  ------------ Click listeners -------------
  --------------------------------------- */

  fun getSelectedTimeRanges(): List<TimeRange> = timePickerDataHolder.getTimeRanges()

  fun setSelectedTimeRanges(timeRanges: List<TimeRange>) {
    if(timePickerDataHolder.radius == 0.0f) {
      viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
          } else {
            viewTreeObserver
              .removeGlobalOnLayoutListener(this)
          }
          timePickerDataHolder.setTimeRanges(timeRanges, minutesPerDot, context, thumbRadius)
        }
      })
    } else {
      timePickerDataHolder.setTimeRanges(timeRanges, minutesPerDot, context, thumbRadius)
      invalidate()
    }
  }

  fun timeRangesObservable(): Observable<List<TimeRange>> = timeRangesSelectedSubject
}
