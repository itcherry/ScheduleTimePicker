package com.chernysh.scheduletimepicker

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chernysh.timerangepicker.TimeRange

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Handler().postDelayed({
            scheduleTimePicker.setSelectedTimeRanges(
              listOf(TimeRange(120, 360), TimeRange(720, 1080))
            )
        }, 2000L)

        scheduleTimePicker.timeRangesSelected = {
            it.toString()
        }
    }
}
