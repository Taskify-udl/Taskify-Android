package com.taskify.taskify_android.screens.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.PrimaryColor
import com.taskify.taskify_android.ui.theme.TopGradientEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerName: String,
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf("services") } // "services" ili "reviews"

    val services = listOf("Plumbing", "Electric Work", "Cleaning")
    val reviews = listOf("★★★★☆ Great service!", "★★★★★ Excellent!", "★★★☆☆ Average")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(providerName, color = PrimaryColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TopGradientEnd)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Profilna slika + ime i profesija
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray, RoundedCornerShape(40.dp))
                        .clickable { /* kasnije možeš dodati navigaciju */ }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(providerName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TopGradientEnd)
                    Text("Plumber", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Dark)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kratka deskripcija
            Text(
                text = "Experienced professional offering top-quality services. Reliable and punctual.",
                color = Dark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dugmad za Services i Reviews
            Row {
                Button(
                    onClick = { selectedTab = "services" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "services") TopGradientEnd else Color(0xFFD1E8FF)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Services", color = if (selectedTab == "services") Color.White else Dark)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { selectedTab = "reviews" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "reviews") TopGradientEnd else Color(0xFFD1E8FF)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reviews", color = if (selectedTab == "reviews") Color.White else Dark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dinamički sadržaj
            if (selectedTab == "services") {
                Column {
                    services.forEach { service ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(vertical = 4.dp)
                                .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(service, modifier = Modifier.padding(start = 16.dp), color = Dark, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                Column {
                    reviews.forEach { review ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(vertical = 4.dp)
                                .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(review, modifier = Modifier.padding(start = 16.dp), color = Dark)
                        }
                    }
                }
            }
        }
    }
}
