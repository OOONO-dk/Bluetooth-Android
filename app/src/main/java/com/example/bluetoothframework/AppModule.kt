package com.example.bluetoothframework

import android.content.Context
import com.example.bluetoothframework.data.BluetoothScanner
import com.example.bluetoothframework.domain.scanner.BluetoothScannerInterface
import com.example.bluetoothframework.domain.scanner.BluetoothScanCallback
import com.example.bluetoothframework.domain.scanner.DeviceDiscoverTimeout
import com.example.bluetoothframework.domain.scanner.DeviceDiscoverTimeoutInterface
import com.example.bluetoothframework.domain.scanner.ScanTracker
import com.example.bluetoothframework.domain.scanner.ScanTrackerInterface
import com.example.bluetoothframework.domain.BluetoothHelper
import com.example.bluetoothframework.domain.BluetoothHelperInterface
import com.example.bluetoothframework.domain.controller.BluetoothController
import com.example.bluetoothframework.domain.controller.BluetoothControllerInterface
import com.example.bluetoothframework.domain.scanner.GG
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
    fun provideBluetoothScanCallback(): GG {
        return BluetoothHelper()
    }

    //@Provides
    //@Singleton
    //fun provideBluetoothScanCallback(): BluetoothScanCallback {
    //    return BluetoothHelper()
    //}

    //@Provides
    //@Singleton
    //fun provideBluetoothHelper(): BluetoothHelperInterface {
    //    return BluetoothHelper()
    //}


    @Singleton
    @Provides
    fun providesBluetoothScanner(
        callback: GG,
        @ApplicationContext context: Context,
        @Singleton scanTracker: ScanTrackerInterface,
        @Singleton deviceDiscoverTimeout: DeviceDiscoverTimeoutInterface
    ): BluetoothScannerInterface {
        return BluetoothScanner(context, callback, scanTracker, deviceDiscoverTimeout)
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