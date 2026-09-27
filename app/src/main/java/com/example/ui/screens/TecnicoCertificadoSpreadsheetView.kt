package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IndicatorRecord
import com.example.data.User
import com.example.ui.KpiSummary
import com.example.ui.theme.ClaroRedDark
import com.example.ui.theme.ClaroRedLight
import com.example.ui.theme.ClaroRedPrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateSurfaceVariant
import com.example.ui.theme.CorporateTextPrimary
import com.example.ui.theme.CorporateTextSecondary
import com.example.ui.theme.CorporateTextTertiary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import java.util.Locale

@Composable
fun TecnicoCertificadoSpreadsheetView(
    records: List<IndicatorRecord>,
    kpi: KpiSummary,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onSelectTechnician: ((String) -> Unit)? = null
) {
    var selectedRecordForDetail by remember { mutableStateOf<IndicatorRecord?>(null) }

    // Agrupar registros por técnico para visão consolidada sem repetição
    val distinctTechRecords = remember(records) {
        records
            .groupBy { it.tecnicoLogin }
            .map { (_, list) ->
                list.maxByOrNull { it.id } ?: list.first()
            }
            .sortedWith(
                compareByDescending<IndicatorRecord> { it.isCertificado }
                    .thenBy { it.tecnicoNome }
            )
    }

    val totalTechs = distinctTechRecords.size
    val totalCertificados = distinctTechRecords.count { it.isCertificado }
    val pctCertificados = if (totalTechs > 0) (totalCertificados * 100.0 / totalTechs) else 0.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner Superior Consolidado Claro
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, ClaroRedPrimary.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ClaroRedLight,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ClaroRedPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Visão Técnico Certificado",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = ClaroRedDark,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ClaroRedPrimary
                            ) {
                                Text(
                                    text = "CLARO OFICIAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Matriz de Homologação: Produção, Revisita e TEC1 com Saldos e Ganhos",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CorporateTextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resumo dos 3 Pilares e Percentual de Homologação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        title = "PRODUÇÃO",
                        meta = ">= 5.0",
                        color = Color(0xFF1E3A8A),
                        bgColor = Color(0xFFEFF6FF)
                    )
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        title = "REVISITA",
                        meta = "<= 7.0%",
                        color = Color(0xFFC2410C),
                        bgColor = Color(0xFFFFF7ED)
                    )
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        title = "TEC1",
                        meta = ">= 95.0%",
                        color = Color(0xFF047857),
                        bgColor = Color(0xFFECFDF5)
                    )
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        title = "CERTIFICADOS",
                        meta = "$totalCertificados/$totalTechs (${String.format(Locale.getDefault(), "%.0f", pctCertificados)}%)",
                        color = if (pctCertificados >= 70.0) StatusSuccess else StatusWarning,
                        bgColor = if (pctCertificados >= 70.0) StatusSuccessBg else StatusWarningBg
                    )
                }
            }
        }

        // Lista Completa dos Indicadores de Cada Técnico (Ajustada 100% na tela, sem rolagem lateral)
        if (distinctTechRecords.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CorporateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nenhum registro de técnico encontrado.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CorporateTextSecondary)
                    )
                }
            }
        } else {
            distinctTechRecords.forEach { record ->
                TecnicoIndicatorsFullWidthCard(
                    record = record,
                    isCurrentUser = record.tecnicoLogin.equals(currentUser?.login, ignoreCase = true),
                    onClick = { selectedRecordForDetail = record }
                )
            }
        }
    }

    // Modal de Detalhes ao Clicar no Técnico
    selectedRecordForDetail?.let { record ->
        TecnicoDetailDialog(
            record = record,
            onDismiss = { selectedRecordForDetail = null }
        )
    }
}

/**
 * Card Completo de Indicadores do Técnico:
 * Encaixa 100% na largura da tela sem necessidade de rolagem lateral (sem arrastar para o lado).
 * Apresenta:
 * - Topo: Nome do Técnico + Login + Selo Claro (CERTIFICADO / EM EVOLUÇÃO)
 * - Grade proporcional de 3 colunas:
 *   1. PRODUÇÃO (Produtividade e Saldo)
 *   2. REVISITA (Revisita %, Saldo e Ganho)
 *   3. TEC1 (TEC1 % e Situação)
 */
