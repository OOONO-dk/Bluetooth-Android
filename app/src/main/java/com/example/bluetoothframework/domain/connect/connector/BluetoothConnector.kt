package com.example.bluetoothframework.domain.connect.connector

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.bluetoothframework.domain.connect.BluetoothConnectCallback
import com.example.bluetoothframework.domain.extensions.determineCharacteristicTypes
import com.example.bluetoothframework.domain.extensions.isIndicatable
import com.example.bluetoothframework.domain.extensions.isNotifiable
import com.example.bluetoothframework.domain.extensions.printGattTable
import com.example.bluetoothframework.domain.scan.scanner.BluetoothScanner
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
                val bluetoothServices = getBluetoothServices(gatt)
                setServiceNotifiers(gatt, bluetoothServices)
            }
        }
    }

    private fun setServiceNotifiers(gatt: BluetoothGatt, services: List<BluetoothService>) {
        services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                if (characteristic.characteristicUUID == "72e80210-d54b-48ea-a00c-5ea3973cebc7") {
                    println("RRR types: ${characteristic.characteristicTypes}")
                }
                when {
                    characteristic.characteristic.isNotifiable() -> {
                        enableNotifications(gatt, service.service, characteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                    characteristic.characteristic.isIndicatable() -> {
                        enableNotifications(gatt, service.service, characteristic, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
                    }
                }
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, service: BluetoothGattService, characteristic: BluetoothCharacteristic, descriptorValue: ByteArray) {
        gatt.setCharacteristicNotification(characteristic.characteristic, true)
//        val descriptor = characteristic.characteristic.descriptors
//
//        descriptor.forEach {
//            println("RRR ------------------------")
//            println("RRR - ${characteristic.characteristic.uuid}")
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                gatt.writeDescriptor(it, descriptorValue)
//            } else {
//                it.value = descriptorValue
//                gatt.writeDescriptor(it)
//            }
//        }

        val descriptor1 = gatt.getService("72e80200-d54b-48ea-a00c-5ea3973cebc7".toUuid()).getCharacteristic("72e80210-d54b-48ea-a00c-5ea3973cebc7".toUuid()).descriptors.first()
        val descriptor2 = service.getCharacteristic(characteristic.characteristicUUID.toUuid())?.descriptors?.first() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor2, descriptorValue)
        } else {
            descriptor2.value = descriptorValue
            gatt.writeDescriptor(descriptor2)
        }
    }










    private fun getBluetoothServices(gatt: BluetoothGatt): List<BluetoothService> {
        return gatt.services.map { service ->
            val characteristics = service.characteristics.map { characteristic ->
                BluetoothCharacteristic(
                    characteristicUUID = characteristic.uuid.toString(),
                    characteristicTypes = characteristic.determineCharacteristicTypes(),
                    characteristic = characteristic
                )
            }
            BluetoothService(
                service = service, //TODO remove this?
                serviceUUID = service.uuid.toString(),
                characteristics = characteristics
            )
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