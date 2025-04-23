package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable

@Composable
fun PostJobScreen(
    navController: NavHostController,
    jobViewModel: JobViewModel = viewModel()
) {
    val context = LocalContext.current

    val categories = listOf("Limpieza", "Mudanza", "Reparación", "Jardinería", "Otro")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expandedCategory by remember { mutableStateOf(false) }


    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var dateTime by remember { mutableStateOf(TextFieldValue("")) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Publicar Trabajo", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

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

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedCategory = true }
            )

            DropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(onClick = {
                        selectedCategory = option
                        expandedCategory = false
                    }) {
                        Text(option)
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
            label = { Text("Fecha y hora (ej. 2025-04-30 10:00)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colors.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                isLoading = true
                jobViewModel.postJob(
                    title.text,
                    description.text,
                    category = selectedCategory,
                    location.text,
                    dateTime.text,
                    onSuccess = {
                        isLoading = false
                        Toast.makeText(context, "Trabajo publicado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // vuelve atrás
                    },
                    onFailure = {
                        isLoading = false
                        errorMessage = it
                        Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                    }

                )
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Publicar Trabajo")
            }
        }
    }
}
