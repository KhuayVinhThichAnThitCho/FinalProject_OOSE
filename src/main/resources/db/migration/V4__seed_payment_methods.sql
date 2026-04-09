-- Add more payment methods for demo/mock payment gateway

INSERT INTO payment_methods (code, description, created_at, created_by)
VALUES
  ('MOMO', 'MOMO e-wallet (mock)', NOW(6), 'seed'),
  ('VNPAY', 'VNPAY gateway (mock)', NOW(6), 'seed'),
  ('ZALOPAY', 'ZaloPay e-wallet (mock)', NOW(6), 'seed'),
  ('BANK_TRANSFER', 'Bank transfer (mock)', NOW(6), 'seed');

