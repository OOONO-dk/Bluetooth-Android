package com.example.bluetoothframework.implementation_examples.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluetoothframework.model.data.BluetoothDeviceInfo
import com.example.bluetoothframework.model.enums.ConnectionState
import com.example.bluetoothframework.ui.theme.BluetoothFrameworkTheme

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val isSearching = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf("Start Search") }
    val state by viewModel.state.collectAsState()

    fun onSearchPressed() {
        if (!isSearching.value) {
            viewModel.startScan()
            searchText.value = "Stop Search"
        } else {
            viewModel.stopScan()
            searchText.value = "Start Search"
        }
        isSearching.value = !isSearching.value
    }

    BluetoothFrameworkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(22.dp))
                ClickableText(searchText.value) { onSearchPressed() }
                Spacer(Modifier.height(10.dp))

                Title("Available Devices")
                Container (Modifier.weight(1f)) {
                    AvailableDevicesList(state.filter { it.connectionState == ConnectionState.DISCOVERED }) { viewModel.connectToDevice(it)}
                }

                Spacer(Modifier.height(10.dp))

                Title("Connected Devices")
                Container (Modifier.weight(1f)) {
                    ConnectedDevicesList(
                        state.filter { it.connectionState != ConnectionState.DISCOVERED },
                        blink = { viewModel.blinkSirene(it) }
                    ) { viewModel.disconnectDevice(it) }
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ClickableText(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        color =  Color(0xFF2CA7E9),
        maxLines = 1,
        modifier = modifier
            .clickable { onClick() }
    )
}

@Composable
private fun AvailableDevicesList(
    devices: List<BluetoothDeviceInfo>,
    onConnectClick: (BluetoothDeviceInfo) -> Unit
) {
    LazyColumn (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(13.dp))
        }
        items(devices) { device ->
            Spacer(Modifier.height(7.dp))
            DiscoveredDevice(device) { onConnectClick(device) }
        }
        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ConnectedDevicesList(
    devices: List<BluetoothDeviceInfo>,
    blink: (BluetoothDeviceInfo) -> Unit,
    disconnect: (BluetoothDeviceInfo) -> Unit
) {
    LazyColumn (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(13.dp))
        }
        items(devices) { device ->
            Spacer(Modifier.height(7.dp))
            ConnectedDevice(device, blink = { blink(device) }) { disconnect(device) }
        }
        item {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DiscoveredDevice(
    device: BluetoothDeviceInfo,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row (
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${device.device.name}\n${device.device.address}\n${device.connectionState}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            ClickableText(
                "connect",
                Modifier.padding(horizontal = 12.dp)
            ) { onClick() }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun ConnectedDevice(
    device: BluetoothDeviceInfo,
    blink: () -> Unit,
    disconnect: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row (
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${device.device.name}\n${device.device.address}\n${device.connectionState}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            ClickableText(
                "blink",
                Modifier.padding(horizontal = 12.dp)
            ) { blink() }
            ClickableText(
                "disconnect",
                Modifier.padding(horizontal = 12.dp)
            ) { disconnect() }
        }
    }
}

@Composable
private fun Title(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(12.dp)
    )
}

@Composable
private fun Container(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth(0.9f)
            .background(Color(0xFFE0E7EB)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}