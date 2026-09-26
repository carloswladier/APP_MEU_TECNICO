package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.IndicatorRecord
import com.example.data.IndicatorType
import com.example.data.User
import com.example.data.ValidacaoCertidao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.zip.ZipInputStream

data class ProcessResult(
    val success: Boolean,
    val records: List<IndicatorRecord>,
    val matchedTechnicians: List<String>,
    val unmatchedLogins: List<String>,
    val message: String,
    val totalRows: Int
)

object ExcelProcessor {

    suspend fun processFile(
        context: Context,
        uri: Uri,
        fileName: String,
        registeredUsers: List<User>,
        indicatorHint: IndicatorType? = null
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext ProcessResult(
                    success = false,
                    records = emptyList(),
                    matchedTechnicians = emptyList(),
                    unmatchedLogins = emptyList(),
                    message = "Não foi possível abrir o arquivo selecionado.",
                    totalRows = 0
                )
            processInputStream(inputStream, fileName, registeredUsers, indicatorHint)
        } catch (e: Exception) {
            ProcessResult(
                success = false,
                records = emptyList(),
                matchedTechnicians = emptyList(),
                unmatchedLogins = emptyList(),
                message = "Erro ao processar planilha: ${e.localizedMessage ?: "Formato inválido"}",
                totalRows = 0
            )
        }
    }

    suspend fun processInputStream(
        inputStream: InputStream,
        fileName: String,
        registeredUsers: List<User>,
        indicatorHint: IndicatorType? = null
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            val lowerName = fileName.lowercase()
            val rawRows: List<List<String>> = if (lowerName.endsWith(".xlsx")) {
                parseXlsx(inputStream)
            } else {
                parseCsvOrTsv(inputStream)
            }

            if (rawRows.isEmpty()) {
                return@withContext ProcessResult(
                    success = false,
                    records = emptyList(),
                    matchedTechnicians = emptyList(),
                    unmatchedLogins = emptyList(),
                    message = "A planilha está vazia ou o formato não foi reconhecido.",
                    totalRows = 0
                )
            }

            mapRowsToRecords(rawRows, fileName, registeredUsers, indicatorHint)
        } catch (e: Exception) {
            ProcessResult(
                success = false,
                records = emptyList(),
                matchedTechnicians = emptyList(),
                unmatchedLogins = emptyList(),
                message = "Erro ao processar planilha: ${e.localizedMessage ?: "Formato inválido"}",
                totalRows = 0
            )
        }
    }

    private fun parseCsvOrTsv(inputStream: InputStream): List<List<String>> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rows = mutableListOf<List<String>>()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isEmpty()) continue

            val separator = when {
                trimmed.contains(";") -> ";"
                trimmed.contains("\t") -> "\t"
                else -> ","
            }

            val cells = trimmed.split(separator).map { it.trim().removeSurrounding("\"") }
            rows.add(cells)
        }
        reader.close()
        return rows
    }

    private fun parseXlsx(inputStream: InputStream): List<List<String>> {
        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        val sharedStrings = mutableListOf<String>()
        var sheetXmlContent: String? = null

        while (entry != null) {
            if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                sharedStrings.addAll(parseSharedStrings(zip))
            } else if (entry.name.equals("xl/worksheets/sheet1.xml", ignoreCase = true)) {
                sheetXmlContent = zip.bufferedReader(Charsets.UTF_8).readText()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()

        if (sheetXmlContent == null) return emptyList()

        return parseSheetXml(sheetXmlContent, sharedStrings)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val strings = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(stream, "UTF-8")

            var eventType = parser.eventType
            var inTextTag = false
            val currentText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inTextTag = true
                            currentText.clear()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inTextTag) {
                            currentText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inTextTag = false
                            strings.add(currentText.toString())
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return strings
    }

    private fun parseSheetXml(xmlContent: String, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            val currentRow = mutableListOf<String>()
            var currentCell = ""
            var isStringCell = false
            var inValueTag = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "row" -> currentRow.clear()
                            "c" -> {
                                val typeAttr = parser.getAttributeValue(null, "t")
                                isStringCell = (typeAttr == "s")
                                currentCell = ""
                            }
                            "v" -> inValueTag = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inValueTag) {
                            currentCell = parser.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "v" -> inValueTag = false
                            "c" -> {
                                val value = if (isStringCell) {
                                    val index = currentCell.toIntOrNull()
                                    if (index != null && index in sharedStrings.indices) {
                                        sharedStrings[index]
                                    } else {
                                        currentCell
                                    }
                                } else {
                                    currentCell
                                }
                                currentRow.add(value)
                            }
                            "row" -> {
                                if (currentRow.isNotEmpty()) {
                                    rows.add(currentRow.toList())
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return rows
    }

    fun mapRowsToRecords(
        rows: List<List<String>>,
        fileName: String,
        registeredUsers: List<User>,
        indicatorHint: IndicatorType? = null
    ): ProcessResult {
        if (rows.isEmpty()) {
            return ProcessResult(false, emptyList(), emptyList(), emptyList(), "Sem dados", 0)
        }

        val header = rows[0].map { it.trim().uppercase() }
        
        val fileHint = indicatorHint ?: when {
            fileName.contains("certificao", ignoreCase = true) || fileName.contains("certificado", ignoreCase = true) -> IndicatorType.TECNICO_CERTIFICADO
            fileName.contains("revisita", ignoreCase = true) || fileName.contains("30d", ignoreCase = true) -> IndicatorType.REVISITA_30D
            fileName.contains("certidao", ignoreCase = true) || fileName.contains("atendimento", ignoreCase = true) -> IndicatorType.CERTIDAO_ATENDIMENTO
            fileName.contains("cop", ignoreCase = true) || fileName.contains("360", ignoreCase = true) -> IndicatorType.COP_360
            fileName.contains("tnps", ignoreCase = true) || fileName.contains("nps", ignoreCase = true) -> IndicatorType.TNPS
            else -> null
        }

        // Localizar índices das colunas de forma flexível
        val colLogin = header.indexOfFirst { it.contains("LOGIN") || it.contains("TECNICO") || it.contains("USUARIO") }
            .takeIf { it >= 0 } ?: 0
        val colNome = header.indexOfFirst { it.contains("NOME") }
            .takeIf { it >= 0 } ?: 1
        val colIndicador = header.indexOfFirst { it.contains("INDICADOR") || it.contains("TIPO") }
            .takeIf { it >= 0 }
        val colMes = header.indexOfFirst { it.contains("MES") || it.contains("MÊS") }
            .takeIf { it >= 0 }
        val colPeriodo = header.indexOfFirst { it.contains("PERIODO") || it.contains("PERÍODO") }
            .takeIf { it >= 0 }
        val colData = header.indexOfFirst { it.contains("DATA") }
            .takeIf { it >= 0 }
        val colMeta = header.indexOfFirst { it.contains("META") }
            .takeIf { it >= 0 }
        val colRealizado = header.indexOfFirst { it.contains("REALIZADO") || it.contains("VALOR") || it.contains("RESULTADO") }
            .takeIf { it >= 0 }
        val colAtendimentos = header.indexOfFirst { it.contains("ATEND") || it.contains("TOTAL") || it.contains("AMOSTRA") }
            .takeIf { it >= 0 }
        val colStatus = header.indexOfFirst { it.contains("STATUS") }
            .takeIf { it >= 0 }
        val colOS = header.indexOfFirst { it.contains("OS") || it.contains("ORDEM") }
            .takeIf { it >= 0 }
        val colProdutividade = header.indexOfFirst { it.contains("PRODUTIV") || it.contains("PROD") }
            .takeIf { it >= 0 }
        val colRevisita = header.indexOfFirst { it.contains("REVISITA") || it.contains("REV") }
            .takeIf { it >= 0 }
        val colTec1 = header.indexOfFirst { it.contains("TEC1") || it.contains("TEC 1") || it.contains("TEC_1") }
            .takeIf { it >= 0 }
        val colCategoria = header.indexOfFirst { it.contains("VALIDA") || it.contains("CATEGORIA") }
            .takeIf { it >= 0 }
        val colVSemFalha = header.indexOfFirst { it.contains("V_SEM") || (it.contains("SEM FALHA") && !it.contains("NV") && !it.contains("NÃO")) }
            .takeIf { it >= 0 }
        val colVComFalha = header.indexOfFirst { it.contains("V_COM") || (it.contains("COM FALHA") && !it.contains("NV") && !it.contains("NÃO")) }
            .takeIf { it >= 0 }
        val colVFalhaApi = header.indexOfFirst { (it.contains("API") && !it.contains("NV") && !it.contains("NÃO")) }
            .takeIf { it >= 0 }
        val colVJustificado = header.indexOfFirst { it.contains("JUSTIFICAD") }
            .takeIf { it >= 0 }
        val colNvComFalha = header.indexOfFirst { (it.contains("COM FALHA") && (it.contains("NV") || it.contains("NÃO") || it.contains("NAO"))) }
            .takeIf { it >= 0 }
        val colNvSemFalha = header.indexOfFirst { (it.contains("SEM FALHA") && (it.contains("NV") || it.contains("NÃO") || it.contains("NAO"))) }
            .takeIf { it >= 0 }
        val colNvFalhaApi = header.indexOfFirst { (it.contains("API") && (it.contains("NV") || it.contains("NÃO") || it.contains("NAO"))) }
            .takeIf { it >= 0 }

        val systemLogins = registeredUsers.map { it.login.lowercase().trim() }.toSet()
        val records = mutableListOf<IndicatorRecord>()
        val matchedTechnicians = mutableSetOf<String>()
        val unmatchedLogins = mutableSetOf<String>()

        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.isEmpty() || row.all { it.isBlank() }) continue

            fun getCell(idx: Int?, default: String = "") = if (idx != null && idx in row.indices) row[idx].trim() else default

            val rawLogin = getCell(colLogin)
            if (rawLogin.isBlank()) continue

            val cleanLogin = rawLogin.lowercase().trim()
            val rawNome = getCell(colNome, "Técnico $cleanLogin")
            
            val indicatorType = if (colIndicador != null) {
                val rawVal = getCell(colIndicador)
                if (rawVal.isNotBlank()) IndicatorType.fromString(rawVal) else (fileHint ?: IndicatorType.TECNICO_CERTIFICADO)
            } else {
                fileHint ?: IndicatorType.TECNICO_CERTIFICADO
            }

            val mes = getCell(colMes, "Setembro")
            val periodo = getCell(colPeriodo, "Últimos 30 Dias")
            val data = getCell(colData, "09/09/2026")
            val meta = getCell(colMeta, "${indicatorType.defaultTarget}").replace(",", ".").toDoubleOrNull() ?: indicatorType.defaultTarget
            val realizado = getCell(colRealizado, "$meta").replace(",", ".").toDoubleOrNull() ?: meta
            val atendimentos = getCell(colAtendimentos, "45").toIntOrNull() ?: 45
            val os = getCell(colOS, "OS-2026${1000 + i}")
            val cliente = "Cliente Corporativo #${100 + i}"

            // Comparar login da planilha x logins do sistema
            if (systemLogins.contains(cleanLogin)) {
                matchedTechnicians.add(cleanLogin)
            } else {
                unmatchedLogins.add(cleanLogin)
            }

            val atingimentoPercentual = if (indicatorType == IndicatorType.REVISITA_30D) {
                if (realizado > 0) ((meta / realizado) * 100.0).coerceIn(50.0, 200.0) else 100.0
            } else {
                if (meta > 0) ((realizado / meta) * 100.0) else 100.0
            }

            val status = when {
                indicatorType == IndicatorType.REVISITA_30D -> if (realizado <= meta) "ATINGIDA" else "CRITICO"
                realizado >= meta -> "ATINGIDA"
                atingimentoPercentual >= 90.0 -> "ATENCAO"
                else -> "CRITICO"
            }

            val mesNumero = when (mes.lowercase()) {
                "janeiro" -> 1
                "fevereiro" -> 2
                "março", "marco" -> 3
                "abril" -> 4
                "maio" -> 5
                "junho" -> 6
                "julho" -> 7
                "agosto" -> 8
                "setembro" -> 9
                "outubro" -> 10
                "novembro" -> 11
                "dezembro" -> 12
                else -> 9
            }

            val prodVal = if (colProdutividade != null) {
                getCell(colProdutividade, "5.4").replace(",", ".").toDoubleOrNull() ?: 5.4
            } else 5.4

            val revVal = if (colRevisita != null) {
                getCell(colRevisita, "4.8").replace(",", ".").toDoubleOrNull() ?: 4.8
            } else if (indicatorType == IndicatorType.REVISITA_30D) {
                realizado
            } else 4.8

            val tec1Val = if (colTec1 != null) {
                getCell(colTec1, "96.5").replace(",", ".").toDoubleOrNull() ?: 96.5
            } else if (indicatorType == IndicatorType.TECNICO_CERTIFICADO) {
                realizado
            } else 96.5

            val isCert = (prodVal >= 5.0 && revVal <= 7.0 && tec1Val >= 95.0)

            val rawCategoria = if (colCategoria != null) getCell(colCategoria) else ""
            val valCategoria = if (rawCategoria.isNotBlank()) ValidacaoCertidao.fromString(rawCategoria).label else ""

            val vSem = if (colVSemFalha != null) getCell(colVSemFalha, "0").toIntOrNull() ?: 0 else if (valCategoria == "V. SEM FALHA") 1 else 0
            val vCom = if (colVComFalha != null) getCell(colVComFalha, "0").toIntOrNull() ?: 0 else if (valCategoria == "V. COM FALHA") 1 else 0
            val vApi = if (colVFalhaApi != null) getCell(colVFalhaApi, "0").toIntOrNull() ?: 0 else if (valCategoria == "V. FALHA API") 1 else 0
            val vJust = if (colVJustificado != null) getCell(colVJustificado, "0").toIntOrNull() ?: 0 else if (valCategoria == "V. JUSTIFICADO") 1 else 0
            val nvCom = if (colNvComFalha != null) getCell(colNvComFalha, "0").toIntOrNull() ?: 0 else if (valCategoria == "NV. COM FALHA") 1 else 0
            val nvSem = if (colNvSemFalha != null) getCell(colNvSemFalha, "0").toIntOrNull() ?: 0 else if (valCategoria == "NV. SEM FALHA") 1 else 0
            val nvApi = if (colNvFalhaApi != null) getCell(colNvFalhaApi, "0").toIntOrNull() ?: 0 else if (valCategoria == "NV. FALHA API") 1 else 0

            records.add(
                IndicatorRecord(
                    tecnicoLogin = cleanLogin,
                    tecnicoNome = rawNome,
                    indicatorType = indicatorType.name,
                    mes = mes,
                    mesNumero = mesNumero,
                    periodo = periodo,
                    dataRegistro = data,
                    meta = meta,
                    realizado = realizado,
                    atingimentoPercentual = Math.round(atingimentoPercentual * 10.0) / 10.0,
                    totalAtendimentos = atendimentos,
                    status = status,
                    ordemServico = os,
                    cliente = cliente,
                    observacoes = "Processado via upload do arquivo '$fileName'",
                    origemArquivo = fileName,
                    produtividade = prodVal,
                    revisita = revVal,
                    tec1 = tec1Val,
                    isCertificado = isCert,
                    categoriaCertidao = valCategoria,
                    vSemFalha = vSem,
                    vComFalha = vCom,
                    vFalhaApi = vApi,
                    vJustificado = vJust,
                    nvComFalha = nvCom,
                    nvSemFalha = nvSem,
                    nvFalhaApi = nvApi
                )
            )
        }

        val message = StringBuilder()
        message.append("Processamento concluído com sucesso: ${records.size} indicadores extraídos em tempo real.\n")
        message.append("• Técnicos vinculados com o sistema (${matchedTechnicians.size}): ${matchedTechnicians.joinToString(", ")}\n")
        if (unmatchedLogins.isNotEmpty()) {
            message.append("⚠️ Atenção: ${unmatchedLogins.size} login(s) na planilha não encontrados no cadastro de usuários: ${unmatchedLogins.joinToString(", ")}. O ADM pode cadastrá-los na aba Usuários.")
        }

        return ProcessResult(
            success = true,
            records = records,
            matchedTechnicians = matchedTechnicians.toList(),
            unmatchedLogins = unmatchedLogins.toList(),
            message = message.toString(),
            totalRows = records.size
        )
    }

    fun generateSampleCsvTemplate(): String {
        val header = "LOGIN_TECNICO,NOME_TECNICO,TIPO_INDICADOR,MES,PERIODO,DATA_REGISTRO,META,REALIZADO,TOTAL_ATENDIMENTOS,ORDEM_SERVICO,STATUS\n"
        val sb = StringBuilder(header)
        val technicians = listOf(
            Pair("tec.carlos", "Carlos Wladier"),
            Pair("tec.silva", "Marcos Silva"),
            Pair("tec.santos", "Lucas Santos"),
            Pair("tec.oliveira", "Juliana Oliveira")
        )
        val indicators = listOf(
            "Técnico Certificado",
            "Certidão de Atendimento",
            "Revisita 30D",
            "Cop 360",
            "TNPS"
        )
        var count = 1
        for (tech in technicians) {
            for (ind in indicators) {
                val (meta, real) = when (ind) {
                    "Técnico Certificado" -> Pair("95.0", "98.5")
                    "Certidão de Atendimento" -> Pair("98.0", "99.2")
                    "Revisita 30D" -> Pair("5.0", "3.2")
                    "Cop 360" -> Pair("90.0", "94.5")
                    "TNPS" -> Pair("80.0", "88.0")
                    else -> Pair("90.0", "90.0")
                }
                sb.append("${tech.first},${tech.second},$ind,Setembro,Últimos 30 Dias,09/09/2026,$meta,$real,48,OS-202600$count,ATINGIDA\n")
                count++
            }
        }
        return sb.toString()
    }
}
