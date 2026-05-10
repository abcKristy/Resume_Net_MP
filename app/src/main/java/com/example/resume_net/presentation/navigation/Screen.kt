package com.example.resume_net.presentation.navigation

sealed class Screen(val route: String) {
    data object Analysis : Screen("analysis")
    data object Result : Screen("result")
}