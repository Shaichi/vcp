# DATABASE DESIGN – VNeID Civic Point (VCP)

**Project:** VNeID Civic Point (VCP)  
**Subject:** SWD392  
**Architecture:** Client – Server (Flutter + Spring Boot + MS SQL Server)  
**Pattern:** MVC (Model – View – Controller)  
**Database:** Microsoft SQL Server  
**Version:** 1.0

---

## 1. Overview

The VCP system tracks and rewards citizen engagement in Vietnam's national digital transformation program. Citizens earn "Civic Points" through verified digital activities (profile completion, administrative services, financial transactions, healthcare, education, civic engagement, etc.). Based on accumulated points, citizens are ranked and may qualify for tax/fee incentives.

---

## 2. Architecture Mapping (MVC – Spring Boot)

```
┌─────────────────────────────────────────────────────────────────┐
│  CLIENT (Flutter Mobile App)                                    │
│  - Views: Citizen dashboard, point history, notifications, etc. │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP/REST (TLS 1.3)
┌────────────────────────▼────────────────────────────────────────┐
│  SERVER (Spring Boot – MVC)                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Controller  │  │   Service    │  │   Repository (JPA)   │  │
│  │  (REST APIs) │→ │ (Business    │→ │   (Data Access)      │  │
│  │              │  │   Logic)     │  │                      │  │
│  └──────────────┘  └──────────────┘  └──────────┬───────────┘  │
└─────────────────────────────────────────────────┼───────────────┘
                                                  │ JDBC / JPA
┌─────────────────────────────────────────────────▼───────────────┐
│  DATABASE (Microsoft SQL Server)                                │
│  - Spring Boot starter: spring-boot-starter-data-jpa            │
│  - Driver: mssql-jdbc                                           │
└─────────────────────────────────────────────────────────────────┘
```

**Spring Boot packages (conventional MVC layout):**

| Package | Role |
|---------|------|
| `model` / `entity` | JPA Entities → DB Tables |
| `repository` | Spring Data JPA Repositories |
| `service` | Business logic (point calculation, ranking, etc.) |
| `controller` | REST Controllers (endpoints exposed to Flutter) |
| `dto` | Data Transfer Objects (request/response bodies) |
| `config` | Security, CORS, JPA config |

---

## 3. Entity Relationship Diagram (ERD Summary)

```
CITIZEN ──< POINT_LEDGER
CITIZEN ──< ACTIVITY_LOG
CITIZEN ──< RANK_HISTORY
CITIZEN ──< NOTIFICATION
CITIZEN ──< FEEDBACK
CITIZEN ──< SUSPENSION_RECORD
CITIZEN ──< VULNERABLE_BONUS
CITIZEN ──< INCENTIVE_HISTORY
SCORING_RULE ──< POINT_LEDGER
SCORING_RULE ──< ACTIVITY_LOG
RANK_TIER ──< RANK_HISTORY
INCENTIVE_POLICY >──< RANK_TIER
EXPORT_BATCH ──< INCENTIVE_HISTORY
FEEDBACK >── ADMIN_USER (resolved by)
AUDIT_LOG (system-wide events)
```

---

## 4. Table Definitions

### 4.1. CITIZEN

