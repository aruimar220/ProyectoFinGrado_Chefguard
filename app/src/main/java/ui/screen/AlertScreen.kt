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
fun AlertScreen(navController: NavController) { // Composición de la pantalla de alertas con su contenido y botones correspondientes
    val context = LocalContext.current // Obtiene el contexto de la aplicación para acceder a la base de datos y a las preferencias compartidas
    val db = AppDatabase.getDatabase(context) // Obtiene la instancia de la base de datos utilizando el contexto proporcionado

    val userId = PreferencesManager.getUserId(context) // Obtiene el ID del usuario de las preferencias compartidas y navega a la pantalla de inicio de sesión si no hay un usuario
    if (userId == -1) { // Si el ID del usuario es -1, redirige al usuario a la pantalla de inicio de sesión
        LaunchedEffect(Unit) { // Ejecuta una acción una vez en el ciclo de vida de la composición
            navController.navigate("login") { // Navega a la pantalla de inicio de sesión y elimina la pantalla actual de la pila de navegación
                popUpTo("alerts") { inclusive = true } // Elimina la pantalla actual de la pila de navegación
            }
        }
        return
    }

    var alimentos by remember { mutableStateOf<List<AlimentoEntity>>(emptyList()) } // Variable para almacenar la lista de alimentos del usuario
    var filtroAlerta by remember { mutableStateOf("Todos") } // Variable para almacenar el filtro de alerta seleccionado por el usuario

    val filtros = listOf("Todos", "Caducados", "Por caducar pronto", "Disponibles") // Lista de filtros de alerta disponibles para el usuario

    LaunchedEffect(Unit) { // Ejecuta una acción una vez en el ciclo de vida de la composición
        alimentos = db.alimentoDao().obtenerAlimentosPorUsuario(userId)
    }

    val filteredAlimentos = alimentos.filter { alimento -> // Filtra la lista de alimentos según el filtro de alerta seleccionado por el usuario y ordena los alimentos por fecha de caducidad
        try {
            val fechaCaducidad = LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE) // Intenta analizar la fecha de caducidad del alimento utilizando el formato ISO
            val hoy = LocalDate.now() // Obtiene la fecha actual en el formato ISO

            when (filtroAlerta) { // Comprueba el filtro de alerta seleccionado por el usuario y devuelve una lista de alimentos que cumplen con el filtro
                "Todos" -> true // Si el filtro es "Todos", devuelve true para incluir todos los alimentos
                "Caducados" -> fechaCaducidad.isBefore(hoy) // Si el filtro es "Caducados", devuelve true si la fecha de caducidad del alimento es anterior a la fecha actual
                "Por caducar pronto" -> fechaCaducidad.isAfter(hoy) && fechaCaducidad.isBefore(hoy.plusDays(2)) // Si el filtro es "Por caducar pronto", devuelve true si la fecha de caducidad del alimento está entre la fecha actual y dos días después
                "Disponibles" -> fechaCaducidad.isAfter(hoy.plusDays(5))// Si el filtro es "Disponibles", devuelve true si la fecha de caducidad del alimento está después de dos días después de la fecha actual
                else -> false // Si el filtro no es válido, devuelve false para excluir el alimento
            }
        } catch (e: Exception) { // Si hay un error al analizar la fecha de caducidad del alimento, devuelve false para excluir el alimento y continuar con el siguiente alimento
            false
        }
    }.sortedBy { // Ordena la lista de alimentos por fecha de caducidad de menor a mayor utilizando el formato ISO
        try {
            LocalDate.parse(it.fechaCaducidad, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            LocalDate.MAX
        }
    }

    if (filteredAlimentos.any { // Si hay al menos un alimento que cumpla con el filtro de alerta "Por caducar pronto", programa una notificación diaria para el alimento
            val fechaCaducidad = LocalDate.parse(it.fechaCaducidad, DateTimeFormatter.ISO_DATE) // Intenta analizar la fecha de caducidad del alimento utilizando el formato ISO
            val hoy = LocalDate.now() // Obtiene la fecha actual en el formato ISO y comprueba si la fecha de caducidad del alimento está entre la fecha actual y dos días después
            fechaCaducidad.isAfter(hoy) && fechaCaducidad.isBefore(hoy.plusDays(2)) // Si el filtro es Por caducar pronto, devuelve true si la fecha de caducidad del alimento está entre la fecha actual y dos días después
        }) {
        scheduleDailyNotification(context) // Programa una notificación diaria para el alimento utilizando el contexto proporcionado
    }

    Scaffold( // Composición del Scaffold de la pantalla de alertas con su contenido y botones correspondientes
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton( // Botón flotante para agregar un nuevo alimento con su acción correspondiente y navega a la pantalla de añadir alimento después de hacer clic en el botón flotante
                onClick = { // Navega a la pantalla de añadir alimento y elimina la pantalla actual de la pila de navegación cuando se hace clic en el botón flotante para agregar un nuevo alimento
                    navController.navigate("add_items") // Navega a la pantalla de añadir alimento y elimina la pantalla actual de la pila de navegación
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall) // Texto del botón flotante con su estilo y texto correspondiente para agregar un nuevo alimento
            }
        },
        floatingActionButtonPosition = FabPosition.End // Posición del botón flotante en la pantalla de alertas
    ) { innerPadding ->
        Column( // Composición de la columna para mostrar el contenido de la pantalla de alertas en la pantalla de alertas
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) { // Título de la pantalla de alertas con su estilo y botones correspondientes
            Text( // Texto del título de la pantalla de alertas con su estilo y texto correspondiente para mostrar el título de la pantalla de alertas
                text = "Alertas", // Texto del título de la pantalla de alertas con su estilo y texto correspondiente para mostrar el título de la pantalla de alertas
                style = MaterialTheme.typography.headlineLarge, // Estilo del título de la pantalla de alertas con su estilo correspondiente
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row( // Composición de la fila para mostrar el filtro de alerta con su acción correspondiente y botones correspondientes
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                DropdownMenuExample( // Composición del menú desplegable para el filtro de alerta con su acción correspondiente y botones correspondientes
                    items = filtros,
                    selectedValue = filtroAlerta,
                    onItemSelected = { nuevoFiltro -> filtroAlerta = nuevoFiltro }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAlimentos.isEmpty()) { // Si no hay alimentos que cumplan con el filtro de alerta seleccionado, muestra un mensaje indicando que no hay alertas pendientes
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text( // Texto del mensaje indicando que no hay alertas pendientes con su estilo y texto correspondiente para mostrar el mensaje indicando que no hay alertas pendientes
                        text = "No hay alertas pendientes", // Texto del mensaje indicando que no hay alertas pendientes con su estilo y texto correspondiente para mostrar el mensaje indicando que no hay alertas pendientes
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn { // Composición de la lista de alimentos que cumplan con el filtro de alerta seleccionado con su acción correspondiente y botones correspondientes
                    items(filteredAlimentos.size) { index ->
                        val alimento = filteredAlimentos[index] // Obtiene el alimento en la posición actual de la lista de alimentos que cumplan con el filtro de alerta seleccionado

                        val hoy = LocalDate.now() // Obtiene la fecha actual en el formato ISO y calcula la diferencia en días entre la fecha actual y la fecha de caducidad del alimento
                        val fechaCaducidad = runCatching { // Intenta analizar la fecha de caducidad del alimento utilizando el formato ISO y devuelve null si hay un error
                            LocalDate.parse(alimento.fechaCaducidad, DateTimeFormatter.ISO_DATE)
                        }.getOrNull()

                        val color = when { // Comprueba la fecha de caducidad del alimento y devuelve un color correspondiente para mostrar en la lista de alimentos
                            fechaCaducidad == null -> MaterialTheme.colorScheme.surface
                            fechaCaducidad.isBefore(hoy) -> MaterialTheme.colorScheme.errorContainer
                            fechaCaducidad.isBefore(hoy.plusDays(5)) -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val diasTexto = when { // Comprueba la fecha de caducidad del alimento y devuelve un texto correspondiente para mostrar en la lista de alimentos
                            fechaCaducidad == null -> "Fecha inválida" // Si la fecha de caducidad del alimento es nula, devuelve un texto indicando que la fecha de caducidad del alimento es inválida
                            fechaCaducidad.isBefore(hoy) -> "⚠️ Caducado hace ${java.time.temporal.ChronoUnit.DAYS.between(fechaCaducidad, hoy)} días" // Si la fecha de caducidad del alimento es anterior a la fecha actual, devuelve un texto indicando que el alimento está caducado
                            fechaCaducidad.isEqual(hoy) -> "⚠️ Caduca hoy" // Si la fecha de caducidad del alimento es igual a la fecha actual, devuelve un texto indicando que el alimento está caducando hoy
                            else -> "⏳ Caduca en ${java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaCaducidad)} días"
                        }

                        Card( // Composición de la tarjeta para mostrar el alimento en la lista de alimentos con su acción correspondiente y botones correspondientes
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .clickable {
                                    navController.navigate("item_details/${alimento.id}") // Navega a la pantalla de detalles del alimento y pasa el ID del alimento como argumento
                                },
                            colors = CardDefaults.cardColors(containerColor = color),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) { // Contenido de la tarjeta para mostrar el alimento en la lista de alimentos con su acción correspondiente y botones correspondientes
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