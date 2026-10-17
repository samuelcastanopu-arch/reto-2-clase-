package com.divisas.simulator.presentation.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.presentation.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onNavigateToTrading: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    val profileCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            authViewModel.updateUserPhotoFromCamera(context, bitmap)
        }
    }

    val profilePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            profileCameraLauncher.launch(null)
        }
    }

    fun launchProfileCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            profileCameraLauncher.launch(null)
        } else {
            profilePermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Iniciar Sesión, 1 = Registrarse

    // Campos de Login
    var loginEmail by remember { mutableStateOf("usuario@divisas.com") }
    var loginPassword by remember { mutableStateOf("user123") }

    // Campos de Registro
    var regFirstName by remember { mutableStateOf("") }
    var regLastName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.CLIENT) }

    val darkCardColor = Color(0xFF101726)
    val darkBorderColor = Color(0xFF1E293B)
    val inputBgColor = Color(0xFF090D16)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Encabezado
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "DIVISAS SIMULATOR 🪙",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFF8FAFC),
            letterSpacing = 1.sp
        )
        Text(
            text = "Plataforma Contable de Divisas • Auditoría 2.5% • Finnhub Pro",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mensaje de Error
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF280B10)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⚠️ $msg",
                        color = Color(0xFFFCA5A5),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Si ya hay usuario autenticado, mostrar tarjeta de sesión activa
        if (currentUser != null) {
            val user = currentUser!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, darkBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = darkCardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (user.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Foto de perfil del cliente",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFF38BDF8), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (user.isAdmin) Color(0xFF7C3AED) else Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (user.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón para tomar foto del cliente con el hardware de la cámara
                    OutlinedButton(
                        onClick = { launchProfileCamera() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (user.photoUrl.isNotBlank()) "📸 Actualizar Foto con Cámara" else "📸 Tomar Foto del Cliente (Cámara)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge de Rol
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (user.isAdmin) Color(0xFF2E1065) else Color(0xFF082F49)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(
                            1.dp,
                            if (user.isAdmin) Color(0xFF8B5CF6) else Color(0xFF0284C7),
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            text = if (user.isAdmin) "🛡️ ROL: ADMINISTRADOR (AUDITOR)" else "👤 ROL: USUARIO (COMPRA / VENTA)",
                            color = if (user.isAdmin) Color(0xFFC4B5FD) else Color(0xFF7DD3FC),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para alternar rol rápido para evaluación
                    OutlinedButton(
                        onClick = {
                            val nextRole = if (user.isAdmin) UserRole.CLIENT else UserRole.ADMIN
                            authViewModel.switchRoleForDemo(nextRole)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (user.isAdmin) "Cambiar a rol Usuario (Cliente)" else "Cambiar a rol Administrador (Auditor)",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Acceso a la vista correspondiente
                    Button(
                        onClick = {
                            if (user.isAdmin) onNavigateToAdmin() else onNavigateToTrading()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isAdmin) Color(0xFF7C3AED) else Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (user.isAdmin) "Ir al Panel de Auditoría" else "Ir a Compra y Venta",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cerrar Sesión")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        } else {
            // Formulario de Autenticación (Tabs: Iniciar Sesión / Registrarse)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, darkBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = darkCardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF090D16),
                        contentColor = Color(0xFF38BDF8),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF38BDF8)
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, darkBorderColor, RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                authViewModel.clearError()
                            },
                            text = {
                                Text(
                                    "Iniciar Sesión",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) Color(0xFF38BDF8) else Color(0xFF64748B)
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                authViewModel.clearError()
                            },
                            text = {
                                Text(
                                    "Registrarse",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) Color(0xFF38BDF8) else Color(0xFF64748B)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFF8FAFC),
                        unfocusedTextColor = Color(0xFFCBD5E1),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = Color(0xFF64748B),
                        focusedLeadingIconColor = Color(0xFF38BDF8),
                        unfocusedLeadingIconColor = Color(0xFF64748B),
                        cursorColor = Color(0xFF38BDF8)
                    )

                    if (selectedTab == 0) {
                        // VISTA INICIAR SESIÓN
                        Text(
                            text = "Acceso a la Plataforma",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(10.dp)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(10.dp)),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                authViewModel.login(loginEmail, loginPassword) {
                                    val user = authViewModel.currentUser.value
                                    if (user?.isAdmin == true) onNavigateToAdmin() else onNavigateToTrading()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Iniciar Sesión", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // BOTONES DE ACCESO RÁPIDO PARA EVALUACIÓN
                        Text(
                            text = "Accesos Rápidos de Prueba (Evaluación):",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    loginEmail = "usuario@divisas.com"
                                    loginPassword = "user123"
                                    authViewModel.login("usuario@divisas.com", "user123") {
                                        onNavigateToTrading()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("👤 Usuario", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    loginEmail = "admin@divisas.com"
                                    loginPassword = "admin123"
                                    authViewModel.login("admin@divisas.com", "admin123") {
                                        onNavigateToAdmin()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🛡️ Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // VISTA REGISTRARSE
                        Text(
                            text = "Crear Cuenta Nueva",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = regFirstName,
                                onValueChange = { regFirstName = it },
                                label = { Text("Nombre") },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(inputBgColor, RoundedCornerShape(10.dp)),
                                singleLine = true,
                                colors = textFieldColors,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = regLastName,
                                onValueChange = { regLastName = it },
                                label = { Text("Apellido") },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(inputBgColor, RoundedCornerShape(10.dp)),
                                singleLine = true,
                                colors = textFieldColors,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(10.dp)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Contraseña (mín. 6 caracteres)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBgColor, RoundedCornerShape(10.dp)),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Seleccione el Rol Contable:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = regRole == UserRole.CLIENT,
                                onClick = { regRole = UserRole.CLIENT },
                                label = { Text("👤 Usuario (Cliente)") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0369A1),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFF94A3B8)
                                )
                            )

                            FilterChip(
                                selected = regRole == UserRole.ADMIN,
                                onClick = { regRole = UserRole.ADMIN },
                                label = { Text("🛡️ Administrador") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6D28D9),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFF94A3B8)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                authViewModel.register(
                                    email = regEmail,
                                    password = regPassword,
                                    firstName = regFirstName,
                                    lastName = regLastName,
                                    role = regRole
                                ) {
                                    if (regRole == UserRole.ADMIN) onNavigateToAdmin() else onNavigateToTrading()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Registrar Cuenta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
