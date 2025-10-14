package com.taskify.taskify_android.screens.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun CategoryScreen(
    categoryName: String,
    navController: NavController
) {
    val offers = listOf(
        "$categoryName Service 1",
        "$categoryName Service 2",
        "$categoryName Service 3"
    )
    var selectedOffer by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TopGradientEnd
                        )
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            offers.forEach { offer ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp))
                        .clickable { selectedOffer = offer },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        offer,
                        modifier = Modifier.padding(start = 16.dp),
                        color = Dark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Popup for order details
    if (selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { selectedOffer = null },
            title = { Text(text = selectedOffer ?: "", color = TopGradientEnd) },
            text = {
                Column {
                    Text("Description: Demo description for $selectedOffer")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Price: €50")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reviews: ★★★★☆")
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedOffer = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
                ) {
                    Text("Book Service", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOffer = null }) {
                    Text("Close", color = TopGradientEnd)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
