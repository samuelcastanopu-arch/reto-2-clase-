package com.divisas.simulator.presentation.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divisas.simulator.core.util.CurrencyFormatter
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.presentation.viewmodel.AdminAuditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminAuditScreen(
    currentUser: User?,
    adminAuditViewModel: AdminAuditViewModel
) {
    val allTransactions by adminAuditViewModel.allTransactions.collectAsState()
    val statusFilter by adminAuditViewModel.statusFilter.collectAsState()
    val isRefreshing by adminAuditViewModel.isRefreshing.collectAsState()
    val auditMessage by adminAuditViewModel.auditMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTxForReject by remember { mutableStateOf<Operation?>(null) }
    var rejectObservationInput by remember { mutableStateOf("") }
    var alertRuleMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(auditMessage) {
        auditMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminAuditViewModel.clearAuditMessage()
        }
    }

    val filteredList = remember(allTransactions, statusFilter) {
        if (statusFilter == null) allTransactions else allTransactions.filter { it.status == statusFilter }
    }

    val pendingCount = allTransactions.count { it.status == OperationStatus.PENDIENTE_AUDITORIA }
    val successCount = allTransactions.count { it.status == OperationStatus.EXITOSA }
    val rejectCount = allTransactions.count { it.status == OperationStatus.RECHAZADA }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(16.dp)
        ) {
            // ENCABEZADO DE AUDITORÍA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D95)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Auditoría de Administrador 🛡️",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Auditor: ${currentUser?.fullName ?: "Administrador"}",
                                    color = Color(0xFFDDD6FE),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                adminAuditViewModel.loadTransactions()
                                adminAuditViewModel.refreshLiveRates()
                            },
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Regla 2.5%: El Administrador audita que la compra/venta sea exitosa y que la variación respecto a la tasa al momento de la transición NO supere el 2.5%.",
                        color = Color(0xFFEDE9FE),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // MÉTRICAS RESUMEN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = "Pendientes",
                    count = pendingCount,
                    color = Color(0xFFD97706),
                    bgColor = Color(0xFFFEF3C7),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Exitosas",
                    count = successCount,
                    color = Color(0xFF059669),
                    bgColor = Color(0xFFD1FAE5),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Rechazadas",
                    count = rejectCount,
                    color = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FILTROS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { adminAuditViewModel.setFilter(null) },
                    label = { Text("Todas (${allTransactions.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OperationStatus.PENDIENTE_AUDITORIA,
                    onClick = { adminAuditViewModel.setFilter(OperationStatus.PENDIENTE_AUDITORIA) },
                    label = { Text("Pendientes ($pendingCount)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OperationStatus.EXITOSA,
                    onClick = { adminAuditViewModel.setFilter(OperationStatus.EXITOSA) },
                    label = { Text("Exitosas ($successCount)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == OperationStatus.RECHAZADA,
                    onClick = { adminAuditViewModel.setFilter(OperationStatus.RECHAZADA) },
                    label = { Text("Rechazadas ($rejectCount)", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LISTA DE AUDITORÍA
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay transacciones en este filtro.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
                        val rateAtTransition = adminAuditViewModel.getRateAtTransition(tx.currencyCode, tx.rateAtOrder)
                        val devAtTransition = adminAuditViewModel.calculateDeviationAtTransition(
                            rateAtOrder = tx.rateAtOrder,
                            rateAtTransition = rateAtTransition
                        )

                        AdminTransactionAuditCard(
                            tx = tx,
                            liveRateAtTransition = rateAtTransition,
                            deviationAtTransition = devAtTransition,
                            onApprove = { observation ->
                                adminAuditViewModel.auditTransaction(
                                    transactionId = tx.id,
                                    adminName = currentUser?.fullName ?: "Administrador",
                                    rateAtTransition = rateAtTransition,
                                    approve = true,
                                    observation = observation
                                ) { success, msg ->
                                    if (!success) {
                                        alertRuleMessage = msg
                                    }
                                }
                            },
                            onRejectClick = {
                                selectedTxForReject = tx
                                rejectObservationInput = ""
                            }
                        )
                    }
                }
            }
        }

        // DIÁLOGO PARA RECHAZO CON OBSERVACIÓN
        selectedTxForReject?.let { tx ->
            val rateAtTransition = adminAuditViewModel.getRateAtTransition(tx.currencyCode, tx.rateAtOrder)
            AlertDialog(
                onDismissRequest = { selectedTxForReject = null },
                title = { Text("Rechazar Transacción (${tx.id})", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Ingrese el motivo u observación de auditoría para el rechazo:",
                            fontSize = 13.sp,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectObservationInput,
                            onValueChange = { rejectObservationInput = it },
                            label = { Text("Motivo de rechazo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val obs = if (rejectObservationInput.isBlank()) "Rechazada por decisión de auditoría." else rejectObservationInput
                            adminAuditViewModel.auditTransaction(
                                transactionId = tx.id,
                                adminName = currentUser?.fullName ?: "Administrador",
                                rateAtTransition = rateAtTransition,
                                approve = false,
                                observation = obs
                            ) { _, _ -> }
                            selectedTxForReject = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Confirmar Rechazo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTxForReject = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // DIÁLOGO DE ADVERTENCIA SI SUPERA LA REGLA DEL 2.5%
        alertRuleMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { alertRuleMessage = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626)) },
                title = { Text("Violación de Regla del 2.5%", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B)) },
                text = {
                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { alertRuleMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Text("Entendido")
                    }
                }
            )
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
fun MetricChip(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AdminTransactionAuditCard(
    tx: Operation,
    liveRateAtTransition: Double,
    deviationAtTransition: Double,
    onApprove: (String) -> Unit,
    onRejectClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    var observationInput by remember { mutableStateOf("") }
    val isPending = tx.status == OperationStatus.PENDIENTE_AUDITORIA
    val compliesWith25Rule = deviationAtTransition <= 2.5

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: ID y Usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${if (tx.operationType == OperationType.COMPRA) "🟢 COMPRA" else "🔴 VENTA"} • ${tx.amount} ${tx.currencyCode}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Usuario: ${tx.userName} (${tx.userEmail})",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                // Estado actual
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (tx.status) {
                            OperationStatus.EXITOSA -> Color(0xFFD1FAE5)
                            OperationStatus.RECHAZADA -> Color(0xFFFEE2E2)
                            OperationStatus.PENDIENTE_AUDITORIA -> Color(0xFFFEF3C7)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = tx.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (tx.status) {
                            OperationStatus.EXITOSA -> Color(0xFF065F46)
                            OperationStatus.RECHAZADA -> Color(0xFF991B1B)
                            OperationStatus.PENDIENTE_AUDITORIA -> Color(0xFF92400E)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detalle contable y tasas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Tasa Ordenada:", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(
                        text = CurrencyFormatter.formatCop(tx.rateAtOrder),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Total COP: ${CurrencyFormatter.formatCop(tx.totalCop)}",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5563)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPending) "Tasa en Transición (En vivo):" else "Tasa en Transición Registrada:",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = CurrencyFormatter.formatCop(if (isPending) liveRateAtTransition else tx.rateAtTransition),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF4C1D95)
                    )
                    val dev = if (isPending) deviationAtTransition else tx.percentageDeviation
                    Text(
                        text = "Desviación: $dev%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (dev <= 2.5) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CHIP DE CUMPLIMIENTO DE REGLA DEL 2.5%
            val effectiveDev = if (isPending) deviationAtTransition else tx.percentageDeviation
            val isCompliant = effectiveDev <= 2.5

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompliant) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompliant) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCompliant) Color(0xFF059669) else Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompliant) {
                            "Auditoría: Variación en transición ($effectiveDev%) <= 2.5% • Regla Cumplida"
                        } else {
                            "Auditoría: Variación en transición ($effectiveDev%) SUPERA el límite de 2.5% • NO admisible como exitosa"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompliant) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                }
            }

            if (tx.auditObservation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Observación: ${tx.auditObservation}",
                    fontSize = 11.sp,
                    color = Color(0xFF4B5563)
                )
            }

            Text(
                text = "Fecha: ${dateFormat.format(Date(tx.timestamp))} • ID: ${tx.id}",
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(top = 4.dp)
            )

            // ACCIONES DE AUDITORÍA SI ESTÁ PENDIENTE
            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = observationInput,
                    onValueChange = { observationInput = it },
                    label = { Text("Nota de auditoría (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApprove(observationInput) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (compliesWith25Rule) Color(0xFF059669) else Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (compliesWith25Rule) "Aprobar (Exitosa)" else "Supera 2.5%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
