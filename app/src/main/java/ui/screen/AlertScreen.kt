package com.example.chefguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chefguard.model.AlimentoEntity
import com.example.chefguard.model.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun AlertScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Cargar todos los alimentos desde la base de datos
        alimentos = db.alimentoDao().obtenerTodosLosAlimentos()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_items")
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            if (alimentos.isEmpty()) {
                // Mostrar mensaje si no hay alimentos
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No hay alimentos registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn {
                    items(alimentos.size) { index ->
                        val alimento = alimentos[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    navController.navigate("item_details/${alimento.id}")
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "Nombre: ${alimento.nombre}")
                                Text(text = "Fecha de caducidad: ${alimento.fechaCaducidad}")
                            }
                        }
                    }
                }
            }
        }
    }
}