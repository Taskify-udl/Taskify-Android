package com.taskify.taskify_android.screens.general

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.taskify.taskify_android.ui.theme.*
import com.taskify.taskify_android.screens.general.homescreen.CreateServiceScreen
import com.taskify.taskify_android.screens.general.homescreen.FavoritesScreen
import com.taskify.taskify_android.screens.general.homescreen.OffersScreen
import com.taskify.taskify_android.screens.general.homescreen.OrdersScreen
import com.taskify.taskify_android.screens.general.homescreen.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val user by authViewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val tabs = listOf(
        context.getString(R.string.tab_offers),
        context.getString(R.string.tab_favorites),
        context.getString(R.string.tab_taskify),
        context.getString(R.string.tab_orders),
        context.getString(R.string.tab_settings)
    )

    // 🌈 Animated gradient background
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

        // 🔹 Scaffold sa topBar i bottomBar
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
                                contentDescription = context.getString(R.string.notifications),
                                tint = BrandBlue
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Chat */ }) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = context.getString(R.string.chat),
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
                    0 -> OffersScreen(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        navController = navController,
                        authViewModel = authViewModel
                    )

                    1 -> FavoritesScreen(navController)
                    2 -> CreateServiceScreen(user, authViewModel, navController)
                    3 -> OrdersScreen(authViewModel = authViewModel)
                    4 -> SettingsScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
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