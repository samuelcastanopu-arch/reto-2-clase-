package com.divisas.simulator.presentation.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.divisas.simulator.presentation.components.OperationStatusBadge
import com.divisas.simulator.presentation.theme.EmeraldPrimary
import com.divisas.simulator.presentation.theme.ErrorContainerRose
import com.divisas.simulator.presentation.theme.ErrorRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.isAdmin) "Historial Global de Operaciones" else "Mi Historial de Operaciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Filtros horizontales
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.statusFilter == null,
                onClick = { viewModel.onStatusFilterChange(null) },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = state.statusFilter == OperationStatus.APPROVED,
                onClick = { viewModel.onStatusFilterChange(OperationStatus.APPROVED) },
                label = { Text("Aprobadas") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = EmeraldPrimary
                )
            )
            FilterChip(
                selected = state.statusFilter == OperationStatus.REJECTED,
                onClick = { viewModel.onStatusFilterChange(OperationStatus.REJECTED) },
                label = { Text("Rechazadas") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ErrorContainerRose,
                    selectedLabelColor = ErrorRose
                )
            )
            FilterChip(
                selected = state.currencyFilter == "USD",
                onClick = {
                    viewModel.onCurrencyFilterChange(if (state.currencyFilter == "USD") null else "USD")
                },
                label = { Text("USD") }
            )
            FilterChip(
                selected = state.currencyFilter == "EUR",
                onClick = {
                    viewModel.onCurrencyFilterChange(if (state.currencyFilter == "EUR") null else "EUR")
                },
                label = { Text("EUR") }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else if (state.filteredOperations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No se encontraron operaciones registradas con los filtros seleccionados.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.filteredOperations) { operation ->
                    HistoryItemCard(operation = operation)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(operation: Operation) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(operation.createdAt))
    val isApproved = operation.status == OperationStatus.APPROVED

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (operation.operationType == OperationType.BUY) "COMPRA" else "VENTA"} ${operation.amount} ${operation.currencyCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                OperationStatusBadge(status = operation.status)
            }

            Text(
                text = "Fecha: $formattedDate",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Tasa Base:", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = CurrencyFormatter.formatRate(operation.baseRate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text(text = "Tasa Aplicada:", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = CurrencyFormatter.formatRate(operation.appliedRate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Desviación:", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${operation.percentageDeviation} %",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isApproved) EmeraldPrimary else ErrorRose
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total en COP:",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = CurrencyFormatter.formatCop(operation.totalCop),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                )
            }

            if (!isApproved && !operation.rejectionReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorContainerRose, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Motivo de Rechazo: ${operation.rejectionReason}",
                        fontSize = 11.sp,
                        color = ErrorRose,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
