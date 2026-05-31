
import sqlite3
import datetime
import random
import os

# 服务器数据库路径
db_path = '/root/aquaculture/data/aquaculture.db'

# 连接数据库
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

try:
    print("=== 智慧渔业系统服务器数据修复 ===")
    print()
    
    # 1. 先删除旧的投喂记录
    print("1. 清理旧投喂数据...")
    cursor.execute("DELETE FROM feeding")
    print("   ✓ 已删除旧投喂数据")
    
    # 2. 清理旧的预警记录
    print("2. 清理旧预警数据...")
    cursor.execute("DELETE FROM alert")
    print("   ✓ 已删除旧预警数据")
    
    # 3. 生成最近7天的投喂数据
    print("3. 生成最近7天投喂数据...")
    
    feed_types = ["混合饲料", "鱼粉饲料", "颗粒饲料A", "颗粒饲料B"]
    now = datetime.datetime.now()
    count = 0
    
    for day_offset in range(0, 7):  # 0到6天前
        for cage_id in [1, 2, 3, 5]:  # 对应有网箱的ID
            # 每天2-4次投喂
            feeding_times = random.randint(2, 4)
            for _ in range(feeding_times):
                hour = random.randint(6, 20)
                minute = random.randint(0, 59)
                
                feeding_time = (now - datetime.timedelta(days=day_offset)).replace(hour=hour, minute=minute, second=0)
                feed_type = random.choice(feed_types)
                amount = round(random.uniform(10.0, 50.0), 1)
                operator = "饲养员" + str(random.randint(1, 3))
                
                create_time = now.strftime('%Y-%m-%d %H:%M:%S')
                
                cursor.execute('''
                    INSERT INTO feeding (cage_id, feed_type, feed_amount, feeding_time, operator, create_time, update_time, is_deleted)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                ''', (
                    cage_id,
                    feed_type,
                    amount,
                    feeding_time.strftime('%Y-%m-%d %H:%M:%S'),
                    operator,
                    create_time,
                    create_time
                ))
                count += 1
    
    print(f"   ✓ 已生成 {count} 条投喂记录")
    
    # 4. 生成最近的预警记录
    print("4. 生成最近预警数据...")
    
    indicator_labels = ["溶解氧", "pH值", "水温", "饲料库存"]
    alert_levels = ["一般", "重要", "严重"]
    
    alert_count = 0
    for i in range(5):
        cage_id = random.choice([1, 2, 3, 5])
        indicator_idx = random.randint(0, 2)  # 前3个指标
        indicator_label = indicator_labels[indicator_idx]
        
        # 随机生成当前值和阈值
        if indicator_idx == 0:  # 溶解氧
            current_value = round(random.uniform(2.0, 4.5), 1)
            threshold_value = 5.0
        elif indicator_idx == 1:  # pH
            current_value = round(random.uniform(4.0, 6.0), 1)
            threshold_value = 6.5
        else:  # 水温
            current_value = round(random.uniform(31.0, 35.0), 1)
            threshold_value = 30.0
        
        create_time = (now - datetime.timedelta(hours=random.randint(1, 48))).strftime('%Y-%m-%d %H:%M:%S')
        
        cursor.execute('''
            INSERT INTO alert (cage_id, indicator_name, indicator_label, current_value, threshold_value, alert_type, alert_level, is_handled, create_time, update_time, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0)
        ''', (
            cage_id,
            indicator_labels[indicator_idx].replace('值', '').lower(),
            indicator_label,
            current_value,
            threshold_value,
            "低于下限" if current_value < threshold_value else "高于上限",
            random.choice(alert_levels),
            create_time,
            create_time
        ))
        alert_count += 1
    
    print(f"   ✓ 已生成 {alert_count} 条预警记录")
    
    # 5. 确保饲料库存有正确的中文数据
    print("5. 刷新饲料库存数据...")
    
    cursor.execute("DELETE FROM feed_stock")
    
    feed_stock_data = [
        ("混合饲料", 500.0, 12.5, "海大饲料有限公司"),
        ("鱼粉饲料", 200.0, 18.0, "通威饲料"),
        ("颗粒饲料A", 300.0, 15.0, "粤海饲料"),
        ("颗粒饲料B", 250.0, 13.5, "恒兴饲料")
    ]
    
    for fs_data in feed_stock_data:
        cursor.execute('''
            INSERT INTO feed_stock (feed_type, stock_amount, unit_price, supplier, last_restock_time, create_time, update_time, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0)
        ''', (
            fs_data[0],
            fs_data[1],
            fs_data[2],
            fs_data[3],
            now.strftime('%Y-%m-%d %H:%M:%S'),
            now.strftime('%Y-%m-%d %H:%M:%S'),
            now.strftime('%Y-%m-%d %H:%M:%S')
        ))
    
    print("   ✓ 已刷新饲料库存数据")
    
    # 6. 查询验证数据
    print()
    print("=== 数据验证 ===")
    
    # 检查投喂记录
    cursor.execute("SELECT COUNT(*) FROM feeding")
    print(f"投喂记录总数: {cursor.fetchone()[0]}")
    
    # 检查最近7天的数据
    seven_days_ago = (now - datetime.timedelta(days=7)).strftime('%Y-%m-%d %H:%M:%S')
    cursor.execute("SELECT COUNT(*) FROM feeding WHERE feeding_time >= ?", (seven_days_ago,))
    print(f"最近7天投喂记录: {cursor.fetchone()[0]}")
    
    # 检查饲料库存
    cursor.execute("SELECT feed_type, stock_amount FROM feed_stock")
    print("\n饲料库存:")
    for row in cursor.fetchall():
        print(f"  - {row[0]}: {row[1]} kg")
    
    conn.commit()
    print()
    print("=== ✓ 服务器数据修复成功 ===")
    
except Exception as e:
    conn.rollback()
    print(f"错误: {e}")
    import traceback
    traceback.print_exc()
finally:
    conn.close()
