@file:Suppress("DEPRECATION")

package com.example.bluetoothframework.domain.connect.connector

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import com.example.bluetoothframework.domain.connect.BluetoothCharacteristicChangeCallback
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.extensions.isIndicatable
import com.example.bluetoothframework.domain.extensions.isNotifiable
import com.example.bluetoothframework.domain.extensions.printGattTable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothConnector @Inject constructor(
    private val context: Context
) : BluetoothConnectorInterface {
    private var writeDescriptorsBuffer: MutableMap<BluetoothGattDescriptor, CompletableDeferred<Unit>> = mutableMapOf()
    private var bluetoothConnectCallback: BluetoothConnectCallback? = null
    private var bluetoothCharacteristicChangeCallback: BluetoothCharacteristicChangeCallback? = null
    private val _gattDevices = MutableStateFlow<List<BluetoothGatt>>(emptyList())

    override fun setConnectionCallback(listener: BluetoothConnectCallback) {
        bluetoothConnectCallback = listener
    }

    override fun setCharacteristicChangeCallback(listener: BluetoothCharacteristicChangeCallback) {
        bluetoothCharacteristicChangeCallback = listener
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
            bluetoothCharacteristicChangeCallback?.onCharacteristicChanged(value, gatt.device)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            val completion = writeDescriptorsBuffer.remove(descriptor)
            completion?.complete(Unit)
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
        CoroutineScope(Dispatchers.IO).launch {
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
    }

    private suspend fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, descriptorValue: ByteArray) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptors =  characteristic.descriptors

        descriptors.forEach { descriptor ->
            val writeCompletion = CompletableDeferred<Unit>()
            writeDescriptorsBuffer[descriptor] = writeCompletion

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, descriptorValue)
            } else {
                descriptor.value = descriptorValue
                gatt.writeDescriptor(descriptor)
            }

            writeCompletion.await()
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
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionFailed(gatt)
            return
        }

        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> onDeviceConnected(gatt)
            BluetoothGatt.STATE_DISCONNECTED -> onDeviceDisconnected(gatt)
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