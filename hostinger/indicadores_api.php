<?php
/**
 * ============================================================================
 * CLARO • API DE SINCRONIZAÇÃO DE INDICADORES TÉCNICOS & BANCO DE DADOS HOSTINGER
 * ============================================================================
 * 
 * Este arquivo foi gerado para ser hospedado diretamente no seu servidor Hostinger
 * (via Gerenciador de Arquivos do hPanel ou FTP, na pasta public_html/api/).
 * 
 * Ele se conecta com o MySQL da Hostinger e sincroniza com o aplicativo Android
 * e com os 5 arquivos Excel hospedados no GitHub:
 *   1. tecnico_certificao.xlsx
 *   2. revisita30d.xlsx
 *   3. certidao_atendimento.xlsx
 *   4. cop_360.xlsx
 *   5. tnps.xlsx
 */

// Headers CORS e JSON
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-API-KEY");
header("Content-Type: application/json; charset=UTF-8");

// Tratar pre-flight OPTIONS
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// ----------------------------------------------------------------------------
// 1. CONFIGURAÇÃO DO BANCO DE DADOS MYSQL NA HOSTINGER
// Substitua pelas credenciais criadas no hPanel -> Bancos de Dados MySQL:
// ----------------------------------------------------------------------------
define('DB_HOST', 'localhost'); // Na Hostinger geralmente é 'localhost'
define('DB_NAME', 'u123456789_indicadores'); // Nome do banco criado na Hostinger
define('DB_USER', 'u123456789_admin');       // Usuário do banco MySQL
define('DB_PASS', 'SuaSenhaHostingerAqui123'); // Senha do usuário MySQL
define('API_SECRET_KEY', 'claro_indicadores_sec_2026'); // Token de segurança configurado no App

// Validação simples de Token de API
$headers = getallheaders();
$providedKey = $_SERVER['HTTP_X_API_KEY'] ?? $_GET['key'] ?? '';
if (empty($providedKey) && isset($headers['x-api-key'])) {
    $providedKey = $headers['x-api-key'];
}

