package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IndicatorRecord
import com.example.data.IndicatorType
import com.example.ui.AppTab
import com.example.ui.CertidaoSummary
import com.example.ui.KpiSummary
import com.example.ui.theme.CorporateBlueDark
import com.example.ui.theme.CorporateBlueLight
import com.example.ui.theme.CorporateBluePrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateOrangeAccent
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
import com.example.util.ProcessResult

@Composable
fun KpiSummaryCard(
    kpi: KpiSummary,
    tab: AppTab,
    modifier: Modifier = Modifier
) {
    if (tab == AppTab.TECNICO_CERTIFICADO) {
        TecnicoCertificadoCard(kpi = kpi, modifier = modifier)
        return
    }

    if (tab == AppTab.CERTIDAO_ATENDIMENTO) {
        CertidaoAtendimentoOverview(certidao = kpi.certidao, modifier = modifier)
        return
    }

    val isRevisita = tab == AppTab.REVISITA_30D
    val isMetaAtingida = if (isRevisita) {
        kpi.realizadoMedio <= kpi.metaMedia
    } else {
        kpi.realizadoMedio >= kpi.metaMedia
    }

    val statusColor = when {
        isMetaAtingida -> StatusSuccess
        kpi.atingimentoGeralPct >= 90.0 -> StatusWarning
        else -> StatusDanger
    }

    val statusBg = when {
        isMetaAtingida -> StatusSuccessBg
        kpi.atingimentoGeralPct >= 90.0 -> StatusWarningBg
        else -> StatusDangerBg
    }

    val progressValue = (kpi.atingimentoGeralPct / 100f).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressValue, label = "kpiProgress")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CorporateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resultado Consolidado • ${tab.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateBlueDark
                        )
                    )
                    Text(
                        text = "Base calculada a partir dos dados processados da planilha Claro",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMetaAtingida) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMetaAtingida) "META BATIDA" else if (kpi.atingimentoGeralPct >= 90.0) "EM ATENÇÃO" else "ABAIXO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Indicadores Principais em Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Meta
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "META ESTIPULADA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextTertiary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = if (isRevisita) "<= ${kpi.metaMedia}%" else "${kpi.metaMedia} ${kpi.unit}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextPrimary
                        )
                    )
                }

                // Realizado
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "REALIZADO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextTertiary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${kpi.realizadoMedio} ${kpi.unit}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    )
                }

                // Atingimento %
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ATINGIMENTO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextTertiary,
                            fontSize = 11.sp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMetaAtingida) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${kpi.atingimentoGeralPct}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de Progresso Visual
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = CorporateBorder
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total de Atendimentos: ${kpi.totalAtendimentos}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CorporateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Registros Avaliados: ${kpi.totalRegistros}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CorporateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TecnicoCertificadoCard(
    kpi: KpiSummary,
    modifier: Modifier = Modifier
) {
    val isCertificado = kpi.isTecnicoCertificado
    val statusColor = if (isCertificado) StatusSuccess else if (kpi.prodAtingida || kpi.tec1Atingida || kpi.revisitaAtingida) StatusWarning else StatusDanger
    val statusBg = if (isCertificado) StatusSuccessBg else if (kpi.prodAtingida || kpi.tec1Atingida || kpi.revisitaAtingida) StatusWarningBg else StatusDangerBg

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, if (isCertificado) StatusSuccess.copy(alpha = 0.5f) else CorporateBluePrimary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header com Selo Claro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCertificado) StatusSuccessBg else CorporateBlueLight,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCertificado) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isCertificado) StatusSuccess else CorporateBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Certificação Técnica Claro",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorporateBlueDark
                            )
                        )
                        Text(
                            text = "Metas: Produtividade >= 5 | Revisita <= 7% | TEC1 >= 95%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CorporateBluePrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCertificado) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCertificado) "CERTIFICADO" else "EM EVOLUÇÃO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Os 3 Pilares da Certificação Claro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Produtividade (Meta >= 5.0)
                CertificationPillarItem(
                    modifier = Modifier.weight(1f),
                    title = "Produtividade",
                    metaLabel = "Meta >= 5.0",
                    realizadoText = "${kpi.prodMedia}",
                    unit = "OS/dia",
                    isAtingida = kpi.prodAtingida,
                    progress = (kpi.prodMedia / 5.0).toFloat().coerceIn(0f, 1.2f)
                )

                // 2. Revisita (Meta <= 7.0%)
                CertificationPillarItem(
                    modifier = Modifier.weight(1f),
                    title = "Revisita 30D",
                    metaLabel = "Meta <= 7.0%",
                    realizadoText = "${kpi.revisitaMedia}%",
                    unit = "",
                    isAtingida = kpi.revisitaAtingida,
                    progress = if (kpi.revisitaMedia > 0) (7.0 / kpi.revisitaMedia).toFloat().coerceIn(0f, 1.2f) else 1f
                )

                // 3. TEC1 (Meta >= 95.0%)
                CertificationPillarItem(
                    modifier = Modifier.weight(1f),
                    title = "TEC1",
                    metaLabel = "Meta >= 95.0%",
                    realizadoText = "${kpi.tec1Media}%",
                    unit = "",
                    isAtingida = kpi.tec1Atingida,
                    progress = (kpi.tec1Media / 95.0).toFloat().coerceIn(0f, 1.2f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Banner Informativo do Status de Homologação
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCertificado) StatusSuccessBg.copy(alpha = 0.7f) else CorporateBlueLight.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, if (isCertificado) StatusSuccess.copy(alpha = 0.3f) else CorporateBluePrimary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCertificado) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (isCertificado) StatusSuccess else CorporateBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCertificado) {
                            "Parabéns! O técnico cumpre com excelência os 3 pilares operacionais da Claro."
                        } else {
                            val faltam = listOf(
                                if (!kpi.prodAtingida) "Produtividade (>=5.0)" else null,
                                if (!kpi.revisitaAtingida) "Revisita (<=7.0%)" else null,
                                if (!kpi.tec1Atingida) "TEC1 (>=95.0%)" else null
                            ).filterNotNull()
                            "Para certificação Claro, meta(s) pendente(s): ${faltam.joinToString(", ")}."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCertificado) StatusSuccess else CorporateBlueDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CertificationPillarItem(
    title: String,
    metaLabel: String,
    realizadoText: String,
    unit: String,
    isAtingida: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val pillColor = if (isAtingida) StatusSuccess else StatusDanger
    val pillBg = if (isAtingida) StatusSuccessBg else StatusDangerBg

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isAtingida) StatusSuccess.copy(alpha = 0.3f) else CorporateBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CorporateTextSecondary,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
            Text(
                text = metaLabel,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CorporateTextTertiary,
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (unit.isNotEmpty()) "$realizadoText $unit" else realizadoText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = pillColor
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = pillBg
            ) {
                Text(
                    text = if (isAtingida) "ATINGIDA" else "ABAIXO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = pillColor,
                        fontSize = 9.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CertidaoAtendimentoOverview(
    certidao: CertidaoSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 4 Cards de Resumo Superior
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 760.dp
            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NotaCertidaoCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                    TotalAtendimentosCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                    ValidadoCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                    NaoValidadoCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotaCertidaoCard(certidao = certidao, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TotalAtendimentosCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                        ValidadoCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                        NaoValidadoCard(certidao = certidao, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        // Título da Seção: STATUS DE VALIDAÇÃO (7 CATEGORIAS)
        Text(
            text = "STATUS DE VALIDAÇÃO (7 CATEGORIAS)",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CorporateTextSecondary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )

        // Linha com os 7 Cards das Categorias em scroll horizontal suave
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryValidationCard(
                title = "V. SEM FALHA",
                count = certidao.vSemFalha,
                pct = certidao.vSemFalhaPct,
                accentColor = Color(0xFF1E293B),
                impactoColor = Color(0xFF0F172A)
            )
            CategoryValidationCard(
                title = "V. COM FALHA",
                count = certidao.vComFalha,
                pct = certidao.vComFalhaPct,
                accentColor = Color(0xFFEF4444),
                impactoColor = Color(0xFFEF4444)
            )
            CategoryValidationCard(
                title = "V. FALHA API",
                count = certidao.vFalhaApi,
                pct = certidao.vFalhaApiPct,
                accentColor = Color(0xFF991B1B),
                impactoColor = Color(0xFF991B1B)
            )
            CategoryValidationCard(
                title = "V. JUSTIFICADO",
                count = certidao.vJustificado,
                pct = certidao.vJustificadoPct,
                accentColor = Color(0xFF475569),
                impactoColor = Color(0xFF475569)
            )
            CategoryValidationCard(
                title = "NV. COM FALHA",
                count = certidao.nvComFalha,
                pct = certidao.nvComFalhaPct,
                accentColor = Color(0xFFDC2626),
                impactoColor = Color(0xFFDC2626)
            )
            CategoryValidationCard(
                title = "NV. SEM FALHA",
                count = certidao.nvSemFalha,
                pct = certidao.nvSemFalhaPct,
                accentColor = Color(0xFF2563EB),
                impactoColor = Color(0xFF2563EB)
            )
            CategoryValidationCard(
                title = "NV. FALHA API",
                count = certidao.nvFalhaApi,
                pct = certidao.nvFalhaApiPct,
                accentColor = Color(0xFFEA580C),
                impactoColor = Color(0xFFEA580C)
            )
        }
    }
}

@Composable
fun NotaCertidaoCard(
    certidao: CertidaoSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE52320)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NOTA CERTIDÃO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "META >= 85%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${certidao.notaCertidaoPct}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 32.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GERAL (${certidao.totalValidados} / ${certidao.totalAtendimentos})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.sp
                    )
                )
                if (certidao.isMetaAtingida) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "ATINGIDA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TotalAtendimentosCard(
    certidao: CertidaoSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CorporateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL ATEND.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextSecondary,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CorporateTextSecondary,
                                fontSize = 9.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${certidao.totalAtendimentos}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = CorporateTextPrimary,
                        fontSize = 24.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "100.0% BASE GERAL",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CorporateTextSecondary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                    )
            )
        }
    }
}

