package com.cis_ac.cis_ac.ui.feature.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.R
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgot: () -> Unit,
    onRegisterPatient: () -> Unit,
    onRegisterPro: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val s by vm.uiState.collectAsState()


    val primary = MaterialTheme.colorScheme.primary


    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(primary)
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bienvenido",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.cis_logo_white_1024), // usa el nombre real del recurso
                contentDescription = "Logo CIS",
                modifier = Modifier.size(96.dp)
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        "Iniciar sesión",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = s.email,                         // ← ya no vm.uiState.email
                        onValueChange = vm::onEmailChange,
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        trailingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = s.password,                      // ← s.password
                        onValueChange = vm::onPasswordChange,
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (s.isPasswordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = vm::togglePasswordVisibility) { // ← nombre correcto
                                Icon(
                                    imageVector = if (s.isPasswordVisible)
                                        Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "¿Olvidaste tu contraseña?",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onForgot() }
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { vm.signIn { onLoginSuccess() } },                    // ← llama a tu VM
                        enabled = !s.isLoading,                  // ← isLoading (no 'loading')
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (s.isLoading)
                            CircularProgressIndicator(
                                Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        else Text("Iniciar sesión")
                    }

                    AnimatedVisibility(visible = s.error != null) {
                        Text(
                            s.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(Modifier.weight(1f))
                        Text(
                            "¿No tienes una cuenta?",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = onRegisterPatient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Registrarse como paciente", textAlign = TextAlign.Center) }

                    Spacer(Modifier.height(12.dp))

                    FilledTonalButton(
                        onClick = onRegisterPro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Registrarse como profesionista", textAlign = TextAlign.Center) }
                }
            }
        }
    }
}