Stores citizen profile data linked to VNeID identity.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `citizen_id` | `BIGINT` | PK, IDENTITY | Internal surrogate key |
| `cccd_number` | `NVARCHAR(12)` | UNIQUE, NOT NULL | Citizen Identity Number (sole matching key from VNeID) |
| `full_name` | `NVARCHAR(255)` | NOT NULL | Citizen display name |
| `date_of_birth` | `DATE` | NOT NULL | Used to determine elderly status (>60) |
| `account_status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'ACTIVE' | `ACTIVE`, `SUSPENDED`, `REVOKED` |
| `consent_granted` | `BIT` | NOT NULL, DEFAULT 0 | Data-sharing consent (Decree 13/2023) |
| `consent_granted_at` | `DATETIME2` | NULL | Timestamp when consent was first granted |
| `is_disabled` | `BIT` | NOT NULL, DEFAULT 0 | VNeID Core flag for disability |
| `is_ethnic_minority` | `BIT` | NOT NULL, DEFAULT 0 | VNeID Core flag for ethnic minority |
| `current_rank` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'UNRANKED' | Cached rank: `ACTIVE`, `BASIC`, `UNRANKED` |
| `total_points` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0 | Current accumulated points |
| `fiscal_year_points` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0 | Points earned in current fiscal year |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | Auto-provisioned on first login |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | Last update timestamp |

**Indexes:**  
- `UX_CITIZEN_CCCD` UNIQUE on `cccd_number`

---

### 4.2. SCORING_RULE

Configuration table managed by Admin (UC-05). Versioned, soft-delete only.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `rule_id` | `BIGINT` | PK, IDENTITY | Internal ID |
| `activity_code` | `NVARCHAR(50)` | NOT NULL | Unique code for the activity type (e.g., `PROFILE_COMPLETE`) |
| `activity_category` | `NVARCHAR(100)` | NOT NULL | Category: `IDENTITY`, `SERVICE`, `FINANCIAL`, `HEALTHCARE`, `EDUCATION`, `CIVIC` |
| `activity_name` | `NVARCHAR(255)` | NOT NULL | Human-readable name |
| `point_value` | `INT` | NOT NULL | Points awarded (positive integer) |
| `frequency_limit` | `INT` | NULL | Max times per fiscal year (NULL = unlimited) |
| `frequency_period` | `NVARCHAR(20)` | NULL | `LIFETIME`, `ANNUAL`, `MONTHLY`, `WEEKLY` |
| `category_annual_cap` | `DECIMAL(10,2)` | NULL | Max points from this category per year |
| `overall_annual_cap` | `DECIMAL(10,2)` | NULL | Overall citizen cap per year |
| `retention_rate` | `DECIMAL(5,2)` | NOT NULL, DEFAULT 30.0 | Year-end carryover rate (%) |
| `status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE` |
| `valid_from` | `DATETIME2` | NOT NULL | Version start date |
| `valid_to` | `DATETIME2` | NULL | Version expiry (NULL = current) |
| `created_by` | `BIGINT` | FK → ADMIN_USER | Maker |
| `approved_by` | `BIGINT` | FK → ADMIN_USER | Checker |
| `approval_status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'PENDING' | `PENDING`, `APPROVED`, `REJECTED` |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `IX_SCORING_RULE_CODE` on `activity_code`  
- `IX_SCORING_RULE_STATUS` on `status`

---

### 4.3. RANK_TIER

Configurable rank thresholds managed by Admin (UC-06). Versioned.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `tier_id` | `BIGINT` | PK, IDENTITY | |
| `rank_name` | `NVARCHAR(20)` | NOT NULL | `ACTIVE`, `BASIC`, `UNRANKED` |
| `min_points` | `DECIMAL(10,2)` | NOT NULL | Minimum points for this rank (inclusive) |
| `max_points` | `DECIMAL(10,2)` | NULL | NULL means no upper bound |
| `valid_from` | `DATETIME2` | NOT NULL | Version start date |
| `valid_to` | `DATETIME2` | NULL | NULL = currently active |
| `created_by` | `BIGINT` | FK → ADMIN_USER | |
| `approved_by` | `BIGINT` | FK → ADMIN_USER | |
| `approval_status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'PENDING' | |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

---

### 4.4. INCENTIVE_POLICY

Tax/fee reduction rates mapped to ranks (UC-06). Versioned.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `policy_id` | `BIGINT` | PK, IDENTITY | |
| `rank_name` | `NVARCHAR(20)` | NOT NULL | e.g., `ACTIVE` |
| `tax_type` | `NVARCHAR(50)` | NOT NULL | `PIT` (Personal Income Tax), `REGISTRATION_FEE`, `VAT` |
| `reduction_rate` | `DECIMAL(5,2)` | NOT NULL | Reduction percentage (0–100) |
| `description` | `NVARCHAR(500)` | NULL | |
| `status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE` |
| `valid_from` | `DATETIME2` | NOT NULL | |
| `valid_to` | `DATETIME2` | NULL | |
| `created_by` | `BIGINT` | FK → ADMIN_USER | |
| `approved_by` | `BIGINT` | FK → ADMIN_USER | |
| `approval_status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'PENDING' | |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

