package com.example.data

object SampleData {
    fun getInitialUsers(): List<User> = listOf(
        User(
            id = 1,
            login = "admin",
            name = "Gestão Operacional (ADM)",
            email = "adm.indicadores@empresa.com.br",
            role = UserRole.ADMIN,
            passwordHash = "C.1985.w"
        ),
        User(
            id = 2,
            login = "tec.carlos",
            name = "Carlos Wladier",
            email = "carlos.wladier@gmail.com",
            role = UserRole.TECNICO,
            passwordHash = "123456"
        ),
        User(
            id = 3,
            login = "tec.silva",
            name = "Marcos Silva",
            email = "marcos.silva@empresa.com.br",
            role = UserRole.TECNICO,
            passwordHash = "123456"
        ),
        User(
            id = 4,
            login = "tec.santos",
            name = "Lucas Santos",
            email = "lucas.santos@empresa.com.br",
            role = UserRole.TECNICO,
            passwordHash = "123456"
        ),
        User(
            id = 5,
            login = "tec.oliveira",
            name = "Juliana Oliveira",
            email = "juliana.oliveira@empresa.com.br",
            role = UserRole.TECNICO,
            passwordHash = "123456"
        )
    )

    fun getInitialRecords(): List<IndicatorRecord> {
        val records = mutableListOf<IndicatorRecord>()
        val technicians = listOf(
            Pair("tec.carlos", "Carlos Wladier"),
            Pair("tec.silva", "Marcos Silva"),
            Pair("tec.santos", "Lucas Santos"),
            Pair("tec.oliveira", "Juliana Oliveira")
        )

        val months = listOf(
            Triple("Janeiro", 1, "1º Semestre"),
            Triple("Fevereiro", 2, "1º Semestre"),
            Triple("Março", 3, "1º Semestre"),
            Triple("Abril", 4, "1º Semestre"),
            Triple("Maio", 5, "1º Semestre"),
            Triple("Junho", 6, "1º Semestre"),
            Triple("Julho", 7, "2º Semestre"),
            Triple("Agosto", 8, "2º Semestre"),
            Triple("Setembro", 9, "Últimos 30 Dias")
        )

        val types = IndicatorType.values()

        var idCounter = 1L
        for (tech in technicians) {
            for (month in months) {
                for (type in types) {
                    var prodVal = 0.0
                    var revVal = 0.0
                    var tec1Val = 0.0
                    var isCert = false

                    val (meta, realizado, status) = when (type) {
                        IndicatorType.TECNICO_CERTIFICADO -> {
                            val target = 95.0
                            prodVal = when (tech.first) {
                                "tec.carlos" -> 5.6
                                "tec.silva" -> 5.3
                                "tec.santos" -> 4.7
                                else -> 5.4
                            }
                            revVal = when (tech.first) {
                                "tec.carlos" -> 4.2
                                "tec.silva" -> 5.8
                                "tec.santos" -> 7.8
                                else -> 5.9
                            }
                            tec1Val = when (tech.first) {
                                "tec.carlos" -> 97.8
                                "tec.silva" -> 96.2
                                "tec.santos" -> 93.4
                                else -> 96.5
                            }
                            isCert = (prodVal >= 5.0 && revVal <= 7.0 && tec1Val >= 95.0)
                            val st = if (isCert) "ATINGIDA" else "ATENCAO"
                            Triple(target, tec1Val, st)
                        }
                        IndicatorType.CERTIDAO_ATENDIMENTO -> {
                            val target = 85.0
                            val actual = if (tech.first == "tec.carlos") 93.5 else 86.0 + (idCounter % 8)
                            val st = if (actual >= target) "ATINGIDA" else "ATENCAO"
                            Triple(target, actual, st)
                        }
                        IndicatorType.REVISITA_30D -> {
                            // Para revisita Claro, quanto menor melhor (meta máxima 7.0%)
                            val target = 7.0
                            val actual = if (tech.first == "tec.carlos") 4.2 else 5.2 + (idCounter % 3) * 0.9
                            val st = if (actual <= target) "ATINGIDA" else "CRITICO"
                            Triple(target, actual, st)
                        }
                        IndicatorType.COP_360 -> {
                            val target = 90.0
                            val actual = if (tech.first == "tec.carlos") 94.0 else 88.0 + (idCounter % 8)
                            val st = if (actual >= target) "ATINGIDA" else "ATENCAO"
                            Triple(target, actual, st)
                        }
                        IndicatorType.TNPS -> {
                            val target = 80.0
                            val actual = if (tech.first == "tec.carlos") 89.0 else 76.0 + (idCounter % 15)
                            val st = if (actual >= target) "ATINGIDA" else if (actual >= 70.0) "ATENCAO" else "CRITICO"
                            Triple(target, actual, st)
                        }
                    }

                    val pctAtingimento = if (type == IndicatorType.REVISITA_30D) {
                        // cálculo inverso: se realizou 4.2% em meta de 7.0%, bateu meta com folga
                        ((meta / realizado) * 100.0).coerceIn(50.0, 200.0)
                    } else {
                        ((realizado / meta) * 100.0)
                    }

                    val osNumber = "OS-${20260000 + (idCounter % 9000)}"
                    val day = (1 + (idCounter % 27)).toInt().toString().padStart(2, '0')
                    val monthNumStr = month.second.toString().padStart(2, '0')

                    records.add(
                        IndicatorRecord(
                            id = idCounter++,
                            tecnicoLogin = tech.first,
                            tecnicoNome = tech.second,
                            indicatorType = type.name,
                            mes = month.first,
                            mesNumero = month.second,
                            periodo = month.third,
                            dataRegistro = "$day/$monthNumStr/2026",
                            meta = meta,
                            realizado = Math.round(realizado * 10.0) / 10.0,
                            atingimentoPercentual = Math.round(pctAtingimento * 10.0) / 10.0,
                            totalAtendimentos = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) 155 else 40 + (idCounter % 30).toInt(),
                            status = status,
                            ordemServico = osNumber,
                            cliente = "Cliente Claro Residencial #${100 + (idCounter % 80)}",
                            observacoes = "Processamento automático da planilha oficial de performance Claro.",
                            origemArquivo = "planilha_indicadores_oficial_2026.xlsx",
                            dataUpload = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * (month.second)),
                            produtividade = prodVal,
                            revisita = revVal,
                            tec1 = tec1Val,
                            isCertificado = isCert,
                            categoriaCertidao = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) "V. SEM FALHA" else "",
                            vSemFalha = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) 90 else 0,
                            vComFalha = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) 46 else 0,
                            vFalhaApi = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) 9 else 0,
                            vJustificado = 0,
                            nvComFalha = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) 6 else 0,
                            nvSemFalha = if (type == IndicatorType.CERTIDAO_ATENDIMENTO) 4 else 0,
                            nvFalhaApi = 0
                        )
                    )
                }
            }
        }
        return records
    }

    fun getInitialNotifications(): List<AppNotification> = listOf(
        AppNotification(
            id = 1,
            title = "📢 Planilha de Indicadores 2026 Atualizada",
            message = "O administrador disponibilizou uma nova base de indicadores. Dados processados em tempo real.",
            timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 2),
            isRead = false,
            fileName = "planilha_indicadores_oficial_2026.xlsx"
        ),
        AppNotification(
            id = 2,
            title = "✅ Sincronização Concluída",
            message = "Todos os dados estão disponíveis offline para consulta rápida em campo.",
            timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 24),
            isRead = true,
            fileName = "sync_offline_cache"
        )
    )
}
