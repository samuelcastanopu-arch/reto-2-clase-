package com.divisas.simulator.presentation.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.presentation.components.OperationResultDialog
import com.divisas.simulator.presentation.components.RateRangeIndicator
import com.divisas.simulator.presentation.theme.EmeraldPrimary
import java.math.BigDecimal

@Composable
fun OperationsScreen(
    viewModel: OperationsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    if (state.showResultDialog && state.lastResult != null) {
        OperationResultDialog(
            operation = state.lastResult!!,
            onDismiss = { viewModel.dismissResultDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Simulador de Operaciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Selector de Tipo de Operación: COMPRA / VENTA
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isBuy = state.operationType == OperationType.BUY
                Button(
                    onClick = { viewModel.onSelectOperationType(OperationType.BUY) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBuy) EmeraldPrimary else Color.Transparent,
                        contentColor = if (isBuy) Color.White else Color.DarkGray
                    )
                ) {
                    Text("Comprar Divisa", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.onSelectOperationType(OperationType.SELL) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isBuy) EmeraldPrimary else Color.Transparent,
                        contentColor = if (!isBuy) Color.White else Color.DarkGray
                    )
                ) {
                    Text("Vender Divisa", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Selector de Divisa
        Text(
            text = "Selecciona la Divisa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.availableCurrencies.forEach { curr ->
                val isSelected = state.selectedCurrency?.code == curr.code
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onSelectCurrency(curr) },
                    label = { Text("${curr.symbol} ${curr.code} - ${curr.name}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = EmeraldPrimary
                    )
                )
            }
        }

        // 3. Cantidad de divisas
        OutlinedTextField(
            value = state.amountInput,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text("Cantidad a simular (${state.selectedCurrency?.code ?: "USD"})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 4. Tasa aplicada propuesta
        OutlinedTextField(
            value = state.appliedRateInput,
            onValueChange = { viewModel.onAppliedRateChange(it) },
            label = { Text("Tasa Aplicada Propuesta (COP por unidad)") },
            supportingText = {
                Text(
                    text = "Tasa Base Vigente: ${CurrencyFormatter.formatRate(state.selectedCurrency?.baseRate)}",
                    fontWeight = FontWeight.SemiBold
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 5. Componente Visual de la Regla del 2.5 %
        RateRangeIndicator(
            baseRate = state.selectedCurrency?.baseRate ?: BigDecimal.ZERO,
            appliedRate = state.appliedRateInput.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            deviationPercentage = state.deviationPercentage,
            isWithinAllowed = state.isWithinAllowedRange,
            minAllowedRate = state.minAllowedRate,
            maxAllowedRate = state.maxAllowedRate
        )

        // 6. Resumen de Valor Estimado
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = EmeraldPrimary.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (state.operationType == OperationType.BUY) "Total estimado a pagar en COP:" else "Total estimado a recibir en COP:",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = CurrencyFormatter.formatCop(state.estimatedTotalCop),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                )
                Text(
                    text = "${state.amountInput} ${state.selectedCurrency?.code ?: ""} × ${state.appliedRateInput} COP",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // 7. Botón de Simulación
        Button(
            onClick = { viewModel.simulateOperation() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            enabled = !state.isSubmitting && state.amountInput.isNotBlank() && state.appliedRateInput.isNotBlank()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Procesar Simulación en Servidor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
