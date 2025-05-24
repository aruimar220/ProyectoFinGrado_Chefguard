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
fun InventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) }


    val userId = PreferencesManager.getUserId(context)
    if (userId == -1) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("inventory") { inclusive = true }
            }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val lines = reader.readLines()
                        reader.close()

                        val alimentosImportados = lines
                            .drop(1)
                            .mapNotNull { line -> parseCsvLineToAlimento(line, userId) }

                        val existentes = db.alimentoDao().obtenerAlimentosPorUsuario(userId)

                        val nuevos = alimentosImportados.filterNot { nuevo ->
                            existentes.any { existente ->
                                existente.nombre == nuevo.nombre &&
                                        existente.fechaCaducidad == nuevo.fechaCaducidad &&
                                        existente.lote == nuevo.lote
                            }
                        }

                        if (nuevos.isNotEmpty()) {
                            db.alimentoDao().insertarAlimentos(nuevos)
                        }


                        alimentos = db.alimentoDao().obtenerAlimentosPorUsuario(userId)

                        Toast.makeText(
                            context,
                            "Se importaron ${nuevos.size} nuevos alimentos",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error al importar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )



    var searchText by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("Todos") }
    val estados = listOf("Todos", "Disponible", "Agotado", "Caducado")

    LaunchedEffect(userId) {
        alimentos = db.alimentoDao().obtenerAlimentosPorUsuario(userId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_items")
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Inventario",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar alimento") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                DropdownMenuExample(
                    items = estados,
                    selectedValue = filtroEstado,
                    onItemSelected = { nuevoEstado -> filtroEstado = nuevoEstado }
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    scope.launch {
                        val file = exportAlimentosToCsv(context, alimentos)
                        if (file != null) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(
                                    shareIntent,
                                    "Compartir CSV"
                                )
                            )
                        }
                    }
                }) {
                    Text("Exportar CSV")
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = {
                    importLauncher.launch(arrayOf("text/csv"))
                }) {
                    Text("Importar CSV")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredAlimentos = alimentos.filter { alimento ->
                (searchText.isBlank() ||
                        alimento.nombre?.contains(searchText, ignoreCase = true) == true ||
                        alimento.proveedor?.contains(searchText, ignoreCase = true) == true ||
                        alimento.tipoAlimento?.contains(searchText, ignoreCase = true) == true) &&
                        (filtroEstado == "Todos" || alimento.estado?.lowercase() == filtroEstado.lowercase())
            }

            if (filteredAlimentos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se han encontrado alimentos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn {
                    items(filteredAlimentos.size) { index ->
                        val alimento = filteredAlimentos[index]

                        val colorEstado = when (alimento.estado?.lowercase()) {
                            "disponible" -> MaterialTheme.colorScheme.primaryContainer
                            "agotado" -> MaterialTheme.colorScheme.errorContainer
                            "caducado" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    navController.navigate("item_details/${alimento.id}")
                                },
                            colors = CardDefaults.cardColors(containerColor = colorEstado)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "Nombre: ${alimento.nombre}")
                                Text(text = "Cantidad: ${alimento.cantidad}")
                                Text(text = "Proveedor: ${alimento.proveedor}")
                                Text(text = "Estado: ${alimento.estado ?: "No especificado"}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuExample(
    items: List<String>,
    selectedValue: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedValue)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun parseCsvLineToAlimento(line: String, userId: Int): AlimentoEntity? {
    val tokens = line.split(",")
    return try {
        AlimentoEntity(
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
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


