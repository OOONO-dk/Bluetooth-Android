package com.example.bluetoothframework

import android.content.Context
import com.example.bluetoothframework.domain.scan.scanner.BluetoothScanner
import com.example.bluetoothframework.domain.implementation_examples.ScannerExample
import com.example.bluetoothframework.domain.implementation_examples.ScannerExampleInterface
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
    ): ScannerExampleInterface {
        return ScannerExample(bluetoothController)
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
    fun providesBluetoothController(
        @Singleton bluetoothScanner: BluetoothScannerInterface
    ): BluetoothControllerInterface {
        return BluetoothController(bluetoothScanner)
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