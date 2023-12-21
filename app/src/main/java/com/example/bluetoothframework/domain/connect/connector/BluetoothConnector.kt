package com.example.bluetoothframework.domain.connect.connector

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.extensions.printGattTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothConnector @Inject constructor(
    private val context: Context
) : BluetoothConnectorInterface {
    private var bluetoothConnectCallback: BluetoothConnectCallback? = null
    private val _gattDevices = MutableStateFlow<List<BluetoothGatt>>(emptyList())
    private val GATT_MAX_MTU_SIZE = 517

    private fun String.toUuid(): UUID = UUID.fromString(this)
    val SERVICE_UUID = "5E631523-6743-11ED-9022-0242AC120002".toUuid() //TODO: move as parameter
    val READ_CHARACTERISTICS_UUID = "5E63F722-6743-11ED-9022-0242AC120002".toUuid() //TODO: move as parameter
    val WRITE_CHARACTERISTICS_UUID = "5E63F721-6743-11ED-9022-0242AC120002".toUuid() //TODO: move as parameter

    override fun setConnectionCallback(listener: BluetoothConnectCallback) {
        bluetoothConnectCallback = listener
    }

    override fun disconnectDevice(device: BluetoothDevice) {
        _gattDevices.value.find { it.device.address == device.address }?.disconnect()
    }

    override fun connectDevice(device: BluetoothDevice) {
        if (deviceIsConnected(device)) return
        device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            handleConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            handleDiscoveredServices(gatt, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int
        ) {
            println("RRR - read")
        }
    }



    private fun handleDiscoveredServices(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            with(gatt) {
                printGattTable()
            }
        }
    }

    private fun removeFromGattList(gatt: BluetoothGatt) {
        _gattDevices.update { currentList ->
            currentList.filter { it.device.address != gatt.device.address }
        }
    }

    private fun addToGattList(gatt: BluetoothGatt) {
        _gattDevices.update { currentList ->
            currentList + gatt
        }
    }

    private fun handleConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                onDeviceConnected(gatt)
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                onDeviceDisconnected(gatt)
            }
        } else {
            onConnectionFailed(gatt)
        }
    }

    private fun onDeviceConnected(gatt: BluetoothGatt) {
        addToGattList(gatt)
        bluetoothConnectCallback?.onDeviceConnected(gatt.device)
        gatt.discoverServices()
    }

    private fun onDeviceDisconnected(gatt: BluetoothGatt) {
        removeFromGattList(gatt)
        bluetoothConnectCallback?.onDeviceDisconnected(gatt.device)
        gatt.close()
    }

    private fun onConnectionFailed(gatt: BluetoothGatt) {
        removeFromGattList(gatt)
        bluetoothConnectCallback?.onConnectionFail(gatt.device)
        gatt.close()
    }

    private fun deviceIsConnected(device: BluetoothDevice): Boolean {
        return device.address in _gattDevices.value.map { it.device.address }
    }
}