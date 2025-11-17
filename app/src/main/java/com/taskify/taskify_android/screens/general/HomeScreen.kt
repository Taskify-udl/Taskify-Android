package com.taskify.taskify_android.screens.general


import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.taskify.taskify_android.R
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.TopGradientEnd
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.ServiceType
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val user by authViewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("Offers", "Favorites", "Taskify", "Orders", "Settings")

    // 🌈 Animated gradient background (el deixem igual)
    val infiniteTransition = rememberInfiniteTransition(label = "homeBgAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressAnim"
    )

    var sizePx by remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords -> sizePx = coords.size }
    ) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)
        val start = Offset(widthF * (1f - progress), 0f)
        val end = Offset(0f, heightF * progress)

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            BgWhite,
                            BgSecondary,
                            BrandBlue.copy(alpha = 0.12f)
                        ),
                        start = start,
                        end = end
                    )
                )
        )

        // 🔹 Scaffold amb topBar i bottomBar
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = tabs[selectedTab],
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* TODO: Notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = BrandBlue
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Chat */ }) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = BrandBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                BottomNavigationBar(selectedTab) { selectedTab = it }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedTab) {
                    0 -> OffersScreenWithPopup(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        navController = navController
                    )

                    1 -> FavoritesScreen(navController)
                    2 -> CreateServiceScreen(user, authViewModel, navController)
                    3 -> OrdersScreen()
                    4 -> SettingsScreen(navController = navController)
                }
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
    var selectedOffer by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 🔍 Search bar
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

        // 🔹 Service Categories (ikonice koje se pomjeraju s desna na lijevo)
        Text(
            text = "Service Categories",
            fontWeight = FontWeight.SemiBold,
            color = TopGradientEnd
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState(), reverseScrolling = true)
        ) {
            // 🔧 Plumber
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable { navController.navigate("category/Plumber") }
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Plumber",
                        tint = TopGradientEnd,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Plumber", color = Dark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // ⚡ Electrician
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable { navController.navigate("category/Electrician") }
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Electrician",
                        tint = TopGradientEnd,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Electrician", color = Dark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // 🧹 Cleaner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable { navController.navigate("category/Cleaner") }
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Cleaner",
                        tint = TopGradientEnd,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cleaner", color = Dark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // 🎨 Painter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable { navController.navigate("category/Painter") }
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Painter",
                        tint = TopGradientEnd,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Painter", color = Dark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔸 Sponsored Ads (pravougaonici skoro pune širine, vertikalno skrolanje)
        Text("Sponsored Ads", fontWeight = FontWeight.SemiBold, color = TopGradientEnd)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 🧼 SuperClean
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedOffer = "SuperClean - Deep Cleaning Services" }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker1),
                        contentDescription = "SuperClean",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Text(
                        text = "SuperClean - Deep Cleaning Services",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // 🎨 PaintPro
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedOffer = "PaintPro - Interior & Exterior Painting" }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker2),
                        contentDescription = "PaintPro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Text(
                        text = "PaintPro - Interior & Exterior Painting",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // 🔧 QuickFix
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedOffer = "QuickFix - 24/7 Plumbing" }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker3),
                        contentDescription = "QuickFix",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Text(
                        text = "QuickFix - 24/7 Plumbing",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // ⚡ EcoElectric
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedOffer = "EcoElectric - Sustainable Electrical Work" }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker4),
                        contentDescription = "EcoElectric",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Text(
                        text = "EcoElectric - Sustainable Electrical Work",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    // 🪟 Popup za prikaz detalja
    if (selectedOffer != null) {
        AlertDialog(
            onDismissRequest = { selectedOffer = null },
            title = { Text(selectedOffer ?: "", color = TopGradientEnd) },
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
                TextButton(onClick = { selectedOffer = null }) {
                    Text("Close", color = TopGradientEnd)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

// FavoritesScreen
@Composable
fun FavoritesScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 🧰 HandyMan Pro
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { navController.navigate("providerProfile/John Doe") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker3),
                        contentDescription = "John Doe - HandyMan Pro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "John Doe",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Plumber • HandyMan Pro",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ⚡ ElectricFix
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { navController.navigate("providerProfile/Jane Smith") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker4),
                        contentDescription = "Jane Smith - ElectricFix",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Jane Smith",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Electrician • ElectricFix",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 🧼 QuickClean Team
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { navController.navigate("providerProfile/Mike Johnson") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.worker1),
                        contentDescription = "Mike Johnson - QuickClean Team",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Mike Johnson",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Cleaner • QuickClean Team",
                            color = Color.White.copy(alpha = 0.8f),
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "My Orders",
            fontWeight = FontWeight.Bold,
            color = TopGradientEnd,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 🔧 John Doe - Plumber
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F6FA))
                        .border(1.dp, Color(0xFFD1E8FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "John Doe",
                            color = TopGradientEnd,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Plumber",
                            color = Dark.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Date: 14.10.2025",
                            color = Dark.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Time: 14:00",
                            color = Dark.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ⚡ Jane Smith - Electrician
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F6FA))
                        .border(1.dp, Color(0xFFD1E8FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Jane Smith",
                            color = TopGradientEnd,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Electrician",
                            color = Dark.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Date: 15.10.2025",
                            color = Dark.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Time: 10:00",
                            color = Dark.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 🧼 Mike Johnson - Cleaner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F6FA))
                        .border(1.dp, Color(0xFFD1E8FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Mike Johnson",
                            color = TopGradientEnd,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Cleaner",
                            color = Dark.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Date: 16.10.2025",
                            color = Dark.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Time: 09:30",
                            color = Dark.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateServiceScreen(
    user: User? = null,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    // si encara no ha arribat l'usuari
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    Log.d("CreateServiceScreen", "User: $user")

    // Rol del user
    val isProvider = user is Provider

    if (!isProvider) {
        // ❌ CUSTOMER → Missatge "Become a provider"
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    "Become a provider",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = TopGradientEnd
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "To create and offer services, switch your account to Provider.",
                    textAlign = TextAlign.Center,
                    color = Dark.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(26.dp))

                Button(
                    onClick = { navController.navigate("becomeProviderScreen") },
                    colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
                ) {
                    Text("Become Provider", color = Color.White)
                }
            }
        }
    } else {
        // ✅ PROVIDER → Pantalla per gestionar serveis
        ProviderServiceScreen(
            authViewModel = authViewModel
        )
    }
}

@Composable
fun ProviderServiceScreen(authViewModel: AuthViewModel) {

    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    val provider = user as? Provider ?: return

    var showCreateDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ProviderService?>(null) }

    val services = provider.services

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "My Services",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = TopGradientEnd
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
        ) {
            Text("Create New Service", color = Color.White)
        }

        Spacer(Modifier.height(22.dp))

        if (services.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                Alignment.Center
            ) {
                Text("You haven't created any services yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(services) { service ->
                    ServiceCard(
                        service = service,
                        onClick = { serviceToEdit = service }
                    )
                }
            }
        }
    }

    // ============= POPUP CREAR =============
    if (showCreateDialog) {
        ServiceDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, category, desc, price ->
                authViewModel.createService(
                    title = title,
                    category = category.name,
                    description = desc,
                    price = price,
                    context = context,
                    onSuccess = {
                        Toast.makeText(context, "Service created!", Toast.LENGTH_SHORT).show()
                        showCreateDialog = false
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onEdit = { _, _, _, _, _ -> }
        )
    }

    // ============= POPUP EDITAR =============
    if (serviceToEdit != null) {
        val editService = serviceToEdit!!

        ServiceDialog(
            initial = editService,
            onDismiss = { serviceToEdit = null },
            onCreate = { _, _, _, _ -> },
            onEdit = { serv, title, category, desc, price ->
                authViewModel.updateService(
                    serviceToUpdate = serv,
                    newTitle = title,
                    newCategory = category,
                    newDescription = desc,
                    newPrice = price,
                    onSuccess = {
                        Toast.makeText(context, "Service updated!", Toast.LENGTH_SHORT).show()
                        serviceToEdit = null
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun ServiceCard(
    service: ProviderService,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF2F6FF))
            .border(1.dp, TopGradientEnd.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column {
            // NAME
            Text(
                service.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Dark
            )

            Spacer(Modifier.height(6.dp))

            // CATEGORY (safe)
            Text(
                (service.category?.name ?: "OTHER").replace("_", " "),
                fontSize = 14.sp,
                color = TopGradientEnd
            )

            // DESCRIPTION (si existeix)
            if (!service.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    service.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(Modifier.height(10.dp))

            // UPDATED DATE (safe)
            val updatedDate = service.updatedAt?.toLocalDate()?.toString() ?: "N/A"

            Text(
                "Updated: $updatedDate",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDialog(
    initial: ProviderService? = null,
    onDismiss: () -> Unit,
    onCreate: (String, ServiceType, String, Int) -> Unit,
    onEdit: (ProviderService, String, ServiceType, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "") }

    var selectedCategory by remember { mutableStateOf(initial?.category ?: ServiceType.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Create New Service" else "Edit Service",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )

                // ---------------- ExposedDropdownMenuBox ----------------
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor() // important!
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ServiceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    selectedCategory = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            price = input
                        }
                    },
                    label = { Text("Price (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p: Int = price.toInt()
                if (initial == null) onCreate(title, selectedCategory, description, p)
                else onEdit(initial, title, selectedCategory, description, p)
            }) {
                Text(if (initial == null) "Create" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


// Settings
@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 👤 Profilna slika centrirana
        Image(
            painter = painterResource(id = R.drawable.profilepic),
            contentDescription = "User Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE7F1FB))
                .border(2.dp, Color(0xFFD1E8FF), CircleShape)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Account Settings",
            fontWeight = FontWeight.Bold,
            color = TopGradientEnd,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Opcije
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ⚙️ Settings
            item {
                SettingItem("Settings") {
                    // navController.navigate("settingsDetail") // primjer
                }
            }

            // 👤 Profile Info
            item {
                SettingItem("Profile Info") {
                    navController.navigate("profileInfoScreen")
                }
            }

            // 🔐 Security
            item {
                SettingItem("Security") {
                    // navController.navigate("security")
                }
            }

            // 📊 Dashboard
            item {
                SettingItem("Dashboard") {
                    // navController.navigate("dashboard")
                }
            }

            // 🚪 Logout
            item {
                SettingItem("Logout", highlight = true) {
                    // handle logout logic
                }
            }
        }
    }
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