package com.example.bluetoothframework.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.example.bluetoothframework.domain.BluetoothDeviceDomain
import com.example.bluetoothframework.domain.scanner.BluetoothScanCallback
import com.example.bluetoothframework.domain.scanner.BluetoothScannerConfig
import com.example.bluetoothframework.domain.scanner.BluetoothScannerInterface
import com.example.bluetoothframework.domain.scanner.DeviceDiscoverTimeoutInterface
import com.example.bluetoothframework.domain.scanner.ScanTrackerInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// TODO: Handle onFailure

@SuppressLint("MissingPermission")
class BluetoothScanner @Inject constructor(
    private val context: Context,
    private val callback: BluetoothScanCallback,
    private val scanTracker: ScanTrackerInterface,
    private val deviceTimeout: DeviceDiscoverTimeoutInterface
) : BluetoothScannerInterface {
    private var isScanning = false
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bleScanner by lazy {
        bluetoothManager?.adapter?.bluetoothLeScanner
    }

    override fun startDiscovery(scannerConfig: BluetoothScannerConfig) {
        if (isScanning) return
        if (!isScanAllowed(scannerConfig)) return
        if (!hasBluetoothPermission()) return

        println("RRR - Started discovery")
        isScanning = true
        bleScanner?.startScan(scannerConfig.scanFilter, scannerConfig.scanSettings, scanCallback)

        deviceTimeout.checkDeviceDiscoverTimeout(scannerConfig.advertisingUpdateMillis) {
            removeOldDevices(scannerConfig.discoverInactivityDurationMillis)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onDeviceDiscovered(result)
        }
    }

    private fun onDeviceDiscovered(result: ScanResult) {
        val newDevice = result.toBluetoothDeviceDomain()
        val updatedList = updateDeviceInfo(newDevice, result)

        _scannedDevices.update { devices ->
            val deviceExists = updatedList != devices
            if (deviceExists) updatedList else {
                callback.onDeviceDiscovered(newDevice)
                devices + newDevice
            }
        }
    }

    private fun updateDeviceInfo(
        newDevice: BluetoothDeviceDomain,
        result: ScanResult
    ): List<BluetoothDeviceDomain> {
        return _scannedDevices.value.map { device ->
            if (device.address == newDevice.address) {
                device.copy(
                    discoverTimestamp = System.currentTimeMillis(),
                    rssi = result.rssi
                )
            } else {
                device
            }
        }
    }

    override fun stopDiscovery() {
        if (!hasBluetoothPermission()) return
        isScanning = false
        bleScanner?.stopScan(scanCallback)
        deviceTimeout.stopDeviceDiscoverTimeoutCheck()
    }

    private fun removeOldDevices(discoverInactivityDurationMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val thresholdTime = currentTime - discoverInactivityDurationMillis

        val updatedList = _scannedDevices.value.filter {
            it.discoverTimestamp > thresholdTime
        }

        val removedDevices =  _scannedDevices.value - updatedList.toSet()
        removedDevices.forEach { callback.onDeviceRemoved(it) }

        _scannedDevices.value = updatedList
    }

    private fun isScanAllowed(scannerConfig: BluetoothScannerConfig): Boolean {
        return if (!scanTracker.isScanningAllowed()) {
            Handler(Looper.getMainLooper()).postDelayed({
                startDiscovery(scannerConfig)
            }, DISCOVERY_DELAY_MILLIS)
            false
        } else {
            scanTracker.incrementScanCount()
            true
        }
    }

    @SuppressLint("InlinedApi")
    private fun hasBluetoothPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val DISCOVERY_DELAY_MILLIS = 30000L
    }
}