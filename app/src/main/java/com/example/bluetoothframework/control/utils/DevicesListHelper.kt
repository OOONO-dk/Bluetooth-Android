package com.example.bluetoothframework.control.utils

import android.bluetooth.BluetoothGatt
import com.example.bluetoothframework.control.delegates.BluetoothDeviceDelegate
import com.example.bluetoothframework.model.data.BluetoothConnectData
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.enums.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class DevicesListHelper {
    fun getDeviceGatt(list: List<BluetoothDeviceInfo>, device: BluetoothDeviceInfo): BluetoothGatt? {
        return list.firstOrNull { it.device.address == device.device.address }?.gatt
    }

    fun deviceIsConnected(list: List<BluetoothDeviceInfo>, device: BluetoothDeviceInfo): Boolean {
        return list.any {
            it.device.address == device.device.address &&
            it.connectionState == ConnectionState.CONNECTED
        }
    }

    fun removeFromDevices(list: List<BluetoothDeviceInfo>, device: BluetoothDeviceInfo): List<BluetoothDeviceInfo> {
        return list.filter { it.device.address != device.device.address }
    }

    fun getDeviceFromGatt(list: List<BluetoothDeviceInfo>, gatt: BluetoothGatt): BluetoothDeviceInfo? {
        return list.firstOrNull { it.device.address == gatt.device.address }
    }

    fun setServicesForDevice(
        list: MutableStateFlow<List<BluetoothDeviceInfo>>,
        data: BluetoothConnectData
    ): List<BluetoothDeviceInfo> {
        return modifyDeviceInfo(list, data.device.device.address) {
            it.copy(services = data.services)
        }
    }

    fun setGattForDevice(
        list: MutableStateFlow<List<BluetoothDeviceInfo>>,
        gatt: BluetoothGatt
    ): List<BluetoothDeviceInfo> {
        return modifyDeviceInfo(list, gatt.device.address) { it.copy(gatt = gatt) }
    }

    fun updateConnectionState(
        list: MutableStateFlow<List<BluetoothDeviceInfo>>,
        device: BluetoothDeviceInfo,
        delegate: BluetoothDeviceDelegate?,
        connectionState: ConnectionState
    ): List<BluetoothDeviceInfo> {
        return modifyDeviceInfo(list, device.device.address) {
            delegate?.onConnectionStateUpdate(device, connectionState)
            it.copy(connectionState = connectionState)
        }
    }

    private inline fun modifyDeviceInfo(
        list: MutableStateFlow<List<BluetoothDeviceInfo>>,
        address: String,
        operation: (BluetoothDeviceInfo) -> BluetoothDeviceInfo
    ): List<BluetoothDeviceInfo> {
        list.update { devices ->
            devices.map {
                if (it.device.address == address) {
                    operation(it)
                } else {
                    it
                }
            }
        }
        return list.value
    }
}