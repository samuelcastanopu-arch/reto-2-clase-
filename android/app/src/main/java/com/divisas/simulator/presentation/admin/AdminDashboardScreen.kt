package com.divisas.simulator.presentation.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divisas.simulator.core.util.CurrencyFormatter
import com.divisas.simulator.presentation.theme.EmeraldPrimary
import com.divisas.simulator.presentation.theme.ErrorContainerRose
import com.divisas.simulator.presentation.theme.ErrorRose
import com.divisas.simulator.presentation.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Modificar Tasas", "Historial Tasas", "Auditoría")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Panel Administrativo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gestión de tasas base y supervisión de auditoría",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.onTabSelected(index) },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (state.selectedTab) {
            0 -> TabUpdateRates(viewModel = viewModel, state = state)
            1 -> TabRateHistory(state = state)
            2 -> TabAuditLogs(state = state)
        }
    }
}

@Composable
private fun TabUpdateRates(viewModel: AdminViewModel, state: AdminUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Seleccione la Divisa a Modificar:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.currencies.forEach { curr ->
                    val isSelected = state.selectedCurrency?.code == curr.code
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCurrencySelected(curr) },
                        label = { Text("${curr.symbol} ${curr.code}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Tasa Base Actual (${state.selectedCurrency?.code ?: ""}):",
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = CurrencyFormatter.formatRate(state.selectedCurrency?.baseRate),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )

                    OutlinedTextField(
                        value = state.newRateInput,
                        onValueChange = { viewModel.onNewRateChange(it) },
                        label = { Text("Nueva Tasa Base (COP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.reasonInput,
                        onValueChange = { viewModel.onReasonChange(it) },
                        label = { Text("Motivo del cambio (Obligatorio)") },
                        supportingText = { Text("Se guardará de forma inmutable en el historial.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (!state.successMessage.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Text(state.successMessage, color = SuccessGreen, fontSize = 13.sp)
                        }
                    }

                    if (!state.errorMessage.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRose)
                            Text(state.errorMessage, color = ErrorRose, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.updateBaseRate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        enabled = !state.isUpdating && state.newRateInput.isNotBlank() && state.reasonInput.isNotBlank()
                    ) {
                        if (state.isUpdating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Guardar Nueva Tasa Oficial", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabRateHistory(state: AdminUiState) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    if (state.rateHistory.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay cambios de tasas registrados.", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.rateHistory) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Divisa: ${item.currencyCode}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = dateFormat.format(Date(item.changedAt)),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Anterior: ${CurrencyFormatter.formatRate(item.previousRate)}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Nueva: ${CurrencyFormatter.formatRate(item.newRate)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Text(
                        text = "Motivo: \"${item.reason}\"",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Responsable: ${item.adminEmail}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun TabAuditLogs(state: AdminUiState) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())

    if (state.auditLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay eventos de auditoría registrados.", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.auditLogs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.action,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = dateFormat.format(Date(log.timestamp)),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "Ejecutado por: ${log.performedByEmail.ifBlank { log.performedBy }}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    if (log.details.isNotEmpty()) {
                        Text(
                            text = "Detalles: ${log.details}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
