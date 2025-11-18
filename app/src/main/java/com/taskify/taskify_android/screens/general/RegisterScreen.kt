package com.taskify.taskify_android.screens.general

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
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
import kotlinx.coroutines.delay
import androidx.compose.material3.TextField
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

    // 🔹 Animacija pozadine
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 🎨 Gradient pozadine
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
                            onBack = { step = SignupStep.PERSONAL_INFO },
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
                            onBack = { step = SignupStep.ACCOUNT_TYPE },
                            onStart = {
                                authViewModel.register(userDraft, context)
                                navController.navigate("homeScreen") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )

                        SignupStep.PROVIDER_TYPE -> StepProviderType(
                            onBack = { step = SignupStep.ACCOUNT_TYPE },
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
                            onBack = { step = SignupStep.PROVIDER_TYPE },
                            onStart = {
                                authViewModel.register(userDraft, context)
                                navController.navigate("homeScreen") {
                                    popUpTo("register") { inclusive = true }
                                }
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
                            onBack = { step = SignupStep.COMPANY_INFO },
                            onVerify = {
                                userDraft = userDraft.copy(role = Role.COMPANY_ADMIN)
                                step = SignupStep.COMPANY_SUMMARY
                            }
                        )

                        SignupStep.COMPANY_SUMMARY -> StepCompanySummary(
                            companyName = userDraft.companyName,
                            cif = userDraft.cif,
                            companyEmail = userDraft.companyEmail,
                            onBack = { step = SignupStep.COMPANY_CODE },
                            onStart = {
                                authViewModel.register(userDraft, context)
                                navController.navigate("homeScreen") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                // 🔹 Already have an account? Login
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
                            "Already have an account?",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Login",
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
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Step 1 of 2 - Personal Information", color = TextGray)
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
                label = { Text("Full Name", color = Color.Black) },
                placeholder = { Text("Your full name", color = Color.Black.copy(alpha = 0.6f)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.username,
                onValueChange = { onUpdate(userDraft.copy(username = it)) },
                label = { Text("Username", color = Color.Black) },
                placeholder = {
                    Text(
                        "At least 4 characters",
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
                label = { Text("Email", color = Color.Black) },
                placeholder = { Text("example@email.com", color = Color.Black.copy(alpha = 0.6f)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.password,
                onValueChange = { onUpdate(userDraft.copy(password = it)) },
                label = { Text("Password", color = Color.Black) },
                placeholder = { Text("********", color = Color.Black.copy(alpha = 0.6f)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(icon, null, tint = Color.Black)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userDraft.confirmPassword,
                onValueChange = { onUpdate(userDraft.copy(confirmPassword = it)) },
                label = { Text("Confirm Password", color = Color.Black) },
                placeholder = { Text("Repeat password", color = Color.Black.copy(alpha = 0.6f)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(icon, null, tint = Color.Black)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            AnimatedButton(
                text = "Continue →",
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
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Step 2 of 2 - Account Type", color = TextGray)
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
                "Choose how you want to use Taskify",
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Text("Select the account type that best fits your needs", color = TextGray)
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
                    Text("🧑‍💼 Client", fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "I want to hire professional services for my needs",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("• Search and hire services", color = TextGray, fontSize = 14.sp)
                        Text("• Manage orders", color = TextGray, fontSize = 14.sp)
                        Text("• Leave reviews", color = TextGray, fontSize = 14.sp)
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
                    Text("🧑‍🔧 Provider", fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "I offer professional services and want to get clients",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("• Publish services", color = TextGray, fontSize = 14.sp)
                        Text("• Receive requests", color = TextGray, fontSize = 14.sp)
                        Text("• Manage your business", color = TextGray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) { Text("← Back") }
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
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Step 2 of 2 - Account Type", color = TextGray)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 1f },
                color = BrandBlue,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.height(24.dp))

            Text("✅ Your client account is ready!", color = TextDark)
            Spacer(Modifier.height(16.dp))
            Text("Name: $name", color = TextGray)
            Text("Email: $email", color = TextGray)
            Text("Account Type: Client 🧭", color = TextGray)

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("← Back")
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
                    Text("🚀 Start", fontWeight = FontWeight.Bold)
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
                "Step 2 of 2 - Provider Type",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                "This selection will help personalize your experience.",
                color = TextGray
            )
            Spacer(Modifier.height(24.dp))

            // 🧑‍💼 Freelancer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onFreelancerSelected() } // ✅ amb parèntesis
                    .padding(16.dp)
            ) {
                Text("🧑‍💼 Self-employed", fontWeight = FontWeight.Bold, color = TextDark)
            }

            Spacer(Modifier.height(12.dp))

            // 🏢 Company
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onEmpresaSelected() }
                    .padding(16.dp)
            ) {
                Text("🏢 Company", fontWeight = FontWeight.Bold, color = TextDark)
            }

            Spacer(Modifier.height(24.dp))

            // 🔙 Back Button (ara dins del Column)
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Back")
            }
        }
    }
}

// -- StepFreelancerSummary
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
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Step 4 of 4 – Summary", color = TextGray)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 1f },
                color = BrandBlue,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.height(16.dp))

            Text("✅ Your freelancer provider account is ready!", color = TextDark)
            Spacer(Modifier.height(12.dp))
            Text("Name: $name", color = TextGray)
            Text("Email: $email", color = TextGray)
            Text("Account Type: Freelancer Provider 🧭", color = TextGray)

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("← Back")
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
                    Text("🚀 Start", fontWeight = FontWeight.Bold)
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
                "Create Company Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text("Step 3 of 5 - Company Details", color = TextGray)
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
                label = { Text("Company Name", color = Color.Black) },
                placeholder = {
                    Text(
                        "e.g. Taskify Solutions",
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
                label = { Text("CIF", color = Color.Black) },
                placeholder = { Text("e.g. B12345678", color = Color.Black.copy(alpha = 0.6f)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = companyEmail,
                onValueChange = onCompanyEmailChange,
                label = { Text("Company Email", color = Color.Black) },
                placeholder = {
                    Text(
                        "e.g. contact@techify.com",
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
                Text("Continue →")
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
                "Verify Company Email",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text("Step 4 of 5 - Code Verification", color = TextGray)
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
            Text("A verification code has been sent to:", color = TextGray, fontSize = 14.sp)
            Text(companyEmail, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 15.sp)

            Spacer(Modifier.height(28.dp))

            // ✨ Nou component OTP
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
                            "Please enter a valid 6-digit code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Verify →")
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack) { Text("← Back") }
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
            .clip(RoundedCornerShape(12.dp)), // ✅ clip en lloc de background per no bloquejar focus
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
            Text("Enter code", color = Color.White.copy(alpha = 0.4f))
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
                "Company Registration Complete",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text("Step 5 of 5 - Summary", color = TextGray)
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
            Text("✅ Your company provider account is ready!", color = TextDark)
            Spacer(Modifier.height(16.dp))

            Text("Company Name: $companyName", color = TextGray)
            Text("CIF: $cif", color = TextGray)
            Text("Email: $companyEmail", color = TextGray)
            Text("Account Type: Company Provider 🏢", color = TextGray)

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("← Back")
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
                    Text("🚀 Start", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
