package com.example.chefguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.chefguard.ui.theme.ChefguardTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Inicializa la interfaz de usuario y habilita el modo Edge-to-Edge
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
fun Greeting(name: String) {
    Text(text = "$name!") //Imprime mensaje mostrado por pantalla al usuario por la App
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { Greeting("Android") } // Inicia la navegación con la pantalla "home".
        composable("inventory") { Greeting("Inventario") }  //Ruta que lleva a inventario
        composable("alerts") { Greeting("Alertas") } //Ruta que lleva a alertas
        composable("profile") { Greeting("Perfil") } //Ruta que lleva a perfil
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Construye la barra de navegación en la parte inferior.
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("inventory") },
            icon = { Icon(Icons.Filled.List, contentDescription = "Inventario") },
            label = { Text("Inventario") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("alerts") },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Alertas") },
            label = { Text("Alertas") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}
// Esta función es solo para vista previa en el editor de diseño de Android Studio.
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChefguardTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Greeting("Android")
        }
    }
}