---

### 4.5. ACTIVITY_LOG

Immutable ingestion log for all external activity records (UC-08 to UC-15).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `log_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `activity_code` | `NVARCHAR(50)` | NOT NULL | References SCORING_RULE.activity_code |
| `source_system` | `NVARCHAR(50)` | NOT NULL | `VNEID_CORE`, `DVC_PORTAL`, `FINANCIAL_PARTNER`, `HEALTHCARE`, `EDUCATION`, `LEGISLATIVE` |
| `external_reference` | `NVARCHAR(255)` | NULL | Unique reference from source system (idempotency key) |
| `partner_id` | `NVARCHAR(100)` | NULL | Partner identifier for financial/healthcare sources |
| `activity_date` | `DATETIME2` | NOT NULL | Date of the original activity |
| `fiscal_year` | `INT` | NOT NULL | Fiscal year of the activity (derived from activity_date) |
| `ingestion_status` | `NVARCHAR(30)` | NOT NULL | `VALID`, `DUPLICATE`, `REJECTED_NOT_FOUND`, `REJECTED_SUSPENDED`, `REJECTED_UNMAPPED`, `REJECTED_RATE_LIMIT`, `REJECTED_INVALID_DATA` |
| `rejection_reason` | `NVARCHAR(500)` | NULL | Detail when rejected |
| `forwarded_to_engine` | `BIT` | NOT NULL, DEFAULT 0 | Whether passed to UC-16 |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `IX_ACTIVITY_LOG_CITIZEN` on `citizen_id`  
- `IX_ACTIVITY_LOG_EXTERNAL_REF` on `(external_reference, partner_id)` – supports idempotency  
- `IX_ACTIVITY_LOG_FISCAL` on `(citizen_id, activity_code, fiscal_year)`

---

### 4.6. POINT_LEDGER

Append-only ledger of every point transaction (UC-16, UC-18, UC-19). Never modified after creation; corrections via adjustment entries.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `ledger_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `activity_log_id` | `BIGINT` | FK → ACTIVITY_LOG, NULL | NULL for bonus/adjustment entries |
| `rule_id` | `BIGINT` | FK → SCORING_RULE, NULL | Rule applied |
| `transaction_type` | `NVARCHAR(30)` | NOT NULL | `EARNED`, `BONUS_VULNERABLE`, `ADJUSTMENT`, `YEAR_END_CARRYOVER` |
| `points_awarded` | `DECIMAL(10,2)` | NOT NULL | Actual points credited (may be partial) |
| `points_original` | `DECIMAL(10,2)` | NULL | Originally calculated points before cap |
| `fiscal_year` | `INT` | NOT NULL | Points attributed to this fiscal year |
| `cap_applied` | `NVARCHAR(30)` | NULL | `FREQUENCY`, `CATEGORY`, `OVERALL`, `NONE` |
| `rejection_reason` | `NVARCHAR(500)` | NULL | Why points were not fully awarded |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `IX_LEDGER_CITIZEN_FISCAL` on `(citizen_id, fiscal_year)`

---

### 4.7. RANK_HISTORY

