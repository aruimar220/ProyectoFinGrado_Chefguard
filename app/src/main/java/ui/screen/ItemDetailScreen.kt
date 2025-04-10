package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemDetailScreen(nombre: String) {
    val alimento = alimentosEjemplo.find { it.nombre == nombre }

    if (alimento == null) {
        Text("Alimento no encontrado.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Detalle del Alimento", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Nombre: ${alimento.nombre}")
        Text("Estado: ${alimento.estado}")
        Text("Cantidad: ${alimento.cantidad}")
        Text("Fecha de Caducidad: ${alimento.fechaCaducidad}")
    }
}
