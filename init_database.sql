-- ==============================================================================
-- DATABASE INITIALIZATION SCRIPT: VNeID Civic Point (VCP)
-- DBMS: Microsoft SQL Server
-- Description: Creates the VCP database and all required tables, constraints, 
--              and indexes based on the project requirements.
-- ==============================================================================

USE master;
GO

-- 1. Create Database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'VCP')
BEGIN
    CREATE DATABASE VCP;
END
GO

USE VCP;
GO

-- =========================================
-- 2. Create Tables (Order by dependencies)
-- =========================================

-- 2.1. ADMIN_USER table (Independent)
IF OBJECT_ID('dbo.ADMIN_USER', 'U') IS NULL
CREATE TABLE dbo.ADMIN_USER (
    admin_id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    username                NVARCHAR(100)   NOT NULL,
    password_hash           NVARCHAR(255)   NOT NULL,
    role                    NVARCHAR(30)    NOT NULL,
    status                  NVARCHAR(20)    NOT NULL CONSTRAINT DF_ADMIN_STATUS DEFAULT 'ACTIVE',
    failed_login_attempts   INT             NOT NULL CONSTRAINT DF_ADMIN_FAILS DEFAULT 0,
    locked_until            DATETIME2       NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_ADMIN_CA DEFAULT GETDATE(),
    updated_at              DATETIME2       NOT NULL CONSTRAINT DF_ADMIN_UA DEFAULT GETDATE(),
    CONSTRAINT UX_ADMIN_USER_USERNAME UNIQUE (username)
);
GO

-- 2.2. CITIZEN table (Independent)
IF OBJECT_ID('dbo.CITIZEN', 'U') IS NULL
CREATE TABLE dbo.CITIZEN (
    citizen_id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    cccd_number             NVARCHAR(12)    NOT NULL,
    full_name               NVARCHAR(255)   NOT NULL,
    date_of_birth           DATE            NOT NULL,
    account_status          NVARCHAR(20)    NOT NULL CONSTRAINT DF_CITIZEN_STATUS DEFAULT 'ACTIVE',
    consent_granted         BIT             NOT NULL CONSTRAINT DF_CITIZEN_CONSENT DEFAULT 0,
    consent_granted_at      DATETIME2       NULL,
    is_disabled             BIT             NOT NULL CONSTRAINT DF_CITIZEN_DISABLED DEFAULT 0,
    is_ethnic_minority      BIT             NOT NULL CONSTRAINT DF_CITIZEN_ETHNIC DEFAULT 0,
    current_rank            NVARCHAR(20)    NOT NULL CONSTRAINT DF_CITIZEN_RANK DEFAULT 'UNRANKED',
    total_points            DECIMAL(10,2)   NOT NULL CONSTRAINT DF_CITIZEN_TOTAL DEFAULT 0,
    fiscal_year_points      DECIMAL(10,2)   NOT NULL CONSTRAINT DF_CITIZEN_FY DEFAULT 0,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_CITIZEN_CA DEFAULT GETDATE(),
    updated_at              DATETIME2       NOT NULL CONSTRAINT DF_CITIZEN_UA DEFAULT GETDATE(),
    CONSTRAINT UX_CITIZEN_CCCD UNIQUE (cccd_number)
);
GO

-- 2.3. SCORING_RULE (Depends on ADMIN_USER)
IF OBJECT_ID('dbo.SCORING_RULE', 'U') IS NULL
CREATE TABLE dbo.SCORING_RULE (
    rule_id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
    activity_code           NVARCHAR(50)    NOT NULL,
    activity_category       NVARCHAR(100)   NOT NULL,
    activity_name           NVARCHAR(255)   NOT NULL,
    point_value             INT             NOT NULL,
    frequency_limit         INT             NULL,
    frequency_period        NVARCHAR(20)    NULL,
    category_annual_cap     DECIMAL(10,2)   NULL,
    overall_annual_cap      DECIMAL(10,2)   NULL,
    retention_rate          DECIMAL(5,2)    NOT NULL CONSTRAINT DF_RULE_RETENTION DEFAULT 30.0,
    status                  NVARCHAR(20)    NOT NULL CONSTRAINT DF_RULE_STATUS DEFAULT 'ACTIVE',
    valid_from              DATETIME2       NOT NULL,
    valid_to                DATETIME2       NULL,
    created_by              BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    approved_by             BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    approval_status         NVARCHAR(20)    NOT NULL CONSTRAINT DF_RULE_APP_STATUS DEFAULT 'PENDING',
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_RULE_CA DEFAULT GETDATE(),
    updated_at              DATETIME2       NOT NULL CONSTRAINT DF_RULE_UA DEFAULT GETDATE()
);
GO
CREATE INDEX IX_SCORING_RULE_CODE ON dbo.SCORING_RULE (activity_code);
CREATE INDEX IX_SCORING_RULE_STATUS ON dbo.SCORING_RULE (status);
GO

