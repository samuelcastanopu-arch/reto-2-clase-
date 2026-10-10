package com.divisas.simulator.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.presentation.theme.ErrorContainerRose
import com.divisas.simulator.presentation.theme.ErrorRose
import com.divisas.simulator.presentation.theme.SuccessGreen

@Composable
fun OperationStatusBadge(
    status: OperationStatus,
    modifier: Modifier = Modifier
) {
    val isApproved = status == OperationStatus.APPROVED
    val bgColor = if (isApproved) Color(0xFFD1FAE5) else ErrorContainerRose
    val textColor = if (isApproved) SuccessGreen else ErrorRose
    val icon = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel
    val label = if (isApproved) "APROBADA" else "RECHAZADA"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
