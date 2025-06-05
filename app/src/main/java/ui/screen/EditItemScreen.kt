package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import data.local.entity.AlimentoEntity
import data.local.AppDatabase
import com.example.chefguard.utils.PreferencesManager
import com.example.chefguard.viewmodel.AlimentoViewModel

@Composable
fun EditItemScreen(navController: NavController, id: Int, viewModel: AlimentoViewModel = viewModel()) { // Composición de la pantalla de edición de un alimento con su contenido y botones correspondientes
    val context = LocalContext.current // Obtiene el contexto de la aplicación para acceder a la base de datos y a las preferencias compartidas
    val db = AppDatabase.getDatabase(context) // Obtiene la instancia de la base de datos utilizando el contexto proporcionado

    // Variables para almacenar los valores de los campos de entrada de la pantalla de edición de alimento
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") }
    var fechaConsumo by remember { mutableStateOf("") }
    var lote by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var tipoAlimento by remember { mutableStateOf("") }
    var ambiente by remember { mutableStateOf("") }
    val userId = PreferencesManager.getUserId(context)
    val scrollState = rememberScrollState()

    LaunchedEffect(id) { // Ejecuta una acción una vez en el ciclo de vida de la composición cuando cambia el valor de id
        viewModel.cargarAlimento(db, id)
    }

    val alimento = viewModel.alimento.collectAsState().value // Obtiene el alimento de la base de datos utilizando el ViewModel
    LaunchedEffect(alimento) { // Ejecuta una acción una vez en el ciclo de vida de la composición cuando cambia el valor del alimento
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

    Column( // Composición de la columna para mostrar los campos de entrada de la pantalla de edición de alimento en la pantalla de edición de alimento
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) { // Título de la pantalla de edición de alimento con su estilo y botones correspondientes
        Text("Editar Alimento", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField( // Campo de entrada para el nombre del alimento con su acción correspondiente
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del alimento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para la cantidad del alimento con su acción correspondiente
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para la fecha de caducidad del alimento con su acción correspondiente
            value = fechaCaducidad,
            onValueChange = { fechaCaducidad = it },
            label = { Text("Fecha de caducidad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para la fecha de consumo preferente del alimento con su acción correspondiente
            value = fechaConsumo,
            onValueChange = { fechaConsumo = it },
            label = { Text("Fecha de consumo preferente") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para el lote del alimento con su acción correspondiente
            value = lote,
            onValueChange = { lote = it },
            label = { Text("Lote") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para el estado del alimento con su acción correspondiente
            value = estado,
            onValueChange = { estado = it },
            label = { Text("Estado (Disponible / Agotado / Caducado)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para el proveedor del alimento con su acción correspondiente
            value = proveedor,
            onValueChange = { proveedor = it },
            label = { Text("Proveedor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para el tipo de alimento con su acción correspondiente
            value = tipoAlimento,
            onValueChange = { tipoAlimento = it },
            label = { Text("Tipo de alimento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para el ambiente del alimento con su acción correspondiente
            value = ambiente,
            onValueChange = { ambiente = it },
            label = { Text("Ambiente") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button( // Botón de guardar cambios con su acción correspondiente y navega a la pantalla de inventario después de guardar los cambios del alimento
            onClick = { // Guarda los cambios del alimento en la base de datos utilizando el ViewModel y navega a la pantalla de inventario después de guardar los cambios
                val alimentoEditado = AlimentoEntity(
                    id = id,
                    ID_usuario = userId,
                    nombre = nombre,
                    cantidad = cantidad.toIntOrNull() ?: 0,
                    fechaCaducidad = fechaCaducidad.ifBlank { null },
                    fechaConsumo = fechaConsumo.ifBlank { null },
                    lote = lote.ifBlank { null },
                    estado = estado.ifBlank { null },
                    proveedor = proveedor.ifBlank { null },
                    tipoAlimento = tipoAlimento.ifBlank { null },
                    ambiente = ambiente.ifBlank { null }
                )
                viewModel.guardarAlimento(db, alimentoEditado) // Guarda los cambios del alimento en la base de datos utilizando el ViewModel
                navController.navigate("inventory") // Navega a la pantalla de inventario después de guardar los cambios
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios") // Texto del botón de guardar cambios con su estilo
        }
    }
}