# 构建Nginx镜像（仅包含打包后的静态文件和自定义配置）
FROM nginx:alpine

# 优化1：显式删除默认配置（避免覆盖不彻底的极端情况）
RUN rm -f /etc/nginx/conf.d/default.conf

# 将本地的nginx/default.conf复制到容器的conf.d目录下（替换默认配置）
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# ==================== 复制两个前端 ====================
# 这里通过docker-compose里面的语法进行数据卷挂载了，所以不需要复制了
# 唯一问题是不能乱改宿主机里面的services里的前端的dist文件(也不能删除)
# 好处是可以热更新
# 用户中心
# COPY ../user-center/frontend/dist /usr/share/nginx/html/usercenter

# 伙伴匹配
# COPY ../partner-match/frontend/dist /usr/share/nginx/html/partner
# ======================================================

# 暴露端口（与default.conf中listen的端口一致）
EXPOSE 80

# 启动nginx（容器默认会启动，这行可省略，仅作显式声明）
CMD ["nginx", "-g", "daemon off;"]
