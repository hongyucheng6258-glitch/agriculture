import sqlite3
import datetime
import random
import os

db_path = '/root/aquaculture/data/aquaculture.db'

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

try:
    print("=== 重新生成完整数据 ===")
    print()

    # 清空旧数据
    print("1. 清空旧投喂数据...")
    cursor.execute("DELETE FROM feeding")
    print("   ✓ 已清空旧投喂数据")

    feed_types = ["混合饲料", "鱼粉饲料", "颗粒饲料A", "颗粒饲料B"]
    now = datetime.datetime.now()

    count = 0
    for day_offset in range(0, 7):
        for cage_id in [1, 2, 3, 5]:
            feeding_times = random.randint(5, 9)
            for _ in range(feeding_times):
                hour = random.randint(6, 21)
                minute = random.randint(0, 59)

                feeding_time = (now - datetime.timedelta(days=day_offset)).replace(hour=hour, minute=minute, second=0)
                feed_type = random.choice(feed_types)
                amount = round(random.uniform(12.0, 55.0), 1)
                operator = "饲养员" + str(random.randint(1, 5))

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

    cursor.execute("SELECT COUNT(*) FROM feeding")
    print(f"总投喂记录: {cursor.fetchone()[0]}")

    print()
    print("=== 验证最近7天数据:")
    seven_days_ago = (now - datetime.timedelta(days=7)).strftime('%Y-%m-%d %H:%M:%S')
    for day_offset in range(0, 7):
        day_start = (now - datetime.timedelta(days=day_offset)).strftime('%Y-%m-%d 00:00:00')
        day_end = (now - datetime.timedelta(days=day_offset)).strftime('%Y-%m-%d 23:59:59')
        cursor.execute("SELECT COALESCE(SUM(feed_amount), 0) FROM feeding WHERE feeding_time >= ? AND feeding_time <= ?", (day_start, day_end))
        day_total = round(cursor.fetchone()[0])
        date_str = (now - datetime.timedelta(days=day_offset)).strftime('%Y-%m-%d')
        print(f"  {date_str}: {round(day_total, 1)} kg")

    conn.commit()
    print()
    print("=== 完成 ===")

except Exception as e:
    conn.rollback()
    print(f"错误: {e}")
    import traceback
    traceback.print_exc()
finally:
    conn.close()
