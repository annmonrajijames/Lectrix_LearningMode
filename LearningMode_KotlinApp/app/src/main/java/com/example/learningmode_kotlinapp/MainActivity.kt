package com.example.learningmode_kotlinapp

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.learningmode_kotlinapp.ui.theme.LearningMode_KotlinAppTheme

enum class Screen {
    WIFI_DISABLED, SCAN, CONNECTED
}

class MainActivity : ComponentActivity() {

    private lateinit var wifiManager: WifiManager
    private var scanResultsState by mutableStateOf<List<ScanResult>>(emptyList())

    // Receiver to update scan results.
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) == true
            if (success) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    scanResultsState = wifiManager.scanResults
                }
            }
        }
    }

    // Launcher to request necessary permissions.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!fineGranted || !coarseGranted) {
            Log.w("MainActivity", "Location permissions not granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        // Request permissions if not granted.
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (requiredPermissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissionLauncher.launch(requiredPermissions)
        }

        setContent {
            LearningMode_KotlinAppTheme {
                // Capture context and activity.
                val activity = LocalContext.current as? Activity
                val context = LocalContext.current

                // Check if device Wi-Fi is enabled.
                var currentScreen by remember { mutableStateOf(
                    if (wifiManager.isWifiEnabled) Screen.SCAN else Screen.WIFI_DISABLED
                ) }

                // Simple function to refresh Wi-Fi state.
                fun refreshWifiState() {
                    currentScreen = if (wifiManager.isWifiEnabled) Screen.SCAN else Screen.WIFI_DISABLED
                }

                when (currentScreen) {
                    Screen.WIFI_DISABLED -> {
                        WifiDisabledScreen(
                            onEnableClicked = {
                                // Open Wi-Fi settings so user can enable Wi-Fi.
                                activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            },
                            onRefresh = { refreshWifiState() }
                        )
                    }
                    Screen.SCAN -> {
                        WifiScanScreen(
                            scanResults = scanResultsState,
                            onScan = {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    try {
                                        @Suppress("DEPRECATION")
                                        wifiManager.startScan()
                                    } catch (e: SecurityException) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            onNetworkSelected = { ssid ->
                                // For now, simply log the selection.
                                Log.d("MainActivity", "Selected network: $ssid")
                            },
                            onRefresh = { refreshWifiState() }
                        )
                    }
                    Screen.CONNECTED -> {
                        ConnectedScreen()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(scanReceiver)
        super.onDestroy()
    }
}

@Composable
fun WifiDisabledScreen(onEnableClicked: () -> Unit, onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Wi-Fi is disabled.\nPlease turn on Wi-Fi to discover nearby networks.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onEnableClicked) {
                Text(text = "Open Wi-Fi Settings")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Text(text = "Refresh")
            }
        }
    }
}

@Composable
fun WifiScanScreen(
    scanResults: List<ScanResult>,
    onScan: () -> Unit,
    onNetworkSelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Available Wi-Fi Networks", modifier = Modifier.padding(bottom = 8.dp))
        Row {
            Button(onClick = onScan) { Text(text = "Scan Wi-Fi") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onRefresh) { Text(text = "Refresh") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(scanResults) { result ->
                val ssidText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.wifiSsid?.toString() ?: "N/A"
                } else {
                    @Suppress("DEPRECATION")
                    result.SSID
                }
                Text(
                    text = "$ssidText - ${result.level} dBm",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNetworkSelected(ssidText) }
                        .padding(8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun ConnectedScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Connected to ESP32!")
    }
}
