package com.example.chefguard.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chefguard.utils.PreferencesManager
import data.local.AppDatabase
import data.local.entity.UsuarioEntity
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(navController: NavController) { // Composición de la pantalla de edición de perfil de usuario con su contenido y botones correspondientes
    val context = LocalContext.current // Obtiene el contexto de la aplicación para acceder a la base de datos y a las preferencias compartidas
    val db = AppDatabase.getDatabase(context) // Obtiene la instancia de la base de datos utilizando el contexto proporcionado

    val userId = PreferencesManager.getUserId(context) // Obtiene el ID del usuario de las preferencias compartidas
    if (userId == -1) { // Si el ID del usuario es -1, redirige al usuario a la pantalla de inicio de sesión
        LaunchedEffect(Unit) { // Ejecuta una acción una vez en el ciclo de vida de la composición
            navController.navigate("login") { // Navega a la pantalla de inicio de sesión y elimina la pantalla actual de la pila de navegación
                popUpTo("edit_profile") { inclusive = true } // Elimina la pantalla actual de la pila de navegación
            }
        }
        return
    }

    var usuario by remember { mutableStateOf<UsuarioEntity?>(null) } // Variable para almacenar los datos del usuario obtenido de la base de datos
    LaunchedEffect(Unit) {
        usuario = db.usuarioDao().obtenerUsuarioPorId(userId) // Obtiene el usuario de la base de datos utilizando su ID
    }

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") } // Variable para almacenar el nombre del usuario obtenido de la base de datos
    val correoActual = usuario?.correo ?: "" // Variable para almacenar el correo electrónico del usuario obtenido de la base de datos
    var contrasena by remember { mutableStateOf("") } // Variable para almacenar la contraseña del usuario
    var nombreError by remember { mutableStateOf(false) } // Variable para controlar si hay un error en el campo de nombre

    val scope = rememberCoroutineScope()

    Column( // Composición de la columna para mostrar los campos de edición de perfil de usuario en la pantalla de edición de perfil
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text( // Título de la pantalla de edición de perfil con su estilo y botones correspondientes
            text = "Editar Perfil", // Texto del título de la pantalla de edición de perfil
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField( // Campo de entrada para el nombre del usuario con su acción correspondiente y validación correspondiente al campo de nombre
            value = nombre, // Valor del campo de entrada del nombre del usuario
            onValueChange = {
                nombre = it // Actualiza el valor del campo de entrada del nombre del usuario al cambiar su contenido en tiempo real
                if (it.isNotBlank()) {
                    nombreError = false // Si el campo de nombre no está vacío, se desactiva el error
                }
            },
            label = { Text("Nombre") }, // Etiqueta del campo de entrada del nombre del usuario con su estilo
            isError = nombreError, // Indica si el campo de nombre tiene un error
            modifier = Modifier.fillMaxWidth()
        )
        if (nombreError) { // Muestra un mensaje de error si el campo de nombre tiene un error
            Text( // Mensaje de error con su estilo y posición correspondiente
                text = "El nombre no puede estar vacío",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField( // Campo de entrada para el correo electrónico del usuario con su acción correspondiente
            value = correoActual, // Valor del campo de entrada del correo electrónico del usuario
            onValueChange = {}, // No se puede editar el correo electrónico del usuario
            label = { Text("Correo Electrónico") }, // Etiqueta del campo de entrada del correo electrónico del usuario con su estilo
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField( // Campo de entrada para la contraseña del usuario con su acción correspondiente
            value = contrasena, // Valor del campo de entrada de la contraseña del usuario
            onValueChange = { contrasena = it }, // Actualiza el valor del campo de entrada de la contraseña del usuario al cambiar su contenido en tiempo real
            label = { Text("Nueva Contraseña") }, // Etiqueta del campo de entrada de la contraseña del usuario con su estilo
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button( // Botón de guardar cambios con su acción correspondiente y validación correspondiente al campo de nombre del usuario
            onClick = { // Actualiza los datos del usuario en la base de datos con los valores ingresados en los campos de entrada
                if (nombre.isBlank()) { // Si el campo de nombre está vacío, se activa el error correspondiente al campo de nombre
                    nombreError = true // Activa el error correspondiente al campo de nombre del usuario
                    return@Button
                }
                val usuarioActualizado = UsuarioEntity( // Crea un nuevo objeto UsuarioEntity con los valores actualizados del usuario para su actualización en la base de datos
                    id = userId, // Utiliza el ID del usuario obtenido de las preferencias compartidas como identificador único del usuario
                    nombre = nombre, // Actualiza el nombre del usuario con el valor ingresado en el campo de entrada del nombre
                    correo = correoActual, // Utiliza el correo electrónico actual del usuario como el correo electrónico del usuario actualizado
                    contrasena = contrasena.ifBlank { usuario?.contrasena ?: "" } // Actualiza la contraseña del usuario con el valor ingresado en el campo de entrada de la contraseña o con el valor actual si el campo de entrada está vacío
                )

                scope.launch { // Lanza una corrutina para actualizar los datos del usuario en la base de datos utilizando el ViewModel
                    db.usuarioDao().actualizarUsuario(usuarioActualizado) // Actualiza los datos del usuario en la base de datos utilizando el ViewModel
                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show() // Muestra un mensaje de éxito al actualizar los datos del usuario en la base de datos
                    navController.popBackStack()
                }
            },
            enabled = nombre.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Guardar Cambios") // Texto del botón de guardar cambios con su estilo y acción correspondiente al hacer clic en él
        }
    }
}
