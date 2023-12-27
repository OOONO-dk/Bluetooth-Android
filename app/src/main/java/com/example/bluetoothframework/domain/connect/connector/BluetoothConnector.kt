package com.example.bluetoothframework.domain.connect.connector

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.extensions.isIndicatable
import com.example.bluetoothframework.domain.extensions.isNotifiable
import com.example.bluetoothframework.domain.extensions.printGattTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothConnector @Inject constructor(
    private val context: Context
) : BluetoothConnectorInterface {
    private var bluetoothConnectCallback: BluetoothConnectCallback? = null
    private val _gattDevices = MutableStateFlow<List<BluetoothGatt>>(emptyList())

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

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            println("RRR - changed")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            println("RRR - descriptor write")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            println("RRR - characteristic write")
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
                println("RRR - characteristic read")
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            println("RRR - descriptor read")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            println("RRR - mtu changed")
        }

    }



    private fun handleDiscoveredServices(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            with(gatt) {
                printGattTable()
                setServiceNotifiers(gatt)
            }
        }
    }

    private fun setServiceNotifiers(gatt: BluetoothGatt) {
        gatt.services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                when {
                    characteristic.isNotifiable() -> {
                        enableNotifications(gatt, characteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                    characteristic.isIndicatable() -> {
                        enableNotifications(gatt, characteristic, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
                    }
                }
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, descriptorValue: ByteArray) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor =  characteristic.descriptors?.first() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, descriptorValue)
        } else {
            descriptor.value = descriptorValue
            gatt.writeDescriptor(descriptor)
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

        Handler(Looper.getMainLooper()).postDelayed({
        gatt.discoverServices()
        }, 1000)
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