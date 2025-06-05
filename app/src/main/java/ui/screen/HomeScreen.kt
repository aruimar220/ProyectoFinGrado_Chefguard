package com.example.chefguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import data.local.AppDatabase
import com.example.chefguard.utils.PreferencesManager
import com.tuapp.data.remote.FirestoreSyncHelper
 // Función para la pantalla principal de la aplicación
@Composable
fun HomeScreen(navController: NavController) { // Composición de la pantalla principal de la aplicación
    val context = LocalContext.current // Obtener el contexto de la aplicación para acceder a la base de datos
    val db = AppDatabase.getDatabase(context) // Obtener la instancia de la base de datos

    val userId = PreferencesManager.getUserId(context) // Obtener el ID del usuario de las preferencias compartidas
    if (userId == -1) { // Si el ID del usuario es -1, redirige al usuario a la pantalla de inicio de sesión
        LaunchedEffect(Unit) { // Ejecutar una acción una vez en el ciclo de vida de la composición
            navController.navigate("login") { // Navegar a la pantalla de inicio de sesión y eliminar la pantalla actual de la pila de navegación
                popUpTo("home") { inclusive = true } // Eliminar la pantalla actual de la pila de navegación
            }
        }
        return // Salir de la función si el usuario no está autenticado
    }

    LaunchedEffect(Unit) {
        FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(context) // Sincronizar los alimentos desde Firestore cuando se inicie la aplicación
    }

    var nombreUsuario by remember { mutableStateOf("Invitado") } // Variable para almacenar el nombre del usuario
    LaunchedEffect(Unit) { // Ejecutar una acción una vez en el ciclo de vida de la composición
        val usuario = db.usuarioDao().obtenerUsuarioPorId(userId) // Obtener el usuario de la base de datos utilizando su ID
        nombreUsuario = usuario?.nombre ?: "Invitado" // Actualizar el nombre del usuario si se encuentra en la base de datos
    }

    Column( // Composición de la pantalla principal de la aplicación
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text( // Texto de bienvenida a la aplicación
            text = "Bienvenido a ChefGuard!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text( // Texto de bienvenida al usuario
            text = "Hola, $nombreUsuario", // Mostrar el nombre del usuario obtenido anteriormente
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card( // Composición de un card para cada opción de la pantalla principal
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("inventory") }, // Navegar a la pantalla de inventario cuando se hace clic en el card
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Column(modifier = Modifier.padding(16.dp)) { // Composición de la columna para el contenido del card
                Text(text = "Inventario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White) // Texto de la opción de inventario
                Text(text = "Revisa y gestiona tus productos", fontSize = 16.sp, color = Color.White) // Texto de la descripción de la opción de inventario
            }
        }

        Card( // Composición de un card para cada opción de la pantalla principal
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("alerts") }, // Navegar a la pantalla de alertas cuando se hace clic en el card
            shape = RoundedCornerShape(12.dp),//
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800))
        ) {
            Column(modifier = Modifier.padding(16.dp)) { // Composición de la columna para el contenido del card
                Text(text = "Alertas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White) // Texto de la opción de alertas
                Text(text = "Recibe notificaciones importantes", fontSize = 16.sp, color = Color.White) // Texto de la descripción de la opción de alertas
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("profile") }, // Navegar a la pantalla de perfil cuando se hace clic en el card
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
        ) {
            Column(modifier = Modifier.padding(16.dp)) { // Composición de la columna para el contenido del card
                Text(text = "Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White) // Texto de la opción de perfil
                Text(text = "Administra tu información personal", fontSize = 16.sp, color = Color.White) // Texto de la descripción de la opción de perfil
            }
        }
    }
}