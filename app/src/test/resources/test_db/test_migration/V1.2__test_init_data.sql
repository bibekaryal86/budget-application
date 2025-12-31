INSERT INTO category_type (id, name) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST CATEGORY TYPE');
INSERT INTO category (id, category_type_id, name) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', '5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST CATEGORY');
INSERT INTO account (id, name, account_type, bank_name, opening_balance, status) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST ACCOUNT', 'CREDIT', 'TEST BANK', 100.00, 'ACTIVE');
INSERT INTO transaction (id, txn_date, merchant, account_id, total_amount) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', NOW(), 'TEST MERCHANT', '5b15fdaf-758b-4c4f-97d1-2405b716867a', 101.01);
INSERT INTO transaction_item (id, transaction_id, category_id, label, amount, exp_type) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a','5b15fdaf-758b-4c4f-97d1-2405b716867a', '5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST LABEL', 101.01, 'NEEDS');
