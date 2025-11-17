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

    // Editable states
    var name: String? by remember { mutableStateOf("") }
    var email: String? by remember { mutableStateOf("") }
    var username: String? by remember { mutableStateOf("") }
    var phone: String? by remember { mutableStateOf("") }
    var address: String? by remember { mutableStateOf("") }
    var city: String? by remember { mutableStateOf("") }
    var country: String? by remember { mutableStateOf("") }
    var zipCode: String? by remember { mutableStateOf("") }
    var bio: String? by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }

    // Inicialitzar estats amb el user
    LaunchedEffect(user) {
        user?.let {
            name = it.fullName
            email = it.email
            username = it.username
            phone = it.phoneNumber

            if (it is Customer) {
                address = it.address
                city = it.city
                country = it.country
                zipCode = it.zipCode
            }
            if (it is Provider) {
                bio = it.bio
                experienceYears = it.experienceYears.toString()
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
                                val updates = mutableMapOf(
                                    "fullName" to name,
                                    "username" to username,
                                    "email" to email,
                                    "phoneNumber" to phone
                                )

                                if (user is Customer) {
                                    updates["address"] = address
                                    updates["city"] = city
                                    updates["country"] = country
                                    updates["zipCode"] = zipCode
                                }
                                if (user is Provider) {
                                    updates["bio"] = bio
                                    updates["experienceYears"] =
                                        (experienceYears.toIntOrNull() ?: 0).toString()
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
                    ProfileFieldsSection(
                        user = user!!,
                        isEditing = isEditing,
                        name = name, onName = { name = it },
                        username = username, onUsername = { username = it },
                        email = email, onEmail = { email = it },
                        phone = phone, onPhone = { phone = it },
                        address = address, onAddress = { address = it },
                        city = city, onCity = { city = it },
                        country = country, onCountry = { country = it },
                        zipCode = zipCode, onZipCode = { zipCode = it },
                        bio = bio, onBio = { bio = it },
                        experienceYears = experienceYears, onExperienceYears = { experienceYears = it }
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
    name: String?, onName: (String?) -> Unit,
    username: String?, onUsername: (String?) -> Unit,
    email: String?, onEmail: (String?) -> Unit,
    phone: String?, onPhone: (String?) -> Unit,
    address: String?, onAddress: (String?) -> Unit,
    city: String?, onCity: (String?) -> Unit,
    country: String?, onCountry: (String?) -> Unit,
    zipCode: String?, onZipCode: (String?) -> Unit,
    bio: String?, onBio: (String?) -> Unit,
    experienceYears: String, onExperienceYears: (String) -> Unit
) {
    // Camps comuns
    ProfileEditableField("Full Name", name, onName, enabled = isEditing)
    Spacer(Modifier.height(12.dp))
    ProfileEditableField("Username", username, onUsername, enabled = isEditing)
    Spacer(Modifier.height(12.dp))
    ProfileEditableField("Email", email, onEmail, enabled = isEditing)
    Spacer(Modifier.height(12.dp))
    ProfileEditableField("Phone Number", phone, onPhone, enabled = isEditing)
    Spacer(Modifier.height(12.dp))

    // Camps Customer/Provider
    if (user is Customer) {
        ProfileEditableField("Address", address, onAddress, enabled = isEditing)
        Spacer(Modifier.height(12.dp))
        ProfileEditableField("City", city, onCity, enabled = isEditing)
        Spacer(Modifier.height(12.dp))
        ProfileEditableField("Country", country, onCountry, enabled = isEditing)
        Spacer(Modifier.height(12.dp))
        ProfileEditableField("Zip Code", zipCode, onZipCode, enabled = isEditing)
        Spacer(Modifier.height(12.dp))
    }

    if (user is Provider) {
        ProfileEditableField("Bio", bio, onBio, enabled = isEditing)
        Spacer(Modifier.height(12.dp))
        ProfileEditableField(
            "Experience Years",
            experienceYears,
            onExperienceYears,
            enabled = isEditing
        )
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
