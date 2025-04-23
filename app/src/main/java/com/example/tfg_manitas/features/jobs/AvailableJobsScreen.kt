package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AvailableJobsScreen(jobViewModel: JobViewModel = viewModel()) {
    val context = LocalContext.current
    val allJobs by jobViewModel.availableJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()

    // 🎯 Filtros
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var locationFilter by remember { mutableStateOf("") }
    val categoryOptions = listOf("Todas", "Limpieza", "Mudanza", "Reparación", "Otro")

    // 🧠 Filtro local
    val filteredJobs = allJobs.filter { job ->
        val matchesKeyword = searchQuery.isBlank() ||
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.description.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "Todas" || job.category.equals(selectedCategory, ignoreCase = true)

        val matchesLocation = locationFilter.isBlank() ||
                job.location.contains(locationFilter, ignoreCase = true)

        matchesKeyword && matchesCategory && matchesLocation
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Buscar Trabajos", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // 🔍 Campo de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Palabra clave") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 📂 Categoría + 📍 Ubicación
        Row(modifier = Modifier.fillMaxWidth()) {
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(onClick = {
                            selectedCategory = option
                            expanded = false
                        }) {
                            Text(option)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = locationFilter,
                onValueChange = { locationFilter = it },
                label = { Text("Ubicación") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🧹 Limpiar filtros
        Button(
            onClick = {
                searchQuery = ""
                selectedCategory = "Todas"
                locationFilter = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar filtros")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 Resultado
        if (isLoading) {
            CircularProgressIndicator()
        } else if (!error.isNullOrEmpty()) {
            Text("Error: $error", color = MaterialTheme.colors.error)
        } else {
            Text("Mostrando ${filteredJobs.size} trabajos", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredJobs.isEmpty()) {
                Text("No hay trabajos que coincidan con los filtros.")
            } else {
                LazyColumn {
                    items(filteredJobs) { job ->
                        Card(
                            elevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = job.title, style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📍 ${job.location}")
                                Text("🕒 ${job.dateTime}")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = job.description, maxLines = 2)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    Toast.makeText(context, "Te has postulado (simulado)", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Postularme")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