-- 2.4. RANK_TIER (Depends on ADMIN_USER)
IF OBJECT_ID('dbo.RANK_TIER', 'U') IS NULL
CREATE TABLE dbo.RANK_TIER (
    tier_id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
    rank_name               NVARCHAR(20)    NOT NULL,
    min_points              DECIMAL(10,2)   NOT NULL,
    max_points              DECIMAL(10,2)   NULL,
    valid_from              DATETIME2       NOT NULL,
    valid_to                DATETIME2       NULL,
    created_by              BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    approved_by             BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    approval_status         NVARCHAR(20)    NOT NULL CONSTRAINT DF_TIER_APP_STATUS DEFAULT 'PENDING',
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_TIER_CA DEFAULT GETDATE()
);
GO

-- 2.5. INCENTIVE_POLICY (Depends on ADMIN_USER)
IF OBJECT_ID('dbo.INCENTIVE_POLICY', 'U') IS NULL
CREATE TABLE dbo.INCENTIVE_POLICY (
    policy_id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    rank_name               NVARCHAR(20)    NOT NULL,
    tax_type                NVARCHAR(50)    NOT NULL,
    reduction_rate          DECIMAL(5,2)    NOT NULL,
    description             NVARCHAR(500)   NULL,
    status                  NVARCHAR(20)    NOT NULL CONSTRAINT DF_POLICY_STATUS DEFAULT 'ACTIVE',
    valid_from              DATETIME2       NOT NULL,
    valid_to                DATETIME2       NULL,
    created_by              BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    approved_by             BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    approval_status         NVARCHAR(20)    NOT NULL CONSTRAINT DF_POLICY_APP_STATUS DEFAULT 'PENDING',
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_POLICY_CA DEFAULT GETDATE()
);
GO

-- 2.6. EXPORT_BATCH (Independent)
IF OBJECT_ID('dbo.EXPORT_BATCH', 'U') IS NULL
CREATE TABLE dbo.EXPORT_BATCH (
    batch_id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    fiscal_year             INT             NOT NULL,
    export_date             DATETIME2       NOT NULL,
    total_records           INT             NOT NULL,
    status                  NVARCHAR(20)    NOT NULL CONSTRAINT DF_BATCH_STATUS DEFAULT 'PENDING',
    is_supplementary        BIT             NOT NULL CONSTRAINT DF_BATCH_SUPP DEFAULT 0,
    triggered_by            NVARCHAR(20)    NOT NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_BATCH_CA DEFAULT GETDATE(),
    sent_at                 DATETIME2       NULL
);
GO

-- 2.7. ACTIVITY_LOG (Depends on CITIZEN)
IF OBJECT_ID('dbo.ACTIVITY_LOG', 'U') IS NULL
CREATE TABLE dbo.ACTIVITY_LOG (
    log_id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    activity_code           NVARCHAR(50)    NOT NULL,
    source_system           NVARCHAR(50)    NOT NULL,
    external_reference      NVARCHAR(255)   NULL,
    partner_id              NVARCHAR(100)   NULL,
    activity_date           DATETIME2       NOT NULL,
    fiscal_year             INT             NOT NULL,
    ingestion_status        NVARCHAR(30)    NOT NULL,
    rejection_reason        NVARCHAR(500)   NULL,
    forwarded_to_engine     BIT             NOT NULL CONSTRAINT DF_AL_FWD DEFAULT 0,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_AL_CA DEFAULT GETDATE(),
    CONSTRAINT UX_ACTIVITY_EXT_REF UNIQUE (external_reference, partner_id)
);
GO
CREATE INDEX IX_ACTIVITY_LOG_CITIZEN ON dbo.ACTIVITY_LOG (citizen_id);
CREATE INDEX IX_ACTIVITY_LOG_FISCAL ON dbo.ACTIVITY_LOG (citizen_id, activity_code, fiscal_year);
GO

