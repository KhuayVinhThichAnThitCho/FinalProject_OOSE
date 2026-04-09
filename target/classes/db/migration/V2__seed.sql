-- Seed data for demo/dev

INSERT INTO phuong_thuc_thanh_toan (ten_phuong_thuc, mo_ta, created_at, created_by)
VALUES
  ('ONLINE', 'Thanh toan truc tuyen qua cong thanh toan', NOW(6), 'seed'),
  ('COD', 'Thanh toan khi nhan hang', NOW(6), 'seed');

INSERT INTO sach (ten_sach, gia_ban, gia_nhap, so_luong_ton, version, created_at, created_by)
VALUES
  ('Clean Code', 150000, 100000, 50, 0, NOW(6), 'seed'),
  ('Refactoring', 200000, 140000, 30, 0, NOW(6), 'seed'),
  ('Design Patterns', 250000, 170000, 20, 0, NOW(6), 'seed');

INSERT INTO khach_hang (ten_khach_hang, email, so_dien_thoai, dia_chi, created_at, created_by)
VALUES
  ('Demo Customer', 'customer@example.com', '0900000000', 'Ho Chi Minh', NOW(6), 'seed');

INSERT INTO nhan_vien (ten_nhan_vien, created_at, created_by)
VALUES
  ('Demo Staff', NOW(6), 'seed'),
  ('Demo Manager', NOW(6), 'seed');

-- password is 'password' (BCrypt)
INSERT INTO user_account (username, password_hash, khach_hang_id, nhan_vien_id, created_at, created_by)
VALUES
  ('customer', '$2a$10$Qm5WvJw5r5fZKJ5mA9o1He0n6c9xT6pGqvW3b5J8ZqV7gQGZ0e1yK', 1, NULL, NOW(6), 'seed'),
  ('staff',    '$2a$10$Qm5WvJw5r5fZKJ5mA9o1He0n6c9xT6pGqvW3b5J8ZqV7gQGZ0e1yK', NULL, 1, NOW(6), 'seed'),
  ('manager',  '$2a$10$Qm5WvJw5r5fZKJ5mA9o1He0n6c9xT6pGqvW3b5J8ZqV7gQGZ0e1yK', NULL, 2, NOW(6), 'seed');

INSERT INTO user_role (user_id, role)
VALUES
  (1, 'CUSTOMER'),
  (2, 'STAFF'),
  (3, 'MANAGER');

