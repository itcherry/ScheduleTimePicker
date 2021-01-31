package com.chernysh.timerangepicker.internal

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface

internal class PaintsInitialiser(private val context: Context) {
    fun getArcTimeRangePaint(colorToSet: Int, thickness: Float) = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = colorToSet
            style = Paint.Style.STROKE
            strokeWidth = thickness
        }

    fun getArcTimeRangeIntersectedPaint(colorToSet: Int, thickness: Float) =
        Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = colorToSet
                style = Paint.Style.STROKE
                pathEffect = DashPathEffect(floatArrayOf(80f, 10f), 0f)
                strokeWidth = thickness
            }

    fun getThumbTimeRangePaint(colorToSet: Int) = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = colorToSet
            style = Paint.Style.FILL_AND_STROKE
        }

    fun getThumbTimeRangeIntersectedPaint(colorToSet: Int) = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = colorToSet
            style = Paint.Style.FILL_AND_STROKE
        }

    fun getCircleSecondaryPaint(colorToSet: Int) = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = colorToSet
            style = Paint.Style.FILL_AND_STROKE
        }

    fun getCircleTimePaint(colorToSet: Int, size: Float, font: Typeface) =
        Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = colorToSet
                textSize = size
                typeface = font
            }

    fun getCenterTimePaint(colorToSet: Int, size: Float, font: Typeface) =
        Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = colorToSet
                textSize = size
                textAlign = Paint.Align.LEFT
                alpha = 255
                typeface = font
            }
}