-- 2.8. POINT_LEDGER (Depends on CITIZEN, ACTIVITY_LOG, SCORING_RULE)
IF OBJECT_ID('dbo.POINT_LEDGER', 'U') IS NULL
CREATE TABLE dbo.POINT_LEDGER (
    ledger_id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    activity_log_id         BIGINT          NULL REFERENCES dbo.ACTIVITY_LOG(log_id),
    rule_id                 BIGINT          NULL REFERENCES dbo.SCORING_RULE(rule_id),
    transaction_type        NVARCHAR(30)    NOT NULL,
    points_awarded          DECIMAL(10,2)   NOT NULL,
    points_original         DECIMAL(10,2)   NULL,
    fiscal_year             INT             NOT NULL,
    cap_applied             NVARCHAR(30)    NULL,
    rejection_reason        NVARCHAR(500)   NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_PL_CA DEFAULT GETDATE()
);
GO
CREATE INDEX IX_LEDGER_CITIZEN_FISCAL ON dbo.POINT_LEDGER (citizen_id, fiscal_year);
GO

-- 2.9. RANK_HISTORY (Depends on CITIZEN)
IF OBJECT_ID('dbo.RANK_HISTORY', 'U') IS NULL
CREATE TABLE dbo.RANK_HISTORY (
    history_id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id                  BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    previous_rank               NVARCHAR(20)    NULL,
    new_rank                    NVARCHAR(20)    NOT NULL,
    total_points_at_evaluation  DECIMAL(10,2)   NOT NULL,
    trigger_type                NVARCHAR(30)    NOT NULL,
    rank_changed                BIT             NOT NULL,
    evaluated_at                DATETIME2       NOT NULL CONSTRAINT DF_RANK_EVAL_AT DEFAULT GETDATE()
);
GO
CREATE INDEX IX_RANK_HISTORY_CITIZEN ON dbo.RANK_HISTORY (citizen_id);
GO

-- 2.10. YEAR_END_PROCESSING (Depends on CITIZEN)
IF OBJECT_ID('dbo.YEAR_END_PROCESSING', 'U') IS NULL
CREATE TABLE dbo.YEAR_END_PROCESSING (
    processing_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    fiscal_year             INT             NOT NULL,
    previous_total_points   DECIMAL(10,2)   NOT NULL,
    carryover_amount        DECIMAL(10,2)   NOT NULL,
    retention_rate_applied  DECIMAL(5,2)    NOT NULL,
    processed_at            DATETIME2       NOT NULL CONSTRAINT DF_YE_PROC_AT DEFAULT GETDATE(),
    status                  NVARCHAR(20)    NOT NULL CONSTRAINT DF_YE_STATUS DEFAULT 'SUCCESS',
    CONSTRAINT UX_YEAR_END_CITIZEN_YEAR UNIQUE (citizen_id, fiscal_year)
);
GO

-- 2.11. VULNERABLE_BONUS (Depends on CITIZEN)
IF OBJECT_ID('dbo.VULNERABLE_BONUS', 'U') IS NULL
CREATE TABLE dbo.VULNERABLE_BONUS (
    bonus_id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    fiscal_year             INT             NOT NULL,
    bonus_points_awarded    DECIMAL(10,2)   NOT NULL,
    bonus_points_configured DECIMAL(10,2)   NOT NULL,
    baseline_threshold      DECIMAL(10,2)   NOT NULL,
    vulnerable_flags        NVARCHAR(100)   NOT NULL,
    awarded_at              DATETIME2       NOT NULL CONSTRAINT DF_VB_AWARD_AT DEFAULT GETDATE(),
    CONSTRAINT UX_VULNERABLE_BONUS_CITIZEN_YEAR UNIQUE (citizen_id, fiscal_year)
);
GO

-- 2.12. SUSPENSION_RECORD (Depends on CITIZEN)
IF OBJECT_ID('dbo.SUSPENSION_RECORD', 'U') IS NULL
CREATE TABLE dbo.SUSPENSION_RECORD (
    suspension_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    suspension_type         NVARCHAR(20)    NOT NULL,
    reason_code             NVARCHAR(100)   NOT NULL,
    violation_level         NVARCHAR(20)    NULL,
    suspended_at            DATETIME2       NOT NULL,
    auto_resume_date        DATETIME2       NULL,
    resumed_at              DATETIME2       NULL,
    resume_source           NVARCHAR(20)    NULL,
    is_active               BIT             NOT NULL CONSTRAINT DF_SUSP_ACTIVE DEFAULT 1,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_SUSP_CA DEFAULT GETDATE()
);
GO
CREATE INDEX IX_SUSPENSION_CITIZEN ON dbo.SUSPENSION_RECORD (citizen_id);
-- Filtered index approach for active suspensions
CREATE INDEX IX_SUSPENSION_ACTIVE ON dbo.SUSPENSION_RECORD (citizen_id, is_active) WHERE is_active = 1;
GO

