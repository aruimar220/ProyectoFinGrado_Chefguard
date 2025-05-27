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
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun encryptPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    LaunchedEffect(Unit) {
        val savedUserId = PreferencesManager.getUserId(context)
        val savedRememberMe = PreferencesManager.getRememberMe(context)
        if (savedUserId != -1 && savedRememberMe) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        var rememberMe by remember { mutableStateOf(false) } 

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text("Mantener sesión iniciada")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (email.isEmpty()) {
                    error = "El correo electrónico no puede estar vacío"
                } else if (password.isEmpty()) {
                    error = "La contraseña no puede estar vacía"
                } else {
                    val encryptedPassword = encryptPassword(password)

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                scope.launch {
                                    val usuarioLocal = db.usuarioDao().validarUsuario(email, encryptedPassword)
                                    if (usuarioLocal != null) {
                                        PreferencesManager.saveUserId(context, usuarioLocal.id)
                                        PreferencesManager.saveRememberMe(context, rememberMe)
                                        FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(
                                            context,
                                            usuarioLocal.id
                                        )
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        FirebaseFirestore.getInstance()
                                            .collection("usuarios")
                                            .whereEqualTo("correo", email)
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                if (!documents.isEmpty) {
                                                    val doc = documents.documents[0]
                                                    val user = UsuarioEntity(
                                                        id = 0,
                                                        nombre = doc.getString("nombre") ?: "",
                                                        correo = doc.getString("correo") ?: "",
                                                        contrasena = doc.getString("contrasena") ?: ""
                                                    )
                                                    scope.launch {
                                                        val newId = db.usuarioDao().insertarUsuario(user).toInt()
                                                        PreferencesManager.saveUserId(context, newId)
                                                        PreferencesManager.saveRememberMe(context, rememberMe)
                                                        FirestoreSyncHelper.sincronizarAlimentosDesdeFirestore(context, newId)
                                                        navController.navigate("home") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                    }
                                                } else {
                                                    error = "No se encontró el usuario en Firestore"
                                                }
                                            }
                                            .addOnFailureListener {
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
            enabled = email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = {
            navController.navigate("recover")
        }) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}