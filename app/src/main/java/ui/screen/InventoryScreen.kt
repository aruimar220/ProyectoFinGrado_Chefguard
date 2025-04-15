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
fun InventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val coroutineScope = rememberCoroutineScope()

    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Cargar alimentos desde la base de datos
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
            Text(
                "Inventario",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(24.dp)
            )

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
                            Text(text = "Cantidad: ${alimento.cantidad}")
                            Text(text = "Proveedor: ${alimento.proveedor}")
                        }
                    }
                }
            }
        }
    }
}