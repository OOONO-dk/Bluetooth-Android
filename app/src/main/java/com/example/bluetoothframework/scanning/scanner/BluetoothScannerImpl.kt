package com.example.bluetoothframework.scanning.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.example.bluetoothframework.model.data.BluetoothScannerConfig
import com.example.bluetoothframework.scanning.delegates.BluetoothScanForwarderDelegate
import com.example.bluetoothframework.scanning.utils.scan_tracker.ScanTracker
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothScannerImpl @Inject constructor(
    private val context: Context,
    private val scanTracker: ScanTracker
) : BluetoothScanner {
    private var isScanning = false
    private var bluetoothScanForwarderDelegate: BluetoothScanForwarderDelegate? = null

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

        isScanning = true
        bleScanner?.startScan(scannerConfig.scanFilter, scannerConfig.scanSettings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            bluetoothScanForwarderDelegate?.onDeviceDiscovered(result)
        }

        override fun onScanFailed(errorCode: Int) {
            bluetoothScanForwarderDelegate?.onScanFailed()
        }
    }

    override fun stopDiscovery() {
        if (!hasBluetoothPermission()) return
        isScanning = false
        bleScanner?.stopScan(scanCallback)
    }

    override fun setScanDelegate(listener: BluetoothScanForwarderDelegate) {
        bluetoothScanForwarderDelegate = listener
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