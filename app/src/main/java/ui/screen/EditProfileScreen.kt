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
import data.local.AppDatabase
import data.local.entity.UsuarioEntity
import com.example.chefguard.utils.PreferencesManager
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
    var correo by remember { mutableStateOf(usuario?.correo ?: "") }
    var contrasena by remember { mutableStateOf("") } // Contraseña vacía por defecto

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
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
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
                val usuarioActualizado = UsuarioEntity(
                    id = userId,
                    nombre = nombre,
                    correo = correo,
                    contrasena = contrasena.ifBlank { usuario?.contrasena ?: "" }
                )

                scope.launch {
                    db.usuarioDao().actualizarUsuario(usuarioActualizado)
                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Guardar Cambios")
        }
    }
}