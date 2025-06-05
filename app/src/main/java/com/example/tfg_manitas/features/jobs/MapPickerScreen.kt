package com.example.tfg_manitas.features.jobs

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
import java.util.*

@Composable
fun MapPickerScreen(
    navController: NavController,
    onLocationPicked: (latitude: Double, longitude: Double, address: String) -> Unit
) {
    val context = LocalContext.current
    val madrid = LatLng(40.4168, -3.7038)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 6f)
    }

    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }

    Column(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                pickedLatLng = it
            }
        ) {
            pickedLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Ubicación seleccionada"
                )
            }
        }

        pickedLatLng?.let { latLng ->
            Button(
                onClick = {
                    val address = try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        results?.firstOrNull()?.getAddressLine(0) ?: "Ubicación seleccionada"
                    } catch (e: Exception) {
                        "Ubicación seleccionada"
                    }

                    onLocationPicked(latLng.latitude, latLng.longitude, address)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Usar esta ubicación")
            }
        }
    }
}
