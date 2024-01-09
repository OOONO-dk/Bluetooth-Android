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
import com.example.bluetoothframework.connection.enqueue.WriteEnqueuer
import com.example.bluetoothframework.extensions.isIndicatable
import com.example.bluetoothframework.extensions.isNotifiable
import com.example.bluetoothframework.extensions.isWritable
import com.example.bluetoothframework.extensions.isWritableWithoutResponse
import com.example.bluetoothframework.extensions.printGattTable
import com.example.bluetoothframework.extensions.toUuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothConnectorImpl @Inject constructor(
    private val context: Context,
    private val writeEnqueuer: WriteEnqueuer
) : BluetoothConnector {
    private var writeDescriptorsBuffer: MutableMap<BluetoothGattDescriptor, CompletableDeferred<Unit>> = mutableMapOf()
    private var bluetoothConnectForwarderDelegate: BluetoothConnectForwarderDelegate? = null

    override fun setConnectionDelegate(listener: BluetoothConnectForwarderDelegate) {
        bluetoothConnectForwarderDelegate = listener
    }

    override fun connectDevice(device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    override fun disconnectDevice(gatt: BluetoothGatt) {
        gatt.disconnect()
    }

    override fun writeToDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        writeEnqueuer.enqueueWrite(gatt) {
            getWriteOperation(gatt, characteristic, payload)
        }
    }

    override fun discoverServices(gatt: BluetoothGatt) {
        gatt.discoverServices()
    }

    override fun getAllCharacteristics(gatt: BluetoothGatt, serviceUuid: String): List<BluetoothGattCharacteristic> {
        val service = gatt.getService(serviceUuid.toUuid())
        return service?.characteristics?.filter { it.isNotifiable() || it.isIndicatable() } ?: emptyList()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            handleConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                with(gatt) { printGattTable() }
                bluetoothConnectForwarderDelegate?.onServicesDiscovered(gatt)
            }
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

    override fun setServiceNotifiers(gatt: BluetoothGatt) {
        CoroutineScope(Dispatchers.IO).launch {
            gatt.services.forEach { service ->
                val characteristics = service.characteristics.filter { it.isNotifiable() || it.isIndicatable() }
                characteristics.forEach { characteristic ->
                    enableNotifications(gatt, characteristic)
                }
            }
        }
    }

    override suspend fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val descriptorValue = getDescriptorValue(characteristic) ?: return
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

    override fun getCharacteristicFromUuid(gatt: BluetoothGatt, serviceUuid: String, characteristicUuid: String): BluetoothGattCharacteristic? {
        val service = gatt.getService(serviceUuid.toUuid())
        return service?.getCharacteristic(characteristicUuid.toUuid())
    }

    private fun handleConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionFailed(gatt)
            return
        }

        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> bluetoothConnectForwarderDelegate?.onDeviceConnected(gatt)
            BluetoothGatt.STATE_DISCONNECTED -> onDeviceDisconnected(gatt)
        }
    }

    private fun onDeviceDisconnected(gatt: BluetoothGatt) {
        bluetoothConnectForwarderDelegate?.onDeviceDisconnected(gatt)
        writeEnqueuer.clearDeviceQueue(gatt)
        gatt.close()
    }

    private fun onConnectionFailed(gatt: BluetoothGatt) {
        bluetoothConnectForwarderDelegate?.onConnectionFail(gatt)
        writeEnqueuer.clearDeviceQueue(gatt)
        gatt.close()
    }

    private fun getDescriptorValue(characteristic: BluetoothGattCharacteristic): ByteArray? {
        return when {
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            else -> {
                Log.e("getDescriptorValue", "Characteristic ${characteristic.uuid} is neither notifiable nor indicatable")
                null
            }
        }
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

    private fun getWriteOperation(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = getWriteType(characteristic)

        if (writeType == null) {
            writeEnqueuer.dequeueWrite(gatt)
            return
        }

        characteristic.writeType = writeType
        characteristic.value = payload
        gatt.writeCharacteristic(characteristic)
    }
}
