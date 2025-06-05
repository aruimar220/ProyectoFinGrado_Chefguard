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
import data.local.AppDatabase
import data.local.entity.UsuarioEntity
import com.example.chefguard.utils.PreferencesManager
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


// Función para la pantalla de registro
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current // Obtener el contexto de la aplicación para acceder a la base de datos
    val db = AppDatabase.getDatabase(context) // Obtener la instancia de la base de datos
    val auth = FirebaseAuth.getInstance() // Obtener la instancia de autenticación de Firebase para registrar usuarios en Firebase


    var username by remember { mutableStateOf("") } // Variable para usuario
    var email by remember { mutableStateOf("") } // Variable para correo electrónico
    var password by remember { mutableStateOf("") } // Variable para contraseña
    var confirmPassword by remember { mutableStateOf("") } // Variable para confirmar contraseña
    var error by remember { mutableStateOf("") } // Variable para mostrar errores

    val scope = rememberCoroutineScope() // Variable para el ámbito de corrutinas

    fun isValidEmail(email: String): Boolean { // Función para validar el formato del correo electrónico
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return emailPattern.matcher(email).matches()
    }

    fun encryptPassword(password: String): String { // Función para encriptar la contraseña
        val bytes = password.toByteArray() // Convertir la contraseña a un array de bytes
        val md = java.security.MessageDigest.getInstance("SHA-256") // Obtener una instancia del algoritmo SHA-256
        val digest = md.digest(bytes) // Calcular el hash SHA-256
        return digest.fold("") { str, it -> str + "%02x".format(it) } // Convertir el hash a un formato hexadecimal
    }

    Column( // Composición de la pantalla de registro
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Centrar horizontalmente
        verticalArrangement = Arrangement.Center // Centrar verticalmente
    ) {
        Text("Registrar Cuenta", style = MaterialTheme.typography.headlineMedium) // Título de la pantalla

        Spacer(modifier = Modifier.height(20.dp)) // Espacio entre el título y los campos de entrada

        OutlinedTextField(
            value = username, // Valor del campo de usuario
            onValueChange = { username = it }, // Actualizar el valor del campo de usuario al cambiar su contenido en tiempo real
            label = { Text("Nombre Completo") }, // Etiqueta del campo de usuario
            modifier = Modifier.fillMaxWidth() // Asignar el ancho máximo disponible
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email, // Valor del campo de correo electrónico
            onValueChange = { email = it }, // Actualizar el valor del campo de correo electrónico al cambiar su contenido en tiempo real
            label = { Text("Correo electrónico") }, // Etiqueta del campo de correo electrónico
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password, // Valor del campo de contraseña
            onValueChange = { password = it }, // Actualizar el valor del campo de contraseña al cambiar su contenido en tiempo real
            label = { Text("Contraseña") }, // Etiqueta del campo de contraseña
            visualTransformation = PasswordVisualTransformation(), // Mostrar la contraseña como puntos suspensivos en lugar de caracteres reales
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmPassword, // Valor del campo de confirmación de contraseña
            onValueChange = { confirmPassword = it }, // Actualizar el valor del campo de confirmación de contraseña al cambiar su contenido en tiempo real
            label = { Text("Confirmar contraseña") }, // Etiqueta del campo de confirmación de contraseña
            visualTransformation = PasswordVisualTransformation(), // Mostrar la contraseña como puntos suspensivos en lugar de caracteres reales
            modifier = Modifier.fillMaxWidth()
        )
        // Mostrar mensaje de error si hay
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error, // Mostrar el mensaje de error si hay
                color = MaterialTheme.colorScheme.error, // Cambiar el color del texto a rojo
                style = MaterialTheme.typography.bodyMedium // Utilizar el estilo de texto adecuado
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        // Botón de registro que llama a la función de registro de Firebase
        Button(
            onClick = {
                if (password != confirmPassword) { // Verificar si las contraseñas coinciden antes de registrar el usuario
                    error = "Las contraseñas no coinciden"
                } else if (!isValidEmail(email)) { // Verificar el formato del correo electrónico antes de registrar el usuario
                    error = "El correo electrónico no es válido"
                } else if (password.length < 8) { // Verificar la longitud de la contraseña antes de registrar el usuario
                    error = "La contraseña debe tener al menos 8 caracteres"
                } else {
                    scope.launch { // Registrar el usuario en Firebase y en la base de datos local
                        auth.createUserWithEmailAndPassword(email, password) // Registrar el usuario en Firebase con el correo electrónico y la contraseña
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) { // Si el registro es exitoso en Firebase se registra en la base de datos local con el nombre de usuario y el correo electrónico
                                    scope.launch {
                                        val encryptedPassword = encryptPassword(password) // Encriptar la contraseña antes de guardarla en la base de datos local con el nombre de usuario y el correo electrónico
                                        val newUser = UsuarioEntity( // Crear un objeto UsuarioEntity con los datos del usuario
                                            nombre = username,
                                            correo = email,
                                            contrasena = encryptedPassword
                                        )
                                        db.usuarioDao().insertarUsuario(newUser)

                                        val usuarioRegistrado = db.usuarioDao().obtenerUsuarioPorCorreo(email) // Obtener el usuario registrado de la base de datos local con el correo electrónico
                                        usuarioRegistrado?.let { // Si el usuario registrado no es nulo se registra en Firebase con el nombre de usuario y el correo electrónico
                                            val firestore = Firebase.firestore // Obtener una instancia de Firestore para subir el usuario a Firestore
                                            val userMap = hashMapOf( // Crear un mapa con los datos del usuario a subir a Firestore
                                                "idLocal" to it.id,
                                                "nombre" to it.nombre,
                                                "correo" to it.correo
                                            )
                                            firestore.collection("usuarios").document(it.correo).set(userMap) // Subir el usuario a Firestore con el correo electrónico como identificador único en Firestore y los datos del usuario a subir

                                            PreferencesManager.saveUserId(context, it.id) // Guardar el ID del usuario en las preferencias compartidas
                                            navController.navigate("login")// Navegar a la pantalla de inicio de sesión
                                        }
                                    }
                                }else { // Si el registro falla en Firebase se muestra un mensaje de error
                                    error = when (val exception = task.exception) {
                                        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "El correo ya está registrado en Firebase"
                                        else -> "Error al registrar: ${exception?.localizedMessage}"
                                    }
                                }
                            }
                    }

                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse") // Texto del botón de registro
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { // Botón para navegar a la pantalla de inicio de sesión
            navController.navigate("login")
        }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}