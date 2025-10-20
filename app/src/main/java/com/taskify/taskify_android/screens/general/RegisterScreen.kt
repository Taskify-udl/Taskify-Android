package com.taskify.taskify_android.screens.general

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.taskify.taskify_android.data.models.entities.Customer
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.Role
import com.taskify.taskify_android.logic.validateRegister
import com.taskify.taskify_android.logic.viewmodels.AuthUiState
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

enum class RegisterStep {
    ROLE_SELECTION,
    CREDENTIALS,
    VERIFY_CODE,
    PROVIDER_SERVICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val registerState by authViewModel.authState.collectAsState()

    var step by remember { mutableStateOf(RegisterStep.ROLE_SELECTION) }
    var role by remember { mutableStateOf(Role.CUSTOMER) }

    // User data
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Provider data
    var serviceTitle by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("") }

    // Verification code
    var verificationCode by remember { mutableStateOf("") }

    // Local validation error
    var localError by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (step) {
                    RegisterStep.ROLE_SELECTION -> StepRoleSelection(
                        onRoleSelected = {
                            role = it
                            step = RegisterStep.CREDENTIALS
                        }
                    )

                    RegisterStep.CREDENTIALS -> StepCredentials(
                        registerState = registerState,
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        email = email,
                        password = password,
                        localError = localError,
                        onFirstNameChange = { firstName = it },
                        onLastNameChange = { lastName = it },
                        onUsernameChange = { username = it },
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onValidate = {
                            localError = validateRegister(firstName, lastName, email, password)
                        },
                        onNext = {
                            val error = validateRegister(firstName, lastName, email, password)
                            if (error.isEmpty()) {
                                authViewModel.register(
                                    firstName,
                                    lastName,
                                    username,
                                    email,
                                    password,
                                    context
                                )
                                step = RegisterStep.VERIFY_CODE
                            } else {
                                localError = error
                            }
                        }
                    )

                    RegisterStep.VERIFY_CODE -> StepVerifyCode(
                        verificationCode = verificationCode,
                        onCodeChange = { verificationCode = it },
                        onVerifySuccess = {
                            val now = java.time.LocalDateTime.now()
                            val apiUser = registerState.user
                            when (role) {
                                Role.CUSTOMER -> {
                                    val customer = Customer(
                                        id = apiUser?.id?.toLong() ?: 0L, // ID de l'API
                                        firstName = firstName,
                                        lastName = lastName,
                                        username = username,
                                        email = email,
                                        password = password,
                                        phoneNumber = "",
                                        profilePic = null,
                                        createdAt = now,
                                        updatedAt = now,
                                        address = "",
                                        city = "",
                                        country = "",
                                        zipCode = ""
                                    )
                                    authViewModel.saveLocalUser(customer)
                                    navigateToHome(navController)
                                }

                                Role.PROVIDER -> {
                                    step = RegisterStep.PROVIDER_SERVICE
                                }

                                else -> {}
                            }
                        }
                    )

                    RegisterStep.PROVIDER_SERVICE -> StepProviderService(
                        serviceTitle = serviceTitle,
                        serviceCategory = serviceCategory,
                        serviceDescription = serviceDescription,
                        servicePrice = servicePrice,
                        onTitleChange = { serviceTitle = it },
                        onCategoryChange = { serviceCategory = it },
                        onDescriptionChange = { serviceDescription = it },
                        onPriceChange = { servicePrice = it },
                        onCreate = {
                            val now = java.time.LocalDateTime.now()

                            authViewModel.createService(
                                title = serviceTitle,
                                category = serviceCategory,
                                description = serviceDescription,
                                price = servicePrice.toDouble(),
                                context = context,
                                onSuccess = { apiService ->
                                    val provider = Provider(
                                        id = registerState.user?.id?.toLong() ?: 0L,   // 👈 usa el id real
                                        firstName = firstName,
                                        lastName = lastName,
                                        username = username,
                                        email = email,
                                        password = password,
                                        phoneNumber = "",
                                        profilePic = null,
                                        createdAt = now,
                                        updatedAt = now,
                                        address = "",
                                        city = "",
                                        country = "",
                                        zipCode = "",
                                        bio = "",
                                        experienceYears = 0,
                                        averageRating = 0.0,
                                        isVerified = false,
                                        services = listOf(apiService)
                                    )
                                    authViewModel.saveLocalUser(provider)
                                    navigateToHome(navController)
                                },
                                onError = { error ->
                                    Log.e("Register", "Failed to create service: $error")
                                }
                            )

                        }
                    )
                }

                if (localError.isNotEmpty()) {
                    JellyText(
                        text = localError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun JellyText(
    text: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.error,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    // Animació infinita que oscil·la el text com una gelatina
    val infiniteTransition = rememberInfiniteTransition(label = "jelly")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Text(
        text = text,
        color = color,
        style = style.copy(fontSize = 16.sp),
        modifier = Modifier.graphicsLayer(
            rotationZ = rotation,
            transformOrigin = TransformOrigin(0.5f, 1f) // pivot inferior = base fixa
        )
    )
}

// ----------------- STEP FUNCTIONS -----------------

@Composable
fun StepRoleSelection(onRoleSelected: (Role) -> Unit) {
    Text("Register as:", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { onRoleSelected(Role.CUSTOMER) }) { Text("Customer") }
        Button(onClick = { onRoleSelected(Role.PROVIDER) }) { Text("Provider") }
    }
}

@Composable
fun StepCredentials(
    registerState: AuthUiState,
    firstName: String,
    lastName: String,
    username: String,
    email: String,
    password: String,
    localError: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onValidate: () -> Unit,
    onNext: () -> Unit
) {
    Text("Enter your details", style = MaterialTheme.typography.headlineSmall)

    OutlinedTextField(
        value = firstName, onValueChange = { onFirstNameChange(it); onValidate() },
        label = { Text("First Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = lastName, onValueChange = { onLastNameChange(it); onValidate() },
        label = { Text("Last Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = username, onValueChange = { onUsernameChange(it); onValidate() },
        label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = email, onValueChange = { onEmailChange(it); onValidate() },
        label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    PasswordField(
        password = password,
        onPasswordChange = { onPasswordChange(it); onValidate() }
    )

    val isFormValid = localError.isEmpty() &&
            firstName.isNotBlank() && lastName.isNotBlank() &&
            username.isNotBlank() && email.isNotBlank() && password.isNotBlank()

    Button(
        onClick = onNext,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = isFormValid && !registerState.isLoading
    ) { Text("Register") }

    if (registerState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun StepVerifyCode(
    verificationCode: String,
    onCodeChange: (String) -> Unit,
    onVerifySuccess: () -> Unit
) {
    var isVerifying by remember { mutableStateOf(false) }

    LaunchedEffect(isVerifying) {
        if (isVerifying) {
            // TODO: Replace with actual API verification call
            delay(3000)
            isVerifying = false
            onVerifySuccess()
        }
    }

    Text(
        "Enter the verification code sent to your email",
        style = MaterialTheme.typography.headlineSmall
    )

    OutlinedTextField(
        value = verificationCode,
        onValueChange = onCodeChange,
        label = { Text("Verification Code") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = { isVerifying = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = verificationCode.isNotBlank() && !isVerifying
    ) { Text(if (isVerifying) "Verifying..." else "Verify") }

    if (isVerifying) CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
}

@Composable
fun StepProviderService(
    serviceTitle: String,
    serviceCategory: String,
    serviceDescription: String,
    servicePrice: String,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    Text("Create a new service", style = MaterialTheme.typography.headlineSmall)

    OutlinedTextField(
        value = serviceTitle, onValueChange = onTitleChange,
        label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = serviceCategory, onValueChange = onCategoryChange,
        label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = serviceDescription, onValueChange = onDescriptionChange,
        label = { Text("Description") }, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = servicePrice, onValueChange = onPriceChange,
        label = { Text("Price") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    val isFormValid = serviceTitle.isNotBlank() && serviceCategory.isNotBlank() &&
            serviceDescription.isNotBlank() && servicePrice.isNotBlank()

    Button(
        onClick = onCreate, modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = isFormValid
    ) { Text("Create") }
}

// ----------------- NAVIGATION -----------------

private fun navigateToHome(navController: NavController) {
    navController.navigate("homeScreen") {
        popUpTo("register") { inclusive = true }
        launchSingleTop = true
    }
}