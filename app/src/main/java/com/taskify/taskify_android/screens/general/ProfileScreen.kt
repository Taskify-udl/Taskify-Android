package com.taskify.taskify_android.screens.general

import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.data.models.entities.Customer
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current

    val user by authViewModel.currentUser.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()

    LaunchedEffect(Unit) { authViewModel.loadProfile() }

    var isEditing by remember { mutableStateOf(false) }

    // NOU: Instància mutable per a l'edició (Única font de veritat per a l'edició)
    var editableDraft by remember { mutableStateOf(UserDraft()) }

    // Inicialitzar estats amb el user (Càrrega robusta)
    LaunchedEffect(user) {
        user?.let { currentUser ->
            // Carregar dades comunes a UserDraft
            editableDraft = editableDraft.copy(
                fullName = currentUser.fullName ?: "",
                email = currentUser.email ?: "",
                username = currentUser.username ?: "",
                phoneNumber = currentUser.phoneNumber ?: "",
                role = currentUser.role,
            )

            // Carregar camps de Customer/Provider
            if (currentUser is Customer) {
                editableDraft = editableDraft.copy(
                    address = currentUser.address ?: "",
                )
            }
            if (currentUser is Provider) {
                // Carreguem directament a UserDraft, utilitzant els valors de l'objecte Provider
                editableDraft = editableDraft.copy(
                    bio = currentUser.bio ?: ""
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (profileState is Resource.Success) {
                        TextButton(onClick = {
                            if (isEditing) {
                                // SAVE MODE

                                // Split fullName en firstName i lastName per a l'API
                                val fullName = editableDraft.fullName
                                val parts = fullName.split(" ", limit = 2)
                                val firstName = parts.getOrElse(0) { "" }
                                val lastName = parts.getOrElse(1) { "" }

                                val updates = mutableMapOf<String, Any?>(
                                    "first_name" to firstName, // FIX: Utilitzem snake_case per a l'API
                                    "last_name" to lastName,   // FIX: Utilitzem snake_case per a l'API
                                    "username" to editableDraft.username,
                                    "email" to editableDraft.email,
                                    "phone" to editableDraft.phoneNumber
                                )

                                // Afegir camps de Customer/Provider des del draft
                                if (user is Customer) {
                                    updates["location"] = editableDraft.address

                                }
                                if (user is Provider) {
                                    updates["bio"] = editableDraft.bio // Obtenim de UserDraft
                                }

                                authViewModel.updateProfile(
                                    updates = updates,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Profile updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isEditing = false
                                    },
                                    onError = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                isEditing = true
                            }
                        }) {
                            Text(if (isEditing) "Save" else "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->

        when (profileState) {
            is Resource.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }

            is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error loading profile", color = Color.Red)
            }

            is Resource.Success -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(2.dp, BrandBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profilepic),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }

                item {
                    // Adaptem ProfileFieldsSection per usar només el Draft
                    ProfileFieldsSection(
                        user = user!!,
                        isEditing = isEditing,

                        draft = editableDraft,
                        onDraftChange = { editableDraft = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileFieldsSection(
    user: User,
    isEditing: Boolean,
    draft: UserDraft,
    onDraftChange: (UserDraft) -> Unit
) {
    // Camps comuns
    ProfileEditableField(
        "Full Name",
        draft.fullName,
        { onDraftChange(draft.copy(fullName = it)) },
        enabled = isEditing
    )
    Spacer(Modifier.height(12.dp))
    ProfileEditableField(
        "Username",
        draft.username,
        { onDraftChange(draft.copy(username = it)) },
        enabled = isEditing
    )
    Spacer(Modifier.height(12.dp))
    ProfileEditableField(
        "Email",
        draft.email,
        { onDraftChange(draft.copy(email = it)) },
        enabled = isEditing
    )
    Spacer(Modifier.height(12.dp))
    ProfileEditableField(
        "Phone Number",
        draft.phoneNumber,
        { onDraftChange(draft.copy(phoneNumber = it)) },
        enabled = isEditing
    )
    Spacer(Modifier.height(12.dp))

    // Camps Customer/Provider
    if (user is Customer) {
        ProfileEditableField(
            "Address",
            draft.address,
            { onDraftChange(draft.copy(address = it)) },
            enabled = isEditing
        )
        Spacer(Modifier.height(12.dp))
    }

    if (user is Provider) {
        // Camps de Provider (usant el camp del Draft)
        ProfileEditableField(
            "Bio",
            draft.bio,
            { onDraftChange(draft.copy(bio = it)) },
            enabled = isEditing
        )
        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(12.dp))
        ProfileEditableField("Average Rating", user.averageRating.toString(), {}, enabled = false)
        Spacer(Modifier.height(12.dp))
        ProfileEditableField("Verified", if (user.isVerified) "Yes" else "No", {}, enabled = false)
        Spacer(Modifier.height(12.dp))
        ProfileEditableField("Services Count", user.services.size.toString(), {}, enabled = false)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun ProfileEditableField(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    val textColor = Color.Black
    val labelColor = Color.DarkGray
    val backgroundColor = if (enabled) Color.White.copy(alpha = 0.1f) else Color.White

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        if (!enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                if (value != null) {
                    Text(text = value, color = textColor, fontSize = 16.sp)
                }
            }
        } else {
            if (value != null) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = BrandBlue,
                        focusedIndicatorColor = BrandBlue,
                        unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}
