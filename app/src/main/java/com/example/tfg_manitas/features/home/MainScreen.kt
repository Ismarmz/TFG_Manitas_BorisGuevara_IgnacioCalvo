package com.example.tfg_manitas.features.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tfg_manitas.data.repository.ReviewRepository
import com.example.tfg_manitas.features.auth.ResetPasswordScreen
import com.example.tfg_manitas.features.jobs.*
import com.example.tfg_manitas.features.profile.CompleteProfileScreen
import com.example.tfg_manitas.features.profile.PublicProfileScreen
import com.example.tfg_manitas.features.profile.ProfileScreen
import com.example.tfg_manitas.features.auth.RegisterScreen
import com.example.tfg_manitas.features.auth.LoginScreen
import com.example.tfg_manitas.features.reviews.ReviewScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("completeProfile") { CompleteProfileScreen(navController) }
        composable("resetPassword") { ResetPasswordScreen(navController) }
        composable("Main") { MainScreen(rootNavController = navController) }
        // Rutas de reseña y perfil público
        composable(
            route = "review/{jobId}/{toUserId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("toUserId") { type = NavType.StringType }
            )
        ) { back ->
            val jobId = back.arguments!!.getString("jobId")!!
            val toUserId = back.arguments!!.getString("toUserId")!!
            ReviewScreen(jobId, toUserId, navController)
        }
        composable(
            route = "publicProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val userId = back.arguments!!.getString("userId")!!
            PublicProfileScreen(userId, navController)
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("Inicio", "home", Icons.Default.Home),
        BottomNavItem("Mis solicitudes", "applications", Icons.Default.Check),
        BottomNavItem("Publicar", "publish", Icons.Default.Add),
        BottomNavItem("Perfil", "profile", Icons.Default.Person)
    )


    Scaffold(
        bottomBar = {
            BottomNavigation(elevation = 8.dp) {
                val backEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backEntry?.destination?.route
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
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    AvailableJobsScreen(
                        navController = rootNavController,
                        jobViewModel = viewModel()
                    )
                }
                composable("applications") {
                    MyApplicationsScreen(
                        jobViewModel = viewModel(),
                        reviewRepo = ReviewRepository(),
                        navController = rootNavController
                    )
                }
                composable("jobs") {
                    JobsListScreen(
                        jobViewModel = viewModel(),
                        navController = rootNavController
                    )
                }
                composable("profile") {
                    ProfileScreen(onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo("Main") { inclusive = true }
                        }
                    })
                }
                composable("publish") {
                    PublishTabScreen(navController = rootNavController)
                }
            }
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
@Composable
fun HomeContent(onLogout: () -> Unit) {
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
            onLogout()
        }) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Text(text = text, modifier = Modifier.padding(16.dp))
}
