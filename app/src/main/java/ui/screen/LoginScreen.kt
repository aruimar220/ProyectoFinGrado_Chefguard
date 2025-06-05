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
import com.example.chefguard.utils.PreferencesManager
import kotlinx.coroutines.launch
import java.security.MessageDigest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import data.local.entity.UsuarioEntity
import com.tuapp.data.remote.FirestoreSyncHelper

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current // contexto de Android y poder acceder a Room y SharedPreferences
    val db = AppDatabase.getDatabase(context) //Instancia de la base de datos Room
    val auth = FirebaseAuth.getInstance() //Instancia de FirebaseAuth para autenticar usuarios

    // Variables de estado para el correo electrónico y la contraseña, tambien en caso de error.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // permite lanzar corutinas dentro de compose
    val scope = rememberCoroutineScope()

    // Función para encriptar la contraseña
    fun encryptPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256") // algoritmo de encriptación hash SHA-256
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) } // convierte el hash en una cadena hexadecimal
    }
    // Este bloque busca en shared preferences si el usuario ya ha iniciado sesión anteriormente y si esta activado el checkbox de mantener sesión
    LaunchedEffect(Unit) {
        val savedUserId = PreferencesManager.getUserId(context)
        val savedRememberMe = PreferencesManager.getRememberMe(context)
        if (savedUserId != -1 && savedRememberMe) {
            navController.navigate("home") { // si lo encuentra te lleva a la pantalla de inicio
                popUpTo("login") { inclusive = true }
            }
        }
    }
    // Estructura principal de la pantalla, contiene un formulario para iniciar sesión.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente
        verticalArrangement = Arrangement.Center // Centra verticalmente
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium) // Titulo de la pantalla

        Spacer(modifier = Modifier.height(20.dp)) // Espacio entre el titulo y el formulario

        // Formulario de correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it }, // actualiza el valor del correo electrónico cuando cambia el texto
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp)) // Espacio entre el correo electrónico y la contraseña
        // Formulario de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it }, // actualiza el valor de la contraseña cuando cambia el texto
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(), // oculta la contraseña
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        var rememberMe by remember { mutableStateOf(false) }  // variable de estado para el checkbox de mantener sesión

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth() // Centra horizontalmente el checkbox
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text("Mantener sesión iniciada") // Texto del checkbox
        }
         // Muestra un mensaje de error si hay uno
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        // Botón de inicio de sesión
        Button(
            onClick = {
                if (email.isEmpty()) { // si el correo electrónico esta vacio muestra un mensaje de error
                    error = "El correo electrónico no puede estar vacío"
                } else if (password.isEmpty()) { // si la contraseña esta vacia muestra un mensaje de error
                    error = "La contraseña no puede estar vacía"
                } else { // si no hay errores se procede a iniciar sesion
                    val encryptedPassword = encryptPassword(password)
                    // Iniciar sesión con Firebase
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                scope.launch {
                                    val usuarioLocal = db.usuarioDao().validarUsuario(email, encryptedPassword) // valida si el usuario existe en la base de datos
                                    if (usuarioLocal != null) { // si existe lo guarda en shared preferences y te lleva a la pantalla de inicio
                                        PreferencesManager.saveUserId(context, usuarioLocal.id)
                                        PreferencesManager.saveRememberMe(context, rememberMe)
                                        db.usuarioDao().actualizarContraseña(usuarioLocal.id, encryptedPassword)
                                        FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(context)
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else { // si no existe lo recupera de Firestore y lo guarda en la base de datos local
                                        FirebaseFirestore.getInstance()
                                            .collection("usuarios")
                                            .whereEqualTo("correo", email)
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                if (!documents.isEmpty) { // si existe lo guarda en la base de datos local
                                                    val doc = documents.documents[0]
                                                    val user = UsuarioEntity(
                                                        id = 0,
                                                        nombre = doc.getString("nombre") ?: "",
                                                        correo = doc.getString("correo") ?: "",
                                                        contrasena = doc.getString("contrasena") ?: ""
                                                    )
                                                    scope.launch { // lanza una corutina para insertar el usuario en la base de datos local
                                                        val newId = db.usuarioDao().insertarUsuario(user).toInt()
                                                        PreferencesManager.saveUserId(context, newId)
                                                        PreferencesManager.saveRememberMe(context, rememberMe)
                                                        FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(context)
                                                        navController.navigate("home") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                    }
                                                } else { // si no existe muestra un mensaje de error
                                                    error = "No se encontró el usuario en Firestore"
                                                }
                                            }
                                            .addOnFailureListener { // si hay un error muestra un mensaje de error
                                                error = "Error al recuperar usuario: ${it.localizedMessage}"
                                            }
                                    }
                                }
                            } else {
                                error = "Credenciales incorrectas"
                            }
                        }
                }
            },
            enabled = email.isNotEmpty() && password.isNotEmpty(), // solo habilita el boton si los campos no estan vacios
            modifier = Modifier.fillMaxWidth() // Centra horizontalmente el boton
        ) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { // si has olvidado tu contraseña te lleva a la pantalla de recuperar contraseña
            navController.navigate("recover") // al pulsar te lleva a la pantalla de recuperar contraseña
        }) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = { // si no tienes cuenta te lleva a la pantalla de registro
            navController.navigate("register") // al pulsar te lleva a la pantalla de registro
        }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}