package com.chernysh.scheduletimepicker

import android.graphics.PointF
import android.graphics.RectF

data class DrawParamHolder(
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
)
