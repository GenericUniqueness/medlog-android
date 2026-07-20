package com.medlog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medlog.app.ui.navigation.MedLogNavHost
import com.medlog.app.ui.navigation.Route
import com.medlog.app.ui.theme.MedLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedLogTheme {
                MedLogAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedLogAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Route.Dashboard,
        Route.Medications,
        Route.Conditions,
        Route.Appointments
    )

    val showBottomBar = currentRoute in bottomNavRoutes.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavRoutes.forEach { route ->
                        NavigationBarItem(
                            icon = { RouteIcon(route) },
                            label = { Text(RouteLabel(route)) },
                            selected = currentRoute == route.route,
                            onClick = {
                                if (currentRoute != route.route) {
                                    navController.navigate(route.route) {
                                        popUpTo(Route.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        MedLogNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}

@Composable
fun RouteIcon(route: Route) {
    when (route) {
        is Route.Dashboard -> Icon(Icons.Default.Home, contentDescription = "Dashboard")
        is Route.Medications -> Icon(Icons.Default.Medication, contentDescription = "Medications")
        is Route.Conditions -> Icon(Icons.Default.Favorite, contentDescription = "Conditions")
        is Route.Appointments -> Icon(Icons.Default.CalendarToday, contentDescription = "Appointments")
        else -> Icon(Icons.Default.Home, contentDescription = null)
    }
}

@Composable
fun RouteLabel(route: Route): String {
    return when (route) {
        is Route.Dashboard -> "Home"
        is Route.Medications -> "Meds"
        is Route.Conditions -> "Health"
        is Route.Appointments -> "Appts"
        else -> ""
    }
}
