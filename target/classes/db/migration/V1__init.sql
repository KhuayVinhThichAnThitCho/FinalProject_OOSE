-- Schema init for bookstore project
-- MySQL 8+

CREATE TABLE khach_hang (
  ma_khach_hang BIGINT PRIMARY KEY AUTO_INCREMENT,
  ten_khach_hang VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  so_dien_thoai VARCHAR(50) NOT NULL,
  dia_chi VARCHAR(500) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE nhan_vien (
  ma_nhan_vien BIGINT PRIMARY KEY AUTO_INCREMENT,
  ten_nhan_vien VARCHAR(255) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE sach (
  ma_sach BIGINT PRIMARY KEY AUTO_INCREMENT,
  ten_sach VARCHAR(255) NOT NULL,
  gia_ban BIGINT NOT NULL,
  gia_nhap BIGINT NOT NULL,
  so_luong_ton INT NOT NULL,
  version BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE phuong_thuc_thanh_toan (
  ma_phuong_thuc BIGINT PRIMARY KEY AUTO_INCREMENT,
  ten_phuong_thuc VARCHAR(255) NOT NULL UNIQUE,
  mo_ta VARCHAR(1000) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE don_hang (
  ma_don_hang BIGINT PRIMARY KEY AUTO_INCREMENT,
  ma_khach_hang BIGINT NOT NULL,
  ma_nhan_vien_xu_ly BIGINT NULL,
  ngay_dat DATETIME(6) NOT NULL,
  tong_tien BIGINT NOT NULL,
  trang_thai VARCHAR(50) NOT NULL,
  version BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_don_hang_khach_hang FOREIGN KEY (ma_khach_hang) REFERENCES khach_hang(ma_khach_hang),
  CONSTRAINT fk_don_hang_nhan_vien FOREIGN KEY (ma_nhan_vien_xu_ly) REFERENCES nhan_vien(ma_nhan_vien)
);

CREATE INDEX idx_don_hang_khach_hang_ngay_dat ON don_hang(ma_khach_hang, ngay_dat);
CREATE INDEX idx_don_hang_trang_thai ON don_hang(trang_thai);

CREATE TABLE chi_tiet_don_hang (
  ma_chi_tiet BIGINT PRIMARY KEY AUTO_INCREMENT,
  ma_don_hang BIGINT NOT NULL,
  ma_sach BIGINT NOT NULL,
  so_luong INT NOT NULL,
  gia BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_ctdh_don_hang FOREIGN KEY (ma_don_hang) REFERENCES don_hang(ma_don_hang),
  CONSTRAINT fk_ctdh_sach FOREIGN KEY (ma_sach) REFERENCES sach(ma_sach)
);

CREATE INDEX idx_ctdh_don_hang ON chi_tiet_don_hang(ma_don_hang);
CREATE INDEX idx_ctdh_sach ON chi_tiet_don_hang(ma_sach);

CREATE TABLE thong_tin_giao_hang (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ma_don_hang BIGINT NOT NULL UNIQUE,
  dia_chi_giao_hang VARCHAR(500) NOT NULL,
  nguoi_nhan VARCHAR(255) NOT NULL,
  so_dien_thoai_nhan VARCHAR(50) NOT NULL,
  trang_thai_giao_hang VARCHAR(100) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_ttg_hang_don_hang FOREIGN KEY (ma_don_hang) REFERENCES don_hang(ma_don_hang)
);

CREATE TABLE yeu_cau_huy_don (
  ma_yeu_cau BIGINT PRIMARY KEY AUTO_INCREMENT,
  ma_don_hang BIGINT NOT NULL UNIQUE,
  ly_do_huy VARCHAR(1000) NOT NULL,
  ngay_yeu_cau DATETIME(6) NOT NULL,
  trang_thai_yeu_cau VARCHAR(50) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_ychd_don_hang FOREIGN KEY (ma_don_hang) REFERENCES don_hang(ma_don_hang)
);

CREATE INDEX idx_ychd_trang_thai ON yeu_cau_huy_don(trang_thai_yeu_cau);

CREATE TABLE thanh_toan (
  ma_thanh_toan BIGINT PRIMARY KEY AUTO_INCREMENT,
  ma_don_hang BIGINT NOT NULL,
  ma_phuong_thuc BIGINT NOT NULL,
  so_tien BIGINT NOT NULL,
  trang_thai VARCHAR(50) NOT NULL,
  ngay_thanh_toan DATETIME(6) NOT NULL,
  ma_giao_dich_doi_tac VARCHAR(255) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_thanh_toan_don_hang FOREIGN KEY (ma_don_hang) REFERENCES don_hang(ma_don_hang),
  CONSTRAINT fk_thanh_toan_phuong_thuc FOREIGN KEY (ma_phuong_thuc) REFERENCES phuong_thuc_thanh_toan(ma_phuong_thuc)
);

CREATE INDEX idx_thanh_toan_don_hang ON thanh_toan(ma_don_hang);
CREATE INDEX idx_thanh_toan_ma_gd ON thanh_toan(ma_giao_dich_doi_tac);
CREATE INDEX idx_thanh_toan_trang_thai ON thanh_toan(trang_thai);

CREATE TABLE user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  khach_hang_id BIGINT NULL,
  nhan_vien_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_user_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(ma_khach_hang),
  CONSTRAINT fk_user_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(ma_nhan_vien)
);

CREATE TABLE user_role (
  user_id BIGINT NOT NULL,
  role VARCHAR(100) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user_account(id)
);