@Composable
fun ValidadoCard(
    certidao: CertidaoSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CorporateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VALIDADO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextSecondary,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusSuccessBg
                    ) {
                        Text(
                            text = "${certidao.pctValidados}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusSuccess,
                                fontSize = 9.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${certidao.totalValidados}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = CorporateTextPrimary,
                        fontSize = 24.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "% IMPACTO: ${certidao.pctValidados}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = StatusSuccess,
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                    )
            )
        }
    }
}

@Composable
fun NaoValidadoCard(
    certidao: CertidaoSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CorporateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NÃO VALID.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextSecondary,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusDangerBg
                    ) {
                        Text(
                            text = "${certidao.pctNaoValidados}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusDanger,
                                fontSize = 9.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${certidao.totalNaoValidados}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = CorporateTextPrimary,
                        fontSize = 24.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "% IMPACTO: ${certidao.pctNaoValidados}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = StatusDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = StatusDanger,
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                    )
            )
        }
    }
}

@Composable
fun CategoryValidationCard(
    title: String,
    count: Int,
    pct: Double,
    accentColor: Color,
    impactoColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(min = 145.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CorporateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CorporateTextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = CorporateTextPrimary,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IMPACTO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CorporateTextTertiary,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = impactoColor,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
            )
        }
    }
}

