package com.example.tfg_manitas.features.jobs

import android.widget.Toast
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
        val notAssigned = job.selectedWorkerId == null
        val matchesKeyword = searchQuery.isBlank() ||
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.description.contains(searchQuery, ignoreCase = true)

        val matchesTags = selectedTags.isEmpty() || selectedTags.any {
            job.tags.any { tag -> tag.equals(it, ignoreCase = true) }
        }

        val matchesLocation = locationFilter.isBlank() ||
                job.location.contains(locationFilter, ignoreCase = true)

        val minValue = minPaymentFilter.toDoubleOrNull()
        val matchesPayment = minValue == null || job.paymentAmount.toDoubleOrNull()?.let {
            it >= minValue
        } ?: false

        notAssigned && matchesKeyword && matchesTags && matchesLocation && matchesPayment
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Buscar Trabajos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Palabra clave") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = locationFilter,
            onValueChange = { locationFilter = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = minPaymentFilter,
            onValueChange = { minPaymentFilter = it },
            label = { Text("Pago mínimo (USD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Etiquetas", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            tagOptions.forEach { tag ->
                val selected = selectedTags.contains(tag)
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) selectedTags.remove(tag)
                        else selectedTags.add(tag)
                    },
                    label = { Text(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                searchQuery = ""
                locationFilter = ""
                minPaymentFilter = ""
                selectedTags.clear()
            },
            modifier = Modifier.fillMaxWidth()
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
            .padding(vertical = 8.dp),
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
                Text(job.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
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
                TextButton(onClick = { navController.navigate("publicProfile/${job.userId}") }) {
                    Text("Publicado por: $creatorName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (job.userId != userId && !job.applicants.contains(userId)) {
                    Button(onClick = {
                        coroutineScope.launch {
                            val result = jobViewModel.applyToJob(job.id, userId!!)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Te has postulado con éxito", Toast.LENGTH_SHORT).show()
                                jobViewModel.refreshJobs()
                            } else {
                                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
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
