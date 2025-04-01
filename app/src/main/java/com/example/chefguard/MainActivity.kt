package com.example.chefguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.chefguard.ui.screens.HomeScreen
import com.example.chefguard.ui.screens.InventoryScreen
import com.example.chefguard.ui.theme.ChefguardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChefguardTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        NavigationGraph(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(text: String) {
    // Estado para controlar la visibilidad de la animación
    var visible by remember { mutableStateOf(false) }

    // Hacer que la animación se active después de que la Composable es visible
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) // Animación de entrada
    ) {
        Text(
            text = text,
            fontSize = 32.sp, // Tamaño más grande
            fontWeight = FontWeight.Bold, // Negrita
            color = MaterialTheme.colorScheme.primary // Usa el color primario del tema
        )
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(username = "Android") }
        composable("inventory") { InventoryScreen() }
        composable("alerts") { MainScreen("Alertas") }
        composable("profile") { MainScreen("Perfil") }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentDestination by navController.currentBackStackEntryAsState()

    NavigationBar {
        val items = listOf(
            NavigationItem("home", "Inicio", Icons.Filled.Home),
            NavigationItem("inventory", "Inventario", Icons.Filled.List),
            NavigationItem("alerts", "Alertas", Icons.Filled.Warning),
            NavigationItem("profile", "Perfil", Icons.Filled.Person)
        )

        items.forEach { item ->
            val isSelected = currentDestination?.destination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = isSelected, // Resaltar si es la pestaña activa
                onClick = { navController.navigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

data class NavigationItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChefguardTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MainScreen("Bienvenido a ChefGuard")
        }
    }
}


