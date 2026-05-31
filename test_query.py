import sqlite3
import datetime

db_path = '/root/aquaculture/data/aquaculture.db'

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

now = datetime.datetime.now()

print("=== 验证日期:")
print(f"当前时间: {now}")
print()
print("=== 测试最近7天数据:")

for day_offset in range(6, -1, -1):
    day_date = now - datetime.timedelta(days=day_offset)
    day_start = day_date.replace(hour=0, minute=0, second=0, microsecond=0)
    day_end = day_date.replace(hour=23, minute=59, second=59, microsecond=0)
    
    start_str = day_start.strftime('%Y-%m-%d %H:%M:%S')
    end_str = day_end.strftime('%Y-%m-%d %H:%M:%S')
    
    cursor.execute("SELECT COALESCE(SUM(feed_amount), 0) FROM feeding WHERE feeding_time >= ? AND feeding_time <= ?", (start_str, end_str))
    total = cursor.fetchone()[0]
    
    print(f"  {day_date.strftime('%Y-%m-%d')}: {round(total, 1)} kg")
    print(f"    查询: {start_str} to {end_str}")

print()
print("=== 最新的20条投喂记录:")
cursor.execute("SELECT feed_type, feed_amount, feeding_time FROM feeding ORDER BY feeding_time DESC LIMIT 20")
for row in cursor.fetchall():
    print(f"  {row[0]} {row[1]} {row[2]}")

conn.close()
