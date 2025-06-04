package com.example.tfg_manitas.features.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.*
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.google.accompanist.flowlayout.FlowRow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun JobForm(
    initialJob: Job? = null,
    submitLabel: String = "Guardar",
    onSubmit: (Job) -> Unit,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    val allTags = listOf("Hogar", "Exterior", "Técnico", "Express", "Físico", "No presencial")
    val selectedTags = remember { mutableStateListOf<String>() }
    if (initialJob != null && selectedTags.isEmpty()) selectedTags.addAll(initialJob.tags)

    var title by remember { mutableStateOf(TextFieldValue(initialJob?.title ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(initialJob?.description ?: "")) }
    var location by remember { mutableStateOf(TextFieldValue(initialJob?.location ?: "")) }
    var paymentAmount by remember { mutableStateOf(TextFieldValue(initialJob?.paymentAmount ?: "")) }

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

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                descriptionError = null
            },
            label = { Text("Descripción") },
            isError = descriptionError != null,
            modifier = Modifier.fillMaxWidth()
        )
        descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text("Selecciona etiquetas:", style = MaterialTheme.typography.labelLarge)
        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        tagsError = null
                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                    },
                    label = { Text(tag) }
                )
            }
        }
        tagsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = paymentAmount,
            onValueChange = {
                paymentAmount = it
                paymentError = null
            },
            label = { Text("Remuneración (USD)") },
            isError = paymentError != null,
            modifier = Modifier.fillMaxWidth()
        )
        paymentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it
                locationError = null
            },
            label = { Text("Ubicación") },
            isError = locationError != null,
            modifier = Modifier.fillMaxWidth()
        )
        locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { dateDialogState.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (formattedDate.isNotBlank()) "🗓 $formattedDate" else "Seleccionar fecha")
        }

        Button(onClick = { timeDialogState.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (formattedTime.isNotBlank()) "🕒 $formattedTime" else "Seleccionar hora")
        }

        dateTimeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        MaterialDialog(dateDialogState, buttons = {
            positiveButton("Aceptar")
            negativeButton("Cancelar")
        }) { datepicker { pickedDate = it } }

        MaterialDialog(timeDialogState, buttons = {
            positiveButton("Aceptar")
            negativeButton("Cancelar")
        }) { timepicker(is24HourClock = true) { pickedTime = it } }

        Spacer(modifier = Modifier.height(16.dp))

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
                if (location.text.isBlank()) {
                    locationError = "Ubicación obligatoria"
                    valid = false
                }
                if (paymentAmount.text.toDoubleOrNull()?.let { it > 0 } != true) {
                    paymentError = "Remuneración inválida"
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

                if (!valid) return@Button

                val job = (initialJob ?: Job()).copy(
                    title = title.text,
                    description = description.text,
                    tags = selectedTags.toList(),
                    location = location.text,
                    dateTime = combinedDateTime,
                    paymentAmount = paymentAmount.text
                )
                onSubmit(job)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text(submitLabel)
        }
    }
}
