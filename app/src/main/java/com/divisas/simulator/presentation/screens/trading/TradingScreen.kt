package com.divisas.simulator.presentation.screens.trading

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divisas.simulator.core.util.CurrencyFormatter
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.presentation.viewmodel.TradingViewModel
import com.divisas.simulator.service.FinnhubApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradingScreen(
    currentUser: User?,
    tradingViewModel: TradingViewModel
) {
    val currencies by tradingViewModel.currencies.collectAsState()
    val wallet by tradingViewModel.wallet.collectAsState()
    val userTransactions by tradingViewModel.userTransactions.collectAsState()
    val selectedCurrency by tradingViewModel.selectedCurrency.collectAsState()
    val operationType by tradingViewModel.operationType.collectAsState()
    val amountInput by tradingViewModel.amountInput.collectAsState()
    val appliedRateInput by tradingViewModel.appliedRateInput.collectAsState()
    val isRefreshing by tradingViewModel.isRefreshing.collectAsState()
    val message by tradingViewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            tradingViewModel.loadUserData(currentUser)
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            tradingViewModel.clearMessage()
        }
    }

    val deviation = tradingViewModel.calculateDeviationPercentage()
    val isWithinTolerance = tradingViewModel.isWithinTolerance()
    val totalCop = tradingViewModel.calculateTotalCop()

    val darkCardColor = Color(0xFF101726)
    val darkBorderColor = Color(0xFF1E293B)
    val inputBgColor = Color(0xFF090D16)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFFF8FAFC),
        unfocusedTextColor = Color(0xFFCBD5E1),
        focusedBorderColor = Color(0xFF38BDF8),
        unfocusedBorderColor = Color(0xFF1E293B),
        focusedLabelColor = Color(0xFF38BDF8),
        unfocusedLabelColor = Color(0xFF64748B),
        cursorColor = Color(0xFF38BDF8)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070A10))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ENCABEZADO Y TIKER FINNHUB
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mercado en Vivo Finnhub Pro 🌐",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                            Text(
                                text = "Token Activo: ${FinnhubApiService.getMaskedApiKey()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        IconButton(
                            onClick = { tradingViewModel.refreshMarketRates() },
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF38BDF8))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color(0xFF38BDF8))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Carrusel de Cotizaciones Finnhub
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(currencies) { c ->
                            val isSelected = c.code == selectedCurrency.code
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { tradingViewModel.selectCurrency(c) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF16253B) else Color(0xFF0B101B)
                                )
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = "${c.flagEmoji} ${c.code}",
                                        color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatCop(c.baseRate),
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BILLETERA DEL USUARIO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, darkBorderColor, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = darkCardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mi Billetera Persistente",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFF8FAFC)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Saldo Disponible (COP):",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = CurrencyFormatter.formatCop(wallet?.copBalance ?: 25000000.0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "USD: $${wallet?.getForeignBalance("USD") ?: 0.0}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7DD3FC)
                        )
                        Text(
                            text = "EUR: €${wallet?.getForeignBalance("EUR") ?: 0.0}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC4B5FD)
                        )
                        Text(
                            text = "GBP: £${wallet?.getForeignBalance("GBP") ?: 0.0}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE047)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FORMULARIO DE COMPRA Y VENTA
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
                        text = "Operación de Compra y Venta",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF8FAFC)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector de Tipo de Operación: COMPRA o VENTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = operationType == OperationType.COMPRA,
                            onClick = { tradingViewModel.setOperationType(OperationType.COMPRA) },
                            label = { Text("🟢 COMPRA (Pagar COP)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF064E3B),
                                selectedLabelColor = Color(0xFF6EE7B7),
                                containerColor = Color(0xFF090D16),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                        FilterChip(
                            selected = operationType == OperationType.VENTA,
                            onClick = { tradingViewModel.setOperationType(OperationType.VENTA) },
                            label = { Text("🔴 VENTA (Recibir COP)") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7F1D1D),
                                selectedLabelColor = Color(0xFFFCA5A5),
                                containerColor = Color(0xFF090D16),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Divisa seleccionada y tasa Finnhub
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D16), RoundedCornerShape(10.dp))
                            .border(1.dp, darkBorderColor, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Divisa: ${selectedCurrency.flagEmoji} ${selectedCurrency.name} (${selectedCurrency.code})",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tasa actual Finnhub: ${CurrencyFormatter.formatCop(selectedCurrency.baseRate)}",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Entradas de Monto y Tasa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { tradingViewModel.setAmount(it) },
                            label = { Text("Cantidad (${selectedCurrency.code})") },
                            modifier = Modifier
                                .weight(1f)
                                .background(inputBgColor, RoundedCornerShape(10.dp)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = appliedRateInput,
                            onValueChange = { tradingViewModel.setAppliedRate(it) },
                            label = { Text("Tasa Propuesta (COP)") },
                            modifier = Modifier
                                .weight(1.2f)
                                .background(inputBgColor, RoundedCornerShape(10.dp)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // INDICADOR DE TOLERANCIA Y REGLA 2.5%
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isWithinTolerance) Color(0xFF059669) else Color(0xFFD97706),
                                RoundedCornerShape(10.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWithinTolerance) Color(0xFF042F2E) else Color(0xFF451A03)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isWithinTolerance) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isWithinTolerance) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Desviación estimada: $deviation%",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWithinTolerance) Color(0xFF6EE7B7) else Color(0xFFFDE68A),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isWithinTolerance) {
                                    "✅ En rango de tolerancia (<= 2.5%). Transacción admisible para ser auditada y aprobada."
                                } else {
                                    "⚠️ Desviación alta (> 2.5%). La regla contable exige que no supere 2.5% al momento de la transición."
                                },
                                fontSize = 12.sp,
                                color = if (isWithinTolerance) Color(0xFFA7F3D0) else Color(0xFFFEF3C7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Calculado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total estimado COP:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        Text(
                            text = CurrencyFormatter.formatCop(totalCop),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentUser != null) {
                                tradingViewModel.submitTransaction(
                                    currentUser = currentUser,
                                    onSuccess = {},
                                    onError = {}
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (operationType == OperationType.COMPRA) "Registrar Orden de Compra" else "Registrar Orden de Venta",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // HISTORIAL PERSONAL DE TRANSACCIONES
            Text(
                text = "Mis Transacciones Registradas",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF8FAFC)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (userTransactions.isEmpty()) {
                Text(
                    text = "Aún no has registrado transacciones.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            } else {
                userTransactions.forEach { tx ->
                    DarkTransactionItemCard(tx = tx)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun DarkTransactionItemCard(tx: Operation) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (tx.operationType == OperationType.COMPRA) "🟢 COMPRA" else "🔴 VENTA"} • ${tx.amount} ${tx.currencyCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFF8FAFC)
                )

                // Badge de estado
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (tx.status) {
                            OperationStatus.EXITOSA -> Color(0xFF064E3B)
                            OperationStatus.RECHAZADA -> Color(0xFF7F1D1D)
                            OperationStatus.PENDIENTE_AUDITORIA -> Color(0xFF78350F)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when (tx.status) {
                            OperationStatus.EXITOSA -> "EXITOSA"
                            OperationStatus.RECHAZADA -> "RECHAZADA"
                            OperationStatus.PENDIENTE_AUDITORIA -> "PENDIENTE AUDITORÍA"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (tx.status) {
                            OperationStatus.EXITOSA -> Color(0xFF6EE7B7)
                            OperationStatus.RECHAZADA -> Color(0xFFFCA5A5)
                            OperationStatus.PENDIENTE_AUDITORIA -> Color(0xFFFDE68A)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tasa Orden: ${CurrencyFormatter.formatCop(tx.rateAtOrder)} | Total: ${CurrencyFormatter.formatCop(tx.totalCop)}",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1)
            )

            if (tx.status != OperationStatus.PENDIENTE_AUDITORIA) {
                Text(
                    text = "Tasa en Transición: ${CurrencyFormatter.formatCop(tx.rateAtTransition)} | Variación: ${tx.percentageDeviation}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tx.percentageDeviation <= 2.5) Color(0xFF34D399) else Color(0xFFF87171)
                )
            }

            if (tx.auditObservation.isNotBlank()) {
                Text(
                    text = "Nota: ${tx.auditObservation}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Text(
                text = "Fecha: ${dateFormat.format(Date(tx.timestamp))} • ID: ${tx.id}",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
