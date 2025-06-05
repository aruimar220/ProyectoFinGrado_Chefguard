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
import kotlinx.coroutines.launch

@Composable
fun AddItemsScreen(navController: NavController) { // Composición de la pantalla de añadir alimentos con su contenido y botones correspondientes
    // Variables para almacenar los valores ingresados por el usuario en los campos de entrada
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") }
    var fechaConsumo by remember { mutableStateOf("") }
    var lote by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var tipoAlimento by remember { mutableStateOf("") }
    var ambiente by remember { mutableStateOf("") }

    val context = LocalContext.current // Obtiene el contexto de la aplicación para acceder a la base de datos y a las preferencias compartidas
    val db = AppDatabase.getDatabase(context) // Obtiene la instancia de la base de datos utilizando el contexto proporcionado
    val coroutineScope = rememberCoroutineScope() // Crea un ámbito de corrutinas para ejecutar corrutinas asíncronas
    val userId = PreferencesManager.getUserId(context) // Obtiene el ID del usuario de las preferencias compartidas y navega a la pantalla de inicio de sesión si no hay un usuario
    val scrollState = rememberScrollState() // Crea un estado de desplazamiento para el contenido de la pantalla
    val viewModel: AlimentoViewModel = viewModel() // Crea un objeto ViewModel para manejar la lógica de la pantalla de añadir alimentos

    Column( // Composición de la columna para mostrar los campos de entrada de la pantalla de añadir alimentos en la pantalla de añadir alimentos con su acción correspondiente
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) { // Título de la pantalla de añadir alimentos con su estilo y botones correspondientes
        Text("Añadir Alimento", style = MaterialTheme.typography.headlineMedium) // Texto del título de la pantalla de añadir alimentos con su estilo y acción correspondiente al hacer clic en él
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(// Campo de entrada para el nombre del alimento con su acción correspondiente
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
            label = { Text("Fecha de caducidad (AAAA-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField( // Campo de entrada para la fecha de consumo del alimento con su acción correspondiente
            value = fechaConsumo,
            onValueChange = { fechaConsumo = it },
            label = { Text("Fecha de consumo preferente (AAAA-MM-DD)") },
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

        Button( // Botón de guardar alimento con su acción correspondiente y validación correspondiente a los campos de entrada
            onClick = { // Guarda el alimento en la base de datos con los valores ingresados en los campos de entrada
                if (nombre.isNotBlank() && cantidad.toIntOrNull() != null) {
                    val nuevoAlimento = AlimentoEntity(
                        ID_usuario = userId,
                        nombre = nombre,
                        cantidad = cantidad.toInt(),
                        fechaCaducidad = fechaCaducidad,
                        fechaConsumo = fechaConsumo.ifBlank { null },
                        lote = lote,
                        estado = estado,
                        proveedor = proveedor,
                        tipoAlimento = tipoAlimento,
                        ambiente = ambiente
                    )

                    coroutineScope.launch { // Lanza una corrutina para guardar el alimento en la base de datos utilizando el ViewModel
                        viewModel.guardarAlimento(db, nuevoAlimento)
                    }

                    navController.navigate("inventory") // Navega a la pantalla de inventario después de guardar el alimento
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar alimento") // Texto del botón de guardar alimento con su estilo y acción correspondiente al hacer clic en él
        }
    }
}