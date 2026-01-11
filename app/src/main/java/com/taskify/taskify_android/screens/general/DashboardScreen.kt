package com.taskify.taskify_android.screens.general

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskify.taskify_android.data.models.auth.ContractResponse
import com.taskify.taskify_android.data.models.entities.ContractStatus
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.ui.theme.BgSecondary
import com.taskify.taskify_android.ui.theme.BgWhite
import com.taskify.taskify_android.ui.theme.BrandBlue
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.TopGradientEnd

@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel) {
    val contractsState by authViewModel.contractsState.collectAsState()
    val serviceListState by authViewModel.serviceListState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.getMyContracts()
        authViewModel.getServices()
    }

    // --- Càlculs de dades ---
    val allContracts = (contractsState as? Resource.Success)?.data ?: emptyList()
    val allServices = (serviceListState as? Resource.Success)?.data ?: emptyList()

    val totalEarnings = allContracts.filter { it.status == ContractStatus.FINISHED }
        .sumOf { it.price?.toDoubleOrNull() ?: 0.0 }
    val activeContracts = allContracts.count { it.status == ContractStatus.ACTIVE }

    // --- Background Animation Logic (Fidels a la teva estructura) ---
    val infiniteTransition = rememberInfiniteTransition(label = "dashBgAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(10000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "progressAnim"
    )
    var sizePx by remember { mutableStateOf(IntSize(0, 0)) }

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { sizePx = it.size }) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)

        // Fons animat amb gradient
        Box(modifier = Modifier.matchParentSize().background(
            Brush.linearGradient(
                colors = listOf(BgWhite, BgSecondary, BrandBlue.copy(alpha = 0.12f)),
                start = Offset(widthF * (1f - progress), 0f), end = Offset(0f, heightF * progress)
            )
        ))

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // --- TOP BAR AMB BACK BUTTON ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TopGradientEnd)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Advanced Statistics",
                    fontWeight = FontWeight.Bold,
                    color = TopGradientEnd,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // --- KPI CARDS ---
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            label = "Earnings",
                            value = "${String.format("%.2f", totalEarnings)}€",
                            icon = Icons.Default.MonetizationOn,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Active",
                            value = activeContracts.toString(),
                            icon = Icons.Default.Work,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // --- MONTHLY TREND CHART (Visual representation) ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Dark)
                            Spacer(Modifier.height(16.dp))
                            // Simulem el gràfic de la web amb barres de Compose
                            BarChartPlaceholder()
                        }
                    }
                }

                // --- UPCOMING APPOINTMENTS ---
                item {
                    Text("Next Appointments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Dark)
                    Spacer(Modifier.height(8.dp))
                    val upcoming = allContracts.filter { it.status == ContractStatus.ACCEPTED }
                    if (upcoming.isEmpty()) {
                        Text("No upcoming appointments found.", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        upcoming.take(3).forEach { AppointmentItem(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(BrandBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Dark)
            }
        }
    }
}
@Composable
fun AppointmentItem(contract: ContractResponse) {
    // Extraiem el dia de la data "2026-01-11"
    val day = contract.startDate.split("-").lastOrNull() ?: "00"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona estil calendari de la web
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(BgSecondary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("JAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    Text(day, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Dark)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(text = contract.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Dark)
                Text(text = contract.userName, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun BarChartPlaceholder() {
    val barHeights = listOf(0.2f, 0.3f, 0.1f, 0.4f, 0.6f, 0.9f) // Simula la tendència ascendent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEach { heightMultiplier ->
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight(heightMultiplier)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BrandBlue, BrandBlue.copy(alpha = 0.4f))
                        )
                    )
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Oct", fontSize = 10.sp, color = Color.Gray)
        Text("Dec", fontSize = 10.sp, color = Color.Gray)
        Text("Jan", fontSize = 10.sp, color = BrandBlue, fontWeight = FontWeight.Bold)
    }
}