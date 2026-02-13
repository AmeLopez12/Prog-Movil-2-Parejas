package com.example.sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet.data.model.Materia
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.theme.SicenetGreen

@Composable
fun CargaScreen(
    viewModel: SicenetViewModel,
    modifier: Modifier = Modifier
) {
    val materias by viewModel.cargaLocal.collectAsState()
    val syncStatus = viewModel.syncStatus

    // Sincronización automática al entrar
    LaunchedEffect(Unit) {
        if (materias.isEmpty()) {
            viewModel.syncCargaAcademica()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5), // Fondo gris claro para contraste
        topBar = {
            Column(
                modifier = Modifier
                    .background(SicenetGreen)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mi Carga Académica",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.syncCargaAcademica() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = Color.White)
                    }
                }
                Text(
                    text = "Última actualización: ${viewModel.lastUpdateCarga}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (materias.isEmpty() && viewModel.syncStatus == "Sincronizando...") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SicenetGreen)
                }
            } else if (materias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay datos. Desliza o usa el botón para cargar.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(materias) { materia ->
                        MateriaCard(materia)
                    }
                }
            }

            // Barra de estado de sincronización (opcional, pequeña abajo)
            if (syncStatus == "Sincronizando...") {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = Color.White,
                    trackColor = SicenetGreen.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun MateriaCard(materia: Materia) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Nombre y Clave
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = materia.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SicenetGreen
                    )
                    Text(
                        text = "Clave: ${materia.clave}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = SicenetGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${materia.creditos} CR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SicenetGreen
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            // Info del Docente
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = materia.docente,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horario estilizado
            Text(
                text = "HORARIO",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DiaChip("Lun", materia.lunes)
                DiaChip("Mar", materia.martes)
                DiaChip("Mié", materia.miercoles)
                DiaChip("Jue", materia.jueves)
                DiaChip("Vie", materia.viernes)
            }
        }
    }
}

@Composable
fun RowScope.DiaChip(dia: String, horario: String) {
    val activo = horario.isNotBlank()
    Column(
        modifier = Modifier
            .weight(1f)
            .background(
                if (activo) SicenetGreen.copy(alpha = 0.05f) else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dia,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (activo) SicenetGreen else Color.LightGray
        )
        Text(
            text = if (activo) horario.substringBefore(" Aula").replace("-", "\n") else "-",
            fontSize = 9.sp,
            lineHeight = 10.sp,
            color = if (activo) Color.Black else Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (horario.contains("Aula:")) {
            Text(
                text = horario.substringAfter("Aula: ").trim(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = SicenetGreen
            )
        }
    }
}

private fun String.size() = if (this.isBlank()) 0 else 1
