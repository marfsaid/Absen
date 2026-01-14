package com.example.absen.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.absen.navigation.Routes

@Composable
fun MainScaffold(
    navController: NavController,
    showBottomBar: Boolean,
    content: @Composable () -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigateSingleTop(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    val dashboardRoutes = setOf(
        Routes.DASHBOARD_USER,
        Routes.DASHBOARD_ADMIN,
        Routes.DASHBOARD
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 8.dp) {
                    NavigationBarItem(
                        selected = currentRoute in dashboardRoutes,
                        onClick = { navigateSingleTop(Routes.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.ABSEN,
                        onClick = { navigateSingleTop(Routes.ABSEN) },
                        icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                        label = { Text("Absen") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.RIWAYAT_USER,
                        onClick = { navigateSingleTop(Routes.RIWAYAT_USER) },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text("Riwayat") }
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) { content() }
    }
}
