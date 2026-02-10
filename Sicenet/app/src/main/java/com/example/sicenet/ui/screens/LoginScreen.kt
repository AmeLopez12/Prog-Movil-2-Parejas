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

    val loginState = viewModel.loginState
    LaunchedEffect(loginState) {
        if (loginState is SicenetViewModel.LoginResult.Error) {
            snackbarHostState.showSnackbar(loginState.message)
        }
    }

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
                    .height(70.dp),
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
