package com.taskify.taskify_android.screens.general.authentication

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.taskify.taskify_android.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.taskify.taskify_android.data.models.entities.Role
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.logic.validateCompanyInfo
import com.taskify.taskify_android.logic.validateRegister
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.*
import androidx.compose.ui.text.TextStyle


private enum class SignupStep {
    PERSONAL_INFO,
    ACCOUNT_TYPE,
    CUSTOMER_SUMMARY,
    PROVIDER_TYPE,
    FREELANCER_SUMMARY,
    COMPANY_INFO,
    COMPANY_CODE,
    COMPANY_SUMMARY
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var step by remember { mutableStateOf(SignupStep.PERSONAL_INFO) }
    val context = LocalContext.current
    var userDraft by remember { mutableStateOf(UserDraft()) }

    // NOU: Observació de l'estat del Viewmodel
    val authState by authViewModel.authState.collectAsState()

    // NOU: Estat local per al missatge d'error
    var registerError by remember { mutableStateOf<String?>(null) }

    // background animation
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressAnim"
    )

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val startA = Offset(screenWidthPx * (1f - progress), 0f)
    val endA = Offset(0f, screenHeightPx * progress)
    val startB = Offset(0f, screenHeightPx * (1f - progress))
    val endB = Offset(screenWidthPx * progress, screenHeightPx)

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            navController.navigate("homeScreen") {
                popUpTo("register") { inclusive = true }
                launchSingleTop = true
            }
            // IMPORTANT: Un cop naveguem, netegem l'estat per evitar navegacions futures
            authViewModel.resetState()
        }
    }

    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            registerError = authState.error
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // gradient background layers
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(BgWhite, BgSecondary, BrandBlue.copy(alpha = 0.12f)),
                        start = startA,
                        end = endA
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(BrandBlue.copy(alpha = 0.25f), TextGray.copy(alpha = 0.15f)),
                        start = startB,
                        end = endB
                    )
                )
        )

        Scaffold(containerColor = Color.Transparent) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn(tween(350)) + slideInVertically { it / 5 } with
                                fadeOut(tween(300)) + slideOutVertically { -it / 5 }
                    },
                    label = "stepAnim"
                ) { currentStep ->
                    when (currentStep) {

                        SignupStep.PERSONAL_INFO -> StepPersonalInfo(
                            userDraft = userDraft,
                            onUpdate = { userDraft = it },
                            context = context,
                            onContinue = { step = SignupStep.ACCOUNT_TYPE }
                        )

                        SignupStep.ACCOUNT_TYPE -> StepAccountType(
                            onBack = {
                                step = SignupStep.PERSONAL_INFO
                                authViewModel.resetState()
                            },
                            onClientSelected = {
                                userDraft = userDraft.copy(role = Role.CUSTOMER)
                                step = SignupStep.CUSTOMER_SUMMARY
                            },
                            onProviderSelected = {
                                userDraft = userDraft.copy(role = Role.PROVIDER)
                                step = SignupStep.PROVIDER_TYPE
                            }
                        )

                        SignupStep.CUSTOMER_SUMMARY -> StepCustomertSummary(
                            name = userDraft.fullName,
                            email = userDraft.email,
                            onBack = {
                                step = SignupStep.ACCOUNT_TYPE
                                authViewModel.resetState()
                            },
                            onStart = {
                                // Només cridem a la funció register, la navegació es fa a LaunchedEffect
                                authViewModel.register(userDraft, context)
                            }
                        )

                        SignupStep.PROVIDER_TYPE -> StepProviderType(
                            onBack = {
                                step = SignupStep.ACCOUNT_TYPE
                                authViewModel.resetState()
                            },
                            onFreelancerSelected = {
                                userDraft = userDraft.copy(role = Role.FREELANCER)
                                step = SignupStep.FREELANCER_SUMMARY
                            },
                            onEmpresaSelected = {
                                userDraft = userDraft.copy(role = Role.COMPANY_ADMIN)
                                step = SignupStep.COMPANY_INFO
                            }
                        )

                        SignupStep.FREELANCER_SUMMARY -> StepFreelancerSummary(
                            name = userDraft.fullName,
                            email = userDraft.email,
                            onBack = {
                                step = SignupStep.PROVIDER_TYPE
                                authViewModel.resetState()
                            },
                            onStart = {
                                authViewModel.register(userDraft, context)
                            }
                        )

                        SignupStep.COMPANY_INFO -> StepCompanyInfo(
                            companyName = userDraft.companyName,
                            cif = userDraft.cif,
                            companyEmail = userDraft.companyEmail,
                            onCompanyNameChange = { userDraft = userDraft.copy(companyName = it) },
                            onCifChange = { userDraft = userDraft.copy(cif = it) },
                            onCompanyEmailChange = {
                                userDraft = userDraft.copy(companyEmail = it)
                            },
                            onContinue = {
                                val error = validateCompanyInfo(
                                    userDraft.companyName,
                                    userDraft.cif,
                                    userDraft.companyEmail
                                )
                                if (error.isEmpty()) step = SignupStep.COMPANY_CODE
                                else Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )

                        SignupStep.COMPANY_CODE -> StepCompanyCode(
                            companyEmail = userDraft.companyEmail,
                            context = context,
                            onBack = {
                                step = SignupStep.COMPANY_INFO
                                authViewModel.resetState()
                            },
                            onVerify = {
                                userDraft = userDraft.copy(role = Role.COMPANY_ADMIN)
                                step = SignupStep.COMPANY_SUMMARY
                            }
                        )

                        SignupStep.COMPANY_SUMMARY -> StepCompanySummary(
                            companyName = userDraft.companyName,
                            cif = userDraft.cif,
                            companyEmail = userDraft.companyEmail,
                            onBack = {
                                step = SignupStep.COMPANY_CODE
                                authViewModel.resetState()
                            },
                            onStart = {
                                authViewModel.register(userDraft, context)
                            }
                        )
                    }
                }

                // NOU: Indicador de càrrega
                if (authState.isLoading) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandBlue)
                    }
                }

                // NOU: Gestió d'errors
                DisposableEffect(registerError) {
                    if (registerError != null) {
                        Toast.makeText(context, registerError, Toast.LENGTH_LONG).show()
                        registerError = null
                    }
                    onDispose {}
                }

                // Already have an account? Login
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(id = R.string.already_have_account),
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.login),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            modifier = Modifier.clickable {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }

            // mini back arrow
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = configuration.screenHeightDp.dp * 0.38f)
                    .width(configuration.screenWidthDp.dp * 0.22f)
                    .height(configuration.screenHeightDp.dp * 0.07f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        navController.navigate("authScreen") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.go_back),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun StepPersonalInfo(
    userDraft: UserDraft,
    onUpdate: (UserDraft) -> Unit,
    context: Context,
    onContinue: () -> Unit
) {
    var passVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(horizontal = 28.dp, vertical = 36.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_1_personal_info), color = TextGray)
            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = BrandBlue,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = userDraft.fullName,
                onValueChange = { onUpdate(userDraft.copy(fullName = it)) },
                label = { Text(stringResource(id = R.string.full_name), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.full_name_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.username,
                onValueChange = { onUpdate(userDraft.copy(username = it)) },
                label = { Text(stringResource(id = R.string.username_label), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.username_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.email,
                onValueChange = { onUpdate(userDraft.copy(email = it)) },
                label = { Text(stringResource(id = R.string.email_label), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.email_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.password,
                onValueChange = { onUpdate(userDraft.copy(password = it)) },
                label = { Text(stringResource(id = R.string.password_label), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.password_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(icon, contentDescription = null, tint = Color.Black)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.confirmPassword,
                onValueChange = { onUpdate(userDraft.copy(confirmPassword = it)) },
                label = {
                    Text(
                        stringResource(id = R.string.confirm_password_label),
                        color = Color.Black
                    )
                },
                placeholder = {
                    Text(
                        stringResource(id = R.string.confirm_password_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(icon, contentDescription = null, tint = Color.Black)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            AnimatedButton(
                text = stringResource(id = R.string.continue_arrow),
                onClick = {
                    val validation = validateRegister(
                        userDraft.fullName,
                        userDraft.username,
                        userDraft.email,
                        userDraft.password,
                        userDraft.confirmPassword
                    )
                    if (validation.isEmpty()) onContinue()
                    else Toast.makeText(context, validation, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                isOutlined = true
            )
        }
    }
}

// -- StepAccountType
@Composable
fun StepAccountType(
    onBack: () -> Unit,
    onClientSelected: () -> Unit,
    onProviderSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_2_account_type), color = TextGray)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = BrandBlue,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(id = R.string.choose_usage),
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Text(stringResource(id = R.string.select_account_type), color = TextGray)
            Spacer(Modifier.height(24.dp))

            // Client Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onClientSelected() }
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        stringResource(id = R.string.client_title),
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(id = R.string.client_description),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(id = R.string.client_bullet_1),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                        Text(
                            stringResource(id = R.string.client_bullet_2),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                        Text(
                            stringResource(id = R.string.client_bullet_3),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Provider Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onProviderSelected() }
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        stringResource(id = R.string.provider_title),
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(id = R.string.provider_description),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(id = R.string.provider_bullet_1),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                        Text(
                            stringResource(id = R.string.provider_bullet_2),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                        Text(
                            stringResource(id = R.string.provider_bullet_3),
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) { Text(stringResource(id = R.string.back_arrow)) }
        }
    }
}

// -- StepCustomertSummary
@Composable
fun StepCustomertSummary(
    name: String,
    email: String,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_2_account_type), color = TextGray)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 1f },
                color = BrandBlue,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.height(24.dp))

            Text(stringResource(id = R.string.client_ready), color = TextDark)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(id = R.string.name_colon, name), color = TextGray)
            Text(stringResource(id = R.string.email_colon, email), color = TextGray)
            Text(stringResource(id = R.string.account_type_client), color = TextGray)

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(id = R.string.back_arrow))
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(id = R.string.start_rocket), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// -- StepProviderType
@Composable
fun StepProviderType(
    onBack: () -> Unit,
    onFreelancerSelected: () -> Unit,
    onEmpresaSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                stringResource(id = R.string.step_2_provider_type),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                stringResource(id = R.string.provider_personalize),
                color = TextGray
            )
            Spacer(Modifier.height(24.dp))

            // Freelancer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onFreelancerSelected() }
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(id = R.string.freelancer_title),
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(Modifier.height(12.dp))

            // Company
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onEmpresaSelected() }
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(id = R.string.company_title),
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.back_arrow))
            }
        }
    }
}

