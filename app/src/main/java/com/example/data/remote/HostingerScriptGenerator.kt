package com.example.data.remote

object HostingerScriptGenerator {

    fun getPhpScript(config: HostingerConfig): String {
        val s = "$"
        val key = config.apiKey.ifEmpty { "claro_indicadores_sec_2026" }

        return """<?php
/**
 * CLARO • API DE SINCRONIZAÇÃO DE INDICADORES (HOSTINGER)
 * Salve este arquivo em: public_html/api/indicadores_api.php
 */
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-API-KEY");
header("Content-Type: application/json; charset=UTF-8");

if (${s}_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit(); }

// CREDENCIAIS DO BANCO MYSQL CRIADO NA HOSTINGER:
define('DB_HOST', 'localhost');
define('DB_NAME', 'u123456789_indicadores');
define('DB_USER', 'u123456789_admin');
define('DB_PASS', 'SuaSenhaMySQL123');
define('API_SECRET_KEY', '$key');

try {
    ${s}pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
        DB_USER, DB_PASS,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
    );

    ${s}pdo->exec("
        CREATE TABLE IF NOT EXISTS `indicator_records` (
            `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
            `tecnicoLogin` VARCHAR(100) NOT NULL,
            `tecnicoNome` VARCHAR(255) NOT NULL,
            `indicatorType` VARCHAR(100) NOT NULL,
            `mes` VARCHAR(50) NOT NULL,
            `mesNumero` INT NOT NULL,
            `periodo` VARCHAR(100) NOT NULL,
            `dataRegistro` VARCHAR(50) NOT NULL,
            `meta` DOUBLE NOT NULL,
            `realizado` DOUBLE NOT NULL,
            `atingimentoPercentual` DOUBLE NOT NULL,
            `totalAtendimentos` INT NOT NULL,
            `status` VARCHAR(50) NOT NULL,
            `ordemServico` VARCHAR(100) NOT NULL,
            `cliente` VARCHAR(255) NOT NULL,
            `observacoes` TEXT,
            `origemArquivo` VARCHAR(255) NOT NULL,
            `dataUpload` BIGINT NOT NULL,
            `produtividade` DOUBLE DEFAULT 0,
            `revisita` DOUBLE DEFAULT 0,
            `tec1` DOUBLE DEFAULT 0,
            `isCertificado` TINYINT(1) DEFAULT 0,
            `categoriaCertidao` VARCHAR(100) DEFAULT '',
            `vSemFalha` INT DEFAULT 0,
            `vComFalha` INT DEFAULT 0,
            `vFalhaApi` INT DEFAULT 0,
            `vJustificado` INT DEFAULT 0,
            `nvComFalha` INT DEFAULT 0,
            `nvSemFalha` INT DEFAULT 0,
            `nvFalhaApi` INT DEFAULT 0,
            `saldoProducao` DOUBLE DEFAULT 0,
            `saldoRevisita` DOUBLE DEFAULT 0,
            `ganhoRevisita` VARCHAR(100) DEFAULT '',
            `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_login (`tecnicoLogin`),
            INDEX idx_tipo (`indicatorType`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    ");
} catch (PDOException ${s}e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'databaseConnected' => false, 'message' => ${s}e->getMessage()]);
    exit();
}

${s}action = ${s}_GET['action'] ?? ${s}_POST['action'] ?? 'ping';

if (${s}action === 'ping' || ${s}action === 'status') {
    ${s}count = ${s}pdo->query("SELECT COUNT(*) as t FROM indicator_records")->fetch()['t'] ?? 0;
    echo json_encode([
        'status' => 'ok',
        'databaseConnected' => true,
        'recordsCount' => (int)${s}count,
        'serverTime' => date('Y-m-d H:i:s'),
        'message' => 'Servidor Hostinger conectado com sucesso ao MySQL!'
    ]);
} elseif (${s}action === 'pull') {
    ${s}stmt = ${s}pdo->query("SELECT * FROM indicator_records ORDER BY id DESC LIMIT 2000");
    echo json_encode(['status' => 'ok', 'records' => ${s}stmt->fetchAll()]);
} elseif (${s}action === 'push' || ${s}action === 'sync') {
    ${s}body = json_decode(file_get_contents('php://input'), true);
    ${s}records = ${s}body['records'] ?? [];
    ${s}pdo->beginTransaction();
    ${s}stmt = ${s}pdo->prepare("INSERT INTO indicator_records (
        tecnicoLogin, tecnicoNome, indicatorType, mes, mesNumero, periodo, dataRegistro,
        meta, realizado, atingimentoPercentual, totalAtendimentos, status, ordemServico,
        cliente, observacoes, origemArquivo, dataUpload, produtividade, revisita, tec1,
        isCertificado, categoriaCertidao, vSemFalha, vComFalha, vFalhaApi, vJustificado,
        nvComFalha, nvSemFalha, nvFalhaApi, saldoProducao, saldoRevisita, ganhoRevisita
    ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

    foreach (${s}records as ${s}r) {
        ${s}stmt->execute([
            ${s}r['tecnicoLogin'] ?? '', ${s}r['tecnicoNome'] ?? '', ${s}r['indicatorType'] ?? '',
            ${s}r['mes'] ?? 'Setembro', ${s}r['mesNumero'] ?? 9, ${s}r['periodo'] ?? 'Últimos 30 Dias',
            ${s}r['dataRegistro'] ?? date('d/m/Y'), ${s}r['meta'] ?? 0, ${s}r['realizado'] ?? 0,
            ${s}r['atingimentoPercentual'] ?? 0, ${s}r['totalAtendimentos'] ?? 0, ${s}r['status'] ?? 'ATINGIDA',
            ${s}r['ordemServico'] ?? '', ${s}r['cliente'] ?? '', ${s}r['observacoes'] ?? '',
            ${s}r['origemArquivo'] ?? 'app_sync', ${s}r['dataUpload'] ?? (time() * 1000),
            ${s}r['produtividade'] ?? 0, ${s}r['revisita'] ?? 0, ${s}r['tec1'] ?? 0,
            !empty(${s}r['isCertificado']) ? 1 : 0, ${s}r['categoriaCertidao'] ?? '',
            ${s}r['vSemFalha'] ?? 0, ${s}r['vComFalha'] ?? 0, ${s}r['vFalhaApi'] ?? 0,
            ${s}r['vJustificado'] ?? 0, ${s}r['nvComFalha'] ?? 0, ${s}r['nvSemFalha'] ?? 0,
            ${s}r['nvFalhaApi'] ?? 0, ${s}r['saldoProducao'] ?? 0, ${s}r['saldoRevisita'] ?? 0,
            ${s}r['ganhoRevisita'] ?? ''
        ]);
    }
    ${s}pdo->commit();
    ${s}count = ${s}pdo->query("SELECT COUNT(*) as t FROM indicator_records")->fetch()['t'] ?? count(${s}records);
    echo json_encode(['status' => 'ok', 'insertedCount' => count(${s}records), 'totalRecords' => (int)${s}count]);
}
""".trimIndent()
    }

