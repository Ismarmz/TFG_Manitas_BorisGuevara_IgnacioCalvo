package com.example.tfg_manitas.features.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.flowlayout.FlowRow
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun JobForm(
    navController: NavHostController,
    initialJob: Job? = null,
    submitLabel: String = "Guardar",
    onSubmit: (Job) -> Unit,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState,
    latitude: Double? = null,
    longitude: Double? = null
){
    val allTags = listOf("Hogar", "Exterior", "Técnico", "Express", "Físico", "No presencial")
    val selectedTags = remember { mutableStateListOf<String>() }
    if (initialJob != null && selectedTags.isEmpty()) selectedTags.addAll(initialJob.tags)

    var title by remember { mutableStateOf(TextFieldValue(initialJob?.title ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(initialJob?.description ?: "")) }
    var location by remember { mutableStateOf(TextFieldValue(initialJob?.location ?: "")) }
    var paymentAmount by remember { mutableStateOf(TextFieldValue(initialJob?.paymentAmount ?: "")) }

    var paymentSlider by remember { mutableFloatStateOf(initialJob?.paymentAmount?.toFloatOrNull() ?: 50f) }
    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()

    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }
    var pickedTime by remember { mutableStateOf<LocalTime?>(null) }

    val initialDateTime = initialJob?.dateTime?.takeIf { it.isNotBlank() }
    if (initialDateTime != null && pickedDate == null && pickedTime == null) {
        val parts = initialDateTime.split(" ")
        pickedDate = runCatching {
            LocalDate.parse(parts.getOrNull(0), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrNull()
        pickedTime = runCatching {
            LocalTime.parse(parts.getOrNull(1), DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrNull()
    }

    val formattedDate = pickedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
    val formattedTime = pickedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
    val combinedDateTime = if (formattedDate.isNotBlank() && formattedTime.isNotBlank()) "$formattedDate $formattedTime" else ""

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var tagsError by remember { mutableStateOf<String?>(null) }
    var dateTimeError by remember { mutableStateOf<String?>(null) }
    val jobViewModel: JobViewModel = viewModel()
    val selectedLocation by jobViewModel.selectedLocation.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* navController.popBackStack() */ }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF2F4C5A)
                )
            }
            Text(
                text = "Formulario de trabajo",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF2F4C5A),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Título") },
            isError = titleError != null,
            modifier = Modifier.fillMaxWidth()
        )
        titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                descriptionError = null
            },
            label = { Text("Descripción detallada") },
            isError = descriptionError != null,
            modifier = Modifier.fillMaxWidth()
        )
        descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(16.dp))
        Text("Etiquetas del trabajo", style = MaterialTheme.typography.labelLarge)
        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        tagsError = null
                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                    },
                    label = { Text(tag) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF4A950),
                        selectedLabelColor = Color(0xFF2F4C5A),
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
        tagsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(16.dp))
        Text("Ubicación y Pago", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = selectedLocation?.third ?: "Ubicación no seleccionada",
            onValueChange = {},
            enabled = false,
            label = { Text("Ubicación seleccionada") },
            modifier = Modifier.fillMaxWidth()
        )

// Botón para abrir el mapa
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate("map_picker") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F4C5A),
                contentColor = Color.White
            )
        ) {
            Text("Seleccionar ubicación en mapa")
        }

        Spacer(Modifier.height(8.dp))
        Text("Remuneración", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0 USD", fontSize = 12.sp, color = Color.Gray)
            Text("250 USD", fontSize = 12.sp, color = Color.Gray)
        }

            Slider(
                value = paymentSlider,
                onValueChange = {
                    paymentSlider = it
                    paymentError = null
                },
                valueRange = 0f..250f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF4A950),
                    activeTrackColor = Color(0xFF2F4C5A),
                    inactiveTrackColor = Color.LightGray
                )
            )

            Text(
                text = "${paymentSlider.toInt()} USD seleccionados",
                fontSize = 14.sp,
                color = Color(0xFF2F4C5A),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

        paymentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Fecha y Hora", style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { dateDialogState.show() },
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Fecha",
                    tint = Color(0xFF2F4C5A),
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = { timeDialogState.show() },
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Hora",
                    tint = Color(0xFF2F4C5A),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        dateTimeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        MaterialDialog(dateDialogState, buttons = {
            positiveButton("Aceptar")
            negativeButton("Cancelar")
        }) {
            datepicker { pickedDate = it }
        }

        MaterialDialog(timeDialogState, buttons = {
            positiveButton("Aceptar")
            negativeButton("Cancelar")
        }) {
            timepicker(is24HourClock = true) { pickedTime = it }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                var valid = true
                if (title.text.length < 5) {
                    titleError = "El título debe tener al menos 5 caracteres"
                    valid = false
                }
                if (description.text.length < 30) {
                    descriptionError = "La descripción debe tener al menos 30 caracteres"
                    valid = false
                }
                val loc = selectedLocation
                if (loc == null) {
                    locationError = "Selecciona una ubicación válida"
                    return@Button
                }
                if (paymentSlider <= 0f) {
                    paymentError = "La remuneración debe ser mayor a 0"
                    valid = false
                }
                if (selectedTags.isEmpty()) {
                    tagsError = "Selecciona al menos una etiqueta"
                    valid = false
                }
                if (combinedDateTime.isBlank()) {
                    dateTimeError = "Selecciona fecha y hora"
                    valid = false
                }
                if (loc == null) {
                    locationError = "Selecciona una ubicación válida"
                    return@Button
                }

                if (!valid) return@Button

                val job = (initialJob ?: Job()).copy(
                    title = title.text,
                    description = description.text,
                    tags = selectedTags.toList(),
                    location = loc.third,
                    latitude = loc.first,
                    longitude = loc.second,
                    dateTime = combinedDateTime,
                    paymentAmount = paymentSlider.toInt().toString()
                )
                onSubmit(job)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4A950),
                contentColor = Color(0xFF2F4C5A)
            )
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text(submitLabel, fontSize = 14.sp)
        }
    }
}
