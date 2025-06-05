package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chefguard.viewmodel.AlimentoViewModel
import data.local.entity.AlimentoEntity
import data.local.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun ItemDetailsScreen( // Composición de la pantalla de detalles de un alimento con su contenido y botones correspondientes
    navController: NavController, // Controlador de navegación para navegar a otras pantallas dentro de la aplicación
    id: Int
) {
    val context = LocalContext.current // Obtiene el contexto de la aplicación para acceder a la base de datos
    val db = AppDatabase.getDatabase(context) // Obtiene la instancia de la base de datos utilizando el contexto proporcionado
    val coroutineScope = rememberCoroutineScope() // Crea un alcance de corrutina para lanzar corrutinas desde la composición
    val viewModel: AlimentoViewModel = viewModel() // Crea una instancia del ViewModel para la pantalla de detalles de alimento

    var alimento by remember { mutableStateOf<AlimentoEntity?>(null) } // Variable para almacenar los detalles del alimento obtenido de la base de datos

    LaunchedEffect(id) { // Ejecuta una acción una vez en el ciclo de vida de la composición cuando cambia el valor de id
        alimento = db.alimentoDao().obtenerAlimentoPorId(id)
    }

    Column( // Composición de la columna para mostrar los detalles del alimento en la pantalla de detalles de alimento
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Detalles del Alimento", style = MaterialTheme.typography.headlineMedium) // Título de la pantalla de detalles de alimento con su estilo
        Spacer(modifier = Modifier.height(20.dp))

        if (alimento != null) { // Muestra los detalles del alimento si se encuentran en la base de datos y si no, muestra un mensaje de carga de datos
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

            Button( // Botón de eliminar alimento con su acción correspondiente cuando se hace clic en él
                onClick = { // Elimina el alimento de la base de datos utilizando el ViewModel y navega a la pantalla de inventario después de eliminar el alimento
                    coroutineScope.launch { // Lanza una corrutina para eliminar el alimento de la base de datos utilizando el ViewModel
                        viewModel.eliminarAlimento(db, alimento!!.id) // Elimina el alimento de la base de datos utilizando el ViewModel
                        navController.navigate("inventory") // Navega a la pantalla de inventario después de eliminar el alimento
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar alimento") // Texto del botón de eliminar alimento
            }

            Spacer(modifier = Modifier.height(10.dp))


            Button( // Botón de editar alimento con su acción correspondiente cuando se hace clic en él y navega a la pantalla de edición de alimento
                onClick = { // Navega a la pantalla de edición de alimento pasando el ID del alimento como argumento
                    navController.navigate("edit_item/${alimento!!.id}") // Navega a la pantalla de edición de alimento pasando el ID del alimento como argumento
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar alimento") // Texto del botón de editar alimento con su estilo
            }
        } else {
            Text("Cargando detalles...") // Muestra un mensaje de carga de datos si no se encuentran los detalles del alimento en la base de datos
        }
    }
}