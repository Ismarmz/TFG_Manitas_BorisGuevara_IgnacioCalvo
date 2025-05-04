package com.example.tfg_manitas.features.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tfg_manitas.features.jobs.AvailableJobsScreen
import com.example.tfg_manitas.features.jobs.EditJobScreen
import com.example.tfg_manitas.features.jobs.JobListScreen
import com.example.tfg_manitas.features.jobs.PostJobScreen
import com.example.tfg_manitas.features.jobs.ProfileScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem("Inicio", "home", Icons.Default.Home),
        BottomNavItem("Publicar", "post", Icons.Default.Add),
        BottomNavItem("Mis trabajos", "jobs", Icons.Default.List),
        BottomNavItem("Buscar", "explore", Icons.Default.Search),
        BottomNavItem("Perfil", "profile", Icons.Default.Person) // <--- NUEVO
    )


    Scaffold(
        bottomBar = {
            BottomNavigation(elevation = 8.dp) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeContent()
            }
            composable("post") {
                PostJobScreen(navController = rootNavController)
            }
            composable("jobs") {
                JobListScreen(navController = rootNavController)
            }
            composable("explore") {
                AvailableJobsScreen()
            }
            composable("profile") { // <--- NUEVO
                ProfileScreen()
            }
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
@Composable
fun HomeContent(navController: NavHostController = rememberNavController()) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val email = auth.currentUser?.email ?: "Usuario"

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Hola, $email 👋", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bienvenido a Manitas", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Desde el menú puedes publicar trabajos o ver los que ya creaste.")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            auth.signOut()
            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("Main") { inclusive = true }
            }
        }) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Text(text = text, modifier = Modifier.padding(16.dp))
}
