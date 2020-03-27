package com.chernysh.scheduletimepicker

import android.content.Context
import android.content.res.Resources

fun Int.dpToPx(context: Context): Float = (this * context.resources.displayMetrics.density)

fun Int.spToPx(): Float = (this * Resources.getSystem().displayMetrics.scaledDensity)