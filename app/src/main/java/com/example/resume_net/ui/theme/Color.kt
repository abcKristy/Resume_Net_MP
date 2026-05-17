package com.example.resume_net.ui.theme

import androidx.compose.ui.graphics.Color

// Ваши основные цвета
val AccentLight = Color(0xFFB2CBE6)  // Светлый акцент (основной фон)
val AccentDark = Color(0xFF42262A)   // Темный акцент (текст, акценты)

val AssistantGradientStart = Color(0xFF98B7D9)
val AssistantGradientEnd = Color(0xFF9FB5CE)

val AssistantAvatarIcn = Color(0xFF738FAF)

// Light Theme цвета
val PrimaryLight = AccentDark        // Темный как основной акцент
val OnPrimaryLight = AccentLight     // Светлый текст на темном фоне
val PrimaryContainerLight = AccentLight
val OnPrimaryContainerLight = AccentDark

val SecondaryLight = AccentLight
val OnSecondaryLight = AccentDark

// ФОН - используем светлый акцент
val BackgroundLight = AccentLight     // ← Весь фон будет #B2CBE6
val SurfaceLight = AccentLight.copy(alpha = 0.7f)  // Слегка прозрачный для карточек
val OnBackgroundLight = AccentDark    // ← Текст темный #42262A
val OnSurfaceLight = AccentDark

val SurfaceVariantLight = AccentDark.copy(alpha = 0.1f)  // Легкий темный оттенок

val ErrorLight = Color(0xFFD32F2F)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFEBEE)
val OnErrorContainerLight = Color(0xFFB71C1C)

// Dark Theme цвета
val PrimaryDark = AccentLight
val OnPrimaryDark = AccentDark
val PrimaryContainerDark = AccentDark
val OnPrimaryContainerDark = AccentLight

val SecondaryDark = AccentDark
val OnSecondaryDark = AccentLight

val BackgroundDark = AccentDark
val SurfaceDark = AccentDark.copy(alpha = 0.8f)
val OnBackgroundDark = AccentLight
val OnSurfaceDark = AccentLight

val ErrorDark = Color(0xFFEF5350)
val OnErrorDark = Color(0xFFB71C1C)
val ErrorContainerDark = Color(0xFF4A0000)
val OnErrorContainerDark = Color(0xFFFFCDD2)

// Цвета для прогресс-баров тегов
val TagScoreHigh = Color(0xFFD32F2F)   // Красный 60-100% (ярче)
val TagScoreMedium = Color(0xFFFFA000) // Желтый 30-60% (ярче)
val TagScoreLow = Color(0xFF2E7D32)    // Зеленый 0-30% (темнее для контраста)

// Дополнительные цвета
val AssistantAvatarGradientStart = Color(0xFF42262A)   // AccentDark
val AssistantAvatarGradientEnd = Color(0xFF5D3A40)     // чуть светлее AccentDark

val TagHighColor = Color(0xFFE57373)    // мягкий красный для прогресс-бара
val TagMediumColor = Color(0xFFFFB74D)  // мягкий оранжевый
val TagLowColor = Color(0xFF81C784)     // мягкий зелёный

val InputFieldBackground = Color(0xFFF5F5F5)  // светлый фон для поля ввода

val ScoreHighGradientStartSoft = TagLowColor        // мягкий зелёный #81C784
val ScoreHighGradientEndSoft = TagLowColor.copy(alpha = 0.8f)

val ScoreMediumGradientStartSoft = TagMediumColor   // мягкий оранжевый #FFB74D
val ScoreMediumGradientEndSoft = TagMediumColor.copy(alpha = 0.8f)

val ScoreLowGradientStartSoft = TagHighColor        // мягкий красный #E57373
val ScoreLowGradientEndSoft = TagHighColor.copy(alpha = 0.8f)