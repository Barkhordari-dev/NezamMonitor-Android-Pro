package com.nezammonitor.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.nezammonitor.app.ui.NezamMonitorApp
import com.nezammonitor.app.ui.screens.settings.AppPreferences
import com.nezammonitor.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request MANAGE_EXTERNAL_STORAGE for Android 11+
        requestStoragePermission()

        // Copy templates to external storage
        copyTemplatesIfNeeded()

        setContent {
            val context = LocalContext.current
            val prefs = remember { AppPreferences.load(context) }

            val darkTheme = when (prefs.themeMode) {
                1 -> true
                2 -> isSystemInDarkTheme()
                else -> false
            }

            val typography = Typography(
                bodyLarge = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            )

            val layoutDirection = if (prefs.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
                MaterialTheme(colorScheme = colorScheme, typography = typography) {
                    NezamMonitorApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }

    private fun copyTemplatesIfNeeded() {
        try {
            val prefs = getSharedPreferences("nezam_settings", Context.MODE_PRIVATE)
            if (prefs.getBoolean("templates_copied", false)) return

            val templatesDir = java.io.File("/storage/emulated/0/NezamMonitor/templates")
            if (!templatesDir.exists()) templatesDir.mkdirs()

            val templateFiles = listOf("مکانیک مرحله اول.docx", "مکانیک مرحله دوم.docx", "مکانیک مرحله سوم.docx")
            for (fileName in templateFiles) {
                val outFile = java.io.File(templatesDir, fileName)
                if (!outFile.exists()) {
                    try {
                        assets.open("templates/$fileName").use { input ->
                            outFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            prefs.edit().putBoolean("templates_copied", true).apply()
        } catch (_: Exception) {}
    }
}
