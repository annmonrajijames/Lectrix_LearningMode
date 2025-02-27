package com.example.learningmode_kotlinapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.learningmode_kotlinapp.ui.theme.LearningMode_KotlinAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var wifiManager: WifiManager

    // Mutable state to hold the list of Wi-Fi scan results
    private var scanResultsState by mutableStateOf<List<ScanResult>>(emptyList())

    // BroadcastReceiver to receive scan results
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Check if the scan succeeded and if location permission is granted
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

    // Launcher for requesting multiple permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!fineLocationGranted || !coarseLocationGranted) {
            // Handle the case where one or both permissions are not granted.
            Log.w("MainActivity", "Location permissions not granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the WifiManager instance
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Register the BroadcastReceiver for scan results
        registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        // Request permissions if not already granted
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (requiredPermissions.any {
                // 'this' is redundant here since MainActivity is a Context
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissionLauncher.launch(requiredPermissions)
        }

        setContent {
            LearningMode_KotlinAppTheme {
                // Display the Wi-Fi scanning screen with a scan button
                WifiScanScreen(
                    scanResults = scanResultsState,
                    onScan = {
                        // Use applicationContext for permission checks to remove redundant qualifier warnings
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                applicationContext,
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
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(scanReceiver)
        super.onDestroy()
    }
}

@Composable
fun WifiScanScreen(scanResults: List<ScanResult>, onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Wi-Fi Networks",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = onScan) {
            Text(text = "Scan Wi-Fi")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(scanResults) { result ->
                // For API 33+, use the new wifiSsid; for lower API levels, use the deprecated SSID (with suppression)
                val ssidText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.wifiSsid?.toString() ?: "N/A"
                } else {
                    @Suppress("DEPRECATION")
                    result.SSID
                }
                Text(text = "$ssidText - ${result.level} dBm")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
