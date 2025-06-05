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
// Barra de navegación inferior con iconos que muestra varias pestañas
@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem( //Este icono de home muestra la pantalla de inicio
            selected = navController.currentDestination?.route == "home",
            onClick = { navController.navigate("home") }, // al pulsar te lleva a la pantalla de inicio
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") }, // muestra icono de casa
            label = { Text("Inicio") } // muestra texto inicio
        )

        NavigationBarItem( //Este icono de home muestra la pantalla de inventario
            selected = navController.currentDestination?.route == "inventory",
            onClick = { navController.navigate("inventory") }, // al pulsar te lleva a la pantalla de inventario
            icon = { Icon(Icons.Filled.List, contentDescription = "Inventario") }, // muestra icono de lista
            label = { Text("Inventario") } // muestra texto inventario
        )
        NavigationBarItem( //Este icono de home muestra la pantalla de alertas
            selected = navController.currentDestination?.route == "alerts",
            onClick = { navController.navigate("alerts") }, // al pulsar te lleva a la pantalla de alertas
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Alertas") }, // muestra icono warning
            label = { Text("Alertas") } // muestra texto alertas
        )

        NavigationBarItem( //Este icono de home muestra la pantalla de perfil
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") }, // al pulsar te lleva a la pantalla de perfil
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") }, // muestra icono perfil
            label = { Text("Perfil") } // muestra texto perfil
        )
    }
}