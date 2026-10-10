package com.divisas.simulator.presentation.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.divisas.simulator.core.util.CurrencyFormatter
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.presentation.components.CurrencyCard
import com.divisas.simulator.presentation.components.OperationStatusBadge
import com.divisas.simulator.presentation.theme.EmeraldPrimary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToOperations: (String, OperationType) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera de Usuario
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hola, ${state.currentUser?.firstName ?: "Usuario"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (state.currentUser?.role == UserRole.ADMIN) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Panel Administrador",
                                color = EmeraldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Simulador Financiero de Divisas",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                IconButton(onClick = onNavigateToProfile) {
                    if (!state.currentUser?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.currentUser?.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(44.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        // Tasas Base Vigentes
        item {
            Text(
                text = "Tasas Base Vigentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.currencies.forEach { currency ->
                    CurrencyCard(
                        currency = currency,
                        onClick = { onNavigateToOperations(currency.code, OperationType.BUY) }
                    )
                }
            }
        }

        // Acciones Rápidas
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Acciones Rápidas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToOperations("USD", OperationType.BUY) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Comprar")
                        }
                        OutlinedButton(
                            onClick = { onNavigateToOperations("USD", OperationType.SELL) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Vender")
                        }
                    }
                }
            }
        }

        // Últimas Operaciones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimas Simulaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onNavigateToHistory,
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Text("Ver todas", fontSize = 13.sp, color = EmeraldPrimary)
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EmeraldPrimary
                    )
                }
            }
        }

        if (state.recentOperations.isEmpty()) {
            item {
                Text(
                    text = "Aún no has realizado ninguna simulación. ¡Inicia una ahora!",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(state.recentOperations) { op ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${if (op.operationType == OperationType.BUY) "COMPRA" else "VENTA"} ${op.amount} ${op.currencyCode}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Total: ${CurrencyFormatter.formatCop(op.totalCop)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        OperationStatusBadge(status = op.status)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
