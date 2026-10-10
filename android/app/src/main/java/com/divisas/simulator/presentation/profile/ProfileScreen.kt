package com.divisas.simulator.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.divisas.simulator.presentation.theme.EmeraldPrimary
import com.divisas.simulator.presentation.theme.ErrorRose
import com.divisas.simulator.presentation.theme.SuccessGreen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPhoto(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Perfil del Cliente",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        // Foto con Cloudinary
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!state.user?.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = state.user?.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(90.dp),
                        tint = Color.Gray
                    )
                }

                if (state.isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "Toca para cambiar foto (Cloudinary)",
            fontSize = 12.sp,
            color = Color.Gray
        )

        if (!state.successMessage.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                Text(state.successMessage!!, color = SuccessGreen, fontSize = 13.sp)
            }
        }

        if (!state.errorMessage.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRose)
                Text(state.errorMessage!!, color = ErrorRose, fontSize = 13.sp)
            }
        }

        // Datos del Usuario
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileInfoRow("Nombre:", state.user?.fullName ?: "")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                ProfileInfoRow("Correo:", state.user?.email ?: "")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                ProfileInfoRow("Teléfono:", state.user?.phoneNumber ?: "")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                ProfileInfoRow("Rol:", state.user?.role?.name ?: "CLIENT")
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                ProfileInfoRow("Estado de Cuenta:", state.user?.accountStatus?.name ?: "ACTIVE")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.logout(onLogoutSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
