-- Enable UUID extension (safe to run multiple times)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. Category Types
-- ============================================
CREATE TABLE category_type (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL UNIQUE
);

CREATE INDEX idx_category_type_name ON category_type(name);

-- ============================================
-- 2. Categories
-- ============================================
CREATE TABLE category (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_type_id UUID NOT NULL REFERENCES category_type(id) ON DELETE RESTRICT,
    name             VARCHAR(100) NOT NULL,
    UNIQUE (category_type_id, name)
);

CREATE INDEX idx_category_category_type_id ON category(category_type_id);
CREATE INDEX idx_category_name ON category(name);

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

CREATE INDEX idx_account_type ON account(account_type);
CREATE INDEX idx_account_name ON account(name);

-- ============================================
-- 4. Transactions (Income + Expense)
-- ============================================
CREATE TABLE transaction (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    txn_date        TIMESTAMP NOT NULL,
    merchant        VARCHAR(255) NOT NULL,
    account_id      UUID NOT NULL REFERENCES account(id) ON DELETE RESTRICT,
    total_amount    NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transaction_date ON transaction(txn_date);
CREATE INDEX idx_transaction_merchant ON transaction(merchant);
CREATE INDEX idx_transaction_account ON transaction(account_id);

-- ============================================
-- 5. Transaction Items (Line Items)
-- ============================================
CREATE TABLE transaction_item (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id  UUID NOT NULL REFERENCES transaction(id) ON DELETE CASCADE,
    category_id     UUID NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
    label           VARCHAR(255) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    exp_type        VARCHAR(10) NOT NULL CHECK (exp_type IN ('NEEDS', 'WANTS', ''))
);

CREATE INDEX idx_transaction_item_transaction_id ON transaction_item(transaction_id);
CREATE INDEX idx_transaction_item_category_id ON transaction_item(category_id);
CREATE INDEX idx_transaction_item_label ON transaction_item(label);
