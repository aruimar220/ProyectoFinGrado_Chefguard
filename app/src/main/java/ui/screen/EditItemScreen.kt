package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chefguard.model.AlimentoEntity
import com.example.chefguard.model.AppDatabase
import com.example.chefguard.viewmodel.AlimentoViewModel

@Composable
fun EditItemScreen(navController: NavController, id: Int, viewModel: AlimentoViewModel = viewModel()) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") }
    var fechaConsumo by remember { mutableStateOf("") }
    var lote by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var tipoAlimento by remember { mutableStateOf("") }
    var ambiente by remember { mutableStateOf("") }

    LaunchedEffect(id) {
        viewModel.cargarAlimento(db, id)
    }

    val alimento = viewModel.alimento.collectAsState().value
    LaunchedEffect(alimento) {
        if (alimento != null) {
            nombre = alimento.nombre
            cantidad = alimento.cantidad.toString()
            fechaCaducidad = alimento.fechaCaducidad ?: ""
            fechaConsumo = alimento.fechaConsumo ?: ""
            lote = alimento.lote ?: ""
            estado = alimento.estado ?: ""
            proveedor = alimento.proveedor ?: ""
            tipoAlimento = alimento.tipoAlimento ?: ""
            ambiente = alimento.ambiente ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Editar Alimento", style = MaterialTheme.typography.headlineMedium)
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

        Button(
            onClick = {
                val alimentoEditado = AlimentoEntity(
                    id = id,
                    nombre = nombre,
                    cantidad = cantidad.toInt(),
                    fechaCaducidad = fechaCaducidad,
                    fechaConsumo = fechaConsumo.ifBlank { null },
                    lote = lote.ifBlank { "" },
                    estado = estado.ifBlank { "" },
                    proveedor = proveedor.ifBlank { "" },
                    tipoAlimento = tipoAlimento.ifBlank { "" },
                    ambiente = ambiente.ifBlank { "" }
                )
                viewModel.guardarAlimento(db, alimentoEditado)
                navController.navigate("inventory")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }
    }
}