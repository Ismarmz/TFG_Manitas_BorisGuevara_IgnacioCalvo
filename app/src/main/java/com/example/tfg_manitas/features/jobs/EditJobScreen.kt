package com.example.tfg_manitas.features.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun EditJobScreen(
    job: Job,
    jobViewModel: JobViewModel = viewModel(),
    navController: NavHostController
) {
    val isLoading by jobViewModel.isLoading.collectAsState()
    val selectedLocation by jobViewModel.selectedLocation.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Ubicación final que usaremos: si el usuario seleccionó una nueva, la usamos
    val finalLat = selectedLocation?.first ?: job.latitude
    val finalLng = selectedLocation?.second ?: job.longitude
    val finalAddress = selectedLocation?.third ?: job.location

    // Creamos una copia actualizada del Job
    val jobToEdit = job.copy(
        latitude = finalLat,
        longitude = finalLng,
        location = finalAddress
    )

    // Mostrar Snackbar si hay mensaje
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            JobForm(
                navController = navController,
                initialJob = jobToEdit,
                submitLabel = "Guardar Cambios",
                isLoading = isLoading,
                snackbarHostState = snackbarHostState,
                onSubmit = { updatedJob ->
                    jobViewModel.updateJob(
                        jobId = job.id,
                        updatedJob = updatedJob,
                        onSuccess = {
                            snackbarMessage = "Trabajo actualizado"
                            navController.popBackStack()
                        },
                        onFailure = { error ->
                            snackbarMessage = "Error: $error"
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
