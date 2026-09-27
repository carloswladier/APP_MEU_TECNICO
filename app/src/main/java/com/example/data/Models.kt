package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    TECNICO
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val login: String, // Login utilizado na planilha e no sistema (ex: tec.carlos, admin)
    val name: String,
    val email: String, // E-mail cadastrado pelo administrador para recuperação de senha
    val role: UserRole,
    val passwordHash: String, // Senha ou hash seguro
    val createdAt: Long = System.currentTimeMillis()
)

enum class IndicatorType(val label: String, val unit: String, val defaultTarget: Double, val isHigherBetter: Boolean) {
    TECNICO_CERTIFICADO("Técnico Certificado", "%", 95.0, true),
    CERTIDAO_ATENDIMENTO("Certidão de Atendimento", "%", 85.0, true),
    REVISITA_30D("Revisita 30D", "%", 7.0, false), // meta revisita <= 7.0%
    COP_360("Cop 360", "%", 90.0, true),
    TNPS("TNPS", "pts", 80.0, true);

    companion object {
        fun fromString(value: String): IndicatorType {
            val normalized = value.trim().uppercase()
            return when {
                normalized.contains("CERTIFICADO") || normalized.contains("CERTIFICAO") -> TECNICO_CERTIFICADO
                normalized.contains("CERTID") || normalized.contains("ATENDIMENTO") -> CERTIDAO_ATENDIMENTO
                normalized.contains("REVISITA") || normalized.contains("30D") -> REVISITA_30D
                normalized.contains("COP") || normalized.contains("360") -> COP_360
                normalized.contains("TNPS") || normalized.contains("NPS") -> TNPS
                else -> TECNICO_CERTIFICADO
            }
        }
    }
}

@Entity(tableName = "indicator_records")
data class IndicatorRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tecnicoLogin: String, // Chave de vinculação login_sistema x login_planilha
    val tecnicoNome: String,
    val indicatorType: String, // IndicatorType.name
    val mes: String, // ex: "Janeiro", "Fevereiro", etc.
    val mesNumero: Int, // 1..12
    val periodo: String, // ex: "1º Semestre", "2º Semestre", "Últimos 30 Dias"
    val dataRegistro: String, // dd/MM/yyyy
    val meta: Double,
    val realizado: Double,
    val atingimentoPercentual: Double,
    val totalAtendimentos: Int,
    val status: String, // ATINGIDA, ATENCAO, CRITICO
    val ordemServico: String, // Número da OS
    val cliente: String,
    val observacoes: String,
    val origemArquivo: String,
    val dataUpload: Long = System.currentTimeMillis(),
    val produtividade: Double = 0.0, // Meta >= 5.0
    val revisita: Double = 0.0, // Meta <= 7.0%
    val tec1: Double = 0.0, // Meta >= 95.0%
    val isCertificado: Boolean = false, // Verdadeiro se atende aos 3 pilares
    val categoriaCertidao: String = "", // V. SEM FALHA, V. COM FALHA, V. FALHA API, V. JUSTIFICADO, NV. COM FALHA, NV. SEM FALHA, NV. FALHA API
    val vSemFalha: Int = 0,
    val vComFalha: Int = 0,
    val vFalhaApi: Int = 0,
    val vJustificado: Int = 0,
    val nvComFalha: Int = 0,
    val nvSemFalha: Int = 0,
    val nvFalhaApi: Int = 0,
    val saldoProducao: Double = 0.0,
    val saldoRevisita: Double = 0.0,
    val ganhoRevisita: String = ""
)

enum class ValidacaoCertidao(val label: String, val isValidado: Boolean) {
    V_SEM_FALHA("V. SEM FALHA", true),
    V_COM_FALHA("V. COM FALHA", true),
    V_FALHA_API("V. FALHA API", true),
    V_JUSTIFICADO("V. JUSTIFICADO", true),
    NV_COM_FALHA("NV. COM FALHA", false),
    NV_SEM_FALHA("NV. SEM FALHA", false),
    NV_FALHA_API("NV. FALHA API", false);

    companion object {
        fun fromString(str: String): ValidacaoCertidao {
            val upper = str.trim().uppercase()
            return when {
                (upper.startsWith("NV") || upper.contains("NÃO VALIDADO") || upper.contains("NAO VALIDADO")) && (upper.contains("SEM FALHA") || upper.contains("SEM_FALHA")) -> NV_SEM_FALHA
                (upper.startsWith("NV") || upper.contains("NÃO VALIDADO") || upper.contains("NAO VALIDADO")) && (upper.contains("API")) -> NV_FALHA_API
                (upper.startsWith("NV") || upper.contains("NÃO VALIDADO") || upper.contains("NAO VALIDADO")) -> NV_COM_FALHA
                upper.contains("JUSTIFICAD") -> V_JUSTIFICADO
                upper.contains("API") -> V_FALHA_API
                upper.contains("COM FALHA") || upper.contains("COM_FALHA") -> V_COM_FALHA
                else -> V_SEM_FALHA
            }
        }
    }
}

@Entity(tableName = "upload_logs")
data class UploadLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val uploadedBy: String,
    val recordCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val fileName: String = ""
)
