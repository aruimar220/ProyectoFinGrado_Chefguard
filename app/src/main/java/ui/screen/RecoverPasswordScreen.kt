package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chefguard.model.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun RecoverPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Recuperar Contraseña", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nueva Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isEmpty()) {
                    error = "El correo electrónico no puede estar vacío"
                } else if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    error = "La nueva contraseña y su confirmación son obligatorias"
                } else if (newPassword.length < 8) {
                    error = "La contraseña debe tener al menos 8 caracteres"
                } else if (newPassword != confirmPassword) {
                    error = "Las contraseñas no coinciden"
                } else {
                    scope.launch {
                        val user = db.usuarioDao().obtenerUsuarioPorCorreo(email)
                        if (user != null) {
                            // Actualizar la contraseña en la base de datos
                            val encryptedPassword = encryptPassword(newPassword)
                            db.usuarioDao().actualizarContraseña(user.id, encryptedPassword)
                            navController.popBackStack()
                        } else {
                            error = "El correo electrónico no está registrado"
                        }
                    }
                }
            },
            enabled = email.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restablecer Contraseña")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Volver al inicio de sesión")
        }
    }
}

// Función para cifrar la contraseña (SHA-256)
fun encryptPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}