package com.taskify.taskify_android.screens.general.homescreen

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.TopGradientEnd
import java.util.Locale

// Settings Screen
@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 🌙 TEME: Čita se iz ThemeState singletona
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 👤 Profilna slika centrirana
        Image(
            painter = painterResource(id = R.drawable.profilepic),
            contentDescription = stringResource(R.string.user_logo),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE7F1FB))
                .border(2.dp, Color(0xFFD1E8FF), CircleShape)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.account_settings),
            fontWeight = FontWeight.Bold,
            color = TopGradientEnd,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Opcije u listi
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 👤 Profile Info
            item {
                SettingItem(stringResource(R.string.profile_info)) {
                    navController.navigate("profileInfoScreen")
                }
            }

            // 🔐 Security
            item {
                SettingItem(stringResource(R.string.security)) {
                    navController.navigate("securityScreen")
                }
            }

            // 📊 Dashboard
            item { SettingItem(stringResource(R.string.dashboard)) {} }

            // 🌐 Promjena jezika
            item {
                SettingItem(stringResource(R.string.language_Change)) {
                    showLanguageDialog = true
                }
            }

            // 🌙 DARK MODE SA PREKIDAČEM
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F6FA))
                        .border(
                            1.dp,
                            Color(0xFFD1E8FF),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(start = 20.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dark Mode",
                        color = Dark,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ❌ LOGOUT
            item {
                SettingItem(stringResource(R.string.logout), highlight = true) {
                    showLogoutDialog = true
                }
            }
        }
    }

    // ===============================================
    // ➡️ DIALOG ZA POTVRDU ODJAVE
    // ===============================================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.dialog_logout_title)) },
            text = { Text(stringResource(R.string.dialog_logout_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false

                        // 1. Pozovi logiku odjave
                        authViewModel.logout(context)

                        // 2. Navigacija na auth ekran
                        navController.navigate("authScreen") {
                            popUpTo("homeScreen") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.dialog_logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.dialog_logout_cancel))
                }
            }
        )
    }

    // ===============================================
    // Dialog za izbor jezika
    // ===============================================
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    Text(
                        "English",
                        modifier = Modifier.clickable {
                            updateLocale(
                                context,
                                "en"
                            ); showLanguageDialog = false
                        })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Català",
                        modifier = Modifier.clickable {
                            updateLocale(
                                context,
                                "ca"
                            ); showLanguageDialog = false
                        })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Español",
                        modifier = Modifier.clickable {
                            updateLocale(
                                context,
                                "es"
                            ); showLanguageDialog = false
                        })
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun updateLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
    } else {
        @Suppress("DEPRECATION")
        config.locale = locale
    }

    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

// 🔹 Komponenta za svaku stavku u listi
@Composable
fun SettingItem(title: String, highlight: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (highlight) Color(0xFFFFEDED) else Color(0xFFF1F6FA))
            .border(
                1.dp,
                if (highlight) Color(0xFFFFC5C5) else Color(0xFFD1E8FF),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(start = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            color = if (highlight) Color(0xFFD32F2F) else Dark,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

// ===============================================
// 🔒 SECURITY SCREEN (iz vašeg brancha)
// ===============================================
@Composable
fun SecurityScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.security_title),
            fontWeight = FontWeight.Bold,
            color = TopGradientEnd,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // ---------------- Opcije ----------------
        SettingItem(
            title = stringResource(R.string.security_change_password),
            onClick = { showChangePasswordDialog = true }
        )
        Spacer(Modifier.height(8.dp))

        // Aktivne sesije
        SettingItem(
            title = stringResource(R.string.security_active_sessions),
            onClick = {
                Toast.makeText(
                    context,
                    context.getString(R.string.security_active_sessions_placeholder),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        Spacer(Modifier.height(16.dp))

        Divider()
        Spacer(Modifier.height(16.dp))

        // ⚠️ Brisanje naloga
        SettingItem(
            title = stringResource(R.string.security_delete_account),
            highlight = true,
            onClick = { showDeleteAccountDialog = true }
        )
    }

    // ===============================================
    // DIJALOG ZA PROMENU LOZINKE
    // ===============================================
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            authViewModel = authViewModel,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    // ===============================================
    // DIJALOG ZA BRISANJE NALOGA
    // ===============================================
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            authViewModel = authViewModel,
            navController = navController,
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

// ===============================================
// 🔑 DIJALOG: PROMENA LOZINKE
// ===============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.security_change_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text(stringResource(R.string.security_old_password)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.security_new_password)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.security_confirm_password)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.security_password_match_error),
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }
                    if (oldPassword.isBlank() || newPassword.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.security_password_empty_error),
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    /*authViewModel.changePassword(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.security_password_success), Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )*/
                }
            ) {
                Text(stringResource(R.string.security_save_changes))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_logout_cancel)) }
        }
    )
}

// ===============================================
// 🗑️ DIJALOG: BRISANJE NALOGA
// ===============================================
@Composable
fun DeleteAccountDialog(
    authViewModel: AuthViewModel,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.security_delete_account_title)) },
        text = {
            Column {
                Text(stringResource(R.string.security_delete_account_message))
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.security_delete_account_warning),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    /*authViewModel.deleteAccount(
                        context = context,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.security_delete_account_success), Toast.LENGTH_LONG).show()
                            onDismiss()
                            navController.navigate("authScreen") {
                                popUpTo("homeScreen") { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )*/
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.security_delete_account_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_logout_cancel)) }
        }
    )
}