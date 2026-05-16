package com.example.resume_net.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.resume_net.presentation.conversations.ConversationsListScreen
import com.example.resume_net.presentation.newanalysis.NewAnalysisScreen  // будет создан

@Composable
fun NavGraph(
    startDestination: String = Screen.Conversations.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Экран списка диалогов
        composable(Screen.Conversations.route) {
            ConversationsListScreen(
                onNavigateToChat = { conversationId ->
                    navController.navigate("${Screen.Chat.route}/$conversationId")
                },
                onNavigateToNewAnalysis = {
                    navController.navigate(Screen.NewAnalysis.route)
                }
            )
        }

        // Экран нового анализа
        composable(Screen.NewAnalysis.route) {
            NewAnalysisScreen(
                onNavigateBack = { navController.popBackStack() },
                onAnalysisComplete = { conversationId ->
                    navController.popBackStack()
                    navController.navigate("${Screen.Chat.route}/$conversationId")
                }
            )
        }

        // Экран чата
        composable(
            route = "${Screen.Chat.route}/{conversationId}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
            // TODO: ChatAnalysisScreen
            // ChatAnalysisScreen(
            //     conversationId = conversationId,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
    }
}