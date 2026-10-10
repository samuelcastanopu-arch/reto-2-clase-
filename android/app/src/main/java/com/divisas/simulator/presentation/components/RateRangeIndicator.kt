package com.divisas.simulator.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divisas.simulator.core.util.CurrencyFormatter
import com.divisas.simulator.presentation.theme.ErrorContainerRose
import com.divisas.simulator.presentation.theme.ErrorRose
import com.divisas.simulator.presentation.theme.SuccessGreen
import com.divisas.simulator.presentation.theme.WarningAmber
import java.math.BigDecimal

@Composable
fun RateRangeIndicator(
    baseRate: BigDecimal,
    appliedRate: BigDecimal,
    deviationPercentage: BigDecimal,
    isWithinAllowed: Boolean,
    minAllowedRate: BigDecimal,
    maxAllowedRate: BigDecimal,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (isWithinAllowed) SuccessGreen else ErrorRose,
        label = "RateStatusColor"
    )

    val bgColor = if (isWithinAllowed) Color(0xFFECFDF5) else ErrorContainerRose

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isWithinAllowed) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor
                    )
                    Text(
                        text = if (isWithinAllowed) "Tasa dentro del margen (± 2,5%)" else "¡Tasa fuera del margen permitido!",
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${if (appliedRate > baseRate) "+" else if (appliedRate < baseRate) "-" else ""}${deviationPercentage}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mínimo: ${CurrencyFormatter.formatRate(minAllowedRate)}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Base: ${CurrencyFormatter.formatRate(baseRate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Máximo: ${CurrencyFormatter.formatRate(maxAllowedRate)}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            if (!isWithinAllowed) {
                Text(
                    text = "La tasa propuesta difiere en más de 2,5 % de la base. Esta operación será rechazada por el servidor para auditoría.",
                    fontSize = 12.sp,
                    color = ErrorRose,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
