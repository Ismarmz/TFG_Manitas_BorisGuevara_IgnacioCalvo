package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.navigation.NavHostController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.tfg_manitas.ui.theme.VerdeExito
import androidx.compose.ui.unit.sp
import com.example.tfg_manitas.features.profile.User
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore

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
    var locationFilter by remember { mutableStateOf("") }
    var minPaymentFilter by remember { mutableStateOf("") }

    val tagOptions = listOf("Hogar", "Exterior", "Técnico", "Express", "Físico", "No presencial")
    val selectedTags = remember { mutableStateListOf<String>() }

    val filteredJobs = allJobs.filter { job ->
        val cleanedQuery = searchQuery.trim().lowercase()
        val minValue = minPaymentFilter.toDoubleOrNull()

        val matchesKeyword = cleanedQuery.isEmpty() ||
                job.title.lowercase().contains(cleanedQuery) ||
                job.description.lowercase().contains(cleanedQuery)

        val matchesLocation = locationFilter.trim().isEmpty() ||
                job.location.lowercase().contains(locationFilter.trim().lowercase())

        val matchesPayment = minValue == null || job.paymentAmount.toDoubleOrNull()?.let {
            it >= minValue
        } ?: false

        val matchesTags = selectedTags.isEmpty() || selectedTags.any { tag ->
            job.tags.any { it.equals(tag, ignoreCase = true) }
        }

        val notAssigned = job.selectedWorkerId == null

        notAssigned && matchesKeyword && matchesTags && matchesLocation && matchesPayment
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Buscar Trabajos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo siempre visible
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar trabajo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

// Filtros colapsables
                var showAdvancedFilters by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedFilters = !showAdvancedFilters }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Más filtros",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showAdvancedFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = showAdvancedFilters) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = locationFilter,
                                onValueChange = { locationFilter = it },
                                label = { Text("Ubicación") },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                                    textColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            OutlinedTextField(
                                value = minPaymentFilter,
                                onValueChange = { minPaymentFilter = it },
                                label = { Text("Pago mínimo") },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                                    textColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }


                Text("Etiquetas", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Scroll left",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(tagOptions) { tag ->
                            val selected = selectedTags.contains(tag)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) selectedTags.remove(tag)
                                    else selectedTags.add(tag)
                                },
                                label = { Text(tag) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Scroll right",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))


            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // menor espaciado antes de resultados
        Button(
            onClick = {
                searchQuery = ""
                locationFilter = ""
                minPaymentFilter = ""
                selectedTags.clear()
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
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
                        JobCard(job, userMap, userId, navController, jobViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun JobCard(
    job: Job,
    userMap: Map<String, User>,
    userId: String?,
    navController: NavHostController,
    jobViewModel: JobViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize(), // <--- aplicar animación aquí
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.tags.joinToString(", "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("📍 ${job.location}  |  🕒 ${job.dateTime}  |  💰 ${job.paymentAmount}", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(job.description, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                val creatorName = userMap[job.userId]?.name ?: "Usuario desconocido"
                TextButton(
                    onClick = { navController.navigate("publicProfile/${job.userId}") },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Publicado por: $creatorName")
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (job.userId != userId && !job.applicants.contains(userId)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = jobViewModel.applyToJob(job.id, userId!!)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Te has postulado con éxito", Toast.LENGTH_SHORT).show()
                                    jobViewModel.refreshJobs()
                                } else {
                                    Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
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
                            .border(1.dp, VerdeExito, RoundedCornerShape(6.dp))
                            .background(VerdeExito.copy(alpha = 0.1f))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text("✅ Ya te has postulado", color = VerdeExito, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
