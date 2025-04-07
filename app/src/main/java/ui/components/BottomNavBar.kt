package com.example.chefguard.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = navController.currentDestination?.route == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route == "inventory",
            onClick = { navController.navigate("inventory") },
            icon = { Icon(Icons.Filled.List, contentDescription = "Inventario") },
            label = { Text("Inventario") }
        )
        NavigationBarItem(
            selected = navController.currentDestination?.route == "alerts",
            onClick = { navController.navigate("alerts") },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Alertas") },
            label = { Text("Alertas") }
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}