@Composable
fun TecnicoIndicatorsFullWidthCard(
    record: IndicatorRecord,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCert = record.isCertificado
    val prodOk = record.produtividade >= 5.0
    val revOk = record.revisita <= 7.0
    val tec1Ok = record.tec1 >= 95.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) ClaroRedLight.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isCurrentUser) 1.5.dp else 1.dp,
            if (isCurrentUser) ClaroRedPrimary else if (isCert) StatusSuccess.copy(alpha = 0.35f) else CorporateBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabeçalho: Nome do Técnico e Selo Claro de Certificação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCert) StatusSuccessBg else if (isCurrentUser) ClaroRedLight else CorporateSurfaceVariant,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCert) Icons.Default.CheckCircle else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isCert) StatusSuccess else if (isCurrentUser) ClaroRedPrimary else CorporateTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = record.tecnicoNome,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentUser) ClaroRedDark else CorporateTextPrimary,
                                fontSize = 13.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Login: ${record.tecnicoLogin}${if (isCurrentUser) " • (Você)" else ""}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isCurrentUser) ClaroRedPrimary else CorporateTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Selo de Homologação
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCert) StatusSuccessBg else StatusWarningBg,
                    border = BorderStroke(1.dp, if (isCert) StatusSuccess.copy(alpha = 0.4f) else StatusWarning.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCert) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isCert) StatusSuccess else StatusWarning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCert) "CERTIFICADO" else "EM EVOLUÇÃO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isCert) StatusSuccess else StatusWarning,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Os 3 Pilares da Planilha Exibidos Lado a Lado (100% da Largura, sem arrastar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. COLUNA PRODUÇÃO (Produtividade e Saldo)
                IndicatorPillarColumn(
                    modifier = Modifier.weight(1f),
                    title = "PRODUÇÃO",
                    targetText = "Meta >= 5,0",
                    mainLabel = "Produtividade",
                    mainValue = formatDecimal(record.produtividade),
                    isMainOk = prodOk,
                    subLabel = "Saldo",
                    subValue = if (record.saldoProducao > 0) "+${formatDecimal(record.saldoProducao)}" else formatDecimal(record.saldoProducao),
                    isSubPositive = record.saldoProducao >= 0,
                    accentColor = Color(0xFF1E40AF),
                    bgColor = Color(0xFFEFF6FF)
                )

                // 2. COLUNA REVISITA (Revisita %, Saldo e Ganho)
                IndicatorPillarColumn(
                    modifier = Modifier.weight(1f),
                    title = "REVISITA",
                    targetText = "Meta <= 7,0%",
                    mainLabel = "Revisita",
                    mainValue = "${formatDecimal(record.revisita)}%",
                    isMainOk = revOk,
                    subLabel = "Saldo",
                    subValue = if (record.saldoRevisita > 0) "+${formatDecimal(record.saldoRevisita)}" else formatDecimal(record.saldoRevisita),
                    isSubPositive = record.saldoRevisita >= 0,
                    accentColor = Color(0xFFC2410C),
                    bgColor = Color(0xFFFFF7ED),
                    extraTag = if (record.ganhoRevisita.isNotBlank() && record.ganhoRevisita != "—") record.ganhoRevisita else null
                )

                // 3. COLUNA TEC1 (TEC1 e Situação)
                IndicatorPillarColumn(
                    modifier = Modifier.weight(1f),
                    title = "TEC1",
                    targetText = "Meta >= 95%",
                    mainLabel = "TEC1",
                    mainValue = formatDecimal(record.tec1),
                    isMainOk = tec1Ok,
                    subLabel = "Situação",
                    subValue = if (tec1Ok) "✓ Atingida" else "✗ Pendente",
                    isSubPositive = tec1Ok,
                    accentColor = Color(0xFF065F46),
                    bgColor = Color(0xFFECFDF5)
                )
            }
        }
    }
}

