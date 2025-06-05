package com.example.chefguard.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import data.local.entity.AlimentoEntity
import data.local.AppDatabase
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.chefguard.utils.PreferencesManager
import kotlinx.coroutines.launch
import com.example.chefguard.utils.exportAlimentosToCsv
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun InventoryScreen(navController: NavController) { // Función para mostrar el inventario
    val context = LocalContext.current // Contexto de la aplicación para acceder a recursos del sistema y cargar datos
    val db = AppDatabase.getDatabase(context) // Instancia de la base de datos local
    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) } // Lista de alimentos que se mostrará en la pantalla de inventario


    val userId = PreferencesManager.getUserId(context) // ID del usuario actual en la aplicación
    if (userId == -1) { // Si el usuario no está autenticado, navega a la pantalla de inicio de sesión
        LaunchedEffect(Unit) { // Ejecuta el código dentro del bloque solo una vez al comienzo de la composición de la pantalla
            navController.navigate("login") { // Navega a la pantalla de inicio de sesión con la opción de limpiar la pila de navegación
                popUpTo("inventory") { inclusive = true } // Limpia la pila de navegación al navegar a esta pantalla
            }
        }
        return // No muestra nada más si el usuario no está autenticado
    }

    val scope = rememberCoroutineScope() // Alcance para las corrutinas de Compose
    val importLauncher = rememberLauncherForActivityResult( // Manejador de resultados para importar alimentos desde un archivo CSV
        contract = ActivityResultContracts.OpenDocument(), // Abre un documento de tipo CSV para seleccionar un archivo para importar
        onResult = { uri -> // Maneja el resultado de la selección de un archivo
            uri?.let { // Si el usuario seleccionó un archivo, procede con la importación
                scope.launch { // Ejecuta la importación en un alcance de corrutina
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri) // Abre el flujo de entrada del archivo seleccionado
                        val reader = BufferedReader(InputStreamReader(inputStream)) // Crea un lector de BufferedReader para leer el contenido del archivo
                        val lines = reader.readLines()  // Lee todas las líneas del archivo y las almacena en una lista
                        reader.close() // Cierra el lector de BufferedReader

                        val alimentosImportados = lines // Convierte las líneas en una lista de alimentos que se importarán
                            .drop(1) // Elimina la primera línea (encabezado)
                            .mapNotNull { line -> parseCsvLineToAlimento(line, userId) } // Convierte cada línea en un objeto AlimentoEntity utilizando la función parseCsvLineToAlimento

                        val existentes = db.alimentoDao().obtenerAlimentosPorUsuario(userId) // Obtiene los alimentos existentes del usuario actual

                        val nuevos = alimentosImportados.filterNot { nuevo -> // Filtra los alimentos nuevos que no existen en la base de datos
                            existentes.any { existente -> // Verifica si el alimento ya existe en la base de datos
                                existente.nombre == nuevo.nombre && // Compara los atributos relevantes del alimento nuevo con los existentes
                                        existente.fechaCaducidad == nuevo.fechaCaducidad && // Si los atributos son iguales, el alimento ya existe en la base de datos
                                        existente.lote == nuevo.lote // Si los atributos son iguales, el alimento ya existe en la base de datos
                            }
                        }

                        if (nuevos.isNotEmpty()) { // Si hay alimentos nuevos que no existen en la base de datos, los inserta en la base de datos
                            db.alimentoDao().insertarAlimentos(nuevos) // Inserta los alimentos nuevos en la base de datos
                        }


                        alimentos = db.alimentoDao().obtenerAlimentosPorUsuario(userId) // Obtiene los alimentos actualizados del usuario actual desde la base de datos

                        Toast.makeText( // Muestra un mensaje indicando la cantidad de alimentos nuevos importados
                            context,
                            "Se importaron ${nuevos.size} nuevos alimentos",
                            Toast.LENGTH_SHORT // Duración del mensaje
                        ).show()
                    } catch (e: Exception) { // Maneja cualquier excepción que pueda ocurrir durante la importación
                        e.printStackTrace()
                        Toast.makeText(context, "Error al importar", Toast.LENGTH_SHORT).show() // Muestra un mensaje de error
                    }
                }
            }
        }
    )



    var searchText by remember { mutableStateOf("") } // Campo de búsqueda para filtrar los alimentos
    var filtroEstado by remember { mutableStateOf("Todos") } // Filtro para mostrar solo alimentos con un estado específico
    val estados = listOf("Todos", "Disponible", "Agotado", "Caducado") // Lista de estados disponibles para filtrar los alimentos

    LaunchedEffect(userId) { // Ejecuta el código dentro del bloque solo una vez al comienzo de la composición de la pantalla
        alimentos = db.alimentoDao().obtenerAlimentosPorUsuario(userId) // Obtiene los alimentos del usuario actual desde la base de datos
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), // Asegura que el Scaffold ocupe toda la pantalla completa
        floatingActionButton = { // Botón flotante para agregar alimentos al inventario
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_items") // Navega a la pantalla de agregar alimentos
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall) // Muestra el símbolo + en el botón
            }
        },
        floatingActionButtonPosition = FabPosition.End // Posiciona el botón flotante en la parte superior derecha de la pantalla
    ) { innerPadding ->
        Column( // Columna para organizar los elementos de la pantalla de inventario
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            Text( // Título de la pantalla de inventario con alineación centrada
                text = "Inventario",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField( // Campo de búsqueda para filtrar los alimentos
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar alimento") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Row( // Fila para mostrar el menú desplegable para filtrar los alimentos por estado
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                DropdownMenuExample( // Muestra el menú desplegable para filtrar los alimentos por estado
                    items = estados,
                    selectedValue = filtroEstado,
                    onItemSelected = { nuevoEstado -> filtroEstado = nuevoEstado }
                )
            }


            Row( // Fila para mostrar el botón para exportar los alimentos a un archivo CSV y el botón para importar un archivo CSV
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { // Botón para exportar los alimentos a un archivo CSV
                    scope.launch { // Ejecuta la exportación en un alcance de corrutina
                        val file = exportAlimentosToCsv(context, alimentos) // Exporta los alimentos a un archivo CSV
                        if (file != null) {
                            val uri = androidx.core.content.FileProvider.getUriForFile( // Obtiene la URI del archivo CSV para compartirlo
                                context,
                                "${context.packageName}.fileprovider", // Autoridad del proveedor de archivos
                                file
                            )
                            val shareIntent = android.content.Intent().apply { // Crea un intent para compartir el archivo CSV
                                action = android.content.Intent.ACTION_SEND // Acción de compartir contenido
                                type = "text/csv" // Tipo de archivo CSV
                                putExtra(android.content.Intent.EXTRA_STREAM, uri) // Extra con la URI del archivo CSV
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) // Permite compartir el archivo
                            }
                            context.startActivity( // Comienza la actividad de compartir
                                android.content.Intent.createChooser( // Crea un chooser para mostrar las opciones de compartir
                                    shareIntent,
                                    "Compartir CSV"
                                )
                            )
                        }
                    }
                }) {
                    Text("Exportar CSV") // Muestra el texto "Exportar CSV"
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = { // Botón para importar un archivo CSV
                    importLauncher.launch(arrayOf("text/csv"))
                }) {
                    Text("Importar CSV")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredAlimentos = alimentos.filter { alimento -> // Filtra los alimentos según el texto de búsqueda y el estado seleccionado
                (searchText.isBlank() ||
                        alimento.nombre?.contains(searchText, ignoreCase = true) == true ||
                        alimento.proveedor?.contains(searchText, ignoreCase = true) == true ||
                        alimento.tipoAlimento?.contains(searchText, ignoreCase = true) == true) &&
                        (filtroEstado == "Todos" || alimento.estado?.lowercase() == filtroEstado.lowercase())
            }

            if (filteredAlimentos.isEmpty()) { // Muestra un mensaje si no se encontraron alimentos
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text( // Muestra el mensaje si no se encontraron alimentos
                        text = "No se han encontrado alimentos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn { // Muestra la lista de alimentos filtrados
                    items(filteredAlimentos.size) { index -> // Recorre la lista de alimentos filtrados
                        val alimento = filteredAlimentos[index] // Obtiene el alimento actual

                        val colorEstado = when (alimento.estado?.lowercase()) { // Asigna un color de fondo según el estado del alimento
                            "disponible" -> MaterialTheme.colorScheme.primaryContainer // Color de fondo para alimentos disponibles
                            "agotado" -> MaterialTheme.colorScheme.errorContainer // Color de fondo para alimentos agotados
                            "caducado" -> MaterialTheme.colorScheme.surfaceVariant // Color de fondo para alimentos caducados
                            else -> MaterialTheme.colorScheme.surface // Color de fondo por defecto
                        }

                        Card( // Muestra una tarjeta para cada alimento
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    navController.navigate("item_details/${alimento.id}") // Navega a la pantalla de detalles del alimento
                                },
                            colors = CardDefaults.cardColors(containerColor = colorEstado)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "Nombre: ${alimento.nombre}") // Muestra la información del alimento
                                Text(text = "Cantidad: ${alimento.cantidad}") // Muestra la información del alimento
                                Text(text = "Proveedor: ${alimento.proveedor}") // Muestra la información del alimento
                                Text(text = "Estado: ${alimento.estado ?: "No especificado"}") // Muestra la información del alimento
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuExample( // Función para mostrar un menú desplegable con opciones
    items: List<String>, // Lista de opciones del menú
    selectedValue: String, // Valor seleccionado del menú
    onItemSelected: (String) -> Unit // Función para manejar la selección de una opción del menú
) {
    var expanded by remember { mutableStateOf(false) } // Estado para controlar la visibilidad del menú desplegable

    Box {
        Button(onClick = { expanded = true }) { // Botón para mostrar el menú desplegable
            Text(selectedValue)
        }

        DropdownMenu( // Menú desplegable con las opciones proporcionadas
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem( // Opción del menú desplegable
                    text = { Text(item) },
                    onClick = { // Maneja la selección de una opción del menú
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun parseCsvLineToAlimento(line: String, userId: Int): AlimentoEntity? { // Función para convertir una línea CSV en un objeto AlimentoEntity si es válida
    val tokens = line.split(",") // Divide la línea en tokens separados por comas
    return try { // Intenta convertir los tokens en un objeto AlimentoEntity
        AlimentoEntity( // Crea un objeto AlimentoEntity a partir de los tokens
            ID_usuario = userId,
            nombre = tokens.getOrNull(2)?.trim()?.removeSurrounding("\"") ?: return null,
            cantidad = tokens.getOrNull(3)?.trim()?.toIntOrNull() ?: return null,
            fechaCaducidad = tokens.getOrNull(4)?.trim()?.removeSurrounding("\""),
            fechaConsumo = tokens.getOrNull(5)?.trim()?.removeSurrounding("\""),
            lote = tokens.getOrNull(6)?.trim()?.removeSurrounding("\""),
            estado = tokens.getOrNull(7)?.trim()?.removeSurrounding("\""),
            proveedor = tokens.getOrNull(8)?.trim()?.removeSurrounding("\""),
            tipoAlimento = tokens.getOrNull(9)?.trim()?.removeSurrounding("\""),
            ambiente = tokens.getOrNull(10)?.trim()?.removeSurrounding("\"")
        )
    } catch (e: Exception) { // Maneja cualquier excepción que pueda ocurrir durante la conversión
        e.printStackTrace()
        null
    }
}


