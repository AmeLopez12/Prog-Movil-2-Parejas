package com.example.sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicenet.ui.SicenetViewModel
import com.example.sicenet.ui.theme.SicenetGreen

@Composable
fun LoginScreen(
    viewModel: SicenetViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var matricula by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }

    val loginState = viewModel.loginState

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.DarkGray,
        focusedBorderColor = SicenetGreen,
        unfocusedBorderColor = Color.Gray,
        cursorColor = SicenetGreen
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Encabezado Verde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SicenetGreen)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(60.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "SICENET",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Formulario
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Caja de error amigable
                if (loginState is SicenetViewModel.LoginResult.Error) {
                    val friendlyMessage = when {
                        loginState.message.contains("incorrecta", ignoreCase = true) ||
                        loginState.message.contains("Matrícula", ignoreCase = true) -> "Matrícula o contraseña incorrecta"
                        loginState.message.contains("servidor", ignoreCase = true) || 
                        loginState.message.contains("HTTP", ignoreCase = true) ||
                        loginState.message.contains("timeout", ignoreCase = true) -> "Servidor caído o error de red"
                        else -> "Error: No se pudo conectar con el servidor"
                    }

                    Surface(
                        color = Color(0xFFFFEBEE), // Rojo muy claro
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = friendlyMessage,
                            color = Color(0xFFD32F2F), // Rojo material
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it },
                    label = { Text("Ingresa matrícula...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasenia,
                    onValueChange = { contrasenia = it },
                    label = { Text("Contraseña...") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = SicenetGreen)
                } else {
                    // Botón Entrar
                    Button(
                        onClick = {
                            if (matricula.isNotBlank() && contrasenia.isNotBlank()) {
                                viewModel.login(matricula, contrasenia, onLoginSuccess)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = SicenetGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Entrar", color = Color.White)
                    }
                }
            }
        }
    }
}
