package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.data.UserRole
import com.example.ui.AppTab
import com.example.ui.IndicatorsViewModel
import com.example.ui.theme.CorporateBlueDark
import com.example.ui.theme.CorporateBlueLight
import com.example.ui.theme.CorporateBluePrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateTealLight
import com.example.ui.theme.CorporateTealSecondary
import com.example.ui.theme.CorporateTextPrimary
import com.example.ui.theme.CorporateTextSecondary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    currentUser: User,
    viewModel: IndicatorsViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsState()

    // Abas disponíveis:
    // O perfil técnico tem acesso a todas as abas EXCETO a de usuário e hostinger/github
    val availableTabs = remember(currentUser.role) {
        if (currentUser.role == UserRole.ADMIN) {
            AppTab.values().toList()
        } else {
            AppTab.values().filter { it != AppTab.USUARIOS && it != AppTab.HOSTINGER_GITHUB }
        }
    }

    val selectedTabIndex = availableTabs.indexOf(uiState.currentTab).coerceAtLeast(0)

    val filteredRecords = remember(
        allRecords,
        currentUser,
        uiState.currentTab,
        uiState.selectedMonth,
        uiState.selectedPeriod,
        uiState.selectedTechnicianLogin,
        uiState.searchQuery
    ) {
        viewModel.getFilteredRecords(
            records = allRecords,
            user = currentUser,
            currentTab = uiState.currentTab,
            month = uiState.selectedMonth,
            period = uiState.selectedPeriod,
            techLogin = uiState.selectedTechnicianLogin,
            search = uiState.searchQuery
        )
    }

    val kpiSummary = remember(filteredRecords, uiState.currentTab) {
        viewModel.calculateKpiSummary(filteredRecords, uiState.currentTab)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CorporateBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Claro • Indicadores Técnicos",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateBlueDark
                                )
                            )
                        }
                        Text(
                            text = "${currentUser.name} • ${if (currentUser.role == UserRole.ADMIN) "Gestor ADM" else "Técnico Claro (${currentUser.login})"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CorporateTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                actions = {
                    // Status de Conexão e Sincronização
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (uiState.isOnline) CorporateTealLight else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { viewModel.syncDataOnReconnect() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = CorporateBluePrimary
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (uiState.isOnline) StatusSuccess else StatusDanger,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isOnline) "Online" else "Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isOnline) StatusSuccess else StatusDanger,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Botão Upload de Planilha (Disponível para ADM)
                    if (currentUser.role == UserRole.ADMIN) {
                        IconButton(
                            onClick = { viewModel.openUploadDialog() },
                            modifier = Modifier.testTag("open_upload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload Planilha",
                                tint = CorporateBluePrimary
                            )
                        }
                    }

                    // Notificações Push com Badge
                    IconButton(
                        onClick = { viewModel.openNotificationsSheet() },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(
                                        containerColor = StatusDanger,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = "$unreadNotifsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = CorporateTextPrimary
                            )
                        }
                    }

                    // Sair
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair",
                            tint = CorporateTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banner de Status Offline quando desconectado
            AnimatedVisibility(visible = !uiState.isOnline) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = StatusWarning.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modo Offline ativo: Os indicadores continuam acessíveis no banco local e sincronizarão ao reconectar.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CorporateBlueDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Scrollable TabRow com as Abas do Aplicativo
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CorporateBluePrimary,
                edgePadding = 16.dp,
                divider = { Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(CorporateBorder)) }
            ) {
                availableTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        modifier = Modifier.testTag("tab_${tab.name}"),
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CorporateBluePrimary else CorporateTextSecondary
                                )
                            )
                        }
                    )
                }
            }

            // Conteúdo da Aba Ativa
            when (uiState.currentTab) {
                AppTab.USUARIOS -> {
                    // Aba Usuários (Apenas para ADM)
                    UserManagementTab(
                        users = allUsers,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
                AppTab.HOSTINGER_GITHUB -> {
                    // Aba Hostinger & GitHub (Apenas para ADM)
                    HostingerGitHubScreen(
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    // Abas dos Indicadores Técnicos
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    // Filtros por Mês, Período e Técnico
                    item {
                        IndicatorFilterBar(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            selectedMonth = uiState.selectedMonth,
                            selectedPeriod = uiState.selectedPeriod,
                            selectedTechnicianLogin = uiState.selectedTechnicianLogin,
                            searchQuery = uiState.searchQuery
                        )
                    }

                    // Card de Resumo de KPI da Aba
                    item {
                        KpiSummaryCard(
                            kpi = kpiSummary,
                            tab = uiState.currentTab
                        )
                    }

                    // Cabeçalho da Lista de Registros
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Histórico de Registros da Planilha (${filteredRecords.size})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateTextPrimary
                                )
                            )

                            Text(
                                text = uiState.lastSyncTime,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CorporateTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Lista de Registros Detalhados
                    if (filteredRecords.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CorporateBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = CorporateTextSecondary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Nenhum resultado encontrado para os filtros selecionados.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = CorporateTextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredRecords, key = { it.id }) { record ->
                            RecordItemCard(record = record)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Diálogo de Upload de Planilha Excel (ADM)
        if (uiState.showUploadDialog) {
            UploadExcelDialog(
                isUploading = uiState.isUploading,
                progressMessage = uiState.uploadProgressMessage,
                uploadResult = uiState.uploadResult,
                onUploadUri = { uri, fileName ->
                    viewModel.processExcelUpload(uri, fileName)
                },
                onLoadOfficialDemo = {
                    viewModel.loadOfficialDemoSpreadsheet()
                },
                onGoToHostingerGitHub = {
                    viewModel.selectTab(AppTab.HOSTINGER_GITHUB)
                },
                onDismiss = {
                    viewModel.closeUploadDialog()
                }
            )
        }

        // Diálogo da Central de Notificações Push
        if (uiState.showNotificationsSheet) {
            NotificationsDialog(
                notifications = notifications,
                onDismiss = { viewModel.closeNotificationsSheet() }
            )
        }
    }
}
