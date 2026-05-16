package com.example.resume_net.presentation.navigation

sealed class Screen(val route: String) {
    data object Conversations : Screen("conversations")
    data object NewAnalysis : Screen("new_analysis")
    data object Chat : Screen("chat")
    data object Analysis : Screen("analysis")  // старый, можно удалить или оставить
    data object Result : Screen("result")       // старый, можно удалить
}