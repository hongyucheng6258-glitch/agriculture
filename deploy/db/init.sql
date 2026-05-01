-- ============================================================
-- 海洋牧场管理系统 - SQLite 数据库初始化脚本
-- ============================================================

-- ============================================================
-- 1. 笼位表
-- ============================================================
CREATE TABLE IF NOT EXISTS cage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cage_code TEXT NOT NULL UNIQUE,
    location TEXT NOT NULL,
    breed_type TEXT NOT NULL,
    scale REAL,
    status TEXT DEFAULT '使用中',
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 2. 水质监测表
-- ============================================================
CREATE TABLE IF NOT EXISTS water_quality (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cage_id INTEGER NOT NULL,
    water_temp REAL NOT NULL,
    salinity REAL NOT NULL,
    dissolved_oxygen REAL NOT NULL,
    ph REAL NOT NULL,
    record_time TEXT NOT NULL,
    data_source TEXT DEFAULT '手动录入',
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 3. 天气记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS weather (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    temperature REAL NOT NULL,
    wind_speed REAL NOT NULL,
    weather_desc TEXT,
    record_time TEXT NOT NULL,
    data_source TEXT DEFAULT '手动录入',
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 4. 投喂记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS feeding (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cage_id INTEGER NOT NULL,
    feed_type TEXT NOT NULL,
    feed_amount REAL NOT NULL,
    feeding_time TEXT NOT NULL,
    operator TEXT,
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 5. 疾病记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS disease (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cage_id INTEGER NOT NULL,
    disease_name TEXT NOT NULL,
    symptom TEXT NOT NULL,
    severity TEXT DEFAULT '轻度',
    treatment TEXT,
    handler TEXT,
    discover_time TEXT NOT NULL,
    handle_time TEXT,
    status TEXT DEFAULT '处理中',
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 6. 员工表
-- ============================================================
CREATE TABLE IF NOT EXISTS staff (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT,
    position TEXT NOT NULL,
    responsible_cage TEXT,
    entry_date TEXT,
    status TEXT DEFAULT '在职',
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 7. 预警阈值表
-- ============================================================
CREATE TABLE IF NOT EXISTS alert_threshold (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    indicator_name TEXT NOT NULL UNIQUE,
    indicator_label TEXT NOT NULL,
    min_value REAL,
    max_value REAL,
    unit TEXT,
    is_enabled INTEGER DEFAULT 1,
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 8. 预警记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS alert (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cage_id INTEGER,
    indicator_name TEXT NOT NULL,
    indicator_label TEXT NOT NULL,
    current_value REAL NOT NULL,
    threshold_value REAL NOT NULL,
    alert_type TEXT NOT NULL,
    alert_level TEXT DEFAULT '一般',
    is_handled INTEGER DEFAULT 0,
    handler TEXT,
    handle_time TEXT,
    handle_remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 9. 溯源表
-- ============================================================
CREATE TABLE IF NOT EXISTS trace (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trace_code TEXT NOT NULL UNIQUE,
    cage_id INTEGER NOT NULL,
    batch_no TEXT NOT NULL,
    seed_purchase_time TEXT,
    seed_spec TEXT,
    seed_source TEXT,
    feeding_summary TEXT,
    disease_summary TEXT,
    harvest_time TEXT,
    process_standard TEXT,
    product_quality TEXT,
    status TEXT DEFAULT '待审核',
    audit_user TEXT,
    audit_time TEXT,
    consumer_name TEXT,
    consumer_phone TEXT,
    consumer_address TEXT,
    sale_time TEXT,
    sale_quantity REAL,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 10. 饲料库存表
-- ============================================================
CREATE TABLE IF NOT EXISTS feed_stock (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    feed_type TEXT NOT NULL UNIQUE,
    stock_amount REAL NOT NULL DEFAULT 0,
    unit_price REAL,
    supplier TEXT,
    last_restock_time TEXT,
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 11. 系统用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    real_name TEXT NOT NULL,
    role TEXT DEFAULT 'operator',
    is_enabled INTEGER DEFAULT 1,
    last_login_time TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    is_deleted INTEGER DEFAULT 0
);

-- ============================================================
-- 初始数据：预警阈值
-- ============================================================
INSERT OR IGNORE INTO alert_threshold (indicator_name, indicator_label, min_value, max_value, unit, create_time, update_time) VALUES
('dissolved_oxygen', '溶解氧', 5.0, 10.0, 'mg/L', datetime('now'), datetime('now')),
('ph', 'pH值', 6.5, 8.5, '', datetime('now'), datetime('now')),
('water_temp', '水温', 10.0, 30.0, '℃', datetime('now'), datetime('now')),
('feed_stock', '饲料库存', 50.0, NULL, 'kg', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：默认用户
-- ============================================================
INSERT OR IGNORE INTO sys_user (username, password, real_name, role, create_time, update_time) VALUES
('admin', '666666', '系统管理员', 'admin', datetime('now'), datetime('now')),
('operator', '666666', '张操作员', 'operator', datetime('now'), datetime('now')),
('user1', '666666', '李用户', 'operator', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：网箱
-- ============================================================
INSERT OR IGNORE INTO cage (cage_code, location, breed_type, scale, status, create_time, update_time) VALUES
('CAGE-0001', '东南养殖区A区', '鲈鱼', 5000, '使用中', datetime('now'), datetime('now')),
('CAGE-0002', '东南养殖区B区', '石斑鱼', 3000, '使用中', datetime('now'), datetime('now')),
('CAGE-0003', '西北养殖区', '大黄鱼', 4500, '使用中', datetime('now'), datetime('now')),
('CAGE-0004', '东北养殖区', '真鲷', 2800, '空闲', datetime('now'), datetime('now')),
('CAGE-0005', '西南养殖区', '黑鲷', 3200, '使用中', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：人员
-- ============================================================
INSERT OR IGNORE INTO staff (name, phone, position, responsible_cage, entry_date, status, create_time, update_time) VALUES
('张三', '13812345678', '饲养员', 'CAGE-0001,CAGE-0002', '2024-01-15', '在职', datetime('now'), datetime('now')),
('李四', '13987654321', '技术员', 'CAGE-0003', '2024-02-20', '在职', datetime('now'), datetime('now')),
('王五', '13611223344', '饲养员', 'CAGE-0004,CAGE-0005', '2024-03-10', '在职', datetime('now'), datetime('now')),
('赵六', '13755667788', '管理员', '', '2023-12-01', '在职', datetime('now'), datetime('now')),
('钱七', '15899887766', '饲养员', '', '2024-04-01', '离职', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：饲料库存
-- ============================================================
INSERT OR IGNORE INTO feed_stock (feed_type, stock_amount, unit_price, supplier, last_restock_time, create_time, update_time) VALUES
('混合饲料', 500.0, 12.5, '海大饲料有限公司', datetime('now'), datetime('now'), datetime('now')),
('鱼粉饲料', 200.0, 18.0, '通威饲料', datetime('now'), datetime('now'), datetime('now')),
('颗粒饲料A', 300.0, 15.0, '粤海饲料', datetime('now'), datetime('now'), datetime('now')),
('颗粒饲料B', 250.0, 13.5, '恒兴饲料', datetime('now'), datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：水质数据（示例）
-- ============================================================
INSERT OR IGNORE INTO water_quality (cage_id, water_temp, salinity, dissolved_oxygen, ph, record_time, data_source, create_time, update_time) VALUES
(1, 23.5, 28.0, 7.2, 7.8, datetime('now', '-1 hour'), '自动采集', datetime('now'), datetime('now')),
(2, 24.0, 29.5, 6.8, 7.6, datetime('now', '-2 hour'), '自动采集', datetime('now'), datetime('now')),
(3, 22.8, 27.0, 7.5, 8.0, datetime('now', '-3 hour'), '自动采集', datetime('now'), datetime('now')),
(5, 23.0, 28.5, 7.0, 7.9, datetime('now', '-4 hour'), '自动采集', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：天气数据
-- ============================================================
INSERT OR IGNORE INTO weather (temperature, wind_speed, weather_desc, record_time, data_source, create_time, update_time) VALUES
(25.5, 3.2, '晴', datetime('now', '-1 hour'), '气象站', datetime('now'), datetime('now')),
(26.0, 4.5, '多云', datetime('now', '-3 hour'), '气象站', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：投喂记录（精度为1位小数）
-- ============================================================
INSERT OR IGNORE INTO feeding (cage_id, feed_type, feed_amount, feeding_time, operator, remark, create_time, update_time) VALUES
(1, '混合饲料', 25.0, datetime('now', '-2 hour'), '张三', '正常投喂', datetime('now'), datetime('now')),
(1, '颗粒饲料A', 10.0, datetime('now', '-5 hour'), '张三', '', datetime('now'), datetime('now')),
(2, '混合饲料', 20.0, datetime('now', '-3 hour'), '王五', '', datetime('now'), datetime('now')),
(3, '鱼粉饲料', 15.0, datetime('now', '-4 hour'), '李四', '', datetime('now'), datetime('now')),
(5, '颗粒饲料B', 18.0, datetime('now', '-6 hour'), '王五', '', datetime('now'), datetime('now'));

-- ============================================================
-- 初始数据：示例预警（时间分散）
-- ============================================================
INSERT OR IGNORE INTO alert (cage_id, indicator_name, indicator_label, current_value, threshold_value, alert_type, alert_level, is_handled, create_time, update_time) VALUES
(1, 'dissolved_oxygen', '溶解氧', 4.2, 5.0, '低于下限', '一般', 0, datetime('now', '-6 hour'), datetime('now')),
(2, 'water_temp', '水温', 31.2, 30.0, '高于上限', '重要', 0, datetime('now', '-12 hour'), datetime('now')),
(3, 'ph', 'pH值', 6.2, 6.5, '低于下限', '一般', 0, datetime('now', '-24 hour'), datetime('now')),
(5, 'dissolved_oxygen', '溶解氧', 4.8, 5.0, '低于下限', '一般', 0, datetime('now', '-48 hour'), datetime('now'));
