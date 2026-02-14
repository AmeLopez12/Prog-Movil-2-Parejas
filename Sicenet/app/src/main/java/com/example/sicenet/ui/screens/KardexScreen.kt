package com.example.sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.sicenet.data.model.Kardex
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.theme.SicenetGreen

@Composable
fun KardexScreen(
    viewModel: SicenetViewModel,
    modifier: Modifier = Modifier
) {
    val kardex by viewModel.kardexLocal.collectAsState()
    val syncStatus = viewModel.syncStatus

    LaunchedEffect(Unit) {
        if (kardex.isEmpty()) {
            viewModel.syncKardex()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
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
                        text = "Kardex del Alumno",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.syncKardex() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = Color.White)
                    }
                }
                Text(
                    text = "Última actualización: ${viewModel.lastUpdateKardex}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (kardex.isEmpty() && syncStatus?.contains("Kardex") == true) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SicenetGreen)
                }
            } else if (kardex.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay datos de Kardex localmente.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Agrupar por periodo
                    val agrupado = kardex.groupBy { it.periodo }
                    agrupado.forEach { (periodo, materias) ->
                        item {
                            Text(
                                text = periodo,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.DarkGray
                            )
                        }
                        items(materias) { item ->
                            KardexItem(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KardexItem(kardex: Kardex) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kardex.materia,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${kardex.clave} | ${kardex.acreditacion}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            val califInt = kardex.calificacion
            val colorCalif = when {
                califInt >= 90 -> Color(0xFF2E7D32) // Verde fuerte
                califInt >= 70 -> SicenetGreen
                else -> Color.Red
            }

            Surface(
                color = colorCalif.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = califInt.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = colorCalif
                )
            }
        }
    }
}
