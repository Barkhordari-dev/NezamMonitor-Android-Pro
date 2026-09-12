package com.nezammonitor.app

import android.content.Context
import android.os.Bundle
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

        // Copy templates to external storage on first launch
        copyTemplatesIfNeeded()

        setContent {
            val context = LocalContext.current
            val prefs = remember { AppPreferences.load(context) }
            
            val darkTheme = when (prefs.themeMode) {
                1 -> true
                2 -> isSystemInDarkTheme()
                else -> false
            }

            val typography = Typography().let { base ->
                val scale = prefs.fontScale
                base.copy(
                    displayLarge = base.displayLarge.copy(fontSize = base.displayLarge.fontSize * scale),
                    displayMedium = base.displayMedium.copy(fontSize = base.displayMedium.fontSize * scale),
                    displaySmall = base.displaySmall.copy(fontSize = base.displaySmall.fontSize * scale),
                    headlineLarge = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize * scale),
                    headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * scale),
                    headlineSmall = base.headlineSmall.copy(fontSize = base.headlineSmall.fontSize * scale),
                    titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * scale),
                    titleMedium = base.titleMedium.copy(fontSize = base.titleMedium.fontSize * scale),
                    titleSmall = base.titleSmall.copy(fontSize = base.titleSmall.fontSize * scale),
                    bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * scale),
                    bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * scale),
                    bodySmall = base.bodySmall.copy(fontSize = base.bodySmall.fontSize * scale),
                    labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * scale),
                    labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * scale),
                    labelSmall = base.labelSmall.copy(fontSize = base.labelSmall.fontSize * scale)
                )
            }

            val layoutDirection = if (prefs.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
                MaterialTheme(colorScheme = colorScheme, typography = typography) {
                    NezamMonitorApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun copyTemplatesIfNeeded() {
        try {
            val prefs = getSharedPreferences("nezam_settings", Context.MODE_PRIVATE)
            if (prefs.getBoolean("templates_copied", false)) return

            val templatesDir = java.io.File("/storage/emulated/0/NezamMonitor/templates")
            templatesDir.mkdirs()

            val templateFiles = listOf("مکانیک مرحله اول.docx", "مکانیک مرحله دوم.docx", "مکانیک مرحله سوم.docx")
            for (fileName in templateFiles) {
                val outFile = java.io.File(templatesDir, fileName)
                if (!outFile.exists()) {
                    assets.open("templates/$fileName").use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            prefs.edit().putBoolean("templates_copied", true).apply()
        } catch (_: Exception) {}
    }
}
