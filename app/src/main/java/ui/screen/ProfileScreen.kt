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


@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(db)
    )

    var mostrarDialogoBorrarCuenta by remember { mutableStateOf(false) }

    val userId = PreferencesManager.getUserId(context)
    if (userId == -1) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
        return
    }

    var usuario by remember { mutableStateOf<UsuarioEntity?>(null) }
    LaunchedEffect(Unit) {
        usuario = db.usuarioDao().obtenerUsuarioPorId(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (usuario != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Nombre: ${usuario?.nombre}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Correo: ${usuario?.correo}", fontSize = 18.sp)
            }
        } else {
            Text(text = "Cargando datos...", fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("edit_profile")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Editar Perfil")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { mostrarDialogoBorrarCuenta = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Borrar Cuenta")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                PreferencesManager.saveUserId(context, -1)
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Cerrar Sesión")
        }

        if (mostrarDialogoBorrarCuenta) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoBorrarCuenta = false },
                title = { Text(text = "Borrar Cuenta") },
                text = { Text(text = "¿Estás seguro de que deseas borrar tu cuenta? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            user?.let {
                                viewModel.eliminarCuenta(userId) {
                                    it.delete()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                PreferencesManager.saveUserId(context, -1)
                                                navController.navigate("login") {
                                                    popUpTo("profile") { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(context, "Error al eliminar cuenta de Firebase", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            }
                            mostrarDialogoBorrarCuenta = false
                        }
                    ) {
                        Text(text = "Aceptar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoBorrarCuenta = false }
                    ) {
                        Text(text = "Cancelar")
                    }
                }
            )
        }
    }
}