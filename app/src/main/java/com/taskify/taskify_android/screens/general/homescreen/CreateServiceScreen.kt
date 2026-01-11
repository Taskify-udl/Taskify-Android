package com.taskify.taskify_android.screens.general.homescreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.taskify.taskify_android.R
import com.taskify.taskify_android.data.models.entities.API_BASE_URL
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.ServiceType
import com.taskify.taskify_android.data.models.entities.ServiceTypeLookup
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.data.repository.Resource
import com.taskify.taskify_android.logic.viewmodels.AuthViewModel
import com.taskify.taskify_android.ui.theme.Dark
import com.taskify.taskify_android.ui.theme.TopGradientEnd
import com.taskify.taskify_android.ui.theme.taskifyOutlinedTextFieldColors
import java.util.Locale

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
                val fullUrl =
                    if (relativePath.startsWith("/")) API_BASE_URL + relativePath else relativePath
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Required)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                // ---------------- ExposedDropdownMenuBox ----------------
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory.name.replace("_", " ")
                            .lowercase(Locale.getDefault()).capitalize(),
                        onValueChange = {}, readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        ServiceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        type.name.replace("_", " ").lowercase(Locale.getDefault())
                                            .capitalize()
                                    )
                                },
                                onClick = { selectedCategory = type; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = price, onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            price = input
                        }
                    },
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
                        Toast.makeText(
                            context,
                            "Please fill in title and price.",
                            Toast.LENGTH_SHORT
                        ).show()
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