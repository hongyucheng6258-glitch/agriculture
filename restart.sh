#!/bin/bash

cd /root/aquaculture

pkill -f 'aquaculture-system-1.0.0.jar' 2>/dev/null
sleep 5

nohup java -jar aquaculture-system-1.0.0.jar > app.log 2>&1 &

echo "服务已重启"
