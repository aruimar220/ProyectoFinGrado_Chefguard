package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemDetailScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Detalles del Alimento", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Text("Nombre: Tomate")
        Text("Cantidad: 5")
        Text("Fecha de caducidad: 2025-04-30")
        Text("Estado: Disponible")
        Text("Proveedor: Proveedor Demo")
        Text("Tipo de ambiente: Refrigerado")
    }
}
