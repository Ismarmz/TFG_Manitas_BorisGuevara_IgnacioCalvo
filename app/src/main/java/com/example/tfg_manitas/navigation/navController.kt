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
import com.example.tfg_manitas.features.auth.LoginScreen
import com.example.tfg_manitas.features.auth.RegisterScreen
import com.example.tfg_manitas.features.auth.ResetPasswordScreen
import com.example.tfg_manitas.features.auth.VerifyEmailScreen
import com.example.tfg_manitas.features.chat.ChatScreen
import com.example.tfg_manitas.features.home.MainScreen
import com.example.tfg_manitas.features.jobs.EditJobScreen
import com.example.tfg_manitas.features.jobs.JobViewModel
import com.example.tfg_manitas.features.profile.CompleteProfileScreen
import com.example.tfg_manitas.features.profile.PublicProfileScreen
import com.example.tfg_manitas.features.reviews.ReviewScreen
import com.example.tfg_manitas.features.jobs.PublishTabScreen
import com.example.tfg_manitas.features.jobs.PostJobScreen


@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("completeProfile") { CompleteProfileScreen(navController) }
        composable("resetPassword") { ResetPasswordScreen(navController) }
        composable("Main") { MainScreen(navController) }
        composable("publish") { PublishTabScreen(navController = navController) }
        composable("verifyEmail") { VerifyEmailScreen(navController) }
        composable("post") { PostJobScreen(navController = navController) }
        composable(
            "chat/{jobId}/{otherUserId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { backStack ->
            val jobId = backStack.arguments?.getString("jobId") ?: ""
            val otherUserId = backStack.arguments?.getString("otherUserId") ?: ""
            ChatScreen(jobId = jobId, otherUserId = otherUserId)
        }





        // Perfil público
        composable(
            route = "publicProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val userId = back.arguments?.getString("userId") ?: ""
            PublicProfileScreen(userId = userId, navController = navController)
        }

        // Editar trabajo
        composable(
            route = "editJob/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { back ->
            val jobId = back.arguments?.getString("jobId") ?: ""
            val jobViewModel: JobViewModel = viewModel()
            val userJobs by jobViewModel.userJobs.collectAsState()
            val job = userJobs.find { it.id == jobId }

            if (job != null) {
                EditJobScreen(job = job, navController = navController)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // 🆕 Pantalla de reseñas
        composable(
            route = "review/{jobId}/{toUserId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("toUserId") { type = NavType.StringType }
            )
        ) { back ->
            val jobId = back.arguments?.getString("jobId") ?: ""
            val toUserId = back.arguments?.getString("toUserId") ?: ""
            ReviewScreen(
                jobId = jobId,
                toUserId = toUserId,
                navController = navController
            )
        }
    }
}
