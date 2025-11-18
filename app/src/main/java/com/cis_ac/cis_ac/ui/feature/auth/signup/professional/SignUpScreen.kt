package com.cis_ac.cis_ac.ui.feature.auth.signup.professional

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.ui.theme.AppTypography
import java.time.LocalDate
import java.time.ZoneId

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfessionalSignUpScreen(
    modifier: Modifier = Modifier,
    vm: SignUpViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val context = LocalContext.current

    var genderMenuExpanded by remember { mutableStateOf(false) }
    var disciplineMenuExpanded by remember { mutableStateOf(false) }
    var showDobPicker by remember { mutableStateOf(false) }

    val days = listOf(
        1 to "Lunes", 2 to "Martes", 3 to "Miércoles",
        4 to "Jueves", 5 to "Viernes", 6 to "Sábado", 7 to "Domingo"
    )
    var selectedDays by rememberSaveable { mutableStateOf<Set<Int>>(emptySet()) }
    var hoursByDay by rememberSaveable { mutableStateOf<Map<Int, Set<Int>>>(emptyMap()) }

    fun toggleDay(day: Int) {
        selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
        if (day !in hoursByDay) hoursByDay = hoursByDay + (day to emptySet())
    }
    fun toggleHour(day: Int, hour: Int) {
        val cur = hoursByDay[day].orEmpty()
        hoursByDay = hoursByDay + (day to (if (hour in cur) cur - hour else cur + hour))
    }

    LaunchedEffect(selectedDays, hoursByDay) {
        val map = hoursByDay
            .filterKeys { it in selectedDays }
            .mapValues { it.value.sorted() }
        vm.onScheduleChange(map)
    }

    val cvPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            vm.onCvPicked(it)
        }
    }
    val licensePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            vm.onLicensePicked(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
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
                onValueChange = vm::onFullNameChange,
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                isError = state.fullNameError.isNotEmpty(),
                supportingText = { if (state.fullNameError.isNotEmpty()) Text(state.fullNameError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                isError = state.emailError.isNotEmpty(),
                supportingText = { if (state.emailError.isNotEmpty()) Text(state.emailError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.phone,
                onValueChange = { vm.onPhoneChange(it.filter(Char::isDigit).take(10)) },
                label = { Text("Número de teléfono") },
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                isError = state.phoneError.isNotEmpty(),
                supportingText = { if (state.phoneError.isNotEmpty()) Text(state.phoneError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                placeholder = { Text("1234567890") }
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = vm::onTogglePasswordVisibility) {
                        Icon(if (state.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.passwordError.isNotEmpty(),
                supportingText = { if (state.passwordError.isNotEmpty()) Text(state.passwordError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = vm::onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = vm::onToggleConfirmVisibility) {
                        Icon(if (state.isConfirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                visualTransformation = if (state.isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.confirmPasswordError.isNotEmpty(),
                supportingText = { if (state.confirmPasswordError.isNotEmpty()) Text(state.confirmPasswordError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
            )

            OutlinedTextField(
                value = state.licenseNumber,
                onValueChange = vm::onLicenseChange,
                label = { Text("Cédula profesional") },
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                isError = state.licenseError.isNotEmpty(),
                supportingText = { if (state.licenseError.isNotEmpty()) Text(state.licenseError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.dob,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de nacimiento") },
                leadingIcon = {
                    IconButton(onClick = { showDobPicker = true }) { Icon(Icons.Filled.CalendarMonth, null) }
                },
                isError = state.dobError.isNotEmpty(),
                supportingText = { if (state.dobError.isNotEmpty()) Text(state.dobError) },
                modifier = Modifier.fillMaxWidth().clickable { showDobPicker = true }
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
                val dateState = rememberDatePickerState(initialSelectedDateMillis = initialMillis, yearRange = 1900..LocalDate.now().year)
                DatePickerDialog(
                    onDismissRequest = { showDobPicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = { dateState.selectedDateMillis?.let(vm::onDobSelected); showDobPicker = false },
                            enabled = dateState.selectedDateMillis != null
                        ) { Text("Aceptar") }
                    },
                    dismissButton = { TextButton(onClick = { showDobPicker = false }) { Text("Cancelar") } }
                ) { DatePicker(state = dateState, showModeToggle = false) }
            }

            ExposedDropdownMenuBox(
                expanded = genderMenuExpanded,
                onExpandedChange = { genderMenuExpanded = !genderMenuExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                    DropdownMenuItem(text = { Text("Masculino") }, onClick = { vm.onGenderChange(Gender.Male); genderMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Femenino") }, onClick = { vm.onGenderChange(Gender.Female); genderMenuExpanded = false })
                    DropdownMenuItem(text = { Text("Otro") }, onClick = { vm.onGenderChange(Gender.Other); genderMenuExpanded = false })
                }
            }

            ExposedDropdownMenuBox(
                expanded = disciplineMenuExpanded,
                onExpandedChange = { disciplineMenuExpanded = !disciplineMenuExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    value = state.discipline?.spanishName ?: "",
                    onValueChange = {},
                    label = { Text("Especialidad principal") },
                    isError = state.disciplineError.isNotEmpty(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = disciplineMenuExpanded) },
                    supportingText = { if (state.disciplineError.isNotEmpty()) Text(state.disciplineError) }
                )
                ExposedDropdownMenu(
                    expanded = disciplineMenuExpanded,
                    onDismissRequest = { disciplineMenuExpanded = false }
                ) {
                    Discipline.entries.forEach { d ->
                        DropdownMenuItem(text = { Text(d.spanishName) }, onClick = {
                            vm.onDisciplineChange(d); disciplineMenuExpanded = false
                        })
                    }
                }
            }

            OutlinedTextField(
                value = state.speciality,
                onValueChange = vm::onSpecialityChange,
                label = { Text("Especialidad (subárea)") },
                leadingIcon = { Icon(Icons.Filled.Star, null) },
                isError = state.specialityError.isNotEmpty(),
                supportingText = { if (state.specialityError.isNotEmpty()) Text(state.specialityError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                placeholder = { Text("Ej: Terapia breve, Desarrollo infantil…") }
            )

            if (state.discipline == Discipline.PSYCHOLOGY) {
                OutlinedTextField(
                    value = state.enfoque,
                    onValueChange = vm::onEnfoqueChange,
                    label = { Text("Enfoque") },
                    leadingIcon = { Icon(Icons.Filled.Psychology, null) },
                    isError = state.enfoqueError.isNotEmpty(),
                    supportingText = { if (state.enfoqueError.isNotEmpty()) Text(state.enfoqueError) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    placeholder = { Text("Ej: Cognitivo-conductual, Humanista…") }
                )
            }

            OutlinedTextField(
                value = state.topics,
                onValueChange = vm::onTopicsChange,
                label = { Text("Temas / palabras clave") },
                leadingIcon = { Icon(Icons.Filled.Tag, null) },
                isError = state.topicsError.isNotEmpty(),
                supportingText = { if (state.topicsError.isNotEmpty()) Text(state.topicsError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                placeholder = { Text("Ansiedad, depresión, duelo…") }
            )

            OutlinedTextField(
                value = state.expertiz,
                onValueChange = vm::onExpertizChange,
                label = { Text("Área de expertiz") },
                leadingIcon = { Icon(Icons.Filled.Work, null) },
                isError = state.expertizError.isNotEmpty(),
                supportingText = { if (state.expertizError.isNotEmpty()) Text(state.expertizError) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                placeholder = { Text("Ej: Evaluación psicométrica, terapia de pareja…") }
            )

            MultiChoiceChips(
                title = "Modalidad",
                options = listOf(1 to "Presencial", 2 to "En línea", 3 to "Domicilio"),
                selected = state.modalities,
                onToggle = vm::toggleModality
            )

            MultiChoiceChips(
                title = "Tipo de sesión",
                options = listOf(1 to "Individual", 2 to "Pareja", 3 to "Familiar", 4 to "Equipos"),
                selected = state.sessionTypes,
                onToggle = vm::toggleSessionType
            )

            MultiChoiceChips(
                title = "Población objetivo",
                options = listOf(1 to "Infantes", 2 to "Adolescentes", 3 to "Adultos", 4 to "Adultos mayores"),
                selected = state.populations,
                onToggle = vm::togglePopulation
            )

            Text("Agenda", style = MaterialTheme.typography.titleMedium)
            Text("Selecciona las horas disponibles (24h)", style = MaterialTheme.typography.bodySmall)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                days.forEach { (dayInt, dayLabel) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = dayInt in selectedDays, onCheckedChange = { toggleDay(dayInt) })
                        Spacer(Modifier.width(8.dp))
                        Text(dayLabel, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (dayInt in selectedDays) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (0..23).forEach { h ->
                                val selected = h in (hoursByDay[dayInt].orEmpty())
                                FilterChip(
                                    selected = selected,
                                    onClick = { toggleHour(dayInt, h) },
                                    label = { Text(h.toString().padStart(2, '0')) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            FileInputRow(
                title = "Currículum Vitae (PDF)",
                selected = state.cvUri != null,
                onClick = { cvPicker.launch(arrayOf("application/pdf")) }
            )

            FileInputRow(
                title = "Cédula profesional (PDF)",
                selected = state.licenseUri != null,
                onClick = { licensePicker.launch(arrayOf("application/pdf")) }
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.acceptedTerms, onCheckedChange = vm::onAcceptTermsChange)
                Spacer(Modifier.width(8.dp))
                Text("Acepto la política de privacidad y los términos del servicio", style = AppTypography.bodyMedium)
            }
            if (state.termsError.isNotEmpty()) {
                Text(state.termsError, color = MaterialTheme.colorScheme.error, style = AppTypography.labelSmall)
            }

            if (state.successMessage.isNotEmpty()) {
                AssistChip(onClick = {}, label = { Text(state.successMessage) }, leadingIcon = { Icon(Icons.Filled.CheckCircle, null) })
            }
            if (state.errorMessage.isNotEmpty()) {
                AssistChip(onClick = {}, label = { Text(state.errorMessage) }, leadingIcon = { Icon(Icons.Filled.Error, null) })
            }

            Button(
                onClick = {
                    vm.submit(
                        context = context,
                        onSuccess = onNavigateToLogin,
                        onError = { }
                    )
                },
                enabled = !state.isLoading && state.isFormValid,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                else Text("Registrarse")
            }

            TextButton(onClick = onSignInClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiChoiceChips(
    title: String,
    options: List<Pair<Int, String>>,
    selected: Set<Int>,
    onToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = value in selected,
                    onClick = { onToggle(value) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun FileInputRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = if (selected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Description, null)
            Spacer(Modifier.width(10.dp))
            Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(10.dp))
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.CloudUpload,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }
    }
}
