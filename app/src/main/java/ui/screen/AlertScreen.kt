package com.example.chefguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chefguard.model.AlimentoEntity
import com.example.chefguard.model.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AlertScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) }
    var filtroAlerta by remember { mutableStateOf("Todos") }

    val filtros = listOf("Todos", "Caducados", "Por caducar pronto")

    LaunchedEffect(Unit) {
        // Cargar todos los alimentos desde la base de datos
        alimentos = db.alimentoDao().obtenerTodosLosAlimentos()
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
                text = "Alertas",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                DropdownMenuExample(
                    items = filtros,
                    selectedValue = filtroAlerta,
                    onItemSelected = { nuevoFiltro -> filtroAlerta = nuevoFiltro }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredAlimentos = alimentos.filter { alimento ->
                val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
                val hoy = LocalDate.now()

                when (filtroAlerta) {
                    "Todos" -> true
                    "Caducados" -> fechaCaducidad.isBefore(hoy)
                    "Por caducar pronto" -> fechaCaducidad.isAfter(hoy) && fechaCaducidad.isBefore(hoy.plusDays(5))
                    else -> false
                }
            }

            if (filteredAlimentos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No hay alertas pendientes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn {
                    items(filteredAlimentos.size) { index ->
                        val alimento = filteredAlimentos[index]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    navController.navigate("item_details/${alimento.id}")
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "Nombre: ${alimento.nombre}")
                                Text(text = "Fecha de caducidad: ${alimento.fechaCaducidad}")
                            }
                        }
                    }
                }
            }
        }
    }
}