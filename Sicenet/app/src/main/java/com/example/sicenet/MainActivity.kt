package com.example.sicenet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
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
import com.example.sicenet.ui.screens.*
import com.example.sicenet.ui.theme.SicenetGreen
import com.example.sicenet.ui.theme.SicenetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val apiService = SicenetApiService.create()
        val db = SicenetDatabase.getDatabase(this)
        val repository = SicenetRepository(
            context = this,
            apiService = apiService, 
            alumnoDao = db.alumnoDao(), 
            materiaDao = db.materiaDao(), 
            kardexDao = db.kardexDao(),
            califUnidadDao = db.califUnidadDao(),
            califFinalDao = db.califFinalDao()
        )
        
        // Verificar si hay una sesión activa para decidir el startDestination
        val startDestination = if (repository.getSessionCookie() != null) "profile" else "login"
        
        setContent {
            SicenetTheme {
                val viewModel: SicenetViewModel = viewModel(
                    factory = SicenetViewModel.provideFactory(application, repository)
                )
                AppMain(viewModel, startDestination)
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object Carga : Screen("carga", "Carga", Icons.Default.DateRange)
    object Kardex : Screen("kardex", "Kardex", Icons.Default.Info)
    object CalifUnidades : Screen("calif_unidades", "Unidades", Icons.Default.Star)
    object CalifFinal : Screen("calif_final", "Final", Icons.Default.CheckCircle)
}

@Composable
fun AppMain(viewModel: SicenetViewModel, startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = currentDestination?.route != "login"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    val items = listOf(
                        Screen.Profile,
                        Screen.Carga,
                        Screen.Kardex,
                        Screen.CalifUnidades,
                        Screen.CalifFinal
                    )
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label, fontSize = 10.sp, color = Color.Gray) },
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
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = SicenetGreen,
                                unselectedTextColor = Color.Gray,
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
            startDestination = startDestination,
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
            composable(Screen.Kardex.route) {
                KardexScreen(viewModel = viewModel)
            }
            composable(Screen.CalifUnidades.route) {
                CalifUnidadesScreen(viewModel = viewModel)
            }
            composable(Screen.CalifFinal.route) {
                CalifFinalScreen(viewModel = viewModel)
            }
        }
    }
}
