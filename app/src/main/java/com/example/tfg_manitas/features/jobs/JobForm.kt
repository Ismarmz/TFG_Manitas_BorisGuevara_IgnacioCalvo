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

    if (initialJob != null && selectedTags.isEmpty()) {
        selectedTags.addAll(initialJob.tags)
    }

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

    var showValidationError by remember { mutableStateOf(false) }

    if (showValidationError) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Por favor completa todos los campos")
            showValidationError = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Selecciona etiquetas:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedTags.remove(tag)
                        else selectedTags.add(tag)
                    },
                    label = { Text(tag) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = paymentAmount,
            onValueChange = { paymentAmount = it },
            label = { Text("Remuneración (USD)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { dateDialogState.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (formattedDate.isNotBlank()) "🗓 $formattedDate" else "Seleccionar fecha")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { timeDialogState.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (formattedTime.isNotBlank()) "🕒 $formattedTime" else "Seleccionar hora")
        }

        MaterialDialog(dialogState = dateDialogState, buttons = {
            positiveButton("Aceptar")
            negativeButton("Cancelar")
        }) {
            datepicker { pickedDate = it }
        }

        MaterialDialog(dialogState = timeDialogState, buttons = {
            positiveButton("Aceptar")
            negativeButton("Cancelar")
        }) {
            timepicker(is24HourClock = true) { pickedTime = it }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (
                    title.text.isBlank() ||
                    description.text.isBlank() ||
                    location.text.isBlank() ||
                    combinedDateTime.isBlank() ||
                    paymentAmount.text.isBlank()
                ) {
                    showValidationError = true
                    return@Button
                }

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