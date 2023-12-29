package com.example.bluetoothframework.domain.connect.write_queue

import android.bluetooth.BluetoothGatt

interface WriteEnqueuerInterface {
    fun enqueueWrite(gatt: BluetoothGatt, writeFunction: () -> Unit)
    fun dequeueWrite(gatt: BluetoothGatt)
    fun clearDeviceQueue(gatt: BluetoothGatt)
}