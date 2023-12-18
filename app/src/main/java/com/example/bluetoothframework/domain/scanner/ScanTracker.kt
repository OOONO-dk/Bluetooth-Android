package com.example.bluetoothframework.domain.scanner

import android.os.CountDownTimer
import javax.inject.Inject

class ScanTracker @Inject constructor() : ScanTrackerInterface {
    private var scanCount = DEFAULT_SCAN_COUNT
    private var timer: CountDownTimer? = null

    override fun incrementScanCount() {
        if (timer == null) {
            startTimer()
        }
        scanCount++
    }

    override fun isScanningAllowed(): Boolean {
        val isAllowed = scanCount < MAX_SCAN_COUNT

        if (!isAllowed) {
            println("RRR - Scanner is not allowed to scan due to over 5 retries in 30 seconds")
        }

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