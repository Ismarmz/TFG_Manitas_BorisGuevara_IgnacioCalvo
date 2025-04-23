package com.example.tfg_manitas.features.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.Alignment


@Composable
fun EditJobScreen(
    navController: NavHostController,
    jobId: String,
    jobViewModel: JobViewModel = viewModel()
) {
    val context = LocalContext.current
    val jobToEdit by jobViewModel.getJobById(jobId).collectAsState(initial = null)

    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var dateTime by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(jobToEdit) {
        jobToEdit?.let {
            title = TextFieldValue(it.title)
            description = TextFieldValue(it.description)
            category = TextFieldValue(it.category)
            location = TextFieldValue(it.location)
            dateTime = TextFieldValue(it.dateTime)
        }
    }

    if (jobToEdit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Editar Trabajo", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha y hora") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val safeJob = jobToEdit ?: return@Button

            jobViewModel.updateJob(
                jobId = jobId,
                updatedJob = Job(
                    id = jobId,
                    title = title.text,
                    description = description.text,
                    category = category.text,
                    location = location.text,
                    dateTime = dateTime.text,
                    userId = safeJob.userId,
                    createdAt = safeJob.createdAt
                ),
                onSuccess = {
                    Toast.makeText(context, "Trabajo actualizado", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onFailure = {
                    Toast.makeText(context, "Error al actualizar: $it", Toast.LENGTH_LONG).show()
                }
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar Cambios")
        }

    }
}
