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
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejo de errores del login
    val loginState = viewModel.loginState
    LaunchedEffect(loginState) {
        if (loginState is SicenetViewModel.LoginResult.Error) {
            snackbarHostState.showSnackbar(loginState.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado Verde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(SicenetGreen),
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

            Spacer(modifier = Modifier.height(60.dp))

            // Formulario
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it },
                    label = { Text("Ingresa matrícula...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasenia,
                    onValueChange = { contrasenia = it },
                    label = { Text("Contraseña...") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading
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