Immutable record of every rank evaluation and change (UC-17).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `history_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `previous_rank` | `NVARCHAR(20)` | NULL | NULL for first evaluation |
| `new_rank` | `NVARCHAR(20)` | NOT NULL | |
| `total_points_at_evaluation` | `DECIMAL(10,2)` | NOT NULL | Snapshot of total points |
| `trigger_type` | `NVARCHAR(30)` | NOT NULL | `POINTS_ADDED`, `YEAR_END`, `COMPLAINT_RESOLVED`, `SCHEDULED_BATCH`, `THRESHOLD_CHANGE` |
| `rank_changed` | `BIT` | NOT NULL | Whether the rank actually changed |
| `evaluated_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `IX_RANK_HISTORY_CITIZEN` on `citizen_id`

---

### 4.8. YEAR_END_PROCESSING

Records each citizen's year-end reset (UC-18). Append-only, used for idempotency.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `processing_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `fiscal_year` | `INT` | NOT NULL | Year being closed |
| `previous_total_points` | `DECIMAL(10,2)` | NOT NULL | Points before carryover |
| `carryover_amount` | `DECIMAL(10,2)` | NOT NULL | 30% of previous (floor) |
| `retention_rate_applied` | `DECIMAL(5,2)` | NOT NULL | Retention % used at processing time |
| `processed_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |
| `status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'SUCCESS' | `SUCCESS`, `FAILED`, `SKIPPED` |

**Indexes:**  
- `UX_YEAR_END_CITIZEN_YEAR` UNIQUE on `(citizen_id, fiscal_year)` – prevents duplicate processing

---

### 4.9. VULNERABLE_BONUS

Tracks annual bonus for vulnerable group (UC-19). One record per citizen per fiscal year.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `bonus_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `fiscal_year` | `INT` | NOT NULL | |
| `bonus_points_awarded` | `DECIMAL(10,2)` | NOT NULL | Actual points (may be partial due to cap) |
| `bonus_points_configured` | `DECIMAL(10,2)` | NOT NULL | Configured bonus amount at time of award |
| `baseline_threshold` | `DECIMAL(10,2)` | NOT NULL | Threshold used (e.g., 150) |
| `vulnerable_flags` | `NVARCHAR(100)` | NOT NULL | Comma-separated: `ELDERLY`, `DISABLED`, `ETHNIC_MINORITY` |
| `awarded_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `UX_VULNERABLE_BONUS_CITIZEN_YEAR` UNIQUE on `(citizen_id, fiscal_year)`

---

### 4.10. SUSPENSION_RECORD

Tracks suspension and resumption of point accumulation (UC-20).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `suspension_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `suspension_type` | `NVARCHAR(20)` | NOT NULL | `TIME_BOUND`, `INDEFINITE` |
| `reason_code` | `NVARCHAR(100)` | NOT NULL | Code received from VNeID Core |
| `violation_level` | `NVARCHAR(20)` | NULL | `MINOR`, `MODERATE`, `SEVERE` |
| `suspended_at` | `DATETIME2` | NOT NULL | |
| `auto_resume_date` | `DATETIME2` | NULL | NULL for INDEFINITE |
| `resumed_at` | `DATETIME2` | NULL | NULL while still suspended |
| `resume_source` | `NVARCHAR(20)` | NULL | `VNEID_RESTORE`, `AUTO_RESUME` |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | 1 = currently suspended |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `IX_SUSPENSION_CITIZEN` on `citizen_id`  
- `IX_SUSPENSION_ACTIVE` on `(citizen_id, is_active)` (filtered: `is_active=1`)

---

### 4.11. NOTIFICATION

In-app notification inbox for citizens (UC-03).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `notification_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `notification_type` | `NVARCHAR(50)` | NOT NULL | `POINT_ADDED`, `RANK_CHANGED`, `SUSPENSION`, `RESUME`, `INCENTIVE`, `YEAR_END`, `FEEDBACK_RESOLVED`, `VULNERABLE_BONUS` |
| `title` | `NVARCHAR(255)` | NOT NULL | |
| `body` | `NVARCHAR(2000)` | NOT NULL | Details (reason for point change, etc.) |
| `deep_link` | `NVARCHAR(500)` | NULL | In-app navigation link |
| `push_delivered` | `BIT` | NOT NULL, DEFAULT 0 | Whether push notification was sent |
| `is_read` | `BIT` | NOT NULL, DEFAULT 0 | Whether citizen read in-app |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `IX_NOTIFICATION_CITIZEN` on `(citizen_id, is_read, created_at)`

