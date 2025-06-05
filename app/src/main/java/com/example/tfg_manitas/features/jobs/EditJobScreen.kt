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
import kotlinx.coroutines.launch

@Composable
fun EditJobScreen(
    job: Job,
    navController: NavHostController,
    jobViewModel: JobViewModel
)
 {
    val isLoading by jobViewModel.isLoading.collectAsState()
    val selectedLocation by jobViewModel.selectedLocation.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (jobViewModel.selectedLocation.value == null) {
            jobViewModel.setSelectedLocation(
                job.latitude,
                job.longitude,
                job.location
            )
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
                initialJob = job,
                submitLabel = "Guardar Cambios",
                isLoading = isLoading,
                snackbarHostState = snackbarHostState,
                onSubmit = { updatedJob ->
                    jobViewModel.updateJob(
                        jobId = job.id,
                        updatedJob = updatedJob,
                        onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Trabajo actualizado")
                                navController.popBackStack()
                            }
                        },
                        onFailure = { error ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error: $error")
                            }
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
