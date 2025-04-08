package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InventoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Inventario",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // buscador del inventario
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Buscar producto") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false // de momento desactivado
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Aquí irá la lista de productos (de momento vacía)
        Text("No hay productos aún.", style = MaterialTheme.typography.bodyMedium)
    }
}
