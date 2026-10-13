#!/bin/bash
# 图书馆管理系统启动脚本
echo "========================================"
echo "  图书馆管理系统 - Docker Compose 启动"
echo "========================================"
echo ""
echo "正在构建并启动服务（首次或代码更新后需重新构建）..."
echo ""
docker-compose up -d --build
echo ""
echo "服务启动完成！"
echo "访问地址: http://localhost"
echo "API文档: http://localhost/swagger-ui/"
echo "默认管理员: admin / admin123"
