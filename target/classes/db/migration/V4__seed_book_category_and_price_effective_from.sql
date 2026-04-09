-- Set category & price effective time for seeded books

UPDATE sach
SET danh_muc = COALESCE(danh_muc, 'Software'),
    gia_ban_ap_dung_tu = COALESCE(gia_ban_ap_dung_tu, NOW(6))
WHERE danh_muc IS NULL OR gia_ban_ap_dung_tu IS NULL;

