# 构建Nginx镜像（仅包含打包后的静态文件和自定义配置）
FROM nginx:alpine

# 优化1：显式删除默认配置（避免覆盖不彻底的极端情况）
RUN rm -f /etc/nginx/conf.d/default.conf

# 将本地的nginx/default.conf复制到容器的conf.d目录下（替换默认配置）
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 将前端打包后的dist目录复制到nginx的默认静态文件目录
# （如果你的dist在build-stage阶段生成，用下面的COPY --from；如果是本地已打包好的dist，用 COPY ./dist /usr/share/nginx/html）
COPY ./dist /usr/share/nginx/html

# 暴露端口（与default.conf中listen的端口一致）
EXPOSE 80

# 启动nginx（容器默认会启动，这行可省略，仅作显式声明）
CMD ["nginx", "-g", "daemon off;"]
