ALTER TABLE orders
  ADD COLUMN shipping_fee BIGINT NOT NULL DEFAULT 0 AFTER total_amount;

