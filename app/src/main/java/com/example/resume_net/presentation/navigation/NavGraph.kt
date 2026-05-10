package com.example.resume_net.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.resume_net.presentation.analysis.AnalysisScreen
import com.example.resume_net.presentation.analysis.AnalysisViewModel
import com.example.resume_net.presentation.result.ResultScreen
import org.koin.androidx.compose.koinViewModel

@androidx.compose.runtime.Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: AnalysisViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Analysis.route
    ) {
        composable(Screen.Analysis.route) {
            AnalysisScreen(
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route)
                }
            )
        }

        composable(Screen.Result.route) {
            val result = state.result

            LaunchedEffect(result) {
                if (result == null) {
                    navController.popBackStack()
                }
            }

            if (result != null) {
                ResultScreen(
                    result = result,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}