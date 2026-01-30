package com.example.sicenet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet.ui.theme.SicenetGreen
import com.example.sicenet.ui.theme.SicenetTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Perfil Académico",
            style = MaterialTheme.typography.headlineMedium,
            color = SicenetGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Datos de ejemplo para el preview
        ProfileItem(label = "Nombre", value = "América Citlalli López Lemus")
        ProfileItem(label = "Matrícula", value = "S22120161")
        ProfileItem(label = "Carrera", value = "Ingeniería en Sistemas Computacionales")
        ProfileItem(label = "Semestre", value = "8")
        ProfileItem(label = "Promedio General", value = "9.7")
        ProfileItem(label = "Estatus", value = "Regular")
        ProfileItem(label = "Último Periodo", value = "2025-2")
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SicenetTheme {
        ProfileScreen()
    }
}