@Composable
fun StepFreelancerSummary(
    name: String,
    email: String,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_4_summary), color = TextGray)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 1f },
                color = BrandBlue,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.height(16.dp))

            Text(stringResource(id = R.string.freelancer_ready), color = TextDark)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(id = R.string.name_colon, name), color = TextGray)
            Text(stringResource(id = R.string.email_colon, email), color = TextGray)
            Text(stringResource(id = R.string.account_type_freelancer), color = TextGray)

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(id = R.string.back_arrow))
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(id = R.string.start_rocket), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -- StepCompanyInfo
@Composable
fun StepCompanyInfo(
    companyName: String,
    cif: String,
    companyEmail: String,
    onCompanyNameChange: (String) -> Unit,
    onCifChange: (String) -> Unit,
    onCompanyEmailChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(horizontal = 28.dp, vertical = 36.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.create_company_account),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_3_company_details), color = TextGray)
            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = BrandBlue,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = onCompanyNameChange,
                label = { Text(stringResource(id = R.string.company_name), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.company_name_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cif,
                onValueChange = onCifChange,
                label = { Text(stringResource(id = R.string.cif), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.cif_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = companyEmail,
                onValueChange = onCompanyEmailChange,
                label = { Text(stringResource(id = R.string.company_email), color = Color.Black) },
                placeholder = {
                    Text(
                        stringResource(id = R.string.company_email_placeholder),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(stringResource(id = R.string.continue_arrow))
            }
        }
    }
}


@Composable
fun StepCompanyCode(
    companyEmail: String,
    context: Context,
    onBack: () -> Unit,
    onVerify: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(horizontal = 28.dp, vertical = 36.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.verify_company_email),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_4_code_verification), color = TextGray)
            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = BrandBlue,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(id = R.string.verification_sent_to),
                color = TextGray,
                fontSize = 14.sp
            )
            Text(companyEmail, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)

            Spacer(Modifier.height(28.dp))

            OtpCodeInput(
                code = code,
                onCodeChange = { code = it }
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (code.length == 6) {
                        onVerify()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_enter_valid_code),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(stringResource(id = R.string.verify_arrow))
            }


            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack) { Text(stringResource(id = R.string.back_arrow)) }
        }
    }
}


@Composable
fun OtpCodeInput(
    code: String,
    onCodeChange: (String) -> Unit
) {
    OutlinedTextField(
        value = code,
        onValueChange = {
            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                onCodeChange(it)
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp)),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        placeholder = {
            Text(stringResource(id = R.string.enter_code), color = Color.White.copy(alpha = 0.4f))
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = BrandBlue,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.25f),
            cursorColor = BrandBlue,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun StepCompanySummary(
    companyName: String,
    cif: String,
    companyEmail: String,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(horizontal = 28.dp, vertical = 36.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.company_registration_complete),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(stringResource(id = R.string.step_5_summary), color = TextGray)
            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = BrandBlue,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(Modifier.height(24.dp))
            Text(stringResource(id = R.string.company_ready), color = TextDark)
            Spacer(Modifier.height(16.dp))

            Text(stringResource(id = R.string.company_name_colon, companyName), color = TextGray)
            Text(stringResource(id = R.string.cif_colon, cif), color = TextGray)
            Text(stringResource(id = R.string.email_colon, companyEmail), color = TextGray)
            Text(stringResource(id = R.string.account_type_company), color = TextGray)

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(id = R.string.back_arrow))
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(id = R.string.start_rocket), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

