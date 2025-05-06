package com.example.tfg_manitas.features.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun JobForm(
    key: Int = 0,
    initialJob: Job? = null,
    submitLabel: String = "Guardar",
    onSubmit: (Job) -> Unit,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    val categories = listOf("Limpieza", "Mudanza", "Reparación", "Jardinería", "Otro")

    var title by remember { mutableStateOf(TextFieldValue(initialJob?.title ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(initialJob?.description ?: "")) }
    var location by remember { mutableStateOf(TextFieldValue(initialJob?.location ?: "")) }
    var dateTime by remember { mutableStateOf(TextFieldValue(initialJob?.dateTime ?: "")) }

    var selectedCategory by remember { mutableStateOf(initialJob?.category ?: categories[0]) }
    var expandedCategory by remember { mutableStateOf(false) }

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

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(onClick = {
                        selectedCategory = category
                        expandedCategory = false
                    }) {
                        Text(category)
                    }
                }
            }
        }

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dateTime,
            onValueChange = { dateTime = it },
            label = { Text("Fecha y hora") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (
                    title.text.isBlank() ||
                    description.text.isBlank() ||
                    location.text.isBlank() ||
                    dateTime.text.isBlank()
                ) {
                    showValidationError = true
                    return@Button
                }

                val job = (initialJob ?: Job()).copy(
                    title = title.text,
                    description = description.text,
                    category = selectedCategory,
                    location = location.text,
                    dateTime = dateTime.text
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
