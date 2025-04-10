package com.example.chefguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Alimento(
    val nombre: String,
    val estado: String,
    val cantidad: Int,
    val fechaCaducidad: String
)

val alimentosEjemplo = listOf(
    Alimento("Leche", "Disponible", 10, "2025-04-20"),
    Alimento("Pan", "Agotado", 0, "2025-04-10"),
    Alimento("Queso", "Caducado", 2, "2025-03-30"),
    Alimento("Manzanas", "Disponible", 5, "2025-04-15")
)

@Composable
fun InventoryScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_item")
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(alimentosEjemplo) { alimento ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("item_detail/${alimento.nombre}")
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(alimento.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${alimento.estado}")
                        Text("Cantidad: ${alimento.cantidad}")
                        Text("Caduca: ${alimento.fechaCaducidad}")
                    }
                }
            }
        }
    }
}
