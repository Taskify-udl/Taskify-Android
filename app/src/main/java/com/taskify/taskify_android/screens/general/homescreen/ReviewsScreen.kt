package com.taskify.taskify_android.screens.general.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.PrimaryColor
import com.taskify.taskify_android.ui.theme.TopGradientEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    offerName: String,
    navController: NavController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booking Details", color = PrimaryColor, fontWeight = FontWeight.Bold) },
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

            // Provider Name sa profilnom slikom
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profilna slika
                Image(
                    painter = painterResource(id = R.drawable.usericon),
                    contentDescription = "User Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            navController.navigate("providerProfile/John Doe")
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    // Ime providera
                    Text(
                        "John Doe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TopGradientEnd,
                        modifier = Modifier.clickable {
                            navController.navigate("providerProfile/John Doe")
                        }
                    )
                    Text("Plumber", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Dark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offer / Service Name
            Text(
                text = "Service: $offerName",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Dark
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "Description: This is a demo description for $offerName. Service will be delivered professionally and on time.",
                color = Dark
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Reviews
            Text(
                text = "Reviews:",
                fontWeight = FontWeight.SemiBold,
                color = TopGradientEnd
            )
            Column(modifier = Modifier.padding(top = 8.dp)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Customer ${i + 1}: ★★★★☆ Great service!",
                            modifier = Modifier.padding(start = 16.dp),
                            color = Dark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact / Book Button
            Button(
                onClick = {
                    // Navigation for chat screen
                    navController.navigate("chat")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
            ) {
                Text("Contact Provider", color = Color.White)
            }
        }
    }
}
