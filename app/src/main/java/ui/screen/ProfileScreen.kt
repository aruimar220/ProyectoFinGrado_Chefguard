import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chefguard.model.AppDatabase
import com.example.chefguard.model.UsuarioEntity
import com.example.chefguard.utils.PreferencesManager

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Editar Perfil")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { mostrarDialogoCerrarSesion = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Cerrar Sesión", color = MaterialTheme.colorScheme.error)
        }

        if (mostrarDialogoCerrarSesion) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoCerrarSesion = false },
                title = { Text(text = "Cerrar Sesión") },
                text = { Text(text = "¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            PreferencesManager.saveUserId(context, -1)
                            navController.navigate("login") {
                                popUpTo("profile") { inclusive = true }
                            }
                            mostrarDialogoCerrarSesion = false
                        }
                    ) {
                        Text(text = "Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoCerrarSesion = false }
                    ) {
                        Text(text = "Cancelar")
                    }
                }
            )
        }
    }
}