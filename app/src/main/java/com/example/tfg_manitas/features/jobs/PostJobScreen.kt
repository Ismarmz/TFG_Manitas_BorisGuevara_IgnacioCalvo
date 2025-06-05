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
                navController = navController,
                initialJob = null,
                submitLabel = "Publicar Trabajo",
                isLoading = isLoading,
                snackbarHostState = snackbarHostState,
                onSubmit = { job ->
                    val loc = jobViewModel.selectedLocation.value
                    val enrichedJob = job.copy(
                        latitude = loc?.first ?: 0.0,
                        longitude = loc?.second ?: 0.0,
                        location = loc?.third ?: job.location
                    )

                    jobViewModel.postJob(
                        title = enrichedJob.title,
                        description = enrichedJob.description,
                        tags = enrichedJob.tags,
                        dateTime = enrichedJob.dateTime,
                        paymentAmount = enrichedJob.paymentAmount,
                        location = enrichedJob.location,
                        latitude = enrichedJob.latitude,
                        longitude = enrichedJob.longitude,
                        onSuccess = {
                            snackbarMessage = "Trabajo publicado"
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