---

### 4.12. FEEDBACK

Citizen complaint and feedback submissions (UC-04, UC-07a).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `feedback_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `tracking_id` | `NVARCHAR(50)` | UNIQUE, NOT NULL | System-generated tracking code |
| `category` | `NVARCHAR(100)` | NOT NULL | e.g., `POINT_DISPUTE`, `SYSTEM_ERROR`, `SUGGESTION` |
| `title` | `NVARCHAR(255)` | NOT NULL | |
| `description` | `NVARCHAR(4000)` | NOT NULL | |
| `attachment_urls` | `NVARCHAR(2000)` | NULL | Comma-separated attachment file paths |
| `status` | `NVARCHAR(30)` | NOT NULL, DEFAULT 'PENDING' | `PENDING`, `IN_PROGRESS`, `UNDER_INVESTIGATION`, `RESOLVED`, `REJECTED` |
| `is_constructive` | `BIT` | NOT NULL, DEFAULT 0 | Flagged as constructive (triggers 5-point reward) |
| `resolution_reason` | `NVARCHAR(2000)` | NULL | Admin response |
| `resolved_by` | `BIGINT` | FK → ADMIN_USER, NULL | |
| `resolved_at` | `DATETIME2` | NULL | |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `UX_FEEDBACK_TRACKING` UNIQUE on `tracking_id`  
- `IX_FEEDBACK_CITIZEN_STATUS` on `(citizen_id, status)`

---

### 4.13. EXPORT_BATCH

Records export batches sent to Tax Authority (UC-22).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `batch_id` | `BIGINT` | PK, IDENTITY | |
| `fiscal_year` | `INT` | NOT NULL | |
| `export_date` | `DATETIME2` | NOT NULL | |
| `total_records` | `INT` | NOT NULL | Number of citizens included |
| `status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'PENDING' | `PENDING`, `SENT`, `PARTIALLY_CONFIRMED`, `CONFIRMED`, `FAILED` |
| `is_supplementary` | `BIT` | NOT NULL, DEFAULT 0 | True if this is a supplementary export |
| `triggered_by` | `NVARCHAR(20)` | NOT NULL | `SCHEDULED`, `ADMIN_MANUAL` |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |
| `sent_at` | `DATETIME2` | NULL | |

---

### 4.14. INCENTIVE_HISTORY

Per-citizen confirmation records from Tax Authority (UC-22).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `history_id` | `BIGINT` | PK, IDENTITY | |
| `citizen_id` | `BIGINT` | FK → CITIZEN, NOT NULL | |
| `batch_id` | `BIGINT` | FK → EXPORT_BATCH, NOT NULL | Original export batch |
| `fiscal_year` | `INT` | NOT NULL | |
| `tax_type` | `NVARCHAR(50)` | NOT NULL | `PIT`, `REGISTRATION_FEE`, `VAT` |
| `reduction_rate` | `DECIMAL(5,2)` | NOT NULL | Rate applied |
| `rank_name_at_export` | `NVARCHAR(20)` | NOT NULL | Snapshot of rank at export time |
| `confirmation_reference` | `NVARCHAR(255)` | NULL | Reference from Tax Authority |
| `effective_date` | `DATE` | NULL | |
| `confirmation_source` | `NVARCHAR(100)` | NULL | Tax Authority identifier |
| `confirmed_at` | `DATETIME2` | NULL | |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Indexes:**  
- `UX_INCENTIVE_HISTORY` UNIQUE on `(citizen_id, batch_id, tax_type)` – prevents duplicate confirmations

---

### 4.15. ADMIN_USER

