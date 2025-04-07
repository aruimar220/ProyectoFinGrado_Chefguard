package com.example.chefguard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a ChefGuard!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("inventory") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Inventario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Revisa y gestiona tus productos", fontSize = 16.sp, color = Color.White)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("alerts") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Alertas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Recibe notificaciones importantes", fontSize = 16.sp, color = Color.White)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("profile") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Administra tu información personal", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
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