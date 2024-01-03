package com.example.bluetoothframework.connection.enqueue

import android.bluetooth.BluetoothGatt

interface WriteEnqueuerInterface {
    fun enqueueWrite(gatt: BluetoothGatt, writeFunction: () -> Unit)
    fun dequeueWrite(gatt: BluetoothGatt)
    fun clearDeviceQueue(gatt: BluetoothGatt)
}