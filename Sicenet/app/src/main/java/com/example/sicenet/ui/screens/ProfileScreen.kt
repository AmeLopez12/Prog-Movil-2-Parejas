package com.example.sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.theme.SicenetGreen

@Composable
fun ProfileScreen(
    viewModel: SicenetViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alumno by viewModel.alumnoLocal.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SicenetGreen)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(70.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Perfil Académico",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SicenetGreen)
            }
        } else if (alumno != null) {
            val a = alumno!!
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = a.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = a.matricula,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Detalles en Cards
                InfoCard(title = "Carrera", value = a.carrera)
                InfoCard(title = "Especialidad", value = a.especialidad)
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(title = "Semestre", value = a.semActual.toString())
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(title = "Estatus", value = a.estatus)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(title = "Créditos Acum.", value = a.cdtosAcumulados.toString())
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(title = "Créditos Act.", value = a.cdtosActuales.toString())
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(title = "Inscrito", value = if(a.inscrito) "SÍ" else "NO")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCard(title = "Mod. Educativo", value = a.modEducativo.toString())
                    }
                }

                if (a.adeudo) {
                    InfoCard(
                        title = "Adeudo", 
                        value = a.adeudoDescripcion.ifBlank { "Tiene adeudos pendientes" },
                        color = Color(0xFFFFEBEE),
                        contentColor = Color.Red
                    )
                } else {
                    InfoCard(title = "Adeudo", value = "Sin adeudos")
                }

                InfoCard(title = "Fecha Reinscripción", value = a.fechaReins)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Sesión", color = Color.White)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No se pudieron cargar los datos.", color = Color.Black)
                    Button(
                        onClick = { /* El perfil se sincroniza al entrar, pero podríamos reintentar */ },
                        colors = ButtonDefaults.buttonColors(containerColor = SicenetGreen)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String, 
    value: String, 
    color: Color = Color(0xFFF8F9FA),
    contentColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 15.sp, color = contentColor)
        }
    }
}
