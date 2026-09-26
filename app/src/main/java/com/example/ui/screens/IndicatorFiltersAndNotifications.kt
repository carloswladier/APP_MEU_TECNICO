package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppNotification
import com.example.data.User
import com.example.data.UserRole
import com.example.ui.IndicatorsViewModel
import com.example.ui.theme.CorporateBlueDark
import com.example.ui.theme.CorporateBlueLight
import com.example.ui.theme.CorporateBluePrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateTealLight
import com.example.ui.theme.CorporateTealSecondary
import com.example.ui.theme.CorporateTextPrimary
import com.example.ui.theme.CorporateTextSecondary

@Composable
fun IndicatorFilterBar(
    viewModel: IndicatorsViewModel,
    currentUser: User?,
    selectedMonth: String,
    selectedPeriod: String,
    selectedTechnicianLogin: String,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var periodMenuExpanded by remember { mutableStateOf(false) }
    var techMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Banner Informativo de Vinculação para Técnico
        if (currentUser?.role == UserRole.TECNICO) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = CorporateBlueLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, CorporateBluePrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CorporateBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vínculo Ativo: ${currentUser.name} (login: ${currentUser.login}) • Exibindo somente seus resultados",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CorporateBlueDark,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Barra de Filtros com Menus Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filtro Mês
            Box {
                OutlinedButton(
                    onClick = { monthMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_month_button")
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Mês: $selectedMonth", fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = monthMenuExpanded,
                    onDismissRequest = { monthMenuExpanded = false }
                ) {
                    viewModel.monthsList.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                viewModel.setMonthFilter(month)
                                monthMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Filtro Período
            Box {
                OutlinedButton(
                    onClick = { periodMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_period_button")
                ) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Período: $selectedPeriod", fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = periodMenuExpanded,
                    onDismissRequest = { periodMenuExpanded = false }
                ) {
                    viewModel.periodsList.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                viewModel.setPeriodFilter(period)
                                periodMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Filtro Técnico (Apenas para Administrador)
            if (currentUser?.role == UserRole.ADMIN) {
                Box {
                    OutlinedButton(
                        onClick = { techMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_technician_button")
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedTechnicianLogin == "TODOS") "Técnico: Todos" else "Técnico: $selectedTechnicianLogin",
                            fontSize = 12.sp
                        )
                    }
                    DropdownMenu(
                        expanded = techMenuExpanded,
                        onDismissRequest = { techMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos os Técnicos") },
                            onClick = {
                                viewModel.setTechnicianFilter("TODOS")
                                techMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Carlos Wladier (tec.carlos)") },
                            onClick = {
                                viewModel.setTechnicianFilter("tec.carlos")
                                techMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Marcos Silva (tec.silva)") },
                            onClick = {
                                viewModel.setTechnicianFilter("tec.silva")
                                techMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lucas Santos (tec.santos)") },
                            onClick = {
                                viewModel.setTechnicianFilter("tec.santos")
                                techMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Juliana Oliveira (tec.oliveira)") },
                            onClick = {
                                viewModel.setTechnicianFilter("tec.oliveira")
                                techMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Campo de Busca por OS / Cliente
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_indicator_input"),
            placeholder = { Text("Buscar por número da OS, cliente ou observações...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = CorporateTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun NotificationsDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = CorporateBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Central de Notificações",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CorporateBlueDark
                    )
                )
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Text("Nenhuma notificação recente.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (notif.isRead) MaterialTheme.colorScheme.surface else CorporateTealLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CorporateBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CorporateBlueDark
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextPrimary)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
