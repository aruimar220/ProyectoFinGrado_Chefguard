import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import data.local.AppDatabase
import data.local.entity.UsuarioEntity
import com.example.chefguard.utils.PreferencesManager
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.text.input.PasswordVisualTransformation


@Composable
fun ProfileScreen(navController: NavController) { // Composición de la pantalla de perfil de usuario
    val context = LocalContext.current // Obtener el contexto de la aplicación para acceder a la base de datos
    val scope = rememberCoroutineScope() // Crear un alcance de corrutina para lanzar corrutinas desde la composición
    val db = AppDatabase.getDatabase(context) // Obtener la instancia de la base de datos utilizando el contexto proporcionado

    val viewModel: ProfileViewModel = viewModel( // Crear una instancia del ViewModel para la pantalla de perfil de usuario
        factory = ProfileViewModel.provideFactory(db) // Proporcionar la base de datos como dependencia
    )

    var mostrarDialogoBorrarCuenta by remember { mutableStateOf(false) } // Variable para controlar la visibilidad del diálogo de confirmación de eliminación de cuenta

    val userId = PreferencesManager.getUserId(context) // Obtener el ID del usuario de las preferencias compartidas
    if (userId == -1) { // Si el ID del usuario es -1, redirige al usuario a la pantalla de inicio de sesión
        LaunchedEffect(Unit) {
            navController.navigate("login") { // Navegar a la pantalla de inicio de sesión y eliminar la pantalla actual de la pila de navegación
                popUpTo("profile") { inclusive = true } // Eliminar la pantalla actual de la pila de navegación
            }
        }
        return // Salir de la función si el usuario no está autenticado
    }

    var usuario by remember { mutableStateOf<UsuarioEntity?>(null) } // Variable para almacenar los datos del usuario obtenido de la base de datos
    LaunchedEffect(Unit) {
        usuario = db.usuarioDao().obtenerUsuarioPorId(userId) // Obtener el usuario de la base de datos utilizando su ID
    }

    Column( // Composición de la pantalla de perfil de usuario con su contenido y botones correspondientes
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil", // Título de la pantalla de perfil
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (usuario != null) { // Mostrar los datos del usuario si se encuentran en la base de datos y si no, mostrar un mensaje de carga de datos
            Column( // Composición de la columna para mostrar los datos del usuario en la pantalla de perfil
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Nombre: ${usuario?.nombre}", fontSize = 18.sp) // Mostrar el nombre del usuario
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Correo: ${usuario?.correo}", fontSize = 18.sp) // Mostrar el correo del usuario
            }
        } else {
            Text(text = "Cargando datos...", fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp)) // Mostrar un mensaje de carga de datos si no se encuentran los datos del usuario en la base de datos
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { // Navegar a la pantalla de edición de perfil cuando se hace clic en el botón de edición de perfil
                navController.navigate("edit_profile")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Editar Perfil") // Texto del botón de edición de perfil
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton( // Botón de confirmación de eliminación de cuenta con su acción correspondiente
            onClick = { mostrarDialogoBorrarCuenta = true }, // Mostrar el diálogo de confirmación de eliminación de cuenta al hacer clic en el botón
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Borrar Cuenta") // Texto del botón de confirmación de eliminación de cuenta
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { // Cierra sesión al hacer clic en el botón de cerrar sesión y eliminar el ID del usuario de las preferencias compartidas
                PreferencesManager.saveUserId(context, -1) // Elimina el ID del usuario de las preferencias compartidas
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Cerrar Sesión") // Texto del botón de cerrar sesión
        }

        if (mostrarDialogoBorrarCuenta) {// Mostrar el diálogo de confirmación de eliminación de cuenta si la variable mostrarDialogoBorrarCuenta es true
            var password by remember { mutableStateOf("") } // Variable para almacenar la contraseña ingresada por el usuario

            AlertDialog( // Composición del diálogo de confirmación de eliminación de cuenta con sus botones correspondientes
                onDismissRequest = { mostrarDialogoBorrarCuenta = false }, // Cerrar el diálogo al hacer clic fuera de él o al tocar el botón de cancelar
                title = { Text(text = "Confirmar Eliminación") }, // Título del diálogo de confirmación de eliminación de cuenta
                text = { // Contenido del diálogo de confirmación de eliminación de cuenta con un campo de entrada para la contraseña
                    Column {
                        Text(text = "Introduce tu contraseña para confirmar la eliminación de tu cuenta:") // Texto del diálogo de confirmación de eliminación de cuenta
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField( // Campo de entrada para la contraseña del usuario con su acción correspondiente
                            value = password, // Valor del campo de entrada de contraseña
                            onValueChange = { password = it }, // Actualizar el valor del campo de entrada de contraseña al cambiar su contenido en tiempo real
                            label = { Text("Contraseña") }, // Etiqueta del campo de entrada de contraseña
                            singleLine = true, // Permitir solo una línea de texto en el campo de entrada de contraseña
                            visualTransformation = PasswordVisualTransformation() // Mostrar los caracteres de la contraseña como asteriscos
                        )
                    }
                },
                confirmButton = { // Botón de confirmación de eliminación de cuenta con su acción correspondiente
                    TextButton( // Composición del botón de confirmación de eliminación de cuenta
                        onClick = { // Elimina la cuenta de Firebase con la contraseña ingresada por el usuario y cerrar sesión
                            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser // Obtener el usuario actual de Firebase
                            val email = user?.email // Obtener el correo electrónico del usuario

                            if (user != null && !email.isNullOrEmpty()) { // Verificar que el usuario actual no sea nulo y que el correo electrónico no esté vacío
                                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password) // Crea las credenciales de autenticación con el correo electrónico y la contraseña ingresada por el usuario

                                user.reauthenticate(credential) // Re-autenticar el usuario con las credenciales creadas anteriormente
                                    .addOnCompleteListener { reauthTask -> // Manejar la respuesta del servidor de Firebase al re-autenticar el usuario
                                        if (reauthTask.isSuccessful) { // Si la re-autenticación es exitosa se puede eliminar la cuenta
                                            // Ahora sí se puede eliminar
                                            scope.launch {
                                                viewModel.eliminarCuenta(userId) { // Eliminar la cuenta de la base de datos utilizando el ID del usuario
                                                    user.delete() // Eliminar la cuenta de Firebase
                                                        .addOnCompleteListener { deleteTask -> // Manejar la respuesta del servidor de Firebase al eliminar la cuenta
                                                            if (deleteTask.isSuccessful) { // Si la eliminación es exitosa se elimina el ID del usuario de las preferencias compartidas y se redirige al usuario a la pantalla de inicio de sesión
                                                                PreferencesManager.saveUserId(context, -1) // Elimina el ID del usuario de las preferencias compartidas
                                                                navController.navigate("login") { // Navegar a la pantalla de inicio de sesión y eliminar la pantalla actual de la pila de navegación
                                                                    popUpTo("profile") { inclusive = true }
                                                                }
                                                            } else { // Si la eliminación falla se muestra un mensaje de error
                                                                Toast.makeText(context, "Error al eliminar cuenta de Firebase", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Contraseña incorrecta o sesión expirada", Toast.LENGTH_LONG).show() // Si la re-autenticación falla se muestra un mensaje de error
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "No hay usuario autenticado", Toast.LENGTH_LONG).show() // Si no hay usuario autenticado se muestra un mensaje de error
                            }

                            mostrarDialogoBorrarCuenta = false // Cerrar el diálogo de confirmación de eliminación de cuenta al hacer clic en el botón de confirmación
                        }
                    ) {
                        Text(text = "Eliminar", color = MaterialTheme.colorScheme.error) // Texto del botón de confirmación de eliminación de cuenta
                    }
                },
                dismissButton = { // Botón de cancelación de la acción de eliminación de cuenta con su acción correspondiente
                    TextButton(onClick = { mostrarDialogoBorrarCuenta = false }) {
                        Text(text = "Cancelar")
                    }
                }
            )
        }
    }
}