package com.taskify.taskify_android.screens.general.homescreen

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.taskify.taskify_android.data.models.auth.ContractResponse
import com.taskify.taskify_android.data.models.entities.ContractStatus
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.logic.generateQrCode
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.taskify.taskify_android.logic.viewmodels.ChatViewModel

@Composable
fun OrdersScreen(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController
) {
    val contractsState by authViewModel.contractsState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) } // Requests per defecte

    LaunchedEffect(Unit) {
        authViewModel.getMyContracts()
        authViewModel.getServices()
        chatViewModel.fetchConversations()
    }

    // Lògica de comptadors per a la UI
    val allContracts = (contractsState as? Resource.Success)?.data ?: emptyList()
    val upcomingCount =
        allContracts.count { it.status == ContractStatus.ACCEPTED || it.status == ContractStatus.ACTIVE }
    val requestsCount = allContracts.count { it.status == ContractStatus.PENDING }
    val rejectedCount = allContracts.count { it.status == ContractStatus.REJECTED }

    val filteredContracts = remember(contractsState, selectedTab) {
        when (selectedTab) {
            0 -> allContracts.filter { it.status == ContractStatus.ACCEPTED || it.status == ContractStatus.ACTIVE }
            1 -> allContracts.filter { it.status == ContractStatus.PENDING }
            2 -> allContracts.filter {
                it.status == ContractStatus.FINISHED ||
                        it.status == ContractStatus.REJECTED ||
                        it.status == ContractStatus.CANCELLED
            }

            else -> emptyList()
        }
    }

    // --- Background Animation ---
    val infiniteTransition = rememberInfiniteTransition(label = "homeBgAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
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
            .onGloballyPositioned { sizePx = it.size }) {
        val widthF = sizePx.width.toFloat().coerceAtLeast(1f)
        val heightF = sizePx.height.toFloat().coerceAtLeast(1f)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(BgWhite, BgSecondary, BrandBlue.copy(alpha = 0.12f)),
                        start = Offset(widthF * (1f - progress), 0f),
                        end = Offset(0f, heightF * progress)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // --- TOP SUMMARY CARDS (Estil Web) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    label = "PENDING",
                    count = requestsCount,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "ACCEPTED",
                    count = upcomingCount,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "REJECTED",
                    count = rejectedCount,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- TABS AMB BADGES DINÀMICS (>0) ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                contentColor = TopGradientEnd,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TopGradientEnd
                    )
                },
                divider = {}
            ) {
                OrderTab(
                    title = "Upcoming",
                    count = upcomingCount,
                    isSelected = selectedTab == 0
                ) { selectedTab = 0 }
                OrderTab(
                    title = "Requests",
                    count = requestsCount,
                    isSelected = selectedTab == 1
                ) { selectedTab = 1 }
                OrderTab(
                    title = "History",
                    count = null,
                    isSelected = selectedTab == 2
                ) { selectedTab = 2 }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LIST CONTENT ---
            when (contractsState) {
                is Resource.Loading -> Box(
                    Modifier.fillMaxSize(),
                    Alignment.Center
                ) { CircularProgressIndicator(color = BrandBlue) }

                is Resource.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Error loading orders", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                is Resource.Success -> {
                    if (filteredContracts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text("No bookings found in this section.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredContracts) { contract ->
                                ContractItemCard(
                                    contract = contract,
                                    isProvider = currentUser is Provider,
                                    authViewModel = authViewModel,
                                    chatViewModel = chatViewModel,
                                    navController = navController

                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun OrderTab(title: String, count: Int?, isSelected: Boolean, onClick: () -> Unit) {
    Tab(selected = isSelected, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TopGradientEnd else Color.Gray
            )
            if (count != null && count > 0) { // 🚩 Badge només si > 0
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFFF4D6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB08900)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(
                count.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Dark
            )
        }
    }
}

@Composable
fun ContractItemCard(
    contract: ContractResponse,
    isProvider: Boolean,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController
) {
    val statusColor = contract.status.getDisplayColor()
    val statusText = contract.status.getDisplayName()
    val context = LocalContext.current

    // --- ANIMACIÓ DE PAMPALLUGUES (BLINKING) ---
    val infiniteTransition = rememberInfiniteTransition(label = "blinkingStatus")
    val alphaAnim by if (contract.status == ContractStatus.ACTIVE) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    var showProviderCodes by remember { mutableStateOf(false) }
    var showClientVerify by remember { mutableStateOf(false) }
    var verifyIsStart by remember { mutableStateOf(true) }

    if (showProviderCodes) {
        ProviderCodesDialog(contract = contract) { showProviderCodes = false }
    }

    if (showClientVerify) {
        ClientVerifyDialog(
            contract = contract,
            isStart = verifyIsStart,
            authViewModel = authViewModel
        ) { showClientVerify = false }
    }

    var showServiceDetail by remember { mutableStateOf(false) }

    // Nou diàleg de detall
    if (showServiceDetail) {
        ServiceDetailDialog(
            serviceId = contract.serviceId,
            authViewModel = authViewModel,
            onDismiss = { showServiceDetail = false }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contract.serviceName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Dark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isProvider) "Client: ${contract.userName}" else "Professional: ${contract.userName}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            // Determinem l'ID de l'altre usuari segons el rol
                            val otherUserId: Long? = if (isProvider) {
                                contract.userId.toLong()
                            } else {
                                (authViewModel.serviceListState.value as? Resource.Success)?.data
                                    ?.find { it.id == contract.serviceId }?.providerId
                            }

                            if (otherUserId != null) {
                                // Cridem a la funció que ara ja inclou el "get_or_create"
                                chatViewModel.navigateToChatWithUser(
                                    otherUserId = otherUserId,
                                    onSuccess = { conversationId ->
                                        navController.navigate("chatDetail/$conversationId")
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                // Error de seguretat o càrrega de dades
                                Toast.makeText(
                                    context,
                                    "Service provider details not loaded",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.1f * alphaAnim),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.graphicsLayer(alpha = alphaAnim)
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFF0F0F0)
            )

            Column {
                Text(text = "Start date: ${contract.startDate}", fontSize = 12.sp, color = Dark)
                Text(
                    text = "Agreed price: ${contract.price} €",
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓ DE BOTONS DINÀMICS CORREGIDA ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (contract.status) {
                    ContractStatus.PENDING -> {
                        if (isProvider) {
                            Button(
                                onClick = {
                                    authViewModel.updateContractStatus(
                                        contract,
                                        ContractStatus.ACCEPTED
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF00A36C
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) { Text("Accept", fontSize = 12.sp) }

                            Button(
                                onClick = {
                                    authViewModel.updateContractStatus(
                                        contract,
                                        ContractStatus.REJECTED
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) { Text("Reject", fontSize = 12.sp) }
                        } else {
                            Button(
                                onClick = {
                                    authViewModel.updateContractStatus(
                                        contract,
                                        ContractStatus.CANCELLED
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) { Text("Cancel Request", fontSize = 12.sp) }
                        }
                    }

                    ContractStatus.ACCEPTED -> {
                        OutlinedButton(
                            onClick = { showServiceDetail = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("View Service", fontSize = 12.sp, maxLines = 1)
                        }

                        if (isProvider) {
                            Button(
                                onClick = { showProviderCodes = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Dark),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Codes", fontSize = 12.sp, maxLines = 1)
                            }
                        } else {
                            Button(
                                onClick = {
                                    authViewModel.updateContractStatus(
                                        contract,
                                        ContractStatus.CANCELLED
                                    )
                                },
                                modifier = Modifier.weight(0.7f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text("Cancel", fontSize = 12.sp, maxLines = 1) }

                            Button(
                                onClick = {
                                    verifyIsStart = true
                                    showClientVerify = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF00A36C
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text("Start", fontSize = 12.sp, maxLines = 1) }
                        }
                    }

                    ContractStatus.ACTIVE -> {
                        OutlinedButton(
                            onClick = { showServiceDetail = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("View Service", fontSize = 12.sp, maxLines = 1)
                        }

                        if (!isProvider) {
                            Button(
                                onClick = {
                                    verifyIsStart = false
                                    showClientVerify = true
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF2196F3
                                    )
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text("Finish Service", fontSize = 12.sp, maxLines = 1) }
                        } else {
                            Button(
                                onClick = { showProviderCodes = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Dark),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text("End Code", fontSize = 12.sp, maxLines = 1) }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ProviderCodesDialog(contract: ContractResponse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text("Service Codes", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Show these codes to the client to manage the service steps.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Start Code
                    CodeItem(
                        label = "START CODE",
                        code = contract.startCodeAlpha ?: "---",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    // End Code
                    CodeItem(
                        label = "END CODE",
                        code = contract.endCodeAlpha ?: "---",
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
fun CodeItem(label: String, code: String, color: Color, modifier: Modifier = Modifier) {
    // Generem el QR només si el codi és vàlid
    val qrBitmap = remember(code) {
        if (code != "---") generateQrCode(code) else null
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )

        Spacer(Modifier.height(8.dp))

        // QR Container
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code $label",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Alphanumeric code background
        Surface(
            color = BrandBlue.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = code,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = BrandBlue,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun ClientVerifyDialog(
    contract: ContractResponse,
    isStart: Boolean,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var codeText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    // --- GESTIÓ DE PERMISOS ---
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                isScanning = true
            } else {
                error = "Camera permission is required to scan QR"
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (!isScanning) {
                Button(
                    onClick = {
                        authViewModel.verifyContractCode(
                            contract.id, codeText, isStart,
                            onSuccess = onDismiss,
                            onError = { error = it }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStart) Color(
                            0xFF00A36C
                        ) else Color.Red
                    )
                ) { Text(if (isStart) "Confirm Start" else "Confirm End") }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (isScanning) isScanning = false else onDismiss() }) {
                Text(if (isScanning) "Cancel Scan" else "Cancel")
            }
        },
        title = {
            Text(
                if (isStart) "Start Service" else "End Service",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isScanning && hasCameraPermission) {
                    // Vista de la càmera per escanejar
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, BrandBlue, RoundedCornerShape(12.dp))
                    ) {
                        QRScannerView(onCodeScanned = {
                            codeText = it
                            isScanning = false
                        })
                    }
                    Text(
                        "Align the QR code inside the frame",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text("Enter the code provided by the professional:")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        TextField(
                            value = codeText,
                            onValueChange = { codeText = it.uppercase() },
                            placeholder = { Text("ABC123") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(Modifier.width(8.dp))

                        // Botó per obrir la càmera amb lògica de permisos
                        IconButton(
                            onClick = {
                                if (hasCameraPermission) {
                                    isScanning = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = BrandBlue.copy(
                                    alpha = 0.1f
                                )
                            )
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = "Scan QR",
                                tint = BrandBlue
                            )
                        }
                    }
                    if (error != null) Text(error!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        }
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerView(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    @OptIn(ExperimentalGetImage::class)
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { code ->
                                    // Evitem múltiples escanejos ràpids
                                    onCodeScanned(code)
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("ScannerView", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ServiceDetailDialog(
    serviceId: Int,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val serviceListState by authViewModel.serviceListState.collectAsState()

    // Carreguem els serveis si l'estat està buit o en error
    LaunchedEffect(Unit) {
        if (serviceListState !is Resource.Success) {
            authViewModel.getServices()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Service Information", fontWeight = FontWeight.Bold) },
        text = {
            when (val state = serviceListState) {
                is Resource.Loading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }

                is Resource.Success -> {
                    val service = state.data.find { it.id == serviceId }
                    if (service != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = service.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Text(
                                text = "Description:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            service.description?.let {
                                Text(
                                    text = it,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row {
                                Text(
                                    text = "Base Price: ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(text = "${service.price} €", color = Color.Gray)
                            }
                        }
                    } else {
                        Text("Service details not found.", color = Color.Red)
                    }
                }

                is Resource.Error -> Text("Error loading service: ${state.message}")
                else -> {}
            }
        }
    )
}