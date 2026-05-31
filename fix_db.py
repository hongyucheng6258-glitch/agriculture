
import sqlite3
import datetime

# 连接数据库
conn = sqlite3.connect('/root/aquaculture/data/aquaculture.db')
cursor = conn.cursor()

try:
    # 删除旧数据
    cursor.execute('DELETE FROM feed_stock')
    
    # 获取当前时间
    now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    # 插入中文数据
    feed_stocks = [
        ('混合饲料', 500.0, 12.5, '海大饲料有限公司', now),
        ('鱼粉饲料', 200.0, 18.0, '通威饲料', now),
        ('颗粒饲料A', 300.0, 15.0, '粤海饲料', now),
        ('颗粒饲料B', 250.0, 13.5, '恒兴饲料', now)
    ]
    
    for fs in feed_stocks:
        cursor.execute('''
            INSERT INTO feed_stock (feed_type, stock_amount, unit_price, supplier, last_restock_time, create_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (fs[0], fs[1], fs[2], fs[3], fs[4], fs[4], fs[4]))
    
    conn.commit()
    print('数据库修复成功！')
    
    # 验证
    cursor.execute('SELECT * FROM feed_stock')
    print('feed_stock表内容：')
    rows = cursor.fetchall()
    for row in rows:
        print(row)
        
except Exception as e:
    conn.rollback()
    print(f'错误：{e}')
finally:
    conn.close()

