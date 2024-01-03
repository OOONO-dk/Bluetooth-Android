package com.example.bluetoothframework.connection.enqueue

import android.bluetooth.BluetoothGatt
import javax.inject.Inject
import android.util.Log

class WriteEnqueuer @Inject constructor() : WriteEnqueuerInterface {
    private val deviceQueues: MutableMap<BluetoothGatt, ArrayDeque<WriteOperation>> = mutableMapOf()

    override fun enqueueWrite(gatt: BluetoothGatt, writeFunction: () -> Unit) {
        val queue = deviceQueues.getOrPut(gatt) { ArrayDeque() }
        val operation = WriteOperation(gatt, writeFunction)

        queue.add(operation)
        Log.d(logTag, "Enqueued write operation for device: ${gatt.device.address}. Queue size: ${queue.size}")

        if (queue.size == 1) {
            Log.d(logTag, "Executing write operation immediately for device: ${gatt.device.address}")
            writeFunction.invoke()
        }
    }

    override fun dequeueWrite(gatt: BluetoothGatt) {
        deviceQueues[gatt]?.apply {
            removeFirstOrNull()
            Log.d(logTag, "Dequeued write operation for device: ${gatt.device.address}. Remaining operations: $size")
            firstOrNull()?.operation?.invoke()?.also {
                Log.d(logTag, "Executing next write operation for device: ${gatt.device.address}")
            }
        }
    }

    override fun clearDeviceQueue(gatt: BluetoothGatt) {
        deviceQueues.remove(gatt)
        Log.d(logTag, "Cleared write operation queue for device: ${gatt.device.address}")
    }

    data class WriteOperation(
        val gatt: BluetoothGatt,
        val operation: () -> Unit
    )

    companion object {
        private const val logTag = "Bluetooth Write Enqueuer"
    }
}