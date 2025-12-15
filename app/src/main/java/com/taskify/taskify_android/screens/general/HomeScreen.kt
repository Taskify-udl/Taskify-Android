package com.taskify.taskify_android.screens.general

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import com.taskify.taskify_android.data.models.entities.API_BASE_URL
import com.taskify.taskify_android.data.models.entities.ServiceTypeLookup
import com.taskify.taskify_android.data.repository.Resource
import java.util.Locale
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale.getDefault

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
                    3 -> OrdersScreen()
                    4 -> SettingsScreen(navController = navController, authViewModel = authViewModel)
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

@Composable
fun ServiceOfferCard(service: ProviderService, onContractClick: () -> Unit, onClick: () -> Unit) {
    // 1. Lògica per obtenir la URL ABSOLUTA
    val imageUrl = service.images
        ?.firstOrNull()
        ?.image
        ?.let { relativePath ->
            // Si la ruta és relativa (/media/services/...), construïm la URL absoluta
            if (relativePath.startsWith("/")) API_BASE_URL + relativePath else relativePath
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // 🖼️ GESTIÓ DE LA IMATGE
        val painter = rememberAsyncImagePainter(
            model = imageUrl, // URL absoluta del servei (o null)
            error = painterResource(id = R.drawable.worker1) // Imatge per defecte (worker1)
        )

        Image(
            painter = painter,
            contentDescription = service.name,
            contentScale = ContentScale.Crop, // Assegura que la imatge ompli l'àrea
            modifier = Modifier.matchParentSize()
        )

        // Gradient overlay i text
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
                text = service.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "€${service.price} / ${service.categoryNames?.firstOrNull() ?: "N/A"}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        // 🔘 Book Button overlay
        Button(
            onClick = onContractClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TopGradientEnd,
                contentColor = Color.White
            )
        ) {
            Text("Book Now", fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val allServicesState by authViewModel.serviceListState.collectAsState()
    var selectedOffer by remember { mutableStateOf<ProviderService?>(null) }
    var showContractDialog by remember { mutableStateOf(false) }
    var serviceToContract by remember { mutableStateOf<ProviderService?>(null) }
    val context = LocalContext.current

    // Učitavanje servisa
    LaunchedEffect(Unit) {
        authViewModel.getServices()
    }

    // Logika za potvrdu rezervacije
    val onContractConfirmed: (LocalDate, LocalTime, String) -> Unit = { date, time, description ->
        Toast.makeText(
            context,
            "Booking confirmed for ${serviceToContract?.name ?: "service"} on ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} at ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
            Toast.LENGTH_LONG
        ).show()
        showContractDialog = false
        serviceToContract = null
    }

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

        // 🔹 Service Categories
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

        // 🔸 Dostupni servisi
        Text("Available Services", fontWeight = FontWeight.SemiBold, color = TopGradientEnd)
        Spacer(modifier = Modifier.height(8.dp))

        when (allServicesState) {
            is Resource.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f), Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            }

            is Resource.Error -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f), Alignment.Center
                ) {
                    Text(
                        "Error loading offers: ${(allServicesState as Resource.Error).message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is Resource.Success -> {
                val services = (allServicesState as Resource.Success).data


                val filteredServices = services.filter { service ->
                    searchQuery.isBlank() ||
                            service.name.contains(searchQuery, ignoreCase = true) ||
                            (service.description?.contains(searchQuery, ignoreCase = true) ?: false)
                }

                if (filteredServices.isEmpty()) { // Utilitzem filteredServices
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f), Alignment.Center
                    ) {
                        Text("No services found.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredServices) { service ->
                            ServiceOfferCard(
                                service = service,
                                onContractClick = {
                                    serviceToContract = service
                                    showContractDialog = true
                                },
                                onClick = { selectedOffer = service }
                            )
                        }
                    }
                }
            }
        }
    }

    // 🪟 Popup za detalje servisa
    if (selectedOffer != null) {
        val offer = selectedOffer!!
        AlertDialog(
            onDismissRequest = { selectedOffer = null },
            title = { Text(offer.name, color = TopGradientEnd) },
            text = {
                Column {
                    Text("Description: ${offer.description ?: "No description provided."}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Price: €${offer.price}")
                    Spacer(modifier = Modifier.height(8.dp))
                    val categoryDisplayName = offer.categoryNames?.firstOrNull() ?: "N/A"
                    Text("Category: $categoryDisplayName")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("offerDetail/${offer.name}")
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

    // 🆕 Contract Dialog za rezervaciju
    if (showContractDialog && serviceToContract != null) {
        ContractDialog(
            service = serviceToContract!!,
            onDismiss = {
                showContractDialog = false
                serviceToContract = null
            },
            onConfirm = onContractConfirmed
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDialog(
    service: ProviderService,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalTime, String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Book ${service.name}",
                fontWeight = FontWeight.Bold,
                color = TopGradientEnd,
                fontSize = 18.sp
            )
            Text(
                text = "€${service.price} / ${service.categoryNames?.firstOrNull() ?: "N/A"}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 📅 Date Selection
                OutlinedTextField(
                    value = selectedDate.format(dateFormatter),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Date") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick date",
                            tint = TopGradientEnd
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                // 🕐 Time Selection
                OutlinedTextField(
                    value = selectedTime.format(timeFormatter),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Time") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Pick time",
                            tint = TopGradientEnd
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                )

                // 📝 Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Additional Notes (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // 💰 Price Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Price:", fontWeight = FontWeight.Medium)
                        Text(
                            "€${service.price}",
                            fontWeight = FontWeight.Bold,
                            color = TopGradientEnd,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, selectedTime, description) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
            ) {
                Text("Confirm Booking", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = TopGradientEnd)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )

    // 📅 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (1000 * 60 * 60 * 24))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TopGradientEnd)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 🕐 Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = TopGradientEnd)
                }
            },
            title = {
                Text(
                    "Select Time",
                    modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)
                )
            }
        ) {
            TimePicker(state = timePickerState)
        }
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

    // Rol del user
    val isProvider = user is Provider || user.role.toString() == "PROVIDER"
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

// ====================================================================
// NOU COMPONENT: GESTIÓ I PREVISUALITZACIÓ DE LA IMATGE
// ====================================================================

@Composable
fun ServiceImageContainer(
    imageUri: Uri?,
    modifier: Modifier = Modifier,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }
    )

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
            .clickable { launcher.launch("image/*") }
            .border(2.dp, TopGradientEnd.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = "Add Photo",
                    tint = Dark.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text("Add Photo", color = Dark.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        } else {
            // 🚩 FIX: Utilitzem Coil directament tant per a URI locals com per a URL remotes.
            // Coil gestiona la conversió de content:// Uri a Bitmap de manera segura i asíncrona.

            // Nota: Aquí s'assumeix que imageUri conté la URI/URL que es vol carregar.
            // Si la Uri ve de l'API (URL), és una String. Si ve del selector, és una Uri. Coil les maneja totes dues.
            val modelToLoad: Any = imageUri

            // Si la URI és una URL remota, s'haurà resolt prèviament al ServiceDialog.

            val painter = rememberAsyncImagePainter(
                model = modelToLoad,
                // Utilitzem l'error com a placeholder final si la càrrega falla
                error = painterResource(id = R.drawable.worker1)
            )

            Image(
                painter = painter,
                contentDescription = "Service Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// ====================================================================
// SERVICE DIALOG (AFEGIT GESTIÓ DE URI)
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDialog(
    initial: ProviderService? = null,
    onDismiss: () -> Unit,
    // 🚩 SIGNATURA ACTUALITZADA PER INCLOURE URI
    onCreate: (title: String, category: ServiceType, description: String, price: Int, imageUri: Uri?) -> Unit,
    onEdit: (service: ProviderService, title: String, category: ServiceType, description: String, price: Int, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = initial != null

    var title by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            initial?.categoryNames?.firstOrNull()?.let { categoryName ->
                ServiceTypeLookup.nameToEnum(categoryName)
            } ?: ServiceType.OTHER
        )
    }

    // 🚩 ESTAT PER A LA URI (amb URL absoluta si existeix inicialment)
    var imageUri by remember {
        mutableStateOf<Uri?>(
            initial?.images?.firstOrNull()?.image?.let { relativePath ->
                val fullUrl = if (relativePath.startsWith("/")) API_BASE_URL + relativePath else relativePath
                Uri.parse(fullUrl)
            }
        )
    }

    // Configuració de colors per als OutlinedTextFields
    val textFieldColors = taskifyOutlinedTextFieldColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditMode) "Edit Service" else "Create New Service", color = TopGradientEnd)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // 🖼️ GESTIÓ DE LA IMATGE
                ServiceImageContainer(
                    imageUri = imageUri,
                    onImageSelected = { newUri -> imageUri = newUri }
                )
                Spacer(Modifier.height(16.dp))

                // ... (Camps de Text, Categoria, Preu)
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title (Required)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth()
                )
                // ---------------- ExposedDropdownMenuBox ----------------
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory.name.replace("_", " ").lowercase(Locale.getDefault()).capitalize(),
                        onValueChange = {}, readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ServiceType.entries.forEach { type ->
                            DropdownMenuItem(text = { Text(type.name.replace("_", " ").lowercase(Locale.getDefault()).capitalize()) },
                                onClick = { selectedCategory = type; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = price, onValueChange = { input -> if (input.all { it.isDigit() }) { price = input } },
                    label = { Text("Price (€)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p: Int = price.toIntOrNull() ?: 0
                    if (title.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Please fill in title and price.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isEditMode && initial != null) {
                        // 🚩 NOVA CRIDA PER EDITAR AMB URI
                        onEdit(initial, title, selectedCategory, description, p, imageUri)
                    } else {
                        // 🚩 NOVA CRIDA PER CREAR AMB URI
                        onCreate(title, selectedCategory, description, p, imageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
            ) {
                Text(if (isEditMode) "Save Changes" else "Create Service", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TopGradientEnd) }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}


// ====================================================================
// PROVIDER SERVICE SCREEN (INCLOU LA VISUALITZACIÓ DELS SERVEIS DE L'USUARI)
// ====================================================================

@Composable
fun ProviderServiceScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    val serviceListState by authViewModel.serviceListState.collectAsState()
    // Si l'usuari no és Provider o és nul, sortim.
    val provider = user as? Provider ?: return

    var showCreateDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ProviderService?>(null) }

    // 1. CÀRREGA INICIAL
    LaunchedEffect(Unit) {
        // Assegurem que la llista de serveis del provider es carregui
        authViewModel.loadProviderServices()
    }

    // Obtenim els serveis directament de l'objecte Provider de l'estat local
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

        // 2. GESTIÓ D'ESTATS DE CÀRREGA I MOSTRAR LA LLISTA
        when (serviceListState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        "Error loading services: ${(serviceListState as Resource.Error).message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is Resource.Success -> {
                // 🚩 FIX: Mostrem la llista de serveis obtinguda del Provider local
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
        }
    }

    // ============= POPUP CREAR (La lògica per passar la Uri i el Context) =============
    if (showCreateDialog) {
        ServiceDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, category, desc, price, imageUri ->
                authViewModel.createService(
                    title = title,
                    category = category,
                    description = desc,
                    price = price,
                    imageUri = imageUri,
                    context = context, // PASSANT CONTEXT
                    onSuccess = {
                        Toast.makeText(context, "Service created!", Toast.LENGTH_SHORT).show()
                        showCreateDialog = false
                        authViewModel.loadProviderServices() // Refresquem la llista
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onEdit = { _, _, _, _, _, _ -> }
        )
    }

    // ============= POPUP EDITAR (La lògica per passar la Uri) =============
    if (serviceToEdit != null) {
        val editService = serviceToEdit!!

        ServiceDialog(
            initial = editService,
            onDismiss = { serviceToEdit = null },
            onCreate = { _, _, _, _, _ -> },
            onEdit = { serv, title, category, desc, price, imageUri ->
                authViewModel.updateService(
                    serviceToUpdate = serv,
                    newTitle = title,
                    newCategory = category,
                    newDescription = desc,
                    newPrice = price,
                    newImageUri = imageUri, // PASSANT LA NOVA URI
                    onSuccess = {
                        Toast.makeText(context, "Service updated!", Toast.LENGTH_SHORT).show()
                        serviceToEdit = null
                        authViewModel.loadProviderServices() // Refresquem la llista
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
            .background(Color.White) // Fons blanc per contrast
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp)) // Bordura subtil
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 🏷️ Títol i Fletxa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    service.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Dark
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Edit",
                    tint = TopGradientEnd,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // ------------------ Dades del Servei ------------------

            // 💰 Preu
            val price = service.price.toString() ?: "0"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = "Price",
                    tint = TopGradientEnd,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Price: $price €",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TopGradientEnd
                )
            }

            Spacer(Modifier.height(6.dp))

            // 🗂️ Categoria
            val categoryDisplayName = service.categoryNames?.firstOrNull() ?: "OTHER"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = "Category",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    categoryDisplayName,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // 📝 Descripció
            if (!service.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Description",
                        tint = Dark.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        service.description,
                        fontSize = 14.sp,
                        color = Dark.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

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
                    Text("English", modifier = Modifier.clickable { updateLocale(context, "en"); showLanguageDialog = false })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Català", modifier = Modifier.clickable { updateLocale(context, "ca"); showLanguageDialog = false })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Español", modifier = Modifier.clickable { updateLocale(context, "es"); showLanguageDialog = false })
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
            onClick = { Toast.makeText(context, context.getString(R.string.security_active_sessions_placeholder), Toast.LENGTH_SHORT).show() }
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
                        Toast.makeText(context, context.getString(R.string.security_password_match_error), Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (oldPassword.isBlank() || newPassword.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.security_password_empty_error), Toast.LENGTH_LONG).show()
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