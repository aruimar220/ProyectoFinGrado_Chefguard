package com.example.chefguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bienvenido a ChefGuard",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Tarjeta de Inventario
        HomeCard(
            title = "Inventario",
            description = "Gestiona tus ingredientes y productos",
            color = Color(0xFF4CAF50),
            onClick = { navController?.navigate("inventory") }
        )

        // Tarjeta de Alertas
        HomeCard(
            title = "Alertas",
            description = "Revisa las notificaciones importantes",
            color = Color(0xFFFF9800),
            onClick = { navController?.navigate("alerts") }
        )

        // Tarjeta de Perfil
        HomeCard(
            title = "Perfil",
            description = "Administra tu cuenta y configuraciones",
            color = Color(0xFF2196F3),
            onClick = { navController?.navigate("profile") }
        )
    }
}

@Composable
fun HomeCard(title: String, description: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = description, fontSize = 16.sp, color = Color.White)
        }
    }
}
