package com.example.chefguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chefguard.ui.components.BottomNavBar
import com.example.chefguard.ui.screens.*
import com.example.chefguard.ui.theme.ChefguardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChefguardTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: ""

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute !in listOf("login", "register")) {
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
                            startDestination = "login"
                        ) {
                            composable("login") { LoginScreen(navController) }
                            composable("register") { RegisterScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("inventory") { InventoryScreen() }
                            composable("alerts") { AlertsScreen() }
                            composable("profile") { ProfileScreen() }
                        }
                    }
                }
            }
        }
    }
}