package com.example.chefguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
// Función para la pantalla de recuperación de contraseña
@Composable
fun RecoverPasswordScreen(navController: NavController) {
    val context = LocalContext.current // Obtener el contexto de la aplicación para acceder a la base de datos de Firebase
    var email by remember { mutableStateOf("") } // Variable para el correo electrónico del usuario
    var message by remember { mutableStateOf("") } // Variable para mostrar mensajes de error o éxito
    var isSuccess by remember { mutableStateOf<Boolean?>(null) } // Variable para indicar si la operación de recuperación de contraseña fue exitosa

    Column( // Composición de la pantalla de recuperación de contraseña
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center, // Centrar verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente
    ) {
        Text(
            text = "Recuperar Contraseña", // Título de la pantalla
            style = MaterialTheme.typography.headlineMedium // Utilizar el estilo de texto adecuado
        )

        Spacer(modifier = Modifier.height(16.dp)) // Espacio entre el título y los campos de entrada

        OutlinedTextField(
            value = email, // Valor del campo de correo electrónico
            onValueChange = { email = it }, // Actualizar el valor del campo de correo electrónico al cambiar su contenido en tiempo real
            label = { Text("Correo electrónico") }, // Etiqueta del campo de correo electrónico
            singleLine = true, // Permitir solo una línea de texto en el campo de entrada
            modifier = Modifier.fillMaxWidth() // Asignar el ancho máximo disponible
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { // Acción al hacer clic en el botón de recuperación de contraseña con validación del correo electrónico antes de enviar el correo de recuperación
                if (email.isNotBlank()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email) // Enviar el correo de recuperación de contraseña a través de Firebase
                        .addOnCompleteListener { task -> // Manejar la respuesta del servidor de Firebase al enviar el correo de recuperación
                            if (task.isSuccessful) { // Si el correo de recuperación es enviado con éxito se muestra un mensaje de éxito y se limpia el campo de correo electrónico
                                message = "Te hemos enviado un correo para restablecer tu contraseña." // Mensaje de éxito
                                isSuccess = true // Indicar que la operación fue exitosa
                            } else {
                                message = task.exception?.localizedMessage ?: "Ocurrió un error." // Si el correo de recuperación falla se muestra un mensaje de error
                                isSuccess = false // Indicar que la operación falló
                            }
                        }
                } else {
                    message = "Introduce tu correo electrónico." // Si el campo de correo electrónico está vacío se muestra un mensaje de error
                    isSuccess = false // Indicar que la operación falló
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar correo de recuperación") // Texto del botón de recuperación de contraseña
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) { // Mostrar el mensaje de error o éxito si hay alguno en la variable message y el color correspondiente
            Text(
                text = message,
                color = when (isSuccess) {
                    true -> MaterialTheme.colorScheme.primary // Si la operación fue exitosa se muestra en verde
                    false -> MaterialTheme.colorScheme.error // Si la operación falló se muestra en rojo
                    null -> MaterialTheme.colorScheme.onBackground // Si no se ha realizado ninguna operación se muestra en blanco
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Volver al inicio de sesión") // Texto del botón de volver al inicio de sesión
        }
    }
}