package com.example.bluetoothframework.di

import android.content.Context
import com.example.bluetoothframework.connection.connector.BluetoothConnectorImpl
import com.example.bluetoothframework.connection.connector.BluetoothConnector
import com.example.bluetoothframework.connection.enqueue.WriteEnqueuerImpl
import com.example.bluetoothframework.connection.enqueue.WriteEnqueuer
import com.example.bluetoothframework.scanning.scanner.BluetoothScannerImpl
import com.example.bluetoothframework.implementation_examples.service.ImplementationExample
import com.example.bluetoothframework.implementation_examples.service.ImplementationExampleInterface
import com.example.bluetoothframework.control.controller.BluetoothControllerImpl
import com.example.bluetoothframework.control.controller.BluetoothController
import com.example.bluetoothframework.scanning.scanner.BluetoothScanner
import com.example.bluetoothframework.control.advertising_timeout.AdvertisementTimeoutImpl
import com.example.bluetoothframework.control.advertising_timeout.AdvertisementTimeout
import com.example.bluetoothframework.scanning.utils.scan_tracker.ScanTrackerImpl
import com.example.bluetoothframework.scanning.utils.scan_tracker.ScanTracker
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
    fun providesImplementationExample(
        bluetoothController: BluetoothController
    ): ImplementationExampleInterface {
        return ImplementationExample(bluetoothController)
    }

    @Singleton
    @Provides
    fun providesBluetoothScanner(
        @ApplicationContext context: Context,
        @Singleton scanTracker: ScanTracker
    ): BluetoothScanner {
        return BluetoothScannerImpl(context, scanTracker)
    }

    @Singleton
    @Provides
    fun providesWriteEnqueuer(): WriteEnqueuer {
        return WriteEnqueuerImpl()
    }

    @Singleton
    @Provides
    fun providesBluetoothConnector(
        @ApplicationContext context: Context,
        @Singleton writeEnqueuer: WriteEnqueuer
    ): BluetoothConnector {
        return BluetoothConnectorImpl(context, writeEnqueuer)
    }

    @Singleton
    @Provides
    fun providesBluetoothController(
        @Singleton bluetoothScanner: BluetoothScanner,
        @Singleton bluetoothConnector: BluetoothConnector,
        @Singleton deviceAdvertisementTimeout: AdvertisementTimeout
    ): BluetoothController {
        return BluetoothControllerImpl(bluetoothScanner, bluetoothConnector, deviceAdvertisementTimeout)
    }

    @Singleton
    @Provides
    fun providesScanTracker(): ScanTracker {
        return ScanTrackerImpl()
    }

    @Singleton
    @Provides
    fun providesDeviceDiscoverTimeout(): AdvertisementTimeout {
        return AdvertisementTimeoutImpl()
    }
}