package com.cis_ac.cis_ac.ui.feature.auth.signup.patient

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.ui.feature.auth.login.signup.patient.SignUpViewModel
import com.cis_ac.cis_ac.ui.theme.AppTypography
import com.cis_ac.cis_ac.ui.theme.OutlineLight
import com.cis_ac.cis_ac.ui.theme.Tertiary
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSignUpScreen(
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onSignedUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current

    var genderMenuExpanded by remember { mutableStateOf(false) }
    var showDobPicker by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = { Text("Crear cuenta", style = AppTypography.titleLarge, color = MaterialTheme.colorScheme.primary) }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = state.fullNameError.isNotEmpty(),
                supportingText = { if (state.fullNameError.isNotEmpty()) Text(state.fullNameError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                isError = state.emailError.isNotEmpty(),
                supportingText = { if (state.emailError.isNotEmpty()) Text(state.emailError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.phone,
                onValueChange = { input ->
                    val sanitized = input.filter { it.isDigit() }.take(10)
                    viewModel.onPhoneChange(sanitized)
                },
                label = { Text("Teléfono (10 dígitos)") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                isError = state.phoneError.isNotEmpty(),
                supportingText = { if (state.phoneError.isNotEmpty()) Text(state.phoneError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.dob,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de nacimiento") },
                leadingIcon = {
                    IconButton(onClick = { showDobPicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Seleccionar fecha")
                    }
                },
                isError = state.dobError.isNotEmpty(),
                supportingText = { if (state.dobError.isNotEmpty()) Text(state.dobError) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDobPicker = true }
            )

            if (showDobPicker) {
                val initialMillis = remember(state.dob) {
                    try {
                        if (state.dob.isNotBlank()) {
                            val d = LocalDate.parse(state.dob)
                            d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        } else null
                    } catch (_: Exception) { null }
                }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialMillis,
                    yearRange = 1900..LocalDate.now().year
                )
                DatePickerDialog(
                    onDismissRequest = { showDobPicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { viewModel.onDobSelected(it) }
                                showDobPicker = false
                            },
                            enabled = datePickerState.selectedDateMillis != null
                        ) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDobPicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = genderMenuExpanded,
                onExpandedChange = { genderMenuExpanded = !genderMenuExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = when (state.gender) {
                        Gender.Male -> "Masculino"
                        Gender.Female -> "Femenino"
                        Gender.Other -> "Otro"
                        Gender.Unspecified -> ""
                    },
                    onValueChange = {},
                    label = { Text("Género") },
                    isError = state.genderError.isNotEmpty(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderMenuExpanded) },
                    supportingText = { if (state.genderError.isNotEmpty()) Text(state.genderError) }
                )
                ExposedDropdownMenu(
                    expanded = genderMenuExpanded,
                    onDismissRequest = { genderMenuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("Masculino") }, onClick = {
                        viewModel.onGenderChange(Gender.Male); genderMenuExpanded = false
                    })
                    DropdownMenuItem(text = { Text("Femenino") }, onClick = {
                        viewModel.onGenderChange(Gender.Female); genderMenuExpanded = false
                    })
                    DropdownMenuItem(text = { Text("Otro") }, onClick = {
                        viewModel.onGenderChange(Gender.Other); genderMenuExpanded = false
                    })
                }
            }

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (state.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (state.isPasswordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.passwordError.isNotEmpty(),
                supportingText = { if (state.passwordError.isNotEmpty()) Text(state.passwordError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::onToggleConfirmVisibility) {
                        Icon(
                            imageVector = if (state.isConfirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (state.isConfirmVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (state.isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.confirmPasswordError.isNotEmpty(),
                supportingText = { if (state.confirmPasswordError.isNotEmpty()) Text(state.confirmPasswordError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.acceptedTerms, onCheckedChange = { viewModel.onAcceptTermsChange(it) })
                Spacer(Modifier.width(8.dp))
                Text("Acepto los términos y condiciones", style = AppTypography.bodyMedium)
            }
            if (state.termsError.isNotEmpty()) {
                Text(text = state.termsError, color = MaterialTheme.colorScheme.error, style = AppTypography.labelSmall)
            }

            if (state.successMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = state.successMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = AppTypography.bodyMedium
                    )
                }
            }

            if (state.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = state.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = AppTypography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.submit(
                        onSuccess = onNavigateToLogin,
                        onError = { }
                    )
                },
                enabled = !state.isLoading && state.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Crear cuenta")
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("¿Ya tienes cuenta? Inicia sesión") }
        }
    }
}