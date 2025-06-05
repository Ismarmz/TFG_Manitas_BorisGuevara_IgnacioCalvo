package com.example.tfg_manitas.features.jobs

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices

@Composable
fun PostJobScreen(
    navController: NavHostController,
    jobViewModel: JobViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val isLoading by jobViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var formResetKey by remember { mutableStateOf(0) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        }
    }


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
                    val enrichedJob = job.copy(
                        latitude = latitude ?: 0.0,
                        longitude = longitude ?: 0.0
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
