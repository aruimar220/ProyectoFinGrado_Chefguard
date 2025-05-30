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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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

        setContent {
            ChefguardTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: ""

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute !in listOf("login", "register", "recover")) {
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
                            composable("login") { LoginScreen(navController) }
                            composable("register") { RegisterScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("recover") { RecoverPasswordScreen(navController) }
                            composable("add_item") { AddItemsScreen(navController) }
                            composable("alerts") { AlertScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }
                            composable("add_items") { AddItemsScreen(navController) }
                            composable("inventory") { InventoryScreen(navController) }
                            composable(
                                route = "item_details/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("id") ?: 0
                                ItemDetailsScreen(navController, id)
                            }
                            composable(
                                route = "edit_item/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("id") ?: 0
                                EditItemScreen(navController, id)
                            }
                            composable("edit_profile") { EditProfileScreen(navController) }

                            composable("splash") {
                                val context = LocalContext.current
                                val currentUser = FirebaseAuth.getInstance().currentUser

                                LaunchedEffect(Unit) {
                                    when {
                                        !navigateToFromIntent.isNullOrEmpty() -> {
                                            navController.navigate(navigateToFromIntent) {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                        currentUser != null -> {
                                            navController.navigate("home") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                        else -> {
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

        val workRequest =
            PeriodicWorkRequestBuilder<AlertNotificationWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AlertNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas"
            val descriptionText = "Canal para alertas de alimentos por caducar"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("alert_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

fun scheduleDailyNotification(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<AlertNotificationWorker>(15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "alert_notification_work",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
