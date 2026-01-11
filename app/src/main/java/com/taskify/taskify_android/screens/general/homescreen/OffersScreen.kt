package com.taskify.taskify_android.screens.general.homescreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.taskify.taskify_android.R
import com.taskify.taskify_android.data.models.entities.API_BASE_URL
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.BrandBlue
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.TopGradientEnd
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val allServicesState by authViewModel.serviceListState.collectAsState()

    // Estats per controlar els diàlegs
    var selectedOffer by remember { mutableStateOf<ProviderService?>(null) } // Per veure detalls
    var serviceToContract by remember { mutableStateOf<ProviderService?>(null) } // Per contractar

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.getServices()
    }

    // Lògica de confirmació del contracte (connectada a l'API)
    val onContractConfirmed: (LocalDate, LocalTime, String) -> Unit = { date, time, description ->
        serviceToContract?.let { service ->
            val price = service.price.toDoubleOrNull() ?: 0.0
            authViewModel.createContract(
                serviceId = service.id,
                date = date,
                time = time,
                price = price,
                description = description,
                onSuccess = {
                    Toast.makeText(context, "Booking request sent!", Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    Log.e("OffersScreen", "Error creating contract: $error")
                }
            )
        }
        serviceToContract = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // ... (Codi de Search Bar i Categories igual) ...
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

        // 🔸 Llista de Serveis
        Text("Available Services", fontWeight = FontWeight.SemiBold, color = TopGradientEnd)
        Spacer(modifier = Modifier.height(8.dp))

        when (allServicesState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    Text("Error: ${(allServicesState as Resource.Error).message}", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val services = (allServicesState as Resource.Success).data
                val filteredServices = services.filter { service ->
                    searchQuery.isBlank() ||
                            service.name.contains(searchQuery, ignoreCase = true) ||
                            (service.description?.contains(searchQuery, ignoreCase = true) ?: false)
                }

                if (filteredServices.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                        Text("No services found.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(filteredServices) { service ->
                            ServiceOfferCard(
                                service = service,
                                onClick = { selectedOffer = service }
                            )
                        }
                    }
                }
            }
        }
    }

    // ... (Els diàlegs de detalls i contractació es mantenen igual que a la resposta anterior)
    // 🪟 1. DIÀLEG DE DETALLS (selectedOffer)
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
                // 🚩 BOTÓ "BOOK SERVICE" DINS DEL DETALL
                Button(
                    onClick = {
                        selectedOffer = null
                        serviceToContract = offer
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

    // 🆕 2. DIÀLEG DE CONTRACTACIÓ (serviceToContract)
    if (serviceToContract != null) {
        ContractDialog(
            service = serviceToContract!!,
            onDismiss = { serviceToContract = null },
            onConfirm = onContractConfirmed
        )
    }
}

@Composable
fun ServiceOfferCard(service: ProviderService, onClick: () -> Unit) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDialog(
    service: ProviderService,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalTime, String) -> Unit
) {
    // Estats del formulari
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var description by remember { mutableStateOf("") }

    // Estats per controlar la visibilitat dels Pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // 1. EL DIÀLEG PRINCIPAL DEL FORMULARI
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "Book ${service.name}",
                    fontWeight = FontWeight.Bold,
                    color = TopGradientEnd,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "€${service.price} / ${service.categoryNames?.firstOrNull() ?: "N/A"}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 📅 Camp de Data (AMB BOX PER ASSEGURAR EL CLICK)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = selectedDate.format(dateFormatter),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Date") },
                        trailingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick date", tint = TopGradientEnd)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false, // Desactivat perquè no surti el teclat
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = TopGradientEnd,
                            disabledLeadingIconColor = TopGradientEnd,
                            disabledTrailingIconColor = TopGradientEnd,
                            // Important: Assegurar que el contenidor no tapi el click si fos el cas
                            disabledContainerColor = Color.Transparent
                        )
                    )
                }

                // 🕐 Camp d'Hora (AMB BOX PER ASSEGURAR EL CLICK)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                ) {
                    OutlinedTextField(
                        value = selectedTime.format(timeFormatter),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Time") },
                        trailingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick time", tint = TopGradientEnd)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false, // Desactivat perquè no surti el teclat
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = TopGradientEnd,
                            disabledTrailingIconColor = TopGradientEnd,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                }

                // 📝 Descripció
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Additional Notes (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // 💰 Preu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Price:", fontWeight = FontWeight.Medium)
                        Text("€${service.price}", fontWeight = FontWeight.Bold, color = TopGradientEnd, fontSize = 20.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, selectedTime, description) },
                colors = ButtonDefaults.buttonColors(containerColor = TopGradientEnd)
            ) {
                Text("Confirm Booking", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TopGradientEnd)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )

    // 2. ELS PICKERS

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
