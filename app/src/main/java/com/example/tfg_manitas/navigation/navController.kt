package com.example.tfg_manitas.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.authapp.ui.screens.LoginScreen
import com.example.tfg_manitas.features.auth.RegisterScreen
import com.example.tfg_manitas.features.auth.ResetPasswordScreen
import com.example.tfg_manitas.features.home.MainScreen
import com.example.tfg_manitas.features.jobs.EditJobScreen
import com.example.tfg_manitas.features.jobs.JobViewModel
import com.example.tfg_manitas.features.jobs.PostJobScreen
import com.example.tfg_manitas.features.profile.CompleteProfileScreen
import com.example.tfg_manitas.features.profile.PublicProfileScreen

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("completeProfile") { CompleteProfileScreen(navController) }
        composable("resetPassword") { ResetPasswordScreen(navController) }
        composable("Main") { MainScreen(navController) }
        composable("postJob") { PostJobScreen(navController) }

        // 🔧 NUEVO: Perfil público
        composable(
            route = "publicProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PublicProfileScreen(userId = userId)
        }

        // Editar trabajo
        composable(
            route = "editJob/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val jobViewModel: JobViewModel = viewModel()
            val userJobs by jobViewModel.userJobs.collectAsState()
            val job = userJobs.find { it.id == jobId }

            if (job != null) {
                EditJobScreen(
                    job = job,
                    navController = navController
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}