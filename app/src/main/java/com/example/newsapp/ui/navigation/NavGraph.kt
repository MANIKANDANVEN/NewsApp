package com.example.newsapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.newsapp.ui.screens.HeadlinesScreen
import com.example.newsapp.ui.screens.SavedScreen
import com.example.newsapp.ui.screens.SourcesScreen
import com.example.newsapp.ui.screens.WebViewScreen

sealed class Screen(val route: String) {
    object Headlines : Screen("headlines")
    object Sources : Screen("sources")
    object Saved : Screen("saved")

    object Detail : Screen("detail/{url}")
}

@Composable
fun NewsNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Headlines.route
    ) {
        composable(Screen.Headlines.route) {
            HeadlinesScreen(navController)
        }
        composable(Screen.Sources.route) {
            SourcesScreen()
        }
        composable(Screen.Saved.route) {
            SavedScreen(navController)
        }

        // Use the route from your sealed class to keep it consistent
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            WebViewScreen(url = url, navController = navController)
        }
    }
}