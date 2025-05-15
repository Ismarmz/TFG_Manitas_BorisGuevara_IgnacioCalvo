package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.util.Log
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AvailableJobsScreen(navController: NavHostController, jobViewModel: JobViewModel = viewModel())
 {
    val context = LocalContext.current
    val allJobs by jobViewModel.availableJobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val error by jobViewModel.error.collectAsState()
    val userMap by jobViewModel.userMap.collectAsState()


    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    val categoryOptions = listOf("Todas", "Limpieza", "Mudanza", "Reparación", "Jardinería", "Otro")
    var expandedCategory by remember { mutableStateOf(false) }

    val filteredJobs = allJobs.filter { job ->
        val matchesKeyword = searchQuery.isBlank() ||
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.description.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "Todas" ||
                job.category.equals(selectedCategory, ignoreCase = true)

        matchesKeyword && matchesCategory
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Buscar Trabajos", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Palabra clave") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                categoryOptions.forEach { category ->
                    DropdownMenuItem(onClick = {
                        selectedCategory = category
                        expandedCategory = false
                    }) {
                        Text(text = category)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                searchQuery = ""
                selectedCategory = "Todas"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar filtros")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

                                val creatorName = userMap[job.userId]?.name ?: "Usuario desconocido"
                                TextButton(onClick = {
                                    navController.navigate("publicProfile/${job.userId}")
                                }) {
                                    Text("Publicado por: $creatorName", style = MaterialTheme.typography.body2)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📍 ${job.location}")
                                Text("🕒 ${job.dateTime}")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = job.description, maxLines = 2)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (job.userId != userId && !job.applicants.contains(userId)) {
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            val result = jobViewModel.applyToJob(job.id, userId!!)
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "Te has postulado con éxito", Toast.LENGTH_SHORT).show()
                                                jobViewModel.refreshJobs()
                                            } else {
                                                val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                                                Log.e("PostulacionError", "Fallo al postularse: $errorMessage")
                                                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                            }

                                        }
                                    }) {
                                        Text("Postularme")
                                    }
                                } else if (job.applicants.contains(userId)) {
                                    Text("Ya te has postulado", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
