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
import com.example.sicenet.data.model.CalifUnidad
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.theme.SicenetGreen
import org.json.JSONObject

@Composable
fun CalifUnidadesScreen(
    viewModel: SicenetViewModel,
    modifier: Modifier = Modifier
) {
    val unidades by viewModel.califUnidadesLocal.collectAsState()
    val syncStatus = viewModel.syncStatus

    LaunchedEffect(Unit) {
        if (unidades.isEmpty()) {
            viewModel.syncCalifUnidades()
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
                        text = "Calificaciones Parciales",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.syncCalifUnidades() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = Color.White)
                    }
                }
                Text(
                    text = "Última actualización: ${viewModel.lastUpdateCalifUnidades}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (unidades.isEmpty() && syncStatus?.contains("Unidades") == true) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SicenetGreen)
                }
            } else if (unidades.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No hay datos de unidades localmente.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(unidades) { item ->
                        MateriaUnidadesCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun MateriaUnidadesCard(calif: CalifUnidad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = calif.materia,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SicenetGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val json = JSONObject(calif.unidades)
            // Usamos FlowRow o una rejilla si son muchas unidades, 
            // pero para simplificar usamos una fila que permite scroll horizontal si es necesario
            // o simplemente una distribución adaptativa.
            
            val unidadesActivasStr = json.optString("UnidadesActivas", "")
            val numUnidades = unidadesActivasStr.length.coerceAtLeast(1)

            // Dividir las unidades en filas de 4 para que quepan bien
            val unidadesPorFila = 4
            for (i in 1..numUnidades step unidadesPorFila) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (j in 0 until unidadesPorFila) {
                        val unidadIdx = i + j
                        if (unidadIdx <= numUnidades) {
                            val califVal = json.optString("C$unidadIdx", "-")
                            UnidadBox(
                                numero = unidadIdx, 
                                calificacion = if (califVal == "null" || califVal.isBlank()) "-" else califVal,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnidadBox(numero: Int, calificacion: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SicenetGreen.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "U$numero", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        val score = calificacion.toIntOrNull() ?: 0
        Text(
            text = calificacion,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = when {
                calificacion == "-" -> Color.Gray
                score >= 70 -> SicenetGreen
                else -> Color.Red
            }
        )
    }
}
