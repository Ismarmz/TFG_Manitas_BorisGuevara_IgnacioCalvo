package com.example.tfg_manitas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.authapp.ui.screens.LoginScreen
import com.example.authapp.ui.screens.RegisterScreen
import com.example.tfg_manitas.features.auth.ResetPasswordScreen
import com.example.tfg_manitas.features.home.MainScreen
import com.example.tfg_manitas.features.jobs.EditJobScreen
import com.example.tfg_manitas.features.jobs.PostJobScreen


@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("resetPassword") { ResetPasswordScreen(navController) }
        composable("Main") { MainScreen(navController) }
        composable("postJob") { PostJobScreen(navController) }
        composable(
            route = "editJob/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            EditJobScreen(navController = navController, jobId = jobId)
        }
    }
}
