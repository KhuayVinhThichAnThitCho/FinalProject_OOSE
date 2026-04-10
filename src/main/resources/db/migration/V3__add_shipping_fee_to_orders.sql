SET @shipping_fee_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'shipping_fee'
);

SET @shipping_fee_ddl := IF(
  @shipping_fee_exists = 0,
  'ALTER TABLE orders ADD COLUMN shipping_fee BIGINT NOT NULL DEFAULT 0 AFTER total_amount',
  'SELECT 1'
);

PREPARE stmt FROM @shipping_fee_ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

