INSERT INTO category_type (id, name) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', 'Test Category Type');
INSERT INTO category (id, category_type_id, name) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', '5b15fdaf-758b-4c4f-97d1-2405b716867a', 'Test Category');
INSERT INTO transaction (id, txn_date, merchant, total_amount) VALUES ('5b15fdaf-758b-4c4f-97d1-2405b716867a', NOW(), 'Test Merchant', 101.01);
