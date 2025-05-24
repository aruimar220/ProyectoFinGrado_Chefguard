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
import data.local.entity.AlimentoEntity
import data.local.AppDatabase
import com.example.chefguard.scheduleDailyNotification
import com.example.chefguard.utils.PreferencesManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AlertScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val userId = PreferencesManager.getUserId(context)
    if (userId == -1) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("alerts") { inclusive = true }
            }
        }
        return
    }

    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) }
    var filtroAlerta by remember { mutableStateOf("Todos") }

    val filtros = listOf("Todos", "Caducados", "Por caducar pronto", "Disponibles")

    LaunchedEffect(Unit) {
        alimentos = db.alimentoDao().obtenerAlimentosPorUsuario(userId)
    }

    val filteredAlimentos = alimentos.filter { alimento ->
        try {
            val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
            val hoy = LocalDate.now()

            when (filtroAlerta) {
                "Todos" -> true
                "Caducados" -> fechaCaducidad.isBefore(hoy)
                "Por caducar pronto" -> fechaCaducidad.isAfter(hoy) && fechaCaducidad.isBefore(hoy.plusDays(2))
                "Disponibles" -> fechaCaducidad.isAfter(hoy.plusDays(5))
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }.sortedBy {
        try {
            LocalDate.parse(it.fechaCaducidad, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            LocalDate.MAX
        }
    }

    if (filteredAlimentos.any {
            val fechaCaducidad = LocalDate.parse(it.fechaCaducidad, DateTimeFormatter.ISO_DATE)
            val hoy = LocalDate.now()
            fechaCaducidad.isAfter(hoy) && fechaCaducidad.isBefore(hoy.plusDays(2))
        }) {
        scheduleDailyNotification(context)
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

            if (filteredAlimentos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
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

                        val hoy = LocalDate.now()
                        val fechaCaducidad = runCatching {
                            LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
                        }.getOrNull()

                        val color = when {
                            fechaCaducidad == null -> MaterialTheme.colorScheme.surface
                            fechaCaducidad.isBefore(hoy) -> MaterialTheme.colorScheme.errorContainer
                            fechaCaducidad.isBefore(hoy.plusDays(5)) -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val diasTexto = when {
                            fechaCaducidad == null -> "Fecha inválida"
                            fechaCaducidad.isBefore(hoy) -> "⚠️ Caducado hace ${java.time.temporal.ChronoUnit.DAYS.between(fechaCaducidad, hoy)} días"
                            fechaCaducidad.isEqual(hoy) -> "⚠️ Caduca hoy"
                            else -> "⏳ Caduca en ${java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaCaducidad)} días"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    navController.navigate("item_details/${alimento.id}")
                                },
                            colors = CardDefaults.cardColors(containerColor = color),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Nombre: ${alimento.nombre}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Cantidad: ${alimento.cantidad}")
                                Text(text = "Proveedor: ${alimento.proveedor}")
                                Text(text = "Estado: ${alimento.estado ?: "No especificado"}")
                                Text(text = "Fecha de caducidad: ${alimento.fechaCaducidad}")
                                Text(
                                    text = diasTexto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}