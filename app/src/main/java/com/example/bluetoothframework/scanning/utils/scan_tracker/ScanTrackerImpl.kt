package com.example.bluetoothframework.scanning.utils.scan_tracker

import android.os.CountDownTimer
import javax.inject.Inject

class ScanTrackerImpl @Inject constructor() : ScanTracker {
    private var scanCount = DEFAULT_SCAN_COUNT
    private var timer: CountDownTimer? = null

    override fun incrementScanCount() {
        if (timer == null) startTimer()
        scanCount++
    }

    override fun isScanningAllowed(): Boolean {
        return scanCount < MAX_SCAN_COUNT
    }

    private fun startTimer() {
        timer = object : CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                scanCount = DEFAULT_SCAN_COUNT
                timer?.cancel()
                timer = null

            }
        }.start()
    }

    companion object {
        private const val DEFAULT_SCAN_COUNT = 0
        private const val MAX_SCAN_COUNT = 5
        private const val TIMER_INTERVAL = 1000L
        private const val TIMER_DURATION = 30000L
    }
}