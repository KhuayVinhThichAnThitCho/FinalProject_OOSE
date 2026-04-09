-- Seed data for demo/dev

INSERT INTO payment_methods (code, description, created_at, created_by)
VALUES
  ('ONLINE', 'Online payment via payment gateway', NOW(6), 'seed'),
  ('COD', 'Cash on delivery', NOW(6), 'seed');

INSERT INTO books (title, sale_price, sale_price_effective_from, cost_price, category, stock_quantity, version, created_at, created_by)
VALUES
  ('Clean Code', 150000, NOW(6), 100000, 'Software', 50, 0, NOW(6), 'seed'),
  ('Refactoring', 200000, NOW(6), 140000, 'Software', 30, 0, NOW(6), 'seed'),
  ('Design Patterns', 250000, NOW(6), 170000, 'Software', 20, 0, NOW(6), 'seed');

INSERT INTO customers (full_name, email, phone, address, created_at, created_by)
VALUES
  ('Demo Customer', 'customer@example.com', '0900000000', 'Ho Chi Minh', NOW(6), 'seed');

INSERT INTO staff (full_name, created_at, created_by)
VALUES
  ('Demo Staff', NOW(6), 'seed'),
  ('Demo Manager', NOW(6), 'seed');

-- password is 'password' (BCrypt)
INSERT INTO user_account (username, password_hash, customer_id, staff_id, created_at, created_by)
VALUES
  ('customer', '{noop}password', 1, NULL, NOW(6), 'seed'),
  ('staff',    '{noop}password', NULL, 1, NOW(6), 'seed'),
  ('manager',  '{noop}password', NULL, 2, NOW(6), 'seed');

INSERT INTO user_role (user_id, role)
VALUES
  (1, 'CUSTOMER'),
  (2, 'STAFF'),
  (3, 'MANAGER');

