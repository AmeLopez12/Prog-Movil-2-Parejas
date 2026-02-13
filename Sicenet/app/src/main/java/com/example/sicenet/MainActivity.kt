package com.example.sicenet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sicenet.data.local.SicenetDatabase
import com.example.sicenet.data.network.SicenetApiService
import com.example.sicenet.data.repository.SicenetRepository
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.screens.CargaScreen
import com.example.sicenet.ui.screens.LoginScreen
import com.example.sicenet.ui.screens.ProfileScreen
import com.example.sicenet.ui.theme.SicenetGreen
import com.example.sicenet.ui.theme.SicenetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val apiService = SicenetApiService.create()
        val db = SicenetDatabase.getDatabase(this)
        val repository = SicenetRepository(apiService, db.alumnoDao(), db.materiaDao())
        
        setContent {
            SicenetTheme {
                val viewModel: SicenetViewModel = viewModel(
                    factory = SicenetViewModel.provideFactory(application, repository)
                )
                AppMain(viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object Carga : Screen("carga", "Carga", Icons.Default.List)
}

@Composable
fun AppMain(viewModel: SicenetViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = currentDestination?.route != "login"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val items = listOf(Screen.Profile, Screen.Carga)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SicenetGreen,
                                selectedTextColor = SicenetGreen,
                                indicatorColor = SicenetGreen.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { 
                        navController.navigate(Screen.Profile.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Carga.route) {
                CargaScreen(viewModel = viewModel)
            }
        }
    }
}
