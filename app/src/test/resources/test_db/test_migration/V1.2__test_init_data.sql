INSERT INTO category_type (id, name) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST CATEGORY TYPE');
INSERT INTO category (id, category_type_id, name) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', '5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST CATEGORY');
INSERT INTO transaction (id, txn_date, merchant, total_amount) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', NOW(), 'TEST MERCHANT', 101.01);
INSERT INTO transaction_item (id, transaction_id, category_id, label, amount) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a','5b15fdaf-758b-4c4f-97d1-2405b716867a', '5b15fdaf-758b-4c4f-97d1-2405b716867a', 'TEST LABEL', 101.01);