-- 2.13. NOTIFICATION (Depends on CITIZEN)
IF OBJECT_ID('dbo.NOTIFICATION', 'U') IS NULL
CREATE TABLE dbo.NOTIFICATION (
    notification_id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    notification_type       NVARCHAR(50)    NOT NULL,
    title                   NVARCHAR(255)   NOT NULL,
    body                    NVARCHAR(2000)  NOT NULL,
    deep_link               NVARCHAR(500)   NULL,
    push_delivered          BIT             NOT NULL CONSTRAINT DF_NOTIF_PUSH DEFAULT 0,
    is_read                 BIT             NOT NULL CONSTRAINT DF_NOTIF_READ DEFAULT 0,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_NOTIF_CA DEFAULT GETDATE()
);
GO
CREATE INDEX IX_NOTIFICATION_CITIZEN ON dbo.NOTIFICATION (citizen_id, is_read, created_at);
GO

-- 2.14. FEEDBACK (Depends on CITIZEN, ADMIN_USER)
IF OBJECT_ID('dbo.FEEDBACK', 'U') IS NULL
CREATE TABLE dbo.FEEDBACK (
    feedback_id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    tracking_id             NVARCHAR(50)    NOT NULL,
    category                NVARCHAR(100)   NOT NULL,
    title                   NVARCHAR(255)   NOT NULL,
    description             NVARCHAR(4000)  NOT NULL,
    attachment_urls         NVARCHAR(2000)  NULL,
    status                  NVARCHAR(30)    NOT NULL CONSTRAINT DF_FB_STATUS DEFAULT 'PENDING',
    is_constructive         BIT             NOT NULL CONSTRAINT DF_FB_CONSTRUCTIVE DEFAULT 0,
    resolution_reason       NVARCHAR(2000)  NULL,
    resolved_by             BIGINT          NULL REFERENCES dbo.ADMIN_USER(admin_id),
    resolved_at             DATETIME2       NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_FB_CA DEFAULT GETDATE(),
    updated_at              DATETIME2       NOT NULL CONSTRAINT DF_FB_UA DEFAULT GETDATE(),
    CONSTRAINT UX_FEEDBACK_TRACKING UNIQUE (tracking_id)
);
GO
CREATE INDEX IX_FEEDBACK_CITIZEN_STATUS ON dbo.FEEDBACK (citizen_id, status);
GO

-- 2.15. INCENTIVE_HISTORY (Depends on CITIZEN, EXPORT_BATCH)
IF OBJECT_ID('dbo.INCENTIVE_HISTORY', 'U') IS NULL
CREATE TABLE dbo.INCENTIVE_HISTORY (
    history_id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id              BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    batch_id                BIGINT          NOT NULL REFERENCES dbo.EXPORT_BATCH(batch_id),
    fiscal_year             INT             NOT NULL,
    tax_type                NVARCHAR(50)    NOT NULL,
    reduction_rate          DECIMAL(5,2)    NOT NULL,
    rank_name_at_export     NVARCHAR(20)    NOT NULL,
    confirmation_reference  NVARCHAR(255)   NULL,
    effective_date          DATE            NULL,
    confirmation_source     NVARCHAR(100)   NULL,
    confirmed_at            DATETIME2       NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_IH_CA DEFAULT GETDATE(),
    CONSTRAINT UX_INCENTIVE_HISTORY UNIQUE (citizen_id, batch_id, tax_type)
);
GO

-- 2.16. AUDIT_LOG (Independent logically, references CITIZEN/ADMIN as loosely-coupled IDs)
IF OBJECT_ID('dbo.AUDIT_LOG', 'U') IS NULL
CREATE TABLE dbo.AUDIT_LOG (
    audit_id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    event_type              NVARCHAR(100)   NOT NULL,
    actor_type              NVARCHAR(20)    NOT NULL,
    actor_id                BIGINT          NULL,
    ip_address              NVARCHAR(50)    NULL,
    entity_type             NVARCHAR(50)    NULL,
    entity_id               BIGINT          NULL,
    description             NVARCHAR(2000)  NULL,
    event_data              NVARCHAR(MAX)   NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_AUDIT_CA DEFAULT GETDATE()
);
GO

PRINT 'Database VCP and all tables created successfully.';
GO
