# SmartResume 智能简历

> 一款基于 AI 的智能简历制作与优化平台。支持在线编辑、AI 辅助优化、岗位匹配分析、简历打分、语法纠错、版本管理、分享与导出等功能。

## ✨ 功能特性

- **📝 在线简历编辑**
  - 多模板支持（经典、双栏、创意等）
  - 模块化内容管理（基本信息、教育经历、工作经历、项目经历、技能、个人优势等）
  - 富文本编辑器 + 自定义样式配置

- **🤖 AI 助手**
  - AI 对话助手：咨询简历优化建议，支持多轮对话
  - AI 修改模式：直接改写/生成简历内容
  - 岗位匹配分析：上传岗位描述，AI 评估匹配度
  - 简历打分：AI 多维度综合评分
  - 语法纠错：自动检测并高亮语法问题
  - 自我介绍生成：根据简历生成个性化自我介绍

- **📊 简历管理**
  - 多简历管理
  - 版本历史（自动保存 + 手动快照）
  - 简历分享（密码保护 + 有效期）
  - 主动提醒：AI 定期分析和提醒

- **🔐 用户系统**
  - 注册/登录
  - 长期登录（Remember-Me）
  - 头像上传（腾讯云 COS 存储）
  - 邮箱通知

## 🛠️ 技术栈

### 前端

| 技术 | 用途 |
|------|------|
| **Vue 3** (Composition API) | 前端框架 |
| **Vite** | 构建工具 |
| **Vue Router** | 路由管理 |
| **Pinia** | 状态管理 |
| **Axios** | HTTP 请求 |
| **Vitest** | 单元测试 |
| **Playwright** | E2E 测试 |

### 后端

| 技术 | 用途 |
|------|------|
| **Spring Boot 3.5** | 后端框架 |
| **Spring WebFlux** (WebClient) | AI 流式调用 |
| **Spring Session + Redis** | 分布式会话 |
| **MyBatis-Plus** | ORM 框架 |
| **MySQL** | 关系型数据库 |
| **Redis** | 缓存 / Session 存储 |
| **Redisson** | 分布式锁 |
| **Spring Mail** | 邮件发送 |
| **腾讯云 COS** | 文件/头像存储 |
| **DeepSeek API** | AI 大模型能力 |

## 🚀 快速开始

### 前置要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Redis
- Maven (或使用项目自带的 `mvnw`)

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `smart_resume`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
source backend/sql/init.sql
```

### 2. 后端启动

```bash
# 进入后端目录
cd backend

# 修改配置（填入你的密钥）
# 编辑 src/main/resources/application.yml 和 application-dev.yml
# 需要配置：数据库密码、DeepSeek API Key、腾讯云 COS 密钥、QQ邮箱 SMTP 授权码

# 编译运行
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 4. 访问项目

打开浏览器访问 `http://localhost:5173` 即可。

> 开发环境下前端通过 Vite Proxy 转发 `/api` 请求到后端 `localhost:8080`，无需额外配置。

## 📁 项目结构

```
智能简历_项目源码/
├── frontend/                     # 前端项目
│   ├── src/
│   │   ├── api/                  # API 请求封装
│   │   ├── components/           # 公共组件
│   │   ├── composables/          # 组合式函数
│   │   ├── layouts/              # 布局组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # Pinia 状态管理
│   │   ├── views/                # 页面视图
│   │   │   └── resume/
│   │   │       ├── editors/      # 简历编辑模块
│   │   │       └── templates/    # 简历模板
│   │   └── main.js               # 入口文件
│   ├── tests/                    # 测试文件
│   ├── public/                   # 静态资源
│   └── package.json
│
├── backend/                      # 后端项目
│   ├── src/main/java/com/srb/backend/
│   │   ├── ai/                   # DeepSeek AI 客户端
│   │   ├── common/               # 公共工具类
│   │   ├── config/               # 配置类（COS、MyBatis-Plus 等）
│   │   ├── constant/             # 常量
│   │   ├── controller/           # 接口控制器
│   │   ├── mapper/               # MyBatis-Plus Mapper
│   │   ├── model/                # 数据模型（DTO、VO、Entity）
│   │   ├── scheduler/            # 定时任务
│   │   ├── service/              # 业务逻辑
│   │   └── support/              # 辅助工具
│   ├── src/main/resources/
│   │   ├── application.yml       # 主配置
│   │   ├── application-dev.yml   # 开发环境配置
│   │   └── application-prod.yml  # 生产环境配置
│   ├── sql/                      # 数据库初始化脚本
│   └── pom.xml
│
├── 需求文档.md                   # 项目需求文档
├── LICENSE                       # 开源许可证
└── README.md                     # 本文件
```

## 🧪 运行测试

```bash
# 前端单元测试
cd frontend && npm run test:run

# 前端 E2E 测试
cd frontend && npm run test:e2e

# 后端测试
cd backend && ./mvnw test
```

## ⚙️ 环境配置说明

项目配置了开发 (`dev`) 和生产 (`prod`) 两套环境：

| 配置项 | 开发环境 | 生产环境 |
|--------|---------|---------|
| 配置文件 | `application-dev.yml` | `application-prod.yml` |
| 数据库 | `localhost:3306` | `mysql:3306` (Docker) |
| Redis | `localhost:6379` | `redis:6379` (Docker) |
| 前端地址 | `http://localhost:5173` | `https://your-domain.com` |

> **注意**: 所有密钥和密码已替换为占位符，使用前需要填入真实值：
> - `application.yml`: DeepSeek API Key、腾讯云 COS 密钥、QQ邮箱 SMTP 授权码
> - `application-dev.yml`: 数据库密码
> - `application-prod.yml`: 数据库密码、前端域名

## 📄 开源许可

本项目基于 [MIT License](LICENSE) 开源。

## 🙏 致谢

- [鱼皮](https://space.bilibili.com/your-space) - 项目教程与指导