System administrator accounts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `admin_id` | `BIGINT` | PK, IDENTITY | |
| `username` | `NVARCHAR(100)` | UNIQUE, NOT NULL | |
| `password_hash` | `NVARCHAR(255)` | NOT NULL | Hashed password |
| `role` | `NVARCHAR(30)` | NOT NULL | `ADMIN`, `SUPER_ADMIN` |
| `status` | `NVARCHAR(20)` | NOT NULL, DEFAULT 'ACTIVE' | `ACTIVE`, `LOCKED`, `INACTIVE` |
| `failed_login_attempts` | `INT` | NOT NULL, DEFAULT 0 | Reset on successful login |
| `locked_until` | `DATETIME2` | NULL | Lock expiry |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

---

### 4.16. AUDIT_LOG

System-wide append-only audit trail retained for ≥5 years (NF-07).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `audit_id` | `BIGINT` | PK, IDENTITY | |
| `event_type` | `NVARCHAR(100)` | NOT NULL | e.g., `LOGIN_SUCCESS`, `LOGIN_FAIL`, `POINT_CALCULATED`, `RULE_CHANGED`, `RANK_UPDATED`, `EXPORT_SENT`, `SUSPENSION_APPLIED` |
| `actor_type` | `NVARCHAR(20)` | NOT NULL | `CITIZEN`, `ADMIN`, `SYSTEM` |
| `actor_id` | `BIGINT` | NULL | CITIZEN.citizen_id or ADMIN_USER.admin_id |
| `ip_address` | `NVARCHAR(50)` | NULL | Client IP for login events |
| `entity_type` | `NVARCHAR(50)` | NULL | e.g., `SCORING_RULE`, `CITIZEN`, `EXPORT_BATCH` |
| `entity_id` | `BIGINT` | NULL | Primary key of affected entity |
| `description` | `NVARCHAR(2000)` | NULL | Human-readable event description |
| `event_data` | `NVARCHAR(MAX)` | NULL | JSON snapshot for complex events |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT GETDATE() | |

**Note:** This table must be append-only. No UPDATE or DELETE allowed.

---

## 5. Key Business Rules Reflected in Design

| Rule | Implementation |
|------|---------------|
| Citizen CCCD is sole unique key | `UNIQUE` on `CITIZEN.cccd_number` |
| Activity idempotency | `UNIQUE` on `ACTIVITY_LOG.(external_reference, partner_id)` |
| Ledger is permanent | No FK delete cascade; ledger rows never deleted |
| Corrections via adjustments | `POINT_LEDGER.transaction_type = 'ADJUSTMENT'` |
| Single annual vulnerable bonus | `UNIQUE` on `VULNERABLE_BONUS.(citizen_id, fiscal_year)` |
| No duplicate year-end processing | `UNIQUE` on `YEAR_END_PROCESSING.(citizen_id, fiscal_year)` |
| Rule soft-delete only | `SCORING_RULE.status = 'INACTIVE'` (never physically deleted) |
| Rule versioning | New row + `valid_from`/`valid_to` schema |
| Maker-Checker for rules/tiers | `created_by`, `approved_by`, `approval_status` fields |
| Export eligibility snapshot | `INCENTIVE_HISTORY.rank_name_at_export` captures rank at export time |
| Duplicate confirmations rejected | `UNIQUE` on `INCENTIVE_HISTORY.(citizen_id, batch_id, tax_type)` |
| PHI privacy (UC-13) | No medical diagnoses stored. Only `activity_code` + `external_reference` |

---

## 6. SQL Server – T-SQL Schema Snippet (Core Tables)

