package com.example.bluetoothframework.di

import android.content.Context
import com.example.bluetoothframework.connection.connector.BluetoothConnector
import com.example.bluetoothframework.connection.connector.BluetoothConnectorInterface
import com.example.bluetoothframework.connection.enqueue.WriteEnqueuer
import com.example.bluetoothframework.connection.enqueue.WriteEnqueuerInterface
import com.example.bluetoothframework.scanning.scanner.BluetoothScanner
import com.example.bluetoothframework.implementation_examples.service.ImplementationExample
import com.example.bluetoothframework.implementation_examples.service.ImplementationExampleInterface
import com.example.bluetoothframework.controller.BluetoothController
import com.example.bluetoothframework.controller.BluetoothControllerInterface
import com.example.bluetoothframework.scanning.scanner.BluetoothScannerInterface
import com.example.bluetoothframework.scanning.utils.advertising_timeout.DeviceDiscoverTimeout
import com.example.bluetoothframework.scanning.utils.advertising_timeout.DeviceDiscoverTimeoutInterface
import com.example.bluetoothframework.scanning.utils.scan_tracker.ScanTracker
import com.example.bluetoothframework.scanning.utils.scan_tracker.ScanTrackerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providesApplicationContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun provideBluetoothScanDelegate(
        bluetoothController: BluetoothControllerInterface
    ): ImplementationExampleInterface {
        return ImplementationExample(bluetoothController)
    }

    @Singleton
    @Provides
    fun providesBluetoothScanner(
        @ApplicationContext context: Context,
        @Singleton scanTracker: ScanTrackerInterface,
        @Singleton deviceDiscoverTimeout: DeviceDiscoverTimeoutInterface
    ): BluetoothScannerInterface {
        return BluetoothScanner(context, scanTracker, deviceDiscoverTimeout)
    }

    @Singleton
    @Provides
    fun providesWriteEnqueuer(): WriteEnqueuerInterface {
        return WriteEnqueuer()
    }

    @Singleton
    @Provides
    fun providesBluetoothConnector(
        @ApplicationContext context: Context,
        @Singleton writeEnqueuer: WriteEnqueuerInterface
    ): BluetoothConnectorInterface {
        return BluetoothConnector(context, writeEnqueuer)
    }

    @Singleton
    @Provides
    fun providesBluetoothController(
        @Singleton bluetoothScanner: BluetoothScannerInterface,
        @Singleton bluetoothConnector: BluetoothConnectorInterface,
    ): BluetoothControllerInterface {
        return BluetoothController(bluetoothScanner, bluetoothConnector)
    }

    @Singleton
    @Provides
    fun providesScanTracker(): ScanTrackerInterface {
        return ScanTracker()
    }

    @Singleton
    @Provides
    fun providesDeviceDiscoverTimeout(): DeviceDiscoverTimeoutInterface {
        return DeviceDiscoverTimeout()
    }
}