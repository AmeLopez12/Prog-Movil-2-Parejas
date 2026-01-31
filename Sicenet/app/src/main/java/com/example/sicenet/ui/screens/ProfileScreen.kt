package com.example.sicenet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.theme.SicenetGreen

@Composable
fun ProfileScreen(
    viewModel: SicenetViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    val profileXml = viewModel.profileData

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SicenetGreen)
                }
            } else if (profileXml != null) {
                // Aquí podrías parsear el XML, por ahora mostramos el crudo o una parte
                Text(
                    text = "Datos recibidos correctamente.",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Ejemplo de visualización del XML (podrías usar un parser después)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = profileXml,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            } else {
                Text(text = "No se pudieron cargar los datos del perfil.")
                Button(
                    onClick = { viewModel.fetchProfile() },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SicenetGreen)
                ) {
                    Text("Reintentar")
                }
            }
        }
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
