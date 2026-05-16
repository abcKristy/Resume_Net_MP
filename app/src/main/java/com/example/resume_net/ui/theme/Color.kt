// Color.kt - полностью обновленный файл

package com.example.resume_net.ui.theme

import androidx.compose.ui.graphics.Color

// Основные акцентные цвета (ваши)
val AccentLight = Color(0xFFB2CBE6)  // Светлый акцент
val AccentDark = Color(0xFF42262A)   // Темный акцент

// Light Theme цвета
val PrimaryLight = AccentLight
val OnPrimaryLight = Color(0xFF1A1A1A)  // Темный текст на светлом фоне
val PrimaryContainerLight = Color(0xFFD9E8F5)  // Светлый контейнер
val OnPrimaryContainerLight = Color(0xFF001D35)

val SecondaryLight = AccentDark
val OnSecondaryLight = Color(0xFFFFFFFF)  // Белый текст на темном
val SecondaryContainerLight = Color(0xFFFFDAD6)
val OnSecondaryContainerLight = Color(0xFF410000)

val BackgroundLight = Color(0xFFF8F9FA)  // Очень светлый фон
val SurfaceLight = Color(0xFFFFFFFF)      // Белая поверхность
val OnBackgroundLight = Color(0xFF1A1A1A)
val OnSurfaceLight = Color(0xFF1A1A1A)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val SuccessLight = Color(0xFF006E2E)
val WarningLight = Color(0xFFB25C00)

// Dark Theme цвета
val PrimaryDark = AccentLight
val OnPrimaryDark = Color(0xFF001D35)
val PrimaryContainerDark = Color(0xFF004D81)
val OnPrimaryContainerDark = Color(0xFFCBE6FF)

val SecondaryDark = AccentDark
val OnSecondaryDark = Color(0xFFFFFFFF)
val SecondaryContainerDark = Color(0xFF5D3A3E)
val OnSecondaryContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF0D0D0D)
val SurfaceDark = Color(0xFF1A1A1A)
val OnBackgroundDark = Color(0xFFE3E3E3)
val OnSurfaceDark = Color(0xFFE3E3E3)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val SuccessDark = Color(0xFF55B87C)
val WarningDark = Color(0xFFFFB45E)

// Цвета для прогресс-баров тегов (из ТЗ)
val TagScoreHigh = Color(0xFFF44336)   // Красный 60-100%
val TagScoreMedium = Color(0xFFFFC107) // Желтый 30-60%
val TagScoreLow = Color(0xFF4CAF50)    // Зеленый 0-30%

// Дополнительные системные цвета
val DividerLight = Color(0xFFE0E0E0)
val DividerDark = Color(0xFF333333)