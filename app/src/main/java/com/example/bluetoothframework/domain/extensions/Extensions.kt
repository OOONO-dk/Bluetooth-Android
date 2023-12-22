package com.example.bluetoothframework.domain.extensions

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.bluetoothframework.domain.BluetoothDeviceDomain
import com.example.bluetoothframework.domain.connect.connector.CharacteristicType
import java.util.*

@SuppressLint("MissingPermission")
fun ScanResult.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        rssi = rssi,
        name = device.name,
        address = device.address,
        discoverTimestamp = System.currentTimeMillis()
    )
}

fun BluetoothGatt.printGattTable() {
    if (services.isEmpty()) {
        Log.d("Connected Device","No service and characteristic available, call discoverServices() first?")
        return
    }
    Log.d("Connected Device","---------------------------------------------------------------")
    services.forEach { service ->
        val characteristicsTable = service.characteristics.joinToString(
            separator = "\n|--",
            prefix = "|--"
        ) { char ->
            var description = "${char.uuid}: ${char.printProperties()}"
            if (char.descriptors.isNotEmpty()) {
                description += "\n" + char.descriptors.joinToString(
                    separator = "\n|------",
                    prefix = "|------"
                ) { descriptor ->
                    "${descriptor.uuid}: ${descriptor.printProperties()}"
                }
            }
            description
        }
        Log.d("Connected Device","Service ${service.uuid}\nCharacteristics:\n$characteristicsTable")
    }
    Log.d("Connected Device","---------------------------------------------------------------")
}

fun BluetoothGattCharacteristic.determineCharacteristicTypes(): List<CharacteristicType> {
    val types = mutableListOf<CharacteristicType>()
    if (isReadable()) types.add(CharacteristicType.READABLE)
    if (isWritable()) types.add(CharacteristicType.WRITABLE)
    if (isNotifiable()) types.add(CharacteristicType.NOTIFIABLE)
    if (isIndicatable()) types.add(CharacteristicType.INDICATABLE)
    if (isWritableWithoutResponse()) types.add(CharacteristicType.WRITABLE_WITHOUT_RESPONSE)
    if (types.isEmpty()) types.add(CharacteristicType.EMPTY)
    return types
}

fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if (isIndicatable()) add("INDICATABLE")
    if (isNotifiable()) add("NOTIFIABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0

fun BluetoothGattDescriptor.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattDescriptor.isReadable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_READ)

fun BluetoothGattDescriptor.isWritable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE)

fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
    permissions and permission != 0