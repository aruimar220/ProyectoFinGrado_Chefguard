package com.example.chefguard.ui.screens

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
import com.example.chefguard.model.AlimentoEntity
import com.example.chefguard.model.AppDatabase
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.chefguard.utils.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

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

    var searchText by remember { mutableStateOf("") }
    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) }

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

            Spacer(modifier = Modifier.height(8.dp))

            val filteredAlimentos = alimentos.filter { alimento ->
                (searchText.isBlank() ||
                        alimento.nombre?.contains(searchText, ignoreCase = true) == true ||
                        alimento.proveedor?.contains(searchText, ignoreCase = true) == true ||
                        alimento.tipoAlimento?.contains(searchText, ignoreCase = true) == true) &&
                        (filtroEstado == "Todos" || alimento.estado == filtroEstado)
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

                        val colorEstado = when (alimento.estado) {
                            "Disponible" -> MaterialTheme.colorScheme.primaryContainer
                            "Agotado" -> MaterialTheme.colorScheme.errorContainer
                            "Caducado" -> MaterialTheme.colorScheme.surfaceVariant
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