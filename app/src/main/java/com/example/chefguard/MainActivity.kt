package com.example.chefguard

import EditProfileScreen
import ProfileScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chefguard.ui.components.BottomNavBar
import com.example.chefguard.ui.screens.*
import com.example.chefguard.ui.theme.ChefguardTheme
import com.example.chefguard.utils.PreferencesManager

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
                            composable("splash") {
                                val isLoggedIn = PreferencesManager.getLoginState(LocalContext.current)
                                if (isLoggedIn) {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                            composable("edit_profile") { EditProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}