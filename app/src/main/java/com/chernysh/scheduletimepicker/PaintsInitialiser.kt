package com.chernysh.scheduletimepicker

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.core.content.ContextCompat

class PaintsInitialiser(private val context: Context) {
    fun getArcTimeRangePaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, R.color.berry)
            style = Paint.Style.STROKE
            strokeWidth = 8.dpToPx(context)
        }

    fun getArcTimeRangeIntersectedPaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, R.color.brick)
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(80f, 10f), 0f)
            strokeWidth = 8.dpToPx(context)
        }

    fun getThumbTimeRangePaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, R.color.berry)
            style = Paint.Style.FILL_AND_STROKE
        }

    fun getThumbTimeRangeIntersectedPaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, R.color.brick)
            style = Paint.Style.FILL_AND_STROKE
        }

    fun getCircleSecondaryPaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, R.color.midnight)
            style = Paint.Style.FILL_AND_STROKE
        }

    fun getCircleTimePaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, R.color.midnight)
            textSize = 20.spToPx()
        }

    fun getCenterTimePaint() = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
        color = ContextCompat.getColor(context, R.color.midnight)
        textSize = 56.spToPx()
        textAlign = Paint.Align.LEFT
        alpha = 255
    }
}