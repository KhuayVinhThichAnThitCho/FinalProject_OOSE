-- Schema init for bookstore project
-- MySQL 8+

CREATE TABLE customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(50) NOT NULL,
  address VARCHAR(500) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE staff (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(255) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  sale_price BIGINT NOT NULL,
  sale_price_effective_from DATETIME(6) NULL,
  cost_price BIGINT NOT NULL,
  category VARCHAR(255) NULL,
  stock_quantity INT NOT NULL,
  version BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE payment_methods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(1000) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  processed_by_staff_id BIGINT NULL,
  ordered_at DATETIME(6) NOT NULL,
  total_amount BIGINT NOT NULL,
  shipping_fee BIGINT NOT NULL DEFAULT 0,
  status VARCHAR(50) NOT NULL,
  version BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_orders_customers FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_orders_staff FOREIGN KEY (processed_by_staff_id) REFERENCES staff(id)
);

CREATE INDEX idx_orders_customer_ordered_at ON orders(customer_id, ordered_at);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_book ON order_items(book_id);

CREATE TABLE shipping_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  address VARCHAR(500) NOT NULL,
  receiver_name VARCHAR(255) NOT NULL,
  receiver_phone VARCHAR(50) NOT NULL,
  shipping_status VARCHAR(100) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_shipping_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE cancellation_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  reason VARCHAR(1000) NOT NULL,
  requested_at DATETIME(6) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_cancel_req_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_cancel_req_status ON cancellation_requests(status);

CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  payment_method_id BIGINT NOT NULL,
  amount BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  paid_at DATETIME(6) NOT NULL,
  partner_transaction_id VARCHAR(255) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_payments_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_partner_tx ON payments(partner_transaction_id);
CREATE INDEX idx_payments_status ON payments(status);

CREATE TABLE user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  customer_id BIGINT NULL,
  staff_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_user_staff FOREIGN KEY (staff_id) REFERENCES staff(id)
);

CREATE TABLE user_role (
  user_id BIGINT NOT NULL,
  role VARCHAR(100) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user_account(id)
);

