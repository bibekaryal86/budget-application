-- Enable UUID extension (safe to run multiple times)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. Category Types
-- ============================================
CREATE TABLE category_type (
                               id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               name        VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- 2. Categories
-- ============================================
CREATE TABLE category (
                          id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          category_type_id UUID NOT NULL REFERENCES category_type(id) ON DELETE RESTRICT,
                          name             VARCHAR(100) NOT NULL,
                          UNIQUE (category_type_id, name)
);

-- ============================================
-- 3. Account
-- ============================================

CREATE TABLE account (
                         id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name        VARCHAR(100) NOT NULL UNIQUE,
                         account_type VARCHAR(10) NOT NULL CHECK (account_type IN ('CASH', 'CREDIT', 'LOAN', 'CHECKING', 'SAVINGS', 'INVESTMENT', 'OTHER')),
                         bank_name   VARCHAR(100) NOT NULL,
                         opening_balance NUMERIC(12, 2) NOT NULL CHECK (opening_balance >= 0),
                         status      VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
                         created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- 4. Transactions (Income + Expense)
-- ============================================
CREATE TABLE transaction (
                             id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             txn_date        TIMESTAMP NOT NULL,
                             merchant        VARCHAR(255) NOT NULL,
                             total_amount    NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
                             created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- 5. Transaction Items (Line Items)
-- ============================================
CREATE TABLE transaction_item (
                                  id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  transaction_id  UUID NOT NULL REFERENCES transaction(id) ON DELETE CASCADE,
                                  category_id     UUID NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
                                  account_id      UUID NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
                                  amount          NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
                                  tags            TEXT[] NOT NULL DEFAULT '{}',
                                  notes           VARCHAR(1024)
);

-- ============================================
-- 5. Budget
-- ============================================
CREATE TABLE budget
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id     UUID NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    budget_month INTEGER        NOT NULL CHECK (budget_month >= 1 AND budget_month <= 12),
    budget_year  INTEGER        NOT NULL CHECK (budget_year >= 1900),
    amount       DECIMAL(12, 2) NOT NULL CHECK (amount >= 0),
    notes         VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT idx_unique_c_m_y UNIQUE (category_id, budget_month, budget_year)
);

CREATE INDEX idx_budgets_category_id ON budget (category_id);
CREATE INDEX idx_budgets_month_year ON budget (budget_year, budget_month);

-- Function 1: Main budget vs actual with filtering
CREATE OR REPLACE FUNCTION get_budget_vs_actuals(
    p_month INTEGER,
    p_year INTEGER,
    p_show_all_categories BOOLEAN DEFAULT FALSE,
    p_category_type_ids UUID[] DEFAULT NULL
)
RETURNS TABLE(
    category_id UUID,
    category_name VARCHAR,
    category_type_id UUID,
    category_type_name VARCHAR,
    budget_amount DECIMAL(12,2),
    actual_amount DECIMAL(12,2),
    budget_status VARCHAR(20),
    variance DECIMAL(12,2),
    percentage_used DECIMAL(5,2)
)
LANGUAGE plpgsql
STABLE
PARALLEL SAFE
AS $$
BEGIN
RETURN QUERY
    WITH monthly_actuals AS (
        SELECT
            ti.category_id,
            COALESCE(SUM(ti.amount), 0) as actual_total
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transaction_id = t.id
        WHERE EXTRACT(MONTH FROM t.txn_date) = p_month
          AND EXTRACT(YEAR FROM t.txn_date) = p_year
        GROUP BY ti.category_id
    ),
    monthly_budgets AS (
        SELECT
            category_id,
            amount as budget_amount
        FROM budget
        WHERE budget_month = p_month
          AND budget_year = p_year
    )
SELECT
    c.id,
    c.name,
    ct.id,
    ct.name,
    COALESCE(b.budget_amount, 0.00),
    COALESCE(a.actual_total, 0.00),
    CASE
        WHEN b.budget_amount IS NULL THEN 'NOT_SET'
        WHEN COALESCE(a.actual_total, 0) = 0 THEN 'NO_SPENDING'
        WHEN a.actual_total <= b.budget_amount THEN 'UNDER_BUDGET'
        ELSE 'OVER_BUDGET'
        END,
    CASE
        WHEN b.budget_amount IS NULL THEN NULL
        ELSE COALESCE(a.actual_total, 0.00) - b.budget_amount
        END,
    CASE
        WHEN b.budget_amount IS NULL OR b.budget_amount = 0 THEN NULL
        ELSE ROUND((COALESCE(a.actual_total, 0.00) / b.budget_amount) * 100, 2)
        END
FROM categories c
         INNER JOIN category_types ct ON c.category_type_id = ct.id
         LEFT JOIN monthly_budgets b ON c.id = b.category_id
         LEFT JOIN monthly_actuals a ON c.id = a.category_id
WHERE (p_show_all_categories OR
       (COALESCE(b.budget_amount, 0) != 0 OR
            COALESCE(a.actual_total, 0) != 0))
  AND (p_category_type_ids IS NULL OR ct.id = ANY(p_category_type_ids))
ORDER BY
    CASE
        WHEN b.budget_amount IS NOT NULL AND a.actual_total > b.budget_amount THEN 1
        WHEN b.budget_amount IS NOT NULL AND a.actual_total > 0 THEN 2
        WHEN b.budget_amount IS NOT NULL THEN 3
        WHEN a.actual_total > 0 THEN 4
        ELSE 5
        END,
    ABS(COALESCE(a.actual_total, 0.00) - COALESCE(b.budget_amount, 0.00)) DESC NULLS LAST,
    ct.name,
    c.name;
END;
$$;

-- Function 2: Budget summary
CREATE OR REPLACE FUNCTION get_budget_summary(
    p_month INTEGER,
    p_year INTEGER,
    p_category_type_ids UUID[] DEFAULT NULL
)
RETURNS TABLE(
    total_budget DECIMAL(12,2),
    total_actual DECIMAL(12,2),
    total_variance DECIMAL(12,2),
    categories_with_budget INTEGER,
    categories_over_budget INTEGER,
    categories_under_budget INTEGER,
    categories_no_budget INTEGER,
    categories_no_spending INTEGER
)
LANGUAGE plpgsql
STABLE
PARALLEL SAFE
AS $$
BEGIN
RETURN QUERY
SELECT
    COALESCE(SUM(budget_amount), 0.00),
    COALESCE(SUM(actual_amount), 0.00),
    COALESCE(SUM(CASE WHEN budget_status != 'NOT_SET' THEN variance END), 0.00),
    COUNT(CASE WHEN budget_amount > 0 THEN 1 END),
    COUNT(CASE WHEN budget_status = 'OVER_BUDGET' THEN 1 END),
    COUNT(CASE WHEN budget_status = 'UNDER_BUDGET' THEN 1 END),
    COUNT(CASE WHEN budget_status = 'NOT_SET' AND actual_amount > 0 THEN 1 END),
    COUNT(CASE WHEN budget_status IN ('NOT_SET', 'UNDER_BUDGET', 'OVER_BUDGET')
        AND actual_amount = 0 AND budget_amount > 0 THEN 1 END)
FROM get_budget_vs_actuals(p_month, p_year, TRUE, p_category_type_ids);
END;
$$;

-- Function 3: Budget extremes
CREATE OR REPLACE FUNCTION get_budget_extremes(
    p_month INTEGER,
    p_year INTEGER,
    p_limit INTEGER DEFAULT 5,
    p_category_type_ids UUID[] DEFAULT NULL,
    p_show_over_budget BOOLEAN DEFAULT TRUE
)
RETURNS TABLE(
    category_name VARCHAR,
    category_type_name VARCHAR,
    budget_amount DECIMAL(12,2),
    actual_amount DECIMAL(12,2),
    variance DECIMAL(12,2),
    percentage_used DECIMAL(5,2),
    status VARCHAR(20),
    rank_position INTEGER
)
LANGUAGE plpgsql
STABLE
PARALLEL SAFE
AS $$
BEGIN
    IF p_show_over_budget THEN
        RETURN QUERY
SELECT
    bva.category_name,
    bva.category_type_name,
    bva.budget_amount,
    bva.actual_amount,
    bva.variance,
    bva.percentage_used,
    bva.budget_status,
    ROW_NUMBER() OVER (ORDER BY bva.variance ASC) as rank_position
FROM get_budget_vs_actuals(p_month, p_year, FALSE, p_category_type_ids) bva
WHERE bva.budget_status = 'OVER_BUDGET'
ORDER BY bva.variance ASC
    LIMIT p_limit;
ELSE
        RETURN QUERY
SELECT
    bva.category_name,
    bva.category_type_name,
    bva.budget_amount,
    bva.actual_amount,
    bva.variance,
    bva.percentage_used,
    bva.budget_status,
    ROW_NUMBER() OVER (ORDER BY bva.variance DESC) as rank_position
FROM get_budget_vs_actuals(p_month, p_year, FALSE, p_category_type_ids) bva
WHERE bva.budget_status = 'UNDER_BUDGET'
  AND bva.actual_amount > 0
ORDER BY bva.variance DESC
    LIMIT p_limit;
END IF;
END;
$$;
