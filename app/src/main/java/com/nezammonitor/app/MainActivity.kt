package com.nezammonitor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.nezammonitor.app.ui.NezamMonitorApp
import com.nezammonitor.app.data.NezamDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database early (no LocalContext in Composable)
        NezamDatabase.getDatabase(applicationContext)
        
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NezamMonitorApp()
                }
            }
        }
    }
}
