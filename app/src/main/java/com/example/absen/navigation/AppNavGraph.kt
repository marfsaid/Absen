package com.example.absen.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.absen.MapActivity
import com.example.absen.data.FirestoreRepository
import com.example.absen.screen.AbsenScreen
import com.example.absen.screen.DashboardAdminScreen
import com.example.absen.screen.DashboardUserScreen
import com.example.absen.screen.LoginScreen
import com.example.absen.screen.ProfileScreen
import com.example.absen.screen.RiwayatAdminScreen
import com.example.absen.screen.RiwayatUserScreen
import com.example.absen.util.OfficeConfig
import com.example.absen.viewmodel.AbsenViewModel
import com.example.absen.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ROUTE_LOGIN = "login"
private const val ROUTE_DASHBOARD_USER = "dashboard_user"
private const val ROUTE_DASHBOARD_ADMIN = "dashboard_admin"
private const val ROUTE_ABSEN = "absen"
private const val ROUTE_RIWAYAT_USER = "riwayat_user"
private const val ROUTE_RIWAYAT_ADMIN = "riwayat_admin"
private const val ROUTE_PROFILE = "profile"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel,
    absenViewModel: AbsenViewModel,
    onLaunchOneTap: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val repo = remember { FirestoreRepository() }

    var uid by remember { mutableStateOf(auth.currentUser?.uid) }
    DisposableEffect(auth) {
        val l = FirebaseAuth.AuthStateListener { a -> uid = a.currentUser?.uid }
        auth.addAuthStateListener(l)
        onDispose { auth.removeAuthStateListener(l) }
    }

    LaunchedEffect(uid) {
        val currentUid = uid
        if (currentUid.isNullOrBlank()) {
            if (navController.currentDestination?.route != ROUTE_LOGIN) {
                navController.navigate(ROUTE_LOGIN) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            return@LaunchedEffect
        }

        val role = withContext(Dispatchers.IO) {
            runCatching { repo.getUserRoleSuspend(currentUid) }.getOrDefault("user")
        }

        val isAdmin = role.trim().lowercase() == "admin"
        val dest = if (isAdmin) ROUTE_DASHBOARD_ADMIN else ROUTE_DASHBOARD_USER

        if (navController.currentDestination?.route != dest) {
            navController.navigate(dest) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = ROUTE_LOGIN) {

        composable(ROUTE_LOGIN) {
            LoginScreen(
                onOneTapLoginClick = { onLaunchOneTap() },
                onAdminLogin = { usernameOrEmail, password ->
                    loginViewModel.loginAdminSso(usernameOrEmail, password) { _, _ -> }
                },
                isLoading = loginViewModel.isLoading.value,
                errorMessage = loginViewModel.errorMessage.value
            )
        }

        composable(ROUTE_DASHBOARD_USER) {
            DashboardUserScreen(
                onAbsenClick = { navController.navigate(ROUTE_ABSEN) },
                onRiwayatClick = { navController.navigate(ROUTE_RIWAYAT_USER) },
                onProfileClick = { navController.navigate(ROUTE_PROFILE) },
                onLogoutClick = { loginViewModel.logout() }
            )
        }

        composable(ROUTE_DASHBOARD_ADMIN) {
            DashboardAdminScreen(
                onRiwayatSemuaClick = { navController.navigate(ROUTE_RIWAYAT_ADMIN) },
                onProfileClick = { navController.navigate(ROUTE_PROFILE) },
                onLogoutClick = { loginViewModel.logout() }
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSwitchAccount = { loginViewModel.logout() }
            )
        }

        composable(ROUTE_ABSEN) {
            AbsenScreen(viewModel = absenViewModel, onBack = { navController.popBackStack() })
        }

        composable(ROUTE_RIWAYAT_USER) {
            RiwayatUserScreen(viewModel = absenViewModel, onBack = { navController.popBackStack() })
        }

        composable(ROUTE_RIWAYAT_ADMIN) {
            RiwayatAdminScreen(
                viewModel = absenViewModel,
                onBack = { navController.popBackStack() },
                onShowMap = { point ->
                    val i = Intent(context, MapActivity::class.java).apply {
                        putExtra(MapActivity.EXTRA_LAT, point.latitude)
                        putExtra(MapActivity.EXTRA_LNG, point.longitude)
                        putExtra(MapActivity.EXTRA_OFFICE_LAT, OfficeConfig.OFFICE_LAT)
                        putExtra(MapActivity.EXTRA_OFFICE_LNG, OfficeConfig.OFFICE_LNG)
                        putExtra(MapActivity.EXTRA_OFFICE_NAME, OfficeConfig.DEFAULT_OFFICE_NAME)
                        putExtra(MapActivity.EXTRA_OFFICE_RADIUS, OfficeConfig.OFFICE_RADIUS_M.toInt())
                    }
                    context.startActivity(i)
                }
            )
        }
    }
}
