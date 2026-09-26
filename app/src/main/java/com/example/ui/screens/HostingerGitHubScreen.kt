package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GitHubFileItemState
import com.example.data.remote.GitHubFileKey
import com.example.data.remote.GitHubFileStatus
import com.example.data.remote.HostingerConfig
import com.example.data.remote.HostingerConnectionStatus
import com.example.data.remote.HostingerScriptGenerator
import com.example.ui.IndicatorsViewModel
import com.example.ui.theme.CorporateBlueDark
import com.example.ui.theme.CorporateBlueLight
import com.example.ui.theme.CorporateBluePrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateTealLight
import com.example.ui.theme.CorporateTealSecondary
import com.example.ui.theme.CorporateTextPrimary
import com.example.ui.theme.CorporateTextSecondary
import com.example.ui.theme.CorporateTextTertiary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg

@Composable
fun HostingerGitHubScreen(
    viewModel: IndicatorsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var apiUrlInput by remember(uiState.hostingerConfig.apiUrl) { mutableStateOf(uiState.hostingerConfig.apiUrl) }
    var apiKeyInput by remember(uiState.hostingerConfig.apiKey) { mutableStateOf(uiState.hostingerConfig.apiKey) }
    var ghOwnerInput by remember(uiState.hostingerConfig.githubOwner) { mutableStateOf(uiState.hostingerConfig.githubOwner) }
    var ghRepoInput by remember(uiState.hostingerConfig.githubRepo) { mutableStateOf(uiState.hostingerConfig.githubRepo) }
    var ghBranchInput by remember(uiState.hostingerConfig.githubBranch) { mutableStateOf(uiState.hostingerConfig.githubBranch) }
    var ghFolderInput by remember(uiState.hostingerConfig.githubFolderPath) { mutableStateOf(uiState.hostingerConfig.githubFolderPath) }
    var autoSyncInput by remember(uiState.hostingerConfig.autoSyncOnStart) { mutableStateOf(uiState.hostingerConfig.autoSyncOnStart) }
    var showCodeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // --------------------------------------------------------------------
        // 1. BANNER DE FEEDBACK DE SINCRONIZAÇÃO
        // --------------------------------------------------------------------
        uiState.syncFeedbackMessage?.let { msg ->
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_feedback_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (msg.startsWith("❌")) StatusDangerBg else if (msg.startsWith("⚠️")) StatusWarningBg else CorporateTealLight,
                    border = BorderStroke(1.dp, if (msg.startsWith("❌")) StatusDanger else if (msg.startsWith("⚠️")) StatusWarning else CorporateBluePrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CorporateBlueDark
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearFeedbackMessage() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Fechar",
                                tint = CorporateBlueDark
                            )
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // 2. PAINEL DE STATUS DA CONEXÃO HOSTINGER & GITHUB
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CorporateBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CorporateBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = CorporateBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hostinger & GitHub Hub",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CorporateBlueDark
                                    )
                                )
                                Text(
                                    text = "Comunicação Banco MySQL + 5 Planilhas Excel",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                                )
                            }
                        }

                        // Badge de Status Hostinger
                        val (statusText, statusColor, statusBg) = when (uiState.hostingerConnectionStatus) {
                            HostingerConnectionStatus.CONNECTED -> Triple("MYSQL ONLINE", StatusSuccess, StatusSuccessBg)
                            HostingerConnectionStatus.TESTING -> Triple("TESTANDO...", CorporateBluePrimary, CorporateBlueLight)
                            HostingerConnectionStatus.FAILED -> Triple("DESCONECTADO", StatusDanger, StatusDangerBg)
                            HostingerConnectionStatus.NOT_CONFIGURED -> Triple("NÃO TESTADO", StatusWarning, StatusWarningBg)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (uiState.hostingerConnectionStatus == HostingerConnectionStatus.TESTING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = statusColor
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (uiState.hostingerConnectionStatus == HostingerConnectionStatus.CONNECTED) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CorporateBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "REPOSITÓRIO GITHUB",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateTextTertiary,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "${uiState.hostingerConfig.githubOwner}/${uiState.hostingerConfig.githubRepo}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CorporateTextPrimary
                                )
                            )
                            Text(
                                text = "branch: ${uiState.hostingerConfig.githubBranch}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary, fontSize = 11.sp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BANCO MYSQL HOSTINGER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateTextTertiary,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = uiState.hostingerStatusResponse?.databaseName ?: "MySQL Hostinger",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CorporateTextPrimary
                                )
                            )
                            Text(
                                text = "${uiState.hostingerStatusResponse?.recordsCount ?: allRecordsCount(viewModel)} registros salvos",
                                style = MaterialTheme.typography.bodySmall.copy(color = CorporateTealSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botões de Ação Rápida
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testHostingerConnection() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("test_hostinger_connection_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Testar Conexão", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.syncAllGitHubFiles()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("sync_all_github_files_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isSyncingGitHub) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sincronizar 5 Planilhas", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // 3. SEÇÃO DOS 5 ARQUIVOS EXCEL DO GITHUB
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CorporateBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "5 Arquivos Excel no GitHub",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateBlueDark
                                )
                            )
                            Text(
                                text = "Sincronização direta dos 5 pilares de indicadores Claro",
                                style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CorporateBlueLight
                        ) {
                            Text(
                                text = "5 / 5 Arquivos",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateBluePrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lista dos 5 Arquivos
                    GitHubFileKey.values().forEach { fileKey ->
                        val itemState = uiState.gitHubFilesState[fileKey] ?: GitHubFileItemState(fileKey)
                        GitHubFileRowItem(
                            fileKey = fileKey,
                            state = itemState,
                            onSyncClick = { viewModel.syncSingleGitHubFile(fileKey) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botão para carregar dados oficiais de homologação dos 5 arquivos
                    OutlinedButton(
                        onClick = { viewModel.loadDemoGitHubFiles() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("load_demo_5_files_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CorporateTealSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Carregar Demonstração Oficial dos 5 Arquivos (Homologação)",
                            color = CorporateTealSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // 4. CONFIGURAÇÕES DA API HOSTINGER & REPOSITÓRIO GITHUB
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CorporateBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Configurações da Hostinger e GitHub",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateBlueDark
                        )
                    )
                    Text(
                        text = "Informe o endpoint do seu domínio Hostinger e seu repositório",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // URL da API Hostinger
                    Text(
                        text = "URL da API na Hostinger",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = CorporateTextPrimary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = apiUrlInput,
                        onValueChange = { apiUrlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hostinger_api_url_input"),
                        placeholder = { Text("https://seusite.com.br/api/indicadores_api.php") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Token de Segurança / Chave da API
                    Text(
                        text = "Chave de Segurança (API Key)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = CorporateTextPrimary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hostinger_api_key_input"),
                        placeholder = { Text("claro_indicadores_sec_2026") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Usuário GitHub e Repositório
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Usuário GitHub",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = CorporateTextPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ghOwnerInput,
                                onValueChange = { ghOwnerInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("github_owner_input"),
                                placeholder = { Text("carloswladier") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Repositório",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = CorporateTextPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ghRepoInput,
                                onValueChange = { ghRepoInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("github_repo_input"),
                                placeholder = { Text("indicadores-claro") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Branch e Pasta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Branch",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = CorporateTextPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ghBranchInput,
                                onValueChange = { ghBranchInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("main") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Subpasta (Opcional)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = CorporateTextPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ghFolderInput,
                                onValueChange = { ghFolderInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("ex: planilhas") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão Salvar
                    Button(
                        onClick = {
                            viewModel.updateHostingerConfig(
                                HostingerConfig(
                                    apiUrl = apiUrlInput,
                                    apiKey = apiKeyInput,
                                    githubOwner = ghOwnerInput,
                                    githubRepo = ghRepoInput,
                                    githubBranch = ghBranchInput,
                                    githubFolderPath = ghFolderInput,
                                    autoSyncOnStart = autoSyncInput
                                )
                            )
                            Toast.makeText(context, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_hostinger_config_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Configurações")
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // 5. OPERAÇÕES DE BANCO DE DADOS HOSTINGER (PUSH / PULL)
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CorporateBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Sincronização Banco Hostinger MySQL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateBlueDark
                        )
                    )
                    Text(
                        text = "Envie e receba registros consolidados entre o app e o banco remoto",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.pushAllToHostinger() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("push_records_to_hostinger_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateTealSecondary)
                        ) {
                            if (uiState.isSyncingHostinger) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar -> Hostinger", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.pullFromHostinger() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("pull_records_from_hostinger_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isSyncingHostinger) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Puxar <- Hostinger", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // 6. GUIA PASSO A PASSO & EXPORTAÇÃO DOS SCRIPTS HOSTINGER
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CorporateBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = CorporateBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Como Configurar na Hostinger",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateBlueDark
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = { showCodeDialog = !showCodeDialog },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (showCodeDialog) "Ocultar" else "Ver Código", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. No hPanel da Hostinger, vá em 'Bancos de Dados MySQL' e crie seu banco.\n" +
                               "2. No 'Gerenciador de Arquivos', crie a pasta public_html/api/ e suba o arquivo indicadores_api.php.\n" +
                               "3. O script já cria automaticamente a tabela indicator_records no primeiro acesso.\n" +
                               "4. Coloque a URL acima e teste a conexão!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CorporateTextPrimary,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val php = HostingerScriptGenerator.getPhpScript(uiState.hostingerConfig)
                                clipboardManager.setText(AnnotatedString(php))
                                Toast.makeText(context, "Script PHP copiado para a área de transferência!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("copy_php_script_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Script PHP", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val sql = HostingerScriptGenerator.getSqlSchema()
                                clipboardManager.setText(AnnotatedString(sql))
                                Toast.makeText(context, "Schema SQL copiado para a área de transferência!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("copy_sql_schema_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar SQL phpMyAdmin", fontSize = 11.sp)
                        }
                    }

                    // Visualizador expansível do código PHP
                    AnimatedVisibility(visible = showCodeDialog) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = CorporateBlueDark
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "// public_html/api/indicadores_api.php",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CorporateTealSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = HostingerScriptGenerator.getPhpScript(uiState.hostingerConfig).take(800) + "\n... [Clique em 'Copiar Script PHP' para o código completo]",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GitHubFileRowItem(
    fileKey: GitHubFileKey,
    state: GitHubFileItemState,
    onSyncClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CorporateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(CorporateBlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = CorporateBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${fileKey.fileNameBase}.xlsx",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorporateTextPrimary
                            )
                        )
                    }
                    Text(
                        text = "${fileKey.displayName} • Meta: ${fileKey.defaultTarget} ${fileKey.unit}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CorporateTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    state.message?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (state.status == GitHubFileStatus.ERROR) StatusDanger else StatusSuccess,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Status e Ação
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (badgeText, badgeColor, badgeBg) = when (state.status) {
                    GitHubFileStatus.SUCCESS -> Triple("SINCRONIZADO", StatusSuccess, StatusSuccessBg)
                    GitHubFileStatus.SYNCING -> Triple("BAIXANDO...", CorporateBluePrimary, CorporateBlueLight)
                    GitHubFileStatus.ERROR -> Triple("ERRO", StatusDanger, StatusDangerBg)
                    GitHubFileStatus.IDLE -> Triple("PENDENTE", CorporateTextTertiary, CorporateBlueLight.copy(alpha = 0.5f))
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 9.sp
                        )
                    )
                }

                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (state.status == GitHubFileStatus.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CorporateBluePrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar",
                            tint = CorporateBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun allRecordsCount(viewModel: IndicatorsViewModel): Int {
    return viewModel.allRecords.value.size
}
