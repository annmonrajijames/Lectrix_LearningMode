package com.example.learningmode_kotlinapp

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.learningmode_kotlinapp.ui.theme.LearningMode_KotlinAppTheme
import android.util.Log

// Define the possible connection states.
enum class ConnectionScreen {
    CHECK, CONNECTED, NOT_CONNECTED
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearningMode_KotlinAppTheme {
                WiFiConnectionScreen()
            }
        }
    }
}

@Composable
fun WiFiConnectionScreen() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ConnectionScreen.CHECK) }
    // The target SSID is the one your ESP32 SoftAP broadcasts.
    val targetSSID = "Annmon_Lectrix"

    // Function to obtain the current connected SSID.
    fun getCurrentSSID(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        var ssid = info.ssid
        if (ssid != null && ssid != "<unknown ssid>") {
            // Remove extra quotes if present.
            ssid = ssid.removePrefix("\"").removeSuffix("\"")
            return ssid
        }
        return null
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentScreen) {
            ConnectionScreen.CHECK -> {
                Text(text = "Please connect to Wi‑Fi network: $targetSSID")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Launch the system Wi‑Fi settings so the user can connect.
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }) {
                    Text(text = "Open Wi‑Fi Settings")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Check the current connected SSID.
                    val ssid = getCurrentSSID(context)
                    if (ssid == targetSSID) {
                        currentScreen = ConnectionScreen.CONNECTED
                    } else {
                        currentScreen = ConnectionScreen.NOT_CONNECTED
                        Log.d("WiFiConnection", "Current SSID: $ssid; expected: $targetSSID")
                    }
                }) {
                    Text(text = "Check Connection")
                }
                if (currentScreen == ConnectionScreen.NOT_CONNECTED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Not connected to $targetSSID. Please try again.")
                }
            }
            ConnectionScreen.CONNECTED -> {
                Text(text = "Connected to $targetSSID!")
                // Here you can navigate to the next screen (e.g. to initiate WebSocket communication).
            }
            else -> {}
        }
    }
}
