package com.example.resume_net.presentation.conversations.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Компонент для реализации свайпа влево с подтверждением удаления
 *
 * @param onDelete действие при подтверждении удаления
 * @param onDismiss действие при отмене удаления (после анимации)
 * @param content основной контент элемента
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val deleteThreshold = with(LocalDensity.current) { 100.dp.toPx() }

    // Создаем корутину для анимаций
    val scope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.x
                val newOffset = (offsetX.value + delta).coerceIn(-deleteThreshold, 0f)

                // Обновляем смещение только если свайпаем влево
                if (delta < 0 && newOffset <= 0) {
                    scope.launch {
                        offsetX.snapTo(newOffset)
                    }
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Завершаем свайп при отпускании
                if (source == NestedScrollSource.Drag && offsetX.value < 0) {
                    scope.launch {
                        if (offsetX.value <= -deleteThreshold * 0.8f) {
                            // Удаляем с анимацией
                            offsetX.animateTo(
                                targetValue = -deleteThreshold,
                                animationSpec = tween(durationMillis = 150)
                            )
                            onDelete()
                            delay(200)
                            onDismiss()
                        } else {
                            // Возвращаем на место
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200)
                            )
                        }
                    }
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Красный фон с иконкой корзины (появляется при свайпе)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.error)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(32.dp)
            )
        }

        // Контент, который двигается при свайпе
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                }
        ) {
            content()
        }
    }
}