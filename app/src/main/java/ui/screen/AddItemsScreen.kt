package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddItemsScreen(navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") }
    var fechaConsumo by remember { mutableStateOf("") }
    var lote by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var tipoAlimento by remember { mutableStateOf("") }
    var ambiente by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Añadir Alimento", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del alimento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = fechaCaducidad,
            onValueChange = { fechaCaducidad = it },
            label = { Text("Fecha de caducidad (AAAA-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = fechaConsumo,
            onValueChange = { fechaConsumo = it },
            label = { Text("Fecha de consumo preferente (AAAA-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = lote,
            onValueChange = { lote = it },
            label = { Text("Lote") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = estado,
            onValueChange = { estado = it },
            label = { Text("Estado (Disponible / Agotado / Caducado)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = proveedor,
            onValueChange = { proveedor = it },
            label = { Text("Proveedor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = tipoAlimento,
            onValueChange = { tipoAlimento = it },
            label = { Text("Tipo de alimento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = ambiente,
            onValueChange = { ambiente = it },
            label = { Text("Ambiente") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // esta parte guarda en la base de datos cuando se conecte
                navController.navigate("inventory")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar alimento")
        }
    }
}
