package com.chernysh.timerangepicker


/**
 * Start and end time is in minutes starting from 12am.
 * For example startTime == 120 -> it means this is 02am
 *             endTime == 360 -> it means this is 06am
 */
data class TimeRange(
  val startTime: Int,
  val endTime: Int
)