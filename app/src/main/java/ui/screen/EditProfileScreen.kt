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
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val userId = PreferencesManager.getUserId(context)
    if (userId == -1) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("edit_profile") { inclusive = true }
            }
        }
        return
    }

    var usuario by remember { mutableStateOf<UsuarioEntity?>(null) }
    LaunchedEffect(Unit) {
        usuario = db.usuarioDao().obtenerUsuarioPorId(userId)
    }

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    val correoActual = usuario?.correo ?: ""
    var contrasena by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Editar Perfil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                if (it.isNotBlank()) {
                    nombreError = false
                }
            },
            label = { Text("Nombre") },
            isError = nombreError,
            modifier = Modifier.fillMaxWidth()
        )
        if (nombreError) {
            Text(
                text = "El nombre no puede estar vacío",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correoActual,
            onValueChange = {},
            label = { Text("Correo Electrónico") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Nueva Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nombre.isBlank()) {
                    nombreError = true
                    return@Button
                }
                val usuarioActualizado = UsuarioEntity(
                    id = userId,
                    nombre = nombre,
                    correo = correoActual,
                    contrasena = contrasena.ifBlank { usuario?.contrasena ?: "" }
                )

                scope.launch {
                    db.usuarioDao().actualizarUsuario(usuarioActualizado)
                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            enabled = nombre.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Guardar Cambios")
        }
    }
}
