package com.example.tfg_manitas.features.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PublishTabScreen(navController: NavHostController) {
    val jobViewModel: JobViewModel = viewModel()
    val userJobs by jobViewModel.userJobs.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("post")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Publicar nuevo trabajo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            Text("Mis Trabajos Publicados", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))

            if (userJobs.isEmpty()) {
                Text("Aún no has publicado trabajos.")
            } else {
                LazyColumn {
                    items(userJobs) { job ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(job.title, style = MaterialTheme.typography.h6)
                                Text(job.description)
                                Spacer(Modifier.height(4.dp))
                                Text("📍 ${job.location}")
                                Text("🕒 ${job.dateTime}")
                            }
                        }
                    }
                }
            }
        }
    }
}