@Composable
fun RecordItemCard(
    record: IndicatorRecord,
    modifier: Modifier = Modifier
) {
    val isPositive = record.status == "ATINGIDA"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CorporateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CorporateBlueLight,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = CorporateBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = record.ordemServico,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorporateTextPrimary
                            )
                        )
                        Text(
                            text = "${record.dataRegistro} • Mês: ${record.mes} (${record.periodo})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CorporateTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPositive) StatusSuccessBg else if (record.status == "ATENCAO") StatusWarningBg else StatusDangerBg
                ) {
                    Text(
                        text = record.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) StatusSuccess else if (record.status == "ATENCAO") StatusWarning else StatusDanger
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Técnico e Dados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CorporateTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${record.tecnicoNome} (${record.tecnicoLogin})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CorporateTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Text(
                    text = "Atingimento: ${record.atingimentoPercentual}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isPositive) StatusSuccess else StatusDanger,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (record.indicatorType == IndicatorType.TECNICO_CERTIFICADO.name) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, CorporateBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Pilares da Certificação Claro:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorporateBlueDark,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val prodOk = record.produtividade >= 5.0
                            val revOk = record.revisita <= 7.0
                            val tec1Ok = record.tec1 >= 95.0

                            Column {
                                Text(
                                    text = "Prod: ${record.produtividade} (>=5.0)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (prodOk) StatusSuccess else StatusDanger
                                    )
                                )
                                Text(
                                    text = "Saldo: ${if (record.saldoProducao > 0) "+${record.saldoProducao}" else record.saldoProducao}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = CorporateTextSecondary
                                    )
                                )
                            }

                            Column {
                                Text(
                                    text = "Rev: ${record.revisita}% (<=7%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (revOk) StatusSuccess else StatusDanger
                                    )
                                )
                                Text(
                                    text = "Saldo: ${if (record.saldoRevisita > 0) "+${record.saldoRevisita}" else record.saldoRevisita}${if (record.ganhoRevisita.isNotBlank() && record.ganhoRevisita != "—") " • ${record.ganhoRevisita}" else ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = CorporateTextSecondary
                                    )
                                )
                            }

                            Column {
                                Text(
                                    text = "TEC1: ${record.tec1}% (>=95%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tec1Ok) StatusSuccess else StatusDanger
                                    )
                                )
                                Text(
                                    text = if (record.isCertificado) "Selo Claro: ✓" else "Selo: Pendente",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.isCertificado) StatusSuccess else StatusWarning
                                    )
                                )
                            }
                        }
                    }
                }
            } else if (record.indicatorType == IndicatorType.CERTIDAO_ATENDIMENTO.name) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meta: >= 85.0% | Realizado: ${record.realizado}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (record.realizado >= 85.0) StatusSuccess else StatusDanger,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val catLabel = record.categoriaCertidao.ifBlank { "V. SEM FALHA" }
                        val isVal = !catLabel.startsWith("NV")
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isVal) StatusSuccessBg else StatusDangerBg
                        ) {
                            Text(
                                text = catLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVal) StatusSuccess else StatusDanger,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    val totalVal = record.vSemFalha + record.vComFalha + record.vFalhaApi + record.vJustificado
                    val totalNVal = record.nvComFalha + record.nvSemFalha + record.nvFalhaApi
                    if (totalVal + totalNVal > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Validados: $totalVal | Não Validados: $totalNVal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CorporateTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "Vol: ${record.totalAtendimentos} atend.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CorporateTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val metaStr = if (record.indicatorType == IndicatorType.REVISITA_30D.name) "<= ${record.meta}%" else "${record.meta}"
                    Text(
                        text = "Meta: $metaStr | Realizado: ${record.realizado}",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                    )
                    Text(
                        text = "Vol: ${record.totalAtendimentos} atend.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
fun UploadExcelDialog(
    isUploading: Boolean,
    progressMessage: String?,
    uploadResult: ProcessResult?,
    onUploadUri: (Uri, String) -> Unit,
    onLoadOfficialDemo: () -> Unit,
    onGoToHostingerGitHub: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "planilha_importada.xlsx"
            onUploadUri(uri, fileName)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = CorporateBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload de Planilha Excel",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CorporateBlueDark
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "O administrador pode fazer upload de arquivos Excel (.xlsx) ou (.csv). O sistema compara em tempo real o login da planilha com o login dos técnicos cadastrados.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CorporateTextPrimary)
                )

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CorporateBluePrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = progressMessage ?: "Processando planilha em tempo real...",
                                style = MaterialTheme.typography.bodySmall.copy(color = CorporateBluePrimary)
                            )
                        }
                    }
                } else {
                    // Opção 1: Selecionar Arquivo do Dispositivo
                    Button(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel",
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "*/*"
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("select_excel_file_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selecionar Arquivo Excel (.xlsx / .csv)")
                    }

                    // Opção 2: Carregar Planilha Oficial Demonstrativa Completa
                    OutlinedButton(
                        onClick = onLoadOfficialDemo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_demo_excel_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = CorporateTealSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Carregar Planilha Oficial Corporativa (Demo Real)", color = CorporateTealSecondary)
                    }

                    // Opção 3: Sincronizar dos 5 Arquivos do GitHub / Hostinger
                    if (onGoToHostingerGitHub != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onGoToHostingerGitHub()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("go_to_hostinger_github_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = CorporateBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar 5 Planilhas do GitHub / Hostinger", color = CorporateBluePrimary)
                        }
                    }
                }

                // Resultado do processamento
                uploadResult?.let { res ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (res.success) CorporateTealLight else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (res.success) "✅ Processamento Concluído" else "❌ Falha no Processamento",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.success) StatusSuccess else StatusDanger
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = res.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextPrimary)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Concluir")
            }
        }
    )
}