    fun getSqlSchema(): String {
        return """CREATE TABLE IF NOT EXISTS `indicator_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tecnicoLogin` VARCHAR(100) NOT NULL,
    `tecnicoNome` VARCHAR(255) NOT NULL,
    `indicatorType` VARCHAR(100) NOT NULL,
    `mes` VARCHAR(50) NOT NULL,
    `mesNumero` INT NOT NULL,
    `periodo` VARCHAR(100) NOT NULL,
    `dataRegistro` VARCHAR(50) NOT NULL,
    `meta` DOUBLE NOT NULL,
    `realizado` DOUBLE NOT NULL,
    `atingimentoPercentual` DOUBLE NOT NULL,
    `totalAtendimentos` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `ordemServico` VARCHAR(100) NOT NULL,
    `cliente` VARCHAR(255) NOT NULL,
    `observacoes` TEXT,
    `origemArquivo` VARCHAR(255) NOT NULL,
    `dataUpload` BIGINT NOT NULL,
    `produtividade` DOUBLE DEFAULT 0,
    `revisita` DOUBLE DEFAULT 0,
    `tec1` DOUBLE DEFAULT 0,
    `isCertificado` TINYINT(1) DEFAULT 0,
    `categoriaCertidao` VARCHAR(100) DEFAULT '',
    `vSemFalha` INT DEFAULT 0,
    `vComFalha` INT DEFAULT 0,
    `vFalhaApi` INT DEFAULT 0,
    `vJustificado` INT DEFAULT 0,
    `nvComFalha` INT DEFAULT 0,
    `nvSemFalha` INT DEFAULT 0,
    `nvFalhaApi` INT DEFAULT 0,
    `saldoProducao` DOUBLE DEFAULT 0,
    `saldoRevisita` DOUBLE DEFAULT 0,
    `ganhoRevisita` VARCHAR(100) DEFAULT '',
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_login (`tecnicoLogin`),
    INDEX idx_tipo (`indicatorType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;""".trimIndent()
    }
}
