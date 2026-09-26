-- ============================================================================
-- CLARO • SCHEMA SQL PARA BANCO DE DADOS MYSQL NA HOSTINGER
-- ============================================================================
-- Como usar:
-- 1. Acesse o hPanel da Hostinger -> Bancos de Dados MySQL -> phpMyAdmin
-- 2. Selecione o banco de dados criado
-- 3. Clique na aba 'SQL', cole este código e clique em 'Executar'
-- ============================================================================

CREATE TABLE IF NOT EXISTS `indicator_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tecnicoLogin` VARCHAR(100) NOT NULL COMMENT 'Login do técnico (ex: tec.carlos)',
    `tecnicoNome` VARCHAR(255) NOT NULL COMMENT 'Nome completo do técnico',
    `indicatorType` VARCHAR(100) NOT NULL COMMENT 'TECNICO_CERTIFICADO, REVISITA_30D, CERTIDAO_ATENDIMENTO, COP_360, TNPS',
    `mes` VARCHAR(50) NOT NULL COMMENT 'Mês por extenso (ex: Setembro)',
    `mesNumero` INT NOT NULL COMMENT 'Número do mês (1 a 12)',
    `periodo` VARCHAR(100) NOT NULL COMMENT 'Ex: Últimos 30 Dias, 1º Semestre',
    `dataRegistro` VARCHAR(50) NOT NULL COMMENT 'Data no formato dd/MM/yyyy',
    `meta` DOUBLE NOT NULL,
    `realizado` DOUBLE NOT NULL,
    `atingimentoPercentual` DOUBLE NOT NULL,
    `totalAtendimentos` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL COMMENT 'ATINGIDA, ATENCAO, CRITICO',
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

CREATE TABLE IF NOT EXISTS `sync_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `source` VARCHAR(100) NOT NULL,
    `fileName` VARCHAR(255),
    `recordCount` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `message` TEXT,
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
