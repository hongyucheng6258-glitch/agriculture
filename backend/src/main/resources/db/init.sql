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
('dissolved_oxygen', '溶解氧', 5.0, NULL, 'mg/L', datetime('now'), datetime('now')),
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

