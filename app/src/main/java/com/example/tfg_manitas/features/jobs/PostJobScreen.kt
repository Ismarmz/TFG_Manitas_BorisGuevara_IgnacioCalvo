package com.example.tfg_manitas.features.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun PostJobScreen(
    navController: NavHostController,
    jobViewModel: JobViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val isLoading by jobViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var formResetKey by remember { mutableStateOf(0) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            JobForm(

                initialJob = null,
                key = formResetKey,
                submitLabel = "Publicar Trabajo",
                isLoading = isLoading,
                snackbarHostState = snackbarHostState,
                onSubmit = { job ->
                    jobViewModel.postJob(
                        title = job.title,
                        description = job.description,
                        category = job.category,
                        location = job.location,
                        dateTime = job.dateTime,
                        onSuccess = {
                            snackbarMessage = "Trabajo publicado"
                            formResetKey++
                            navController.popBackStack()
                        },
                        onFailure = { error ->
                            snackbarMessage = "Error: $error"
                        }
                    )
                }
            )
        }
    }
}
