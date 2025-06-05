package com.example.tfg_manitas.features.jobs

import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import java.util.*

@Composable
fun MapPickerScreen(
    navController: NavController,
    jobViewModel: JobViewModel,
    returnTo: String
) {
    val context = LocalContext.current
    val madrid = LatLng(40.4168, -3.7038)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 6f)
    }

    val placesClient = remember { Places.createClient(context) }
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.length > 2) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(query)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            predictions = response.autocompletePredictions
                        }
                        .addOnFailureListener {
                            predictions = emptyList()
                        }
                } else {
                    predictions = emptyList()
                }
            },
            label = { Text("Buscar dirección") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        predictions.forEach { prediction ->
            Text(
                text = prediction.getFullText(null).toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val placeId = prediction.placeId
                        val fields = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                        val request = FetchPlaceRequest.builder(placeId, fields).build()

                        placesClient.fetchPlace(request)
                            .addOnSuccessListener { response ->
                                response.place.latLng?.let { latLng ->
                                    pickedLatLng = latLng
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                                    )
                                    val address = response.place.address ?: "Dirección desconocida"
                                    Log.d("MapPicker", "Ubicación buscada seleccionada: $latLng -> $address")
                                    jobViewModel.setSelectedLocation(
                                        latLng.latitude,
                                        latLng.longitude,
                                        address
                                    )
                                    searchQuery = ""
                                    predictions = emptyList()
                                }
                            }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        GoogleMap(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                pickedLatLng = latLng
                val address = try {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(latLng.latitude, latLng.longitude, 1)
                        ?.firstOrNull()?.getAddressLine(0)
                        ?: "Ubicación seleccionada"
                } catch (e: Exception) {
                    "Ubicación seleccionada"
                }
                Log.d("MapPicker", "Ubicación manual seleccionada: $latLng -> $address")
                jobViewModel.setSelectedLocation(latLng.latitude, latLng.longitude, address)
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
                    val location = jobViewModel.selectedLocation.value
                    Log.d("MapPicker", "Ubicación guardada antes de confirmar: $location")
                    navController.popBackStack()
                    navController.navigate(returnTo)
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
