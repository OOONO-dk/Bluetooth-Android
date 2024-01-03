@file:Suppress("DEPRECATION")

package com.example.bluetoothframework.connection.connector

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.bluetoothframework.connection.delegates.BluetoothConnectForwarderDelegate
import com.example.bluetoothframework.connection.enqueue.WriteEnqueuerInterface
import com.example.bluetoothframework.extensions.isIndicatable
import com.example.bluetoothframework.extensions.isNotifiable
import com.example.bluetoothframework.extensions.isWritable
import com.example.bluetoothframework.extensions.isWritableWithoutResponse
import com.example.bluetoothframework.extensions.printGattTable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothConnector @Inject constructor(
    private val context: Context,
    private val writeEnqueuer: WriteEnqueuerInterface
) : BluetoothConnectorInterface {
    private var writeDescriptorsBuffer: MutableMap<BluetoothGattDescriptor, CompletableDeferred<Unit>> = mutableMapOf()
    private var bluetoothConnectForwarderDelegate: BluetoothConnectForwarderDelegate? = null
    private val _gattDevices = MutableStateFlow<List<BluetoothGatt>>(emptyList())

    override fun setConnectionDelegate(listener: BluetoothConnectForwarderDelegate) {
        bluetoothConnectForwarderDelegate = listener
    }

    override fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        writeEnqueuer.enqueueWrite(gatt) {
            writeOperation(gatt, characteristic, payload)
        }
    }

    private fun writeOperation(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = getWriteType(characteristic)

        if (writeType == null) {
            writeEnqueuer.dequeueWrite(gatt)
            return
        }

        characteristic.writeType = writeType
        characteristic.value = payload
        gatt.writeCharacteristic(characteristic)
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
            bluetoothConnectForwarderDelegate?.onCharacteristicChanged(
                value,
                gatt,
                characteristic.uuid.toString()
            )
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            val completion = writeDescriptorsBuffer.remove(descriptor)
            completion?.complete(Unit)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            writeEnqueuer.dequeueWrite(gatt)
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
        bluetoothConnectForwarderDelegate?.onDeviceConnected(gatt)
        gatt.discoverServices()
    }

    private fun onDeviceDisconnected(gatt: BluetoothGatt) {
        removeFromGattList(gatt)
        bluetoothConnectForwarderDelegate?.onDeviceDisconnected(gatt)
        writeEnqueuer.clearDeviceQueue(gatt)
        gatt.close()
    }

    private fun onConnectionFailed(gatt: BluetoothGatt) {
        removeFromGattList(gatt)
        bluetoothConnectForwarderDelegate?.onConnectionFail(gatt)
        writeEnqueuer.clearDeviceQueue(gatt)
        gatt.close()
    }

    private fun deviceIsConnected(device: BluetoothDevice): Boolean {
        return device.address in _gattDevices.value.map { it.device.address }
    }

    private fun getWriteType(characteristic: BluetoothGattCharacteristic): Int? {
        return when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> {
                Log.e("getWriteType", "Characteristic ${characteristic.uuid} cannot be written to")
                null
            }
        }
    }
}
