# 使用 Nginx 作为静态文件服务器
FROM nginx:alpine

# 复制构建好的前端文件
COPY dist/ /usr/share/nginx/html/

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
