package com.example.bluetoothframework.scanning.utils.advertising_timeout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceDiscoverTimeout @Inject constructor() : DeviceDiscoverTimeoutInterface {
    private var isRunning = false
    private var job: Job? = null

    override fun checkDeviceDiscoverTimeout(advertisingUpdateMillis: Long, removeOldDevices: () -> Unit) {
        isRunning = true
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isRunning) {
                removeOldDevices()
                delay(advertisingUpdateMillis)
            }
        }
    }

    override fun stopDeviceDiscoverTimeoutCheck() {
        isRunning = false
        job?.cancel()
    }
}