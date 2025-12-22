-- Enable UUID extension (safe to run multiple times)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. Category Types
-- ============================================
CREATE TABLE category_type (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_category_type_name ON category_type(name);

-- ============================================
-- 2. Categories
-- ============================================
CREATE TABLE category (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_type_id UUID NOT NULL REFERENCES category_type(id) ON DELETE RESTRICT,
    name             VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (category_type_id, name)
);

CREATE INDEX idx_category_category_type_id ON category(category_type_id);
CREATE INDEX idx_category_name ON category(name);

-- ============================================
-- 3. Transactions (Income + Expense)
-- ============================================
CREATE TABLE transaction (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    txn_date        TIMESTAMP NOT NULL,
    description     VARCHAR(255),
    total_amount    NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transaction_date ON transaction(date);
CREATE INDEX idx_transaction_description ON transaction(description);

-- ============================================
-- 4. Transaction Items (Line Items)
-- ============================================
CREATE TABLE transaction_item (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id  UUID NOT NULL REFERENCES transaction(id) ON DELETE CASCADE,
    category_id     UUID NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
    label           VARCHAR(255) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount >= 0)
);

CREATE INDEX idx_transaction_item_transaction_id ON transaction_item(transaction_id);
CREATE INDEX idx_transaction_item_category_id ON transaction_item(category_id);
CREATE INDEX idx_transaction_item_label ON transaction_item(label);
