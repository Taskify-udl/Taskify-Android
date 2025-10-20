package com.taskify.taskify_android.screens.general

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.PrimaryColor
import com.taskify.taskify_android.ui.theme.TopGradientEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val user by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("Offers", "Favorites", "Taskify", "Orders", "Settings")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = tabs[selectedTab],
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { navController.navigate("chat") }) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = TopGradientEnd
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (selectedTab) {
                0 -> OffersScreenWithPopup(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    navController = navController
                )
                1 -> FavoritesScreen(navController = navController)
                2 -> BecomeProviderScreen()
                3 -> OrdersScreen()
                4 -> SettingsScreen()
            }
        }
    }
}

// Bottom navigation bar
@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabIcons = listOf(
            Icons.Default.Home,
            Icons.Default.Favorite,
            Icons.Default.AccountCircle,
            Icons.Default.List,
            Icons.Default.Settings
        )

        tabIcons.forEachIndexed { index, icon ->
            IconButton(onClick = { onTabSelected(index) }) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selectedTab == index) TopGradientEnd else Dark.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// OffersScreen with popup and navigation for category/offerDetail
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreenWithPopup(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    navController: NavController
) {
    val sponsoredAds = listOf("Plumber Fix", "Electric Master", "Quick Clean", "PaintPro")
    val categories = listOf("Plumber", "Electrician", "Cleaner", "Painter")
    val plumberProfiles = listOf(
        "Plumber Expert - Available 24/7",
        "FastFix Plumbing Services",
        "AquaPro - Professional Plumber"
    )

    var selectedOffer by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search services...", color = Dark.copy(alpha = 0.6f)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp)),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.contains("plumber", ignoreCase = true)) {
            Text("Search results:", fontWeight = FontWeight.SemiBold, color = Dark)
            Spacer(modifier = Modifier.height(8.dp))
            plumberProfiles.forEach { profile ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp))
                        .clickable { selectedOffer = profile },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        profile,
                        modifier = Modifier.padding(start = 16.dp),
                        color = Dark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Sponsored Ads
            Text("Sponsored Ads", fontWeight = FontWeight.SemiBold, color = TopGradientEnd)
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                sponsoredAds.forEach { offer ->
                    OfferCardItem(offerName = offer) { selectedOffer = offer }
                }
            }

            // Service Categories
            Text("Service Categories", fontWeight = FontWeight.SemiBold, color = TopGradientEnd)
            Column(modifier = Modifier.padding(top = 8.dp)) {
                categories.forEach { category ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp))
                            .clickable {
                                navController.navigate("category/$category")
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(start = 16.dp),
                            color = Dark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Popup for detailed offers
    if (selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { selectedOffer = null },
            title = { Text(text = selectedOffer ?: "", color = TopGradientEnd) },
            text = {
                Column {
                    Text("Description: This is a demo description for $selectedOffer")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Price: €50")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reviews: ★★★★☆")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("offerDetail/${selectedOffer}")
                        selectedOffer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
                ) {
                    Text("Book Service", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOffer = null }) { Text("Close", color = TopGradientEnd) }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}


@Composable
fun OfferCardItem(offerName: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp)
            .padding(8.dp)
            .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = offerName, color = Dark, fontWeight = FontWeight.Medium)
    }
}

// FavoritesScreen
@Composable
fun FavoritesScreen(navController: NavController) {
    // Lista providera: ime tima, ime osobe, profesija
    val favoriteProviders = listOf(
        Triple("HandyMan Pro", "John Doe", "Plumber"),
        Triple("ElectricFix", "Jane Smith", "Electrician"),
        Triple("QuickClean Team", "Mike Johnson", "Cleaner")
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Your Favorite Providers",
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        favoriteProviders.forEach { (teamName, personName, profession) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp))
                    .clickable {
                        // Navigacija na profil providera
                        navController.navigate("providerProfile/$personName")
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    // Profilna slika
                    Image(
                        painter = painterResource(id = R.drawable.usericon),
                        contentDescription = "User Logo",
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = personName,
                            color = TopGradientEnd,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = profession,
                            color = Dark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}



// OrdersScreen
@Composable
fun OrdersScreen() {
    // Reservations simulation
    val orders = listOf(
        listOf("John Doe", "Plumber", "14.10.2025", "14:00"),
        listOf("Jane Smith", "Electrician", "15.10.2025", "10:00"),
        listOf("Mike Johnson", "Cleaner", "16.10.2025", "09:30")
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("My Orders", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        orders.forEach { order ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {
                    // Profile picture
                    Image(
                        painter = painterResource(id = R.drawable.usericon),
                        contentDescription = "User Logo",
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(verticalArrangement = Arrangement.Center) {
                        Text(order[0], color = TopGradientEnd, fontWeight = FontWeight.Bold, fontSize = 16.sp) // Provider
                        Text(order[1], color = Dark, fontWeight = FontWeight.Medium, fontSize = 14.sp) // Profession
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Date: ${order[2]} • Time: ${order[3]}", color = Dark.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


// Logo / Become Provider
@Composable
fun BecomeProviderScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text("Become a provider", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TopGradientEnd)
    }
}

// Settings
@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        // Profilna slika
        Image(
            painter = painterResource(id = R.drawable.usericon),
            contentDescription = "User Logo",
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        val options = listOf("Profile Info","Security","Privacy","Become a provider")
        options.forEach { opt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFD1E8FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterStart
            ){
                Text(opt, modifier = Modifier.padding(start = 16.dp), color = Dark, fontWeight = FontWeight.Medium)
            }
        }
    }
}
