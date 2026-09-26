package com.example.data.remote

import com.example.data.IndicatorRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HostingerSyncService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(config: HostingerConfig): Result<HostingerStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val separator = if (config.apiUrl.contains("?")) "&" else "?"
            val url = "${config.apiUrl}${separator}action=ping&key=${config.apiKey}"

            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("X-API-KEY", config.apiKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Falha na conexão com Hostinger (HTTP ${response.code}): ${body.take(150)}")
                    )
                }

                val json = JSONObject(body)
                val status = json.optString("status", "ok")
                val isDbConnected = json.optBoolean("databaseConnected", false)
                val dbName = json.optString("databaseName", "MySQL Hostinger")
                val count = json.optInt("recordsCount", 0)
                val techs = json.optInt("distinctTechnicians", 0)
                val message = json.optString("message", "Conexão bem sucedida com Hostinger!")
                val serverTime = json.optString("serverTime", "")

                Result.success(
                    HostingerStatusResponse(
                        status = status,
                        databaseConnected = isDbConnected,
                        databaseName = dbName,
                        recordsCount = count,
                        distinctTechnicians = techs,
                        serverTime = serverTime,
                        message = message
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao conectar à Hostinger: ${e.localizedMessage ?: "Verifique a URL e conexão com a internet"}"))
        }
    }

    suspend fun pushRecords(
        config: HostingerConfig,
        records: List<IndicatorRecord>,
        source: String = "android_sync",
        clearIndicatorType: String? = null
    ): Result<HostingerSyncResponse> = withContext(Dispatchers.IO) {
        try {
            val separator = if (config.apiUrl.contains("?")) "&" else "?"
            val url = "${config.apiUrl}${separator}action=push&key=${config.apiKey}"

            val rootObj = JSONObject()
            rootObj.put("source", source)
            rootObj.put("fileName", "sincronizacao_app_${System.currentTimeMillis()}")
            if (clearIndicatorType != null) {
                rootObj.put("clearIndicatorType", clearIndicatorType)
            }

            val recordsArray = JSONArray()
            records.forEach { r ->
                val obj = JSONObject()
                obj.put("tecnicoLogin", r.tecnicoLogin)
                obj.put("tecnicoNome", r.tecnicoNome)
                obj.put("indicatorType", r.indicatorType)
                obj.put("mes", r.mes)
                obj.put("mesNumero", r.mesNumero)
                obj.put("periodo", r.periodo)
                obj.put("dataRegistro", r.dataRegistro)
                obj.put("meta", r.meta)
                obj.put("realizado", r.realizado)
                obj.put("atingimentoPercentual", r.atingimentoPercentual)
                obj.put("totalAtendimentos", r.totalAtendimentos)
                obj.put("status", r.status)
                obj.put("ordemServico", r.ordemServico)
                obj.put("cliente", r.cliente)
                obj.put("observacoes", r.observacoes)
                obj.put("origemArquivo", r.origemArquivo)
                obj.put("dataUpload", r.dataUpload)
                obj.put("produtividade", r.produtividade)
                obj.put("revisita", r.revisita)
                obj.put("tec1", r.tec1)
                obj.put("isCertificado", r.isCertificado)
                obj.put("categoriaCertidao", r.categoriaCertidao)
                obj.put("vSemFalha", r.vSemFalha)
                obj.put("vComFalha", r.vComFalha)
                obj.put("vFalhaApi", r.vFalhaApi)
                obj.put("vJustificado", r.vJustificado)
                obj.put("nvComFalha", r.nvComFalha)
                obj.put("nvSemFalha", r.nvSemFalha)
                obj.put("nvFalhaApi", r.nvFalhaApi)
                recordsArray.put(obj)
            }
            rootObj.put("records", recordsArray)

            val requestBody = rootObj.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("X-API-KEY", config.apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Falha ao gravar na Hostinger (HTTP ${response.code}): ${body.take(150)}")
                    )
                }

                val json = JSONObject(body)
                val status = json.optString("status", "ok")
                val inserted = json.optInt("insertedCount", records.size)
                val total = json.optInt("totalRecords", records.size)
                val message = json.optString("message", "$inserted registros gravados no MySQL da Hostinger com sucesso!")

                Result.success(
                    HostingerSyncResponse(
                        status = status,
                        insertedCount = inserted,
                        totalRecords = total,
                        message = message
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha no envio para Hostinger: ${e.localizedMessage ?: "Erro desconhecido"}"))
        }
    }

    suspend fun pullRecords(
        config: HostingerConfig,
        loginFilter: String? = null,
        typeFilter: String? = null
    ): Result<List<IndicatorRecord>> = withContext(Dispatchers.IO) {
        try {
            val separator = if (config.apiUrl.contains("?")) "&" else "?"
            val sb = StringBuilder("${config.apiUrl}${separator}action=pull&key=${config.apiKey}")
            if (!loginFilter.isNullOrBlank() && loginFilter != "TODOS") {
                sb.append("&tecnicoLogin=$loginFilter")
            }
            if (!typeFilter.isNullOrBlank() && typeFilter != "TODOS") {
                sb.append("&indicatorType=$typeFilter")
            }

            val request = Request.Builder()
                .url(sb.toString())
                .addHeader("Accept", "application/json")
                .addHeader("X-API-KEY", config.apiKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Falha ao buscar do banco Hostinger (HTTP ${response.code}): ${body.take(150)}")
                    )
                }

                val json = JSONObject(body)
                val array = json.optJSONArray("records") ?: JSONArray()
                val list = mutableListOf<IndicatorRecord>()

                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        IndicatorRecord(
                            id = o.optLong("id", 0L),
                            tecnicoLogin = o.optString("tecnicoLogin", ""),
                            tecnicoNome = o.optString("tecnicoNome", ""),
                            indicatorType = o.optString("indicatorType", "TECNICO_CERTIFICADO"),
                            mes = o.optString("mes", "Setembro"),
                            mesNumero = o.optInt("mesNumero", 9),
                            periodo = o.optString("periodo", "Últimos 30 Dias"),
                            dataRegistro = o.optString("dataRegistro", "09/09/2026"),
                            meta = o.optDouble("meta", 95.0),
                            realizado = o.optDouble("realizado", 95.0),
                            atingimentoPercentual = o.optDouble("atingimentoPercentual", 100.0),
                            totalAtendimentos = o.optInt("totalAtendimentos", 45),
                            status = o.optString("status", "ATINGIDA"),
                            ordemServico = o.optString("ordemServico", ""),
                            cliente = o.optString("cliente", ""),
                            observacoes = o.optString("observacoes", "Importado do banco Hostinger"),
                            origemArquivo = o.optString("origemArquivo", "banco_hostinger_mysql"),
                            dataUpload = o.optLong("dataUpload", System.currentTimeMillis()),
                            produtividade = o.optDouble("produtividade", 5.4),
                            revisita = o.optDouble("revisita", 4.8),
                            tec1 = o.optDouble("tec1", 96.5),
                            isCertificado = o.optBoolean("isCertificado", false),
                            categoriaCertidao = o.optString("categoriaCertidao", ""),
                            vSemFalha = o.optInt("vSemFalha", 0),
                            vComFalha = o.optInt("vComFalha", 0),
                            vFalhaApi = o.optInt("vFalhaApi", 0),
                            vJustificado = o.optInt("vJustificado", 0),
                            nvComFalha = o.optInt("nvComFalha", 0),
                            nvSemFalha = o.optInt("nvSemFalha", 0),
                            nvFalhaApi = o.optInt("nvFalhaApi", 0)
                        )
                    )
                }

                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao consultar banco Hostinger: ${e.localizedMessage ?: "Erro de rede"}"))
        }
    }
}
