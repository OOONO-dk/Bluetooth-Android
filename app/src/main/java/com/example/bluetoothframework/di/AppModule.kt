package com.example.bluetoothframework.di

import android.content.Context
import com.example.bluetoothframework.domain.connect.connector.BluetoothConnector
import com.example.bluetoothframework.domain.connect.connector.BluetoothConnectorInterface
import com.example.bluetoothframework.domain.connect.write_queue.WriteEnqueuer
import com.example.bluetoothframework.domain.connect.write_queue.WriteEnqueuerInterface
import com.example.bluetoothframework.domain.scan.scanner.BluetoothScanner
import com.example.bluetoothframework.domain.implementation_examples.ImplementationExample
import com.example.bluetoothframework.domain.implementation_examples.ImplementationExampleInterface
import com.example.bluetoothframework.domain.controller.BluetoothController
import com.example.bluetoothframework.domain.controller.BluetoothControllerInterface
import com.example.bluetoothframework.domain.scan.scanner.BluetoothScannerInterface
import com.example.bluetoothframework.domain.scan.discover_timeout.DeviceDiscoverTimeout
import com.example.bluetoothframework.domain.scan.discover_timeout.DeviceDiscoverTimeoutInterface
import com.example.bluetoothframework.domain.scan.tracker.ScanTracker
import com.example.bluetoothframework.domain.scan.tracker.ScanTrackerInterface
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
    fun provideBluetoothScanCallback(
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