@Composable
private fun IndicatorPillarColumn(
    title: String,
    targetText: String,
    mainLabel: String,
    mainValue: String,
    isMainOk: Boolean,
    subLabel: String,
    subValue: String,
    isSubPositive: Boolean,
    accentColor: Color,
    bgColor: Color,
    extraTag: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Título do Pilar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 9.sp,
                        letterSpacing = 0.3.sp
                    ),
                    maxLines = 1
                )
            }

            Text(
                text = targetText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CorporateTextTertiary,
                    fontSize = 8.5.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Indicador Principal
            Text(
                text = mainLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CorporateTextSecondary,
                    fontSize = 9.sp
                ),
                maxLines = 1
            )
            Text(
                text = mainValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (isMainOk) StatusSuccess else StatusDanger,
                    fontSize = 14.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subindicador (Saldo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$subLabel:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CorporateTextTertiary,
                        fontSize = 8.5.sp
                    )
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSubPositive) accentColor else StatusDanger,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }

            // Tag de Ganho Opcional
            if (extraTag != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StatusSuccessBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ganho: $extraTag",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            fontSize = 8.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMiniPill(
    title: String,
    meta: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = color,
                    fontSize = 8.5.sp
                ),
                maxLines = 1
            )
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TecnicoDetailDialog(
    record: IndicatorRecord,
    onDismiss: () -> Unit
) {
    val isCert = record.isCertificado
    val prodOk = record.produtividade >= 5.0
    val revOk = record.revisita <= 7.0
    val tec1Ok = record.tec1 >= 95.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ficha de Certificação Técnica",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = record.tecnicoNome,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClaroRedDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCert) StatusSuccessBg else StatusWarningBg,
                    border = BorderStroke(1.dp, if (isCert) StatusSuccess.copy(alpha = 0.4f) else StatusWarning.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCert) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isCert) StatusSuccess else StatusWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isCert) "HOMOLOGADO • TÉCNICO CERTIFICADO" else "EM EVOLUÇÃO OPERACIONAL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (isCert) StatusSuccess else StatusWarning
                                )
                            )
                            Text(
                                text = if (isCert)
                                    "O colaborador atingiu com êxito todos os 3 pilares de qualidade Claro."
                                else
                                    "Há indicador(es) pendente(s) para a conquista do selo de certificação.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CorporateTextPrimary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                PilarDetailItem(
                    pilarTitle = "1. PRODUÇÃO (Meta >= 5.0 OS/dia)",
                    realizado = "${formatDecimal(record.produtividade)} OS/dia",
                    saldoText = "Saldo: ${if (record.saldoProducao > 0) "+${formatDecimal(record.saldoProducao)}" else formatDecimal(record.saldoProducao)}",
                    isOk = prodOk,
                    progress = (record.produtividade / 5.0).toFloat().coerceIn(0f, 1.2f)
                )

                PilarDetailItem(
                    pilarTitle = "2. REVISITA (Meta <= 7.0%)",
                    realizado = "${formatDecimal(record.revisita)}%",
                    saldoText = "Saldo: ${if (record.saldoRevisita > 0) "+${formatDecimal(record.saldoRevisita)}" else formatDecimal(record.saldoRevisita)}${if (record.ganhoRevisita.isNotBlank() && record.ganhoRevisita != "—") " | Ganho: ${record.ganhoRevisita}" else ""}",
                    isOk = revOk,
                    progress = if (record.revisita > 0) (7.0 / record.revisita).toFloat().coerceIn(0f, 1.2f) else 1f
                )

                PilarDetailItem(
                    pilarTitle = "3. TEC1 (Meta >= 95.0%)",
                    realizado = "${formatDecimal(record.tec1)}%",
                    saldoText = if (tec1Ok) "Qualidade Atingida" else "Abaixo da Meta",
                    isOk = tec1Ok,
                    progress = (record.tec1 / 95.0).toFloat().coerceIn(0f, 1.2f)
                )

                Divider(color = CorporateBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mês: ${record.mes} (${record.periodo})",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary, fontSize = 11.sp)
                    )
                    Text(
                        text = "Origem: ${record.origemArquivo}",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextTertiary, fontSize = 11.sp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ClaroRedPrimary)
            ) {
                Text("Entendido")
            }
        }
    )
}

@Composable
private fun PilarDetailItem(
    pilarTitle: String,
    realizado: String,
    saldoText: String,
    isOk: Boolean,
    progress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pilarTitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CorporateTextPrimary,
                    fontSize = 11.sp
                )
            )
            Text(
                text = realizado,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = if (isOk) StatusSuccess else StatusDanger,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isOk) StatusSuccess else StatusDanger,
            trackColor = CorporateBorder
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = saldoText,
            style = MaterialTheme.typography.labelSmall.copy(
                color = CorporateTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun formatDecimal(value: Double): String {
    return String.format(Locale.forLanguageTag("pt-BR"), "%.1f", value)
}
