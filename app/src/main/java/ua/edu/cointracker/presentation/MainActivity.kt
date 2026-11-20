package ua.edu.cointracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import dagger.hilt.android.AndroidEntryPoint
import ua.edu.cointracker.presentation.ui.CryptoTrackerScreen

// Підключає Activity до графа залежностей Hilt.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: CryptoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Однакова поведінка системних панелей на Android 11–15
        enableEdgeToEdge()
        setContent {
            val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            // Обмежуємо масштаб шрифту системи (Samsung дозволяє дуже великий шрифт),
            // щоб інтерфейс не «ламався» на вузьких екранах
            val density = LocalDensity.current
            val limitedDensity = Density(
                density = density.density,
                fontScale = density.fontScale.coerceAtMost(1.3f)
            )
            CompositionLocalProvider(LocalDensity provides limitedDensity) {
                MaterialTheme(colorScheme = colorScheme) {
                    Surface {
                        CryptoTrackerScreen(viewModel)
                    }
                }
            }
        }
    }
}