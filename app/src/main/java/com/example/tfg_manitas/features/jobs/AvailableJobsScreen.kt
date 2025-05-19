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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.navigation.NavHostController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.tfg_manitas.ui.theme.VerdeExito
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AvailableJobsScreen(navController: NavHostController, jobViewModel: JobViewModel = viewModel()) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            "Buscar Trabajos",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Palabra clave") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Limpiar filtros")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (!error.isNullOrEmpty()) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            Text(
                "Mostrando ${filteredJobs.size} trabajos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredJobs.isEmpty()) {
                Text("No hay trabajos que coincidan con los filtros.")
            } else {
                LazyColumn {
                    items(filteredJobs) { job ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {

                                // 🔵 Columna de color según categoría
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight()
                                        .background(
                                            when (job.category.lowercase()) {
                                                "limpieza" -> MaterialTheme.colorScheme.primary
                                                "mudanza" -> MaterialTheme.colorScheme.secondary
                                                "reparación" -> MaterialTheme.colorScheme.tertiary
                                                "jardinería" -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.outline
                                            }
                                        )
                                )

                                // 🔸 Contenido de la tarjeta
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Título
                                    Text(
                                        text = job.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Etiqueta de categoría tipo chip
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = job.category,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Ubicación y fecha
                                    Text(
                                        text = "📍 ${job.location}  |  🕒 ${job.dateTime}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Descripción breve
                                    Text(
                                        text = job.description,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Autor
                                    val creatorName = userMap[job.userId]?.name ?: "Usuario desconocido"
                                    TextButton(onClick = {
                                        navController.navigate("publicProfile/${job.userId}")
                                    }) {
                                        Text(
                                            "Publicado por: $creatorName",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }



                                Spacer(modifier = Modifier.height(8.dp))

                                    if (job.userId != userId && !job.applicants.contains(userId)) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val result =
                                                        jobViewModel.applyToJob(job.id, userId!!)
                                                    if (result.isSuccess) {
                                                        Toast.makeText(
                                                            context,
                                                            "Te has postulado con éxito",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        jobViewModel.refreshJobs()
                                                    } else {
                                                        val errorMessage =
                                                            result.exceptionOrNull()?.message
                                                                ?: "Error desconocido"
                                                        Toast.makeText(
                                                            context,
                                                            "Error: $errorMessage",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text("Postularme")
                                        }
                                    } else if (job.applicants.contains(userId)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(
                                                    width = 1.dp,
                                                    color = VerdeExito,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .background(VerdeExito.copy(alpha = 0.1f))
                                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                        ) {
                                            Text(
                                                text = "✅ Ya te has postulado",
                                                color = VerdeExito,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}