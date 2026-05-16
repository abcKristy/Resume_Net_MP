package com.example.resume_net

import android.content.ComponentCallbacks2
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.resume_net.data.repository.ResumeRepositoryImpl
import com.example.resume_net.presentation.navigation.NavGraph
import com.example.resume_net.ui.theme.Resume_netTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // Инжектируем репозиторий для управления моделью
    private val repository: ResumeRepositoryImpl by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Resume_netTheme {  // ← Используем кастомную тему с новыми цветами
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    /**
     * Обработка тримминга памяти
     * Уровни: TRIM_MEMORY_UI_HIDDEN - приложение ушло в фон
     *         TRIM_MEMORY_COMPLETE - системе критически не хватает памяти
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // Приложение ушло в фон - можно освободить модель
                android.util.Log.d("MainActivity", "App went to background, releasing model")
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.releaseModel()
                }
            }

            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Критическая нехватка памяти - немедленно освобождаем
                android.util.Log.d("MainActivity", "Critical memory pressure, releasing model NOW")
                repository.releaseModel()
            }

            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                // Приложение активно, но памяти мало - освобождаем модель если не используется
                android.util.Log.d("MainActivity", "Low memory while running: level=$level")
                // Можно добавить логику, проверяющую, не идет ли анализ
                // Если анализ не активен - освобождаем
            }
        }
    }

    /**
     * При уничтожении активности также освобождаем ресурсы
     */
    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // Activity завершается окончательно (не поворот экрана)
            repository.releaseModel()
        }
    }
}