```sql
-- CITIZEN
CREATE TABLE dbo.CITIZEN (
    citizen_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    cccd_number         NVARCHAR(12)    NOT NULL,
    full_name           NVARCHAR(255)   NOT NULL,
    date_of_birth       DATE            NOT NULL,
    account_status      NVARCHAR(20)    NOT NULL CONSTRAINT DF_CITIZEN_STATUS DEFAULT 'ACTIVE',
    consent_granted     BIT             NOT NULL CONSTRAINT DF_CITIZEN_CONSENT DEFAULT 0,
    consent_granted_at  DATETIME2       NULL,
    is_disabled         BIT             NOT NULL CONSTRAINT DF_CITIZEN_DISABLED DEFAULT 0,
    is_ethnic_minority  BIT             NOT NULL CONSTRAINT DF_CITIZEN_ETHNIC DEFAULT 0,
    current_rank        NVARCHAR(20)    NOT NULL CONSTRAINT DF_CITIZEN_RANK DEFAULT 'UNRANKED',
    total_points        DECIMAL(10,2)   NOT NULL CONSTRAINT DF_CITIZEN_TOTAL DEFAULT 0,
    fiscal_year_points  DECIMAL(10,2)   NOT NULL CONSTRAINT DF_CITIZEN_FY DEFAULT 0,
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_CITIZEN_CA DEFAULT GETDATE(),
    updated_at          DATETIME2       NOT NULL CONSTRAINT DF_CITIZEN_UA DEFAULT GETDATE(),
    CONSTRAINT UX_CITIZEN_CCCD UNIQUE (cccd_number)
);

-- ACTIVITY_LOG (Ingestion)
CREATE TABLE dbo.ACTIVITY_LOG (
    log_id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id          BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    activity_code       NVARCHAR(50)    NOT NULL,
    source_system       NVARCHAR(50)    NOT NULL,
    external_reference  NVARCHAR(255)   NULL,
    partner_id          NVARCHAR(100)   NULL,
    activity_date       DATETIME2       NOT NULL,
    fiscal_year         INT             NOT NULL,
    ingestion_status    NVARCHAR(30)    NOT NULL,
    rejection_reason    NVARCHAR(500)   NULL,
    forwarded_to_engine BIT             NOT NULL CONSTRAINT DF_AL_FWD DEFAULT 0,
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_AL_CA DEFAULT GETDATE(),
    CONSTRAINT UX_ACTIVITY_EXT_REF UNIQUE (external_reference, partner_id)
);

-- POINT_LEDGER (Append-only)
CREATE TABLE dbo.POINT_LEDGER (
    ledger_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    citizen_id          BIGINT          NOT NULL REFERENCES dbo.CITIZEN(citizen_id),
    activity_log_id     BIGINT          NULL REFERENCES dbo.ACTIVITY_LOG(log_id),
    rule_id             BIGINT          NULL REFERENCES dbo.SCORING_RULE(rule_id),
    transaction_type    NVARCHAR(30)    NOT NULL,
    points_awarded      DECIMAL(10,2)   NOT NULL,
    points_original     DECIMAL(10,2)   NULL,
    fiscal_year         INT             NOT NULL,
    cap_applied         NVARCHAR(30)    NULL,
    rejection_reason    NVARCHAR(500)   NULL,
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_PL_CA DEFAULT GETDATE()
);

-- AUDIT_LOG (Append-only)
CREATE TABLE dbo.AUDIT_LOG (
    audit_id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    event_type          NVARCHAR(100)   NOT NULL,
    actor_type          NVARCHAR(20)    NOT NULL,
    actor_id            BIGINT          NULL,
    ip_address          NVARCHAR(50)    NULL,
    entity_type         NVARCHAR(50)    NULL,
    entity_id           BIGINT          NULL,
    description         NVARCHAR(2000)  NULL,
    event_data          NVARCHAR(MAX)   NULL,
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_AUDIT_CA DEFAULT GETDATE()
);
```

---

## 7. Spring Boot JPA Entity Mapping (MVC – Model Layer)

Each table maps to a JPA `@Entity` class. Example for key entities:

