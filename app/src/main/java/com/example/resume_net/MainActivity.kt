package com.example.resume_net

import android.content.ComponentCallbacks2
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.resume_net.data.repository.ResumeRepositoryImpl
import com.example.resume_net.presentation.navigation.NavGraph
import com.example.resume_net.ui.theme.Resume_netTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val repository: ResumeRepositoryImpl by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка системных панелей
        setupSystemBars()

        setContent {
            Resume_netTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    /**
     * Настройка цвета статус-бара и навигационной панели
     */
    private fun setupSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Получаем контроллер для оконных вставок
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Устанавливаем цвета в зависимости от версии Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            // Цвет статус-бара (AccentLight)
            window.statusBarColor = getColor(com.example.resume_net.R.color.status_bar_color)

            // Цвет навигационной панели (AccentLight)
            window.navigationBarColor = getColor(com.example.resume_net.R.color.navigation_bar_color)
        }

        // Делаем иконки статус-бара тёмными (так как фон светлый)
        windowInsetsController.isAppearanceLightStatusBars = true

        // Делаем иконки навигационной панели тёмными
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowInsetsController.isAppearanceLightNavigationBars = true
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                android.util.Log.d("MainActivity", "App went to background, releasing model")
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.releaseModel()
                }
            }
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                android.util.Log.d("MainActivity", "Critical memory pressure, releasing model NOW")
                repository.releaseModel()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                android.util.Log.d("MainActivity", "Low memory while running: level=$level")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            repository.releaseModel()
        }
    }
}