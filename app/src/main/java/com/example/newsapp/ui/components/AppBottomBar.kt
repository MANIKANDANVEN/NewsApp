package com.example.newsapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.newsapp.ui.navigation.Screen

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        Triple(Screen.Headlines, "Headlines", Icons.Default.Home),
        Triple(Screen.Sources, "Sources", Icons.Default.List),
        Triple(Screen.Saved, "Saved", Icons.Default.Favorite)
    )

    // Using a Surface wrapper to handle the curve and elevation properly
    Surface(
        // Curve the top left and right corners
        shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.graphicsLayer {
            clip = true
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        }
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // Let Surface handle the color
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEach { (screen, label, icon) ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            // Apply purple tint when selected
                            tint = if (isSelected) Color(0xFF7E57C2) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text(label) },
                    selected = isSelected,
                    colors = NavigationBarItemDefaults.colors(
                        // The "pill" background color when selected
                        indicatorColor = Color(0xFFF3E5F5),
                        selectedTextColor = Color(0xFF7E57C2),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}