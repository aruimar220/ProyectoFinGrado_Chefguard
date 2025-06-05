package com.example.chefguard

import AlertNotificationWorker
import com.example.chefguard.ui.screens.EditProfileScreen
import ProfileScreen
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.chefguard.ui.components.BottomNavBar
import com.example.chefguard.ui.screens.*
import com.example.chefguard.ui.theme.ChefguardTheme
import com.example.chefguard.utils.PreferencesManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { // Crea la actividad principal de la aplicación y configura el diseño de la interfaz de usuario
        super.onCreate(savedInstanceState)

        // Solicitar permisos de notificación si la versión de Android es mayor o igual a 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        createNotificationChannel(this)

        val navigateToFromIntent = intent.getStringExtra("navigate_to")
        // Configuración de la actividad principal con Compose y navegación entre pantallas
        setContent {
            ChefguardTheme {
                val navController = rememberNavController() // Controlador de navegación para la pantalla principal de la aplicación
                val navBackStackEntry by navController.currentBackStackEntryAsState() // Obtiene la entrada de la pila de navegación actual
                val currentRoute = navBackStackEntry?.destination?.route ?: "" // Obtiene la ruta actual de la pantalla actual en la pila de navegación

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { // Barra de navegación inferior que muestra un icono para cada pantalla
                        if (currentRoute !in listOf("login", "register", "recover")) { // Si la ruta actual no es login, register o recover muestra la barra de navegación
                            BottomNavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                            composable("login") { LoginScreen(navController) } // Pantalla de inicio de sesión
                            composable("register") { RegisterScreen(navController) } // Pantalla de registro de usuario
                            composable("home") { HomeScreen(navController) } // Pantalla principal de la aplicación
                            composable("recover") { RecoverPasswordScreen(navController) } // Pantalla de recuperación de contraseña
                            composable("add_item") { AddItemsScreen(navController) }
                            composable("alerts") { AlertScreen(navController) } // Pantalla de alertas
                            composable("profile") { ProfileScreen(navController) } // Pantalla de perfil de usuario
                            composable("add_items") { AddItemsScreen(navController) } // Pantalla de añadir alimentos
                            composable("inventory") { InventoryScreen(navController) } // Pantalla inventario
                            composable(
                                route = "item_details/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("id") ?: 0
                                ItemDetailsScreen(navController, id)
                            } // Pantalla de detalles de un alimento
                            composable(
                                route = "edit_item/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("id") ?: 0
                                EditItemScreen(navController, id)
                            } // Pantalla de edición de un alimento
                            composable("edit_profile") { EditProfileScreen(navController) }
                            // Pantalla de edición de perfil de usuario
                            composable("splash") {
                                val context = LocalContext.current // Obtiene el contexto de la aplicación
                                val currentUser = FirebaseAuth.getInstance().currentUser // Obtiene el usuario actual de Firebase
                                // Navega a la pantalla correspondiente según el estado de autenticación
                                LaunchedEffect(Unit) {
                                    when { // Comprueba el estado de autenticación del usuario y navega a la pantalla correspondiente
                                        !navigateToFromIntent.isNullOrEmpty() -> {
                                            navController.navigate(navigateToFromIntent) {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                        currentUser != null -> { // Si el usuario está autenticado, navega a la pantalla principal de la aplicación y limpia la pila de navegación
                                            navController.navigate("home") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                        else -> { // Si el usuario no está autenticado, navega a la pantalla de inicio de sesión y limpia la pila de navegación
                                            navController.navigate("login") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Programa la ejecución de la notificación de alerta diaria en el dispositivo
        val workRequest =
            PeriodicWorkRequestBuilder<AlertNotificationWorker>(15, TimeUnit.MINUTES).build() // Programa la ejecución de la notificación cada 15 minutos
        WorkManager.getInstance(this).enqueueUniquePeriodicWork( // Encola la ejecución de la notificación en el dispositivo
            "AlertNotificationWork", // Nombre único de la tarea de ejecución de la notificación
            ExistingPeriodicWorkPolicy.KEEP, // Si ya existe una tarea con el mismo nombre, se mantendrá su estado actual y se actualizará con la nueva configuración
            workRequest // Configuración de la tarea de ejecución de la notificación
        )
    }
    // Crea un canal de notificación para mostrar las alertas en el dispositivo
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Comprueba si la versión de Android es mayor o igual a Oreo (API 26)
            val name = "Alertas" // Nombre del canal de notificación
            val descriptionText = "Canal para alertas de alimentos por caducar" // Descripción del canal de notificación
            val importance = NotificationManager.IMPORTANCE_HIGH // Importancia del canal de notificación
            val channel = NotificationChannel("alert_channel", name, importance).apply { // Crea el canal de notificación con el nombre y la importancia especificados
                description = descriptionText // Establece la descripción del canal de notificación
            }

            val notificationManager: NotificationManager = // Obtiene el servicio de notificación del sistema y crea el canal de notificación
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager // Obtiene el servicio de notificación del sistema
            notificationManager.createNotificationChannel(channel) // Crea el canal de notificación en el dispositivo de notificación
        }
    }
}
// Programa la ejecución de la notificación de alerta diaria en el dispositivo
fun scheduleDailyNotification(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<AlertNotificationWorker>(15, TimeUnit.MINUTES) // Programa la ejecución de la notificación cada 15 minutos
        .build() // Construye la solicitud de trabajo para la ejecución diaria de la notificación

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "alert_notification_work",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