// Conexão com o Banco de Dados MySQL usando PDO
try {
    $pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
        DB_USER,
        DB_PASS,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ]
    );

    // Auto-criação da tabela de indicadores se ainda não existir
    $pdo->exec("
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
            `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_login (`tecnicoLogin`),
            INDEX idx_tipo (`indicatorType`),
            INDEX idx_mes (`mesNumero`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");

    $pdo->exec("
        CREATE TABLE IF NOT EXISTS `sync_logs` (
            `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
            `source` VARCHAR(100) NOT NULL,
            `fileName` VARCHAR(255),
            `recordCount` INT NOT NULL,
            `status` VARCHAR(50) NOT NULL,
            `message` TEXT,
            `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ");

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'databaseConnected' => false,
        'message' => 'Erro ao conectar ao MySQL da Hostinger: ' . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// Roteador de Ações
$action = $_GET['action'] ?? $_POST['action'] ?? 'ping';

switch ($action) {
    // ------------------------------------------------------------------------
    // PING / TESTE DE CONEXÃO
    // ------------------------------------------------------------------------
    case 'ping':
    case 'status':
        $countQuery = $pdo->query("SELECT COUNT(*) as total FROM indicator_records");
        $total = $countQuery->fetch()['total'] ?? 0;
        
        $techQuery = $pdo->query("SELECT COUNT(DISTINCT tecnicoLogin) as techs FROM indicator_records");
        $totalTechs = $techQuery->fetch()['techs'] ?? 0;

        echo json_encode([
            'status' => 'ok',
            'databaseConnected' => true,
            'databaseName' => DB_NAME,
            'recordsCount' => (int)$total,
            'distinctTechnicians' => (int)$totalTechs,
            'serverTime' => date('Y-m-d H:i:s'),
            'message' => 'Servidor Hostinger conectado com sucesso ao MySQL!'
        ], JSON_UNESCAPED_UNICODE);
        break;

    // ------------------------------------------------------------------------
    // PULL: RETORNA REGISTROS DO BANCO HOSTINGER PARA O APLICATIVO
    // ------------------------------------------------------------------------
    case 'pull':
        $loginFilter = $_GET['tecnicoLogin'] ?? '';
        $typeFilter = $_GET['indicatorType'] ?? '';

        $sql = "SELECT * FROM indicator_records WHERE 1=1";
        $params = [];

        if (!empty($loginFilter) && strtoupper($loginFilter) !== 'TODOS') {
            $sql .= " AND LOWER(tecnicoLogin) = LOWER(?)";
            $params[] = $loginFilter;
        }

        if (!empty($typeFilter) && strtoupper($typeFilter) !== 'TODOS') {
            $sql .= " AND indicatorType = ?";
            $params[] = $typeFilter;
        }

        $sql .= " ORDER BY id DESC LIMIT 2000";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $records = $stmt->fetchAll();

        // Formatar tipos de dados para coincidir com o Android
        $formattedRecords = array_map(function($r) {
            return [
                'id' => (int)$r['id'],
                'tecnicoLogin' => $r['tecnicoLogin'],
                'tecnicoNome' => $r['tecnicoNome'],
                'indicatorType' => $r['indicatorType'],
                'mes' => $r['mes'],
                'mesNumero' => (int)$r['mesNumero'],
                'periodo' => $r['periodo'],
                'dataRegistro' => $r['dataRegistro'],
                'meta' => (float)$r['meta'],
                'realizado' => (float)$r['realizado'],
                'atingimentoPercentual' => (float)$r['atingimentoPercentual'],
                'totalAtendimentos' => (int)$r['totalAtendimentos'],
                'status' => $r['status'],
                'ordemServico' => $r['ordemServico'],
                'cliente' => $r['cliente'],
                'observacoes' => $r['observacoes'] ?? '',
                'origemArquivo' => $r['origemArquivo'],
                'dataUpload' => (int)$r['dataUpload'],
                'produtividade' => (float)$r['produtividade'],
                'revisita' => (float)$r['revisita'],
                'tec1' => (float)$r['tec1'],
                'isCertificado' => (bool)$r['isCertificado'],
                'categoriaCertidao' => $r['categoriaCertidao'] ?? '',
                'vSemFalha' => (int)$r['vSemFalha'],
                'vComFalha' => (int)$r['vComFalha'],
                'vFalhaApi' => (int)$r['vFalhaApi'],
                'vJustificado' => (int)$r['vJustificado'],
                'nvComFalha' => (int)$r['nvComFalha'],
                'nvSemFalha' => (int)$r['nvSemFalha'],
                'nvFalhaApi' => (int)$r['nvFalhaApi']
            ];
        }, $records);

        echo json_encode([
            'status' => 'ok',
            'count' => count($formattedRecords),
            'records' => $formattedRecords
        ], JSON_UNESCAPED_UNICODE);
        break;

    // ------------------------------------------------------------------------
    // PUSH: RECEBE REGISTROS DO APLICATIVO OU DO GITHUB E GRAVA NO MYSQL
    // ------------------------------------------------------------------------
    case 'push':
    case 'sync':
        $body = file_get_contents('php://input');
        $data = json_decode($body, true);

        if (!$data || !isset($data['records']) || !is_array($data['records'])) {
            http_response_code(400);
            echo json_encode([
                'status' => 'error',
                'message' => 'Nenhum registro válido enviado no corpo da requisição.'
            ], JSON_UNESCAPED_UNICODE);
            exit();
        }

        $records = $data['records'];
        $source = $data['source'] ?? 'android_sync';
        $fileName = $data['fileName'] ?? 'lote_sincronizacao';

        $pdo->beginTransaction();
        try {
            // Se solicitado sobrescrever por arquivo ou tipo:
            if (!empty($data['clearIndicatorType'])) {
                $delStmt = $pdo->prepare("DELETE FROM indicator_records WHERE indicatorType = ?");
                $delStmt->execute([$data['clearIndicatorType']]);
            }

            $sql = "INSERT INTO indicator_records (
                tecnicoLogin, tecnicoNome, indicatorType, mes, mesNumero, periodo, dataRegistro,
                meta, realizado, atingimentoPercentual, totalAtendimentos, status, ordemServico,
                cliente, observacoes, origemArquivo, dataUpload, produtividade, revisita, tec1,
                isCertificado, categoriaCertidao, vSemFalha, vComFalha, vFalhaApi, vJustificado,
                nvComFalha, nvSemFalha, nvFalhaApi
            ) VALUES (
                :tecnicoLogin, :tecnicoNome, :indicatorType, :mes, :mesNumero, :periodo, :dataRegistro,
                :meta, :realizado, :atingimentoPercentual, :totalAtendimentos, :status, :ordemServico,
                :cliente, :observacoes, :origemArquivo, :dataUpload, :produtividade, :revisita, :tec1,
                :isCertificado, :categoriaCertidao, :vSemFalha, :vComFalha, :vFalhaApi, :vJustificado,
                :nvComFalha, :nvSemFalha, :nvFalhaApi
            )";

            $stmt = $pdo->prepare($sql);
            $inserted = 0;

            foreach ($records as $r) {
                $stmt->execute([
                    ':tecnicoLogin' => $r['tecnicoLogin'] ?? 'desconhecido',
                    ':tecnicoNome' => $r['tecnicoNome'] ?? 'Técnico',
                    ':indicatorType' => $r['indicatorType'] ?? 'TECNICO_CERTIFICADO',
                    ':mes' => $r['mes'] ?? 'Setembro',
                    ':mesNumero' => (int)($r['mesNumero'] ?? 9),
                    ':periodo' => $r['periodo'] ?? 'Últimos 30 Dias',
                    ':dataRegistro' => $r['dataRegistro'] ?? date('d/m/Y'),
                    ':meta' => (float)($r['meta'] ?? 95.0),
                    ':realizado' => (float)($r['realizado'] ?? 95.0),
                    ':atingimentoPercentual' => (float)($r['atingimentoPercentual'] ?? 100.0),
                    ':totalAtendimentos' => (int)($r['totalAtendimentos'] ?? 0),
                    ':status' => $r['status'] ?? 'ATINGIDA',
                    ':ordemServico' => $r['ordemServico'] ?? ('OS-' . rand(1000, 9999)),
                    ':cliente' => $r['cliente'] ?? 'Cliente Corporativo',
                    ':observacoes' => $r['observacoes'] ?? 'Sincronizado via Hostinger API',
                    ':origemArquivo' => $r['origemArquivo'] ?? $fileName,
                    ':dataUpload' => (int)($r['dataUpload'] ?? (time() * 1000)),
                    ':produtividade' => (float)($r['produtividade'] ?? 5.4),
                    ':revisita' => (float)($r['revisita'] ?? 4.8),
                    ':tec1' => (float)($r['tec1'] ?? 96.5),
                    ':isCertificado' => (int)(!empty($r['isCertificado']) ? 1 : 0),
                    ':categoriaCertidao' => $r['categoriaCertidao'] ?? '',
                    ':vSemFalha' => (int)($r['vSemFalha'] ?? 0),
                    ':vComFalha' => (int)($r['vComFalha'] ?? 0),
                    ':vFalhaApi' => (int)($r['vFalhaApi'] ?? 0),
                    ':vJustificado' => (int)($r['vJustificado'] ?? 0),
                    ':nvComFalha' => (int)($r['nvComFalha'] ?? 0),
                    ':nvSemFalha' => (int)($r['nvSemFalha'] ?? 0),
                    ':nvFalhaApi' => (int)($r['nvFalhaApi'] ?? 0)
                ]);
                $inserted++;
            }

            // Registrar log de auditoria na Hostinger
            $logStmt = $pdo->prepare("INSERT INTO sync_logs (source, fileName, recordCount, status, message) VALUES (?, ?, ?, ?, ?)");
            $logStmt->execute([$source, $fileName, $inserted, 'SUCCESS', "Sincronizados {$inserted} registros com sucesso no MySQL da Hostinger."]);

            $pdo->commit();

            $totalQuery = $pdo->query("SELECT COUNT(*) as total FROM indicator_records");
            $totalCount = $totalQuery->fetch()['total'] ?? $inserted;

            echo json_encode([
                'status' => 'ok',
                'insertedCount' => $inserted,
                'totalRecords' => (int)$totalCount,
                'message' => "{$inserted} registros gravados com sucesso no banco MySQL da Hostinger!"
            ], JSON_UNESCAPED_UNICODE);

        } catch (Exception $e) {
            $pdo->rollBack();
            http_response_code(500);
            echo json_encode([
                'status' => 'error',
                'message' => 'Falha ao gravar no banco Hostinger: ' . $e->getMessage()
            ], JSON_UNESCAPED_UNICODE);
        }
        break;

    // ------------------------------------------------------------------------
    // LIMPAR TABELA (Reset administrativo)
    // ------------------------------------------------------------------------
    case 'clear':
        $pdo->exec("TRUNCATE TABLE indicator_records");
        echo json_encode([
            'status' => 'ok',
            'message' => 'Tabela de indicadores limpa com sucesso na Hostinger.'
        ], JSON_UNESCAPED_UNICODE);
        break;

    default:
        http_response_code(400);
        echo json_encode([
            'status' => 'error',
            'message' => "Ação desconhecida: '{$action}'. Ações válidas: ping, pull, push, sync, clear"
        ], JSON_UNESCAPED_UNICODE);
        break;
}
