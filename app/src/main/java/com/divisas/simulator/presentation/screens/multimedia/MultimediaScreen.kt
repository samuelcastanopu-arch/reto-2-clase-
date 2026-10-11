package com.divisas.simulator.presentation.screens.multimedia

import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.divisas.simulator.domain.model.MultimediaAsset
import com.divisas.simulator.presentation.viewmodel.MultimediaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MultimediaScreen(
    multimediaViewModel: MultimediaViewModel
) {
    val context = LocalContext.current
    val serviceState by multimediaViewModel.serviceState.collectAsState()
    val allAssets by multimediaViewModel.assets.collectAsState()
    val recentTransactions by multimediaViewModel.recentTransactions.collectAsState()

    var selectedType by remember { mutableStateOf("VOUCHER_TRANSACCION") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var selectedAmount by remember { mutableDoubleStateOf(250.0) }
    var selectedTxId by remember { mutableStateOf("TX_${System.currentTimeMillis().toString().takeLast(6)}") }

    val darkCardColor = Color(0xFF101726)
    val darkBorderColor = Color(0xFF1E293B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ENCABEZADO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Servicio de Generación Multimedia 🎨",
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Componente Android Service en segundo plano",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Uso de servicio de Android (MultimediaGenerationService) para sintetizar vouchers digitales oficiales, códigos QR criptográficos y señales acústicas multimedia.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PANEL DE ESTADO DEL SERVICIO ANDROID
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (serviceState.isRunning) Color(0xFF0284C7) else darkBorderColor,
                    RoundedCornerShape(14.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (serviceState.isRunning) Color(0xFF0C192E) else darkCardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado del Servicio Android:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFF8FAFC)
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (serviceState.isRunning) Color(0xFF0369A1) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (serviceState.isRunning) "● EN EJECUCIÓN (Foreground)" else "EN REPOSO",
                            color = if (serviceState.isRunning) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (serviceState.isRunning) {
                    LinearProgressIndicator(
                        progress = { serviceState.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = serviceState.statusMessage,
                    fontSize = 12.sp,
                    color = if (serviceState.isRunning) Color(0xFF7DD3FC) else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CONFIGURADOR DE MULTIMEDIA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, darkBorderColor, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = darkCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configurar Comprobante Multimedia",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFF8FAFC)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tipo de Multimedia:",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "VOUCHER_TRANSACCION",
                        onClick = { selectedType = "VOUCHER_TRANSACCION" },
                        label = { Text("Voucher Digital", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0369A1),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF090D16),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                    FilterChip(
                        selected = selectedType == "CERTIFICADO_AUDITORIA",
                        onClick = { selectedType = "CERTIFICADO_AUDITORIA" },
                        label = { Text("Certificado 2.5%", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6D28D9),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF090D16),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                    FilterChip(
                        selected = selectedType == "CARNET_FINANCIERO",
                        onClick = { selectedType = "CARNET_FINANCIERO" },
                        label = { Text("Carnet Operador", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF065F46),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF090D16),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Divisa para el Comprobante:",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USD", "EUR", "GBP").forEach { c ->
                        FilterChip(
                            selected = selectedCurrency == c,
                            onClick = { selectedCurrency = c },
                            label = { Text(c, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color(0xFF38BDF8),
                                containerColor = Color(0xFF090D16),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN PARA INICIAR EL SERVICIO
                Button(
                    onClick = {
                        val chosenTx = recentTransactions.firstOrNull()?.id ?: selectedTxId
                        multimediaViewModel.triggerMultimediaGeneration(
                            context = context,
                            txId = chosenTx,
                            currencyCode = selectedCurrency,
                            amount = selectedAmount,
                            type = selectedType
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !serviceState.isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (serviceState.isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Servicio Android Procesando...", color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Servicio para Generar Multimedia", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // VISTA PREVIA DEL COMPROBANTE MULTIMEDIA GENERADO
        val activeAsset = serviceState.lastAsset ?: allAssets.firstOrNull()

        if (activeAsset != null) {
            Text(
                text = "Comprobante Multimedia Generado 🖼️",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF8FAFC)
            )

            Spacer(modifier = Modifier.height(10.dp))

            DarkMultimediaVoucherCard(
                asset = activeAsset,
                onPlaySound = { multimediaViewModel.playConfirmationAudio() },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Comprobante Multimedia Oficial: ${activeAsset.title}\nID: ${activeAsset.transactionId}\nHash Seguridad: ${activeAsset.securityHash}\nVerificación QR: ${activeAsset.qrCodeUrl}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartir Comprobante"))
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HISTORIAL DE ASSETS GENERADOS
        if (allAssets.size > 1) {
            Text(
                text = "Historial de Multimedia Generada (${allAssets.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFF8FAFC)
            )

            Spacer(modifier = Modifier.height(8.dp))

            allAssets.drop(1).forEach { asset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, darkBorderColor, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = darkCardColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = asset.qrCodeUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = asset.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFF8FAFC))
                            Text(text = "Divisa: ${asset.currencyCode} • Monto: ${asset.amount} • Hash: ${asset.securityHash}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DarkMultimediaVoucherCard(
    asset: MultimediaAsset,
    onPlaySound: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }
    val darkBorderColor = Color(0xFF1E293B)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, darkBorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Banner Gráfico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                AsyncImage(
                    model = asset.bannerImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xB3000000))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = asset.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Sello Criptográfico: ${asset.securityHash}",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Cuerpo del Comprobante
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Transacción: ${asset.transactionId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monto Operado: ${asset.amount} ${asset.currencyCode}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fecha: ${dateFormat.format(Date(asset.generatedAt))}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Código QR Oficial Dinámico
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = asset.qrCodeUrl,
                            contentDescription = "Código QR Oficial",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "QR Oficial",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estado del Servicio Multimedia
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF065F46), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF042F2E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = asset.statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BOTONES DE INTERACCIÓN MULTIMEDIA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPlaySound,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sonido", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
