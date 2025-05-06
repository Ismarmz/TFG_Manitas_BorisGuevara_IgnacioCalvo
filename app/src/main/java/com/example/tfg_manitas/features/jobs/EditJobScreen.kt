    package com.example.tfg_manitas.features.jobs

    import androidx.compose.foundation.layout.*
    import androidx.compose.material.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavHostController

    @Composable
    fun EditJobScreen(
        job: Job,
        jobViewModel: JobViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
        navController: NavHostController
    ) {
        val isLoading by jobViewModel.isLoading.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var snackbarMessage by remember { mutableStateOf<String?>(null) }

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
            Column(modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)) {

                JobForm(
                    initialJob = job,
                    submitLabel = "Guardar Cambios",
                    isLoading = isLoading,
                    snackbarHostState = snackbarHostState,
                    onSubmit = { updatedJob ->
                        jobViewModel.updateJob(
                            jobId = job.id ?: "",
                            updatedJob = updatedJob.copy(id = job.id),
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
