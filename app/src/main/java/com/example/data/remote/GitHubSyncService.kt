package com.example.data.remote

import com.example.data.IndicatorRecord
import com.example.data.IndicatorType
import com.example.data.User
import com.example.util.ExcelProcessor
import com.example.util.ProcessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

class GitHubSyncService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun downloadAndProcessFile(
        config: HostingerConfig,
        fileKey: GitHubFileKey,
        registeredUsers: List<User>
    ): ProcessResult = withContext(Dispatchers.IO) {
        val owner = config.githubOwner.trim()
        val repo = config.githubRepo.trim()
        val branch = config.githubBranch.trim().ifEmpty { "main" }
        val folder = config.githubFolderPath.trim().let {
            if (it.isEmpty() || it.endsWith("/")) it else "$it/"
        }

        // Tentar primeiramente extensão .xlsx e depois .csv
        val extensions = listOf("xlsx", "csv")
        var lastError: Exception? = null

        for (ext in extensions) {
            val fileName = "${fileKey.fileNameBase}.$ext"
            val rawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$folder$fileName"

            try {
                val request = Request.Builder()
                    .url(rawUrl)
                    .addHeader("Accept", "application/octet-stream, text/csv, */*")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val stream = response.body?.byteStream()
                    if (stream != null) {
                        val bytes = stream.readBytes()
                        response.close()
                        val result = ExcelProcessor.processInputStream(
                            inputStream = ByteArrayInputStream(bytes),
                            fileName = fileName,
                            registeredUsers = registeredUsers,
                            indicatorHint = fileKey.indicatorType
                        )
                        return@withContext result
                    }
                }
                response.close()
            } catch (e: Exception) {
                lastError = e
            }
        }

        ProcessResult(
            success = false,
            records = emptyList(),
            matchedTechnicians = emptyList(),
            unmatchedLogins = emptyList(),
            message = "Não foi possível baixar '${fileKey.fileNameBase}' de $owner/$repo ($branch). Verifique se o repositório é público e os arquivos estão nomeados como '${fileKey.fileNameBase}.xlsx' ou '.csv'.\nDetalhe: ${lastError?.localizedMessage ?: "HTTP 404 (Arquivo não encontrado)"}",
            totalRows = 0
        )
    }

    /**
     * Gera conjunto de dados de demonstração em conformidade estrita com o indicador solicitado
     * (permite validar o processamento dos 5 arquivos mesmo sem conexão imediata com o GitHub).
     */
    fun generateDemoRecordsForIndicator(
        fileKey: GitHubFileKey,
        registeredUsers: List<User>
    ): ProcessResult {
        val fileName = "${fileKey.fileNameBase}.xlsx"
        val techs = registeredUsers.ifEmpty {
            listOf(
                User(login = "tec.carlos", name = "Carlos Wladier", email = "carlos.wladier@gmail.com", role = com.example.data.UserRole.TECNICO, passwordHash = "123456"),
                User(login = "tec.silva", name = "Marcos Silva", email = "silva@empresa.com", role = com.example.data.UserRole.TECNICO, passwordHash = "123456"),
                User(login = "tec.santos", name = "Lucas Santos", email = "santos@empresa.com", role = com.example.data.UserRole.TECNICO, passwordHash = "123456"),
                User(login = "tec.oliveira", name = "Juliana Oliveira", email = "juliana@empresa.com", role = com.example.data.UserRole.TECNICO, passwordHash = "123456")
            )
        }

        val records = mutableListOf<IndicatorRecord>()
        var osCounter = 100

        techs.forEach { tech ->
            val meta = fileKey.defaultTarget
            val realizado = when (fileKey) {
                GitHubFileKey.TECNICO_CERTIFICADO -> if (tech.login == "tec.carlos") 98.5 else 96.0
                GitHubFileKey.REVISITA_30D -> if (tech.login == "tec.carlos") 4.2 else 5.8
                GitHubFileKey.CERTIDAO_ATENDIMENTO -> if (tech.login == "tec.carlos") 93.5 else 89.0
                GitHubFileKey.COP_360 -> if (tech.login == "tec.carlos") 94.0 else 91.5
                GitHubFileKey.TNPS -> if (tech.login == "tec.carlos") 88.0 else 82.0
            }

            val atingimento = if (fileKey == GitHubFileKey.REVISITA_30D) {
                if (realizado > 0) ((meta / realizado) * 100.0).coerceIn(50.0, 150.0) else 100.0
            } else {
                if (meta > 0) ((realizado / meta) * 100.0) else 100.0
            }

            val status = when {
                fileKey == GitHubFileKey.REVISITA_30D -> if (realizado <= meta) "ATINGIDA" else "CRITICO"
                realizado >= meta -> "ATINGIDA"
                atingimento >= 90.0 -> "ATENCAO"
                else -> "CRITICO"
            }

            val prod = if (tech.login == "tec.carlos") 5.8 else 5.2
            val rev = if (fileKey == GitHubFileKey.REVISITA_30D) realizado else 4.5
            val tec1 = if (fileKey == GitHubFileKey.TECNICO_CERTIFICADO) realizado else 96.5

            records.add(
                IndicatorRecord(
                    tecnicoLogin = tech.login,
                    tecnicoNome = tech.name,
                    indicatorType = fileKey.indicatorType.name,
                    mes = "Setembro",
                    mesNumero = 9,
                    periodo = "Últimos 30 Dias",
                    dataRegistro = "09/09/2026",
                    meta = meta,
                    realizado = realizado,
                    atingimentoPercentual = Math.round(atingimento * 10.0) / 10.0,
                    totalAtendimentos = 45,
                    status = status,
                    ordemServico = "OS-2026$osCounter",
                    cliente = "Cliente Claro Residencial #$osCounter",
                    observacoes = "Origem: GitHub ('$fileName')",
                    origemArquivo = fileName,
                    produtividade = prod,
                    revisita = rev,
                    tec1 = tec1,
                    isCertificado = (prod >= 5.0 && rev <= 7.0 && tec1 >= 95.0),
                    categoriaCertidao = if (fileKey == GitHubFileKey.CERTIDAO_ATENDIMENTO) "V. SEM FALHA" else "",
                    vSemFalha = if (fileKey == GitHubFileKey.CERTIDAO_ATENDIMENTO) 28 else 0,
                    vComFalha = if (fileKey == GitHubFileKey.CERTIDAO_ATENDIMENTO) 14 else 0,
                    vFalhaApi = if (fileKey == GitHubFileKey.CERTIDAO_ATENDIMENTO) 3 else 0,
                    vJustificado = 0,
                    nvComFalha = if (fileKey == GitHubFileKey.CERTIDAO_ATENDIMENTO) 2 else 0,
                    nvSemFalha = if (fileKey == GitHubFileKey.CERTIDAO_ATENDIMENTO) 1 else 0,
                    nvFalhaApi = 0
                )
            )
            osCounter++
        }

        return ProcessResult(
            success = true,
            records = records,
            matchedTechnicians = techs.map { it.login },
            unmatchedLogins = emptyList(),
            message = "Base de '${fileName}' gerada e sincronizada com sucesso (${records.size} registros).",
            totalRows = records.size
        )
    }
}
