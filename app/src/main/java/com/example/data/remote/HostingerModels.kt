package com.example.data.remote

import com.example.data.IndicatorType

enum class GitHubFileKey(
    val fileNameBase: String,
    val displayName: String,
    val indicatorType: IndicatorType,
    val defaultTarget: Double,
    val unit: String
) {
    TECNICO_CERTIFICADO(
        fileNameBase = "tecnico_certificao",
        displayName = "Técnico Certificado",
        indicatorType = IndicatorType.TECNICO_CERTIFICADO,
        defaultTarget = 95.0,
        unit = "%"
    ),
    REVISITA_30D(
        fileNameBase = "revisita30d",
        displayName = "Revisita 30D",
        indicatorType = IndicatorType.REVISITA_30D,
        defaultTarget = 7.0,
        unit = "%"
    ),
    CERTIDAO_ATENDIMENTO(
        fileNameBase = "certidao_atendimento",
        displayName = "Certidão de Atendimento",
        indicatorType = IndicatorType.CERTIDAO_ATENDIMENTO,
        defaultTarget = 85.0,
        unit = "%"
    ),
    COP_360(
        fileNameBase = "cop_360",
        displayName = "Cop 360",
        indicatorType = IndicatorType.COP_360,
        defaultTarget = 90.0,
        unit = "%"
    ),
    TNPS(
        fileNameBase = "tnps",
        displayName = "TNPS",
        indicatorType = IndicatorType.TNPS,
        defaultTarget = 80.0,
        unit = "pts"
    )
}

enum class GitHubFileStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class GitHubFileItemState(
    val key: GitHubFileKey,
    val status: GitHubFileStatus = GitHubFileStatus.IDLE,
    val recordCount: Int = 0,
    val lastSyncTime: String? = null,
    val message: String? = null
)

enum class HostingerConnectionStatus {
    NOT_CONFIGURED,
    TESTING,
    CONNECTED,
    FAILED
}

data class HostingerConfig(
    val apiUrl: String = "https://seusite.com.br/api/indicadores_api.php",
    val apiKey: String = "claro_indicadores_sec_2026",
    val githubOwner: String = "carloswladier",
    val githubRepo: String = "indicadores-claro",
    val githubBranch: String = "main",
    val githubFolderPath: String = "",
    val githubToken: String = "",
    val autoSyncOnStart: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val lastSyncStatus: String = "Aguardando primeira sincronização"
)

data class HostingerStatusResponse(
    val status: String = "ok",
    val databaseConnected: Boolean = false,
    val databaseName: String? = null,
    val recordsCount: Int = 0,
    val distinctTechnicians: Int = 0,
    val serverTime: String? = null,
    val message: String = ""
)

data class HostingerSyncResponse(
    val status: String = "ok",
    val insertedCount: Int = 0,
    val totalRecords: Int = 0,
    val message: String = ""
)
