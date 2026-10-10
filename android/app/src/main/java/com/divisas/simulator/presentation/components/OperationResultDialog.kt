package com.divisas.simulator.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.divisas.simulator.core.util.CurrencyFormatter
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.presentation.theme.EmeraldPrimary
import com.divisas.simulator.presentation.theme.ErrorContainerRose
import com.divisas.simulator.presentation.theme.ErrorRose
import com.divisas.simulator.presentation.theme.SuccessGreen

@Composable
fun OperationResultDialog(
    operation: Operation,
    onDismiss: () -> Unit
) {
    val isApproved = operation.status == OperationStatus.APPROVED

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icono animado/circular
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isApproved) Color(0xFFD1FAE5) else ErrorContainerRose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isApproved) SuccessGreen else ErrorRose,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = if (isApproved) "Operación Aprobada" else "Operación Rechazada",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isApproved) SuccessGreen else ErrorRose
                )

                if (!isApproved && !operation.rejectionReason.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorContainerRose)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = operation.rejectionReason,
                            color = ErrorRose,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Resumen de la operación
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(
                        label = "Tipo:",
                        value = if (operation.operationType == OperationType.BUY) "Compra de Divisas" else "Venta de Divisas"
                    )
                    DetailRow(
                        label = "Cantidad:",
                        value = "${operation.amount} ${operation.currencyCode}"
                    )
                    DetailRow(
                        label = "Tasa Base:",
                        value = CurrencyFormatter.formatRate(operation.baseRate)
                    )
                    DetailRow(
                        label = "Tasa Aplicada:",
                        value = CurrencyFormatter.formatRate(operation.appliedRate)
                    )
                    DetailRow(
                        label = "Desviación:",
                        value = "${operation.percentageDeviation} %"
                    )
                    DetailRow(
                        label = "Total en COP:",
                        value = CurrencyFormatter.formatCop(operation.totalCop),
                        isHighlight = true
                    )
                }

                Text(
                    text = "ID Operación: ${operation.id.take(12)}...",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApproved) EmeraldPrimary else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(
            text = value,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isHighlight) 16.sp else 14.sp,
            color = if (isHighlight) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
