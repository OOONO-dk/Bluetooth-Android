package com.example.bluetoothframework.presentation

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothframework.domain.BluetoothHelperInterface
import com.example.bluetoothframework.domain.controller.BluetoothControllerInterface
import com.example.bluetoothframework.domain.scanner.BluetoothScannerConfig
import com.example.bluetoothframework.domain.scanner.GG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    bluetoothHelper: GG,
    private val bluetoothController: BluetoothControllerInterface,
): ViewModel() {
    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothHelper.scannedDevices,
        _state
    ) { scannedDevices, state ->
        state.copy(scannedDevices = scannedDevices)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value) //TODO: need this?

    private val serviceUUIDs = listOf(
        // Sirene
        "5E631523-6743-11ED-9022-0242AC120002",
        "5E630000-6743-11ED-9022-0242AC120002",
        "5E63F720-6743-11ED-9022-0242AC120002",
        "5E63F721-6743-11ED-9022-0242AC120002",
        "5E63F722-6743-11ED-9022-0242AC120002",

        // Co-driver
        "00001523-1212-efde-1523-785feabcd123",
        "0ef51523-d3f3-4e34-93de-8bcac38fc718",
        "0b40dfe0-7c04-43ae-a07a-aa28c52e9328",
        "72e81523-d54b-48ea-a00c-5ea3973cebc7"
    )

    private val scanFilter = serviceUUIDs.map { serviceUuid ->
        ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(serviceUuid)).build()
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanConfig = BluetoothScannerConfig(
        scanFilter = scanFilter,
        scanSettings = scanSettings,
        advertisingUpdateMillis = TimeUnit.SECONDS.toMillis(1),
        discoverInactivityDurationMillis = TimeUnit.SECONDS.toMillis(5)
    )

    fun startScan() {
        bluetoothController.startDiscovery(scanConfig)
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }
}