```java
// CITIZEN entity
@Entity
@Table(name = "CITIZEN")
public class Citizen {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long citizenId;

    @Column(nullable = false, unique = true, length = 12)
    private String cccdNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;  // ACTIVE, SUSPENDED, REVOKED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankType currentRank;          // ACTIVE, BASIC, UNRANKED

    private BigDecimal totalPoints;
    private BigDecimal fiscalYearPoints;
    // ... other fields, getters/setters (Lombok @Data)
}

// POINT_LEDGER entity
@Entity
@Table(name = "POINT_LEDGER")
public class PointLedger {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ledgerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // EARNED, BONUS_VULNERABLE, ADJUSTMENT, YEAR_END_CARRYOVER

    private BigDecimal pointsAwarded;
    private Integer fiscalYear;
    private LocalDateTime createdAt;
    // ...
}
```

**Repository (JPA):**
```java
@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    Optional<Citizen> findByCccdNumber(String cccdNumber);
}

@Repository
public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {
    List<PointLedger> findByCitizenAndFiscalYear(Citizen citizen, Integer fiscalYear);
    BigDecimal sumPointsAwardedByCitizenAndFiscalYear(Citizen citizen, Integer fiscalYear);
}
```

**Service (Business logic):**
```java
@Service
@Transactional
public class PointCalculationService {
    // UC-16: Calculate and Add Points
    public void calculateAndAddPoints(ActivityLog activityLog) { ... }
}

@Service
@Transactional
public class RankEvaluationService {
    // UC-17: Evaluate Citizen Rank
    public void evaluateRank(Citizen citizen) { ... }
}
```

**Controller (REST API):**
```java
@RestController
@RequestMapping("/api/v1/citizens")
public class CitizenController {
    // GET /api/v1/citizens/{cccd}/points  → UC-02
    // GET /api/v1/citizens/{cccd}/rank    → UC-02, UC-21
    // GET /api/v1/citizens/{cccd}/notifications → UC-03
    // POST /api/v1/citizens/{cccd}/feedback     → UC-04
}
```

---

## 8. Non-Functional Support in DB Design

| NF Requirement | Design Decision |
|----------------|-----------------|
| NF-05 AES-256 at rest | SQL Server Transparent Data Encryption (TDE) enabled |
| NF-05 TLS 1.3 in transit | Spring Boot mssql-jdbc connection string: `encrypt=true;trustServerCertificate=false` |
| NF-07 Audit trail (5 years) | `AUDIT_LOG` append-only; DB-level permission: no DELETE/UPDATE on table |
| NF-03 10,000 records/s | Indexes on `citizen_id`, `external_reference`; async ingestion queue |
| NF-09 Rule propagation <30s | Rules loaded from DB; cache invalidated on approval |

---

## 9. Naming Conventions (MS SQL Server)

| Object | Convention | Example |
|--------|-----------|---------|
| Tables | `UPPER_SNAKE_CASE` | `CITIZEN`, `POINT_LEDGER` |
| Columns | `lower_snake_case` | `citizen_id`, `total_points` |
| Primary Keys | `PK_<TableName>` | `PK_CITIZEN` |
| Foreign Keys | `FK_<Table>_<Ref>` | `FK_LEDGER_CITIZEN` |
| Unique Constraints | `UX_<Table>_<Col>` | `UX_CITIZEN_CCCD` |
| Indexes | `IX_<Table>_<Col>` | `IX_ACTIVITY_LOG_CITIZEN` |
| Default Constraints | `DF_<Table>_<Col>` | `DF_CITIZEN_STATUS` |
| Java Entities | `PascalCase` | `Citizen`, `PointLedger` |
| Java fields | `camelCase` | `citizenId`, `totalPoints` |

---

## 10. References

- RDS Document: `SWD392_VNeIDCivicPoint_RDS.md`
- Use Cases: UC-01 to UC-22 (see RDS Section II.5.2)
- Spring Boot: `pom.xml` – `spring-boot-starter-data-jpa`, `mssql-jdbc`, Lombok
- Java version: 17
