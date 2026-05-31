#!/bin/bash

PROJECT_DIR=" /root/aquaculture\
cd \

# Stop old containers if running
docker stop aquaculture-backend 2>/dev/null
docker stop aquaculture-frontend 2>/dev/null
docker rm aquaculture-backend 2>/dev/null
docker rm aquaculture-frontend 2>/dev/null
pkill -f 'java.*aquaculture' 2>/dev/null

# Start backend
echo \Starting backend...\
cd \/backend
nohup java -jar target/aquaculture-system-1.0.0.jar --spring.config.location=../application.yml > ../backend.log 2>&1 &
BACKEND_PID=\$!
echo \ > ../backend.pid
echo \Backend started with PID: \\

# Wait a bit
sleep 5

# Start frontend with Nginx
echo \Starting frontend with Nginx...\
cd \
docker run -d --name aquaculture-frontend \\
 -p 80:80 \\
 -v \/frontend/dist:/usr/share/nginx/html \\
 -v \/nginx.conf:/etc/nginx/conf.d/default.conf \\
 --network host \\
 --restart unless-stopped \\
 nginx:alpine

echo \Deployment complete!\
echo \Backend running at http://localhost:8080\
echo \Frontend running at http://localhost:80\
