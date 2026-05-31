-- 更新饲料库存为中文
BEGIN TRANSACTION;

-- 删除旧的英文库存
DELETE FROM feed_stock;

-- 插入中文库存
INSERT INTO feed_stock (feed_type, stock_amount, unit_price, supplier, last_restock_time, create_time, update_time) VALUES
('混合饲料', 500.0, 12.5, '海大饲料有限公司', datetime('now'), datetime('now'), datetime('now')),
('鱼粉饲料', 200.0, 18.0, '通威饲料', datetime('now'), datetime('now'), datetime('now')),
('颗粒饲料A', 300.0, 15.0, '粤海饲料', datetime('now'), datetime('now'), datetime('now')),
('颗粒饲料B', 250.0, 13.5, '恒兴饲料', datetime('now'), datetime('now'), datetime('now'));

COMMIT;
