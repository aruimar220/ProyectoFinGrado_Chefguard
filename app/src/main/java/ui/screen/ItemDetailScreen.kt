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
import kotlinx.coroutines.launch

@Composable
fun ItemDetailsScreen(
    navController: NavController,
    id: Int // Recibimos el ID del alimento seleccionado
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val coroutineScope = rememberCoroutineScope()

    var alimento by remember { mutableStateOf<AlimentoEntity?>(null) }

    LaunchedEffect(id) {
        // Cargar el alimento específico desde la base de datos
        alimento = db.alimentoDao().obtenerAlimentoPorId(id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Detalles del Alimento", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        if (alimento != null) {
            Text(text = "Nombre: ${alimento!!.nombre}")
            Text(text = "Cantidad: ${alimento!!.cantidad}")
            Text(text = "Fecha de caducidad: ${alimento!!.fechaCaducidad}")
            Text(text = "Fecha de consumo preferente: ${alimento!!.fechaConsumo ?: "No especificada"}")
            Text(text = "Lote: ${alimento!!.lote}")
            Text(text = "Estado: ${alimento!!.estado}")
            Text(text = "Proveedor: ${alimento!!.proveedor}")
            Text(text = "Tipo de alimento: ${alimento!!.tipoAlimento}")
            Text(text = "Ambiente: ${alimento!!.ambiente}")

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        db.alimentoDao().eliminarAlimento(alimento!!.id)
                    }
                    navController.navigate("inventory") // Regresar al inventario
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar alimento")
            }
        } else {
            Text("Cargando detalles...")
        }
    }
}