# FitAgent · 健身 AI 教练平台

基于 **Spring Boot + Spring AI（DeepSeek）** 的健身管理应用：除了打卡、身体指标、训练计划等基础模块，核心是一套「能查知识、也能查你的真实数据」的 AI 教练链路。

> 个人学习 / 求职演示项目。仓库内不含任何真实密钥与服务器地址，所有敏感配置均通过环境变量注入。

## 功能

| 模块 | 说明 |
| --- | --- |
| 账号体系 | 注册 / 登录，密码 BCrypt 存储，JWT 双端鉴权 |
| 打卡 | 每日训练打卡、连续打卡天数统计 |
| 身体指标 | 体重、体脂等指标录入与趋势查询 |
| 训练计划 | 计划与计划项管理，支持进行中计划查询 |
| AI 教练 | 知识库问答 + 真实数据查询 + 训练建议 |

## 技术栈

- **后端**：Java 21、Spring Boot 3、Spring Security、Spring AI（DeepSeek）
- **持久层**：MyBatis-Plus、MySQL 8
- **缓存/会话**：Redis
- **前端**：Vue 3 + Element Plus（`src/main/resources/static/index.html` 单页，无需单独构建）
- **其他**：JWT、Maven Wrapper、Lombok

## 核心设计：AI 教练链路

```
用户提问
  → RAG 检索（知识库片段打分排序，取 Top-K）
  → System Prompt 组装（教练人设 + 用户画像 + 检索片段）
  → 工具注册（Function Calling）
  → 大模型生成回答（按需回查 MySQL 中的真实数据）
```

- **RAG 检索**：健身知识文档按空行切分为片段，用关键词命中次数打分排序（BM25 简化实现），取 Top-3 注入 Prompt；不依赖 embedding 与向量库，本地即可运行，同时预留了向量检索的扩展点。
- **Function Calling**：用 Spring AI 的 `@Tool` 暴露「身体指标 / 今日打卡 / 连续打卡天数 / 进行中训练计划」四个工具，由模型自主决定是否调用；服务端查库后把结果回填给模型再作答，避免模型编造数据。

核心代码：

- `service/ai/KnowledgeRetriever.java` —— 知识库切片与打分检索
- `service/ai/AiTools.java` —— `@Tool` 工具定义
- `service/impl/AiCoachServiceImpl.java` —— 检索 → Prompt → 工具 → 生成的完整链路

## 目录结构

```
src/main/java/com/fitness/agent
├── controller/       # Auth / Checkin / BodyMetric / Plan / Ai
├── service/          # 业务接口与实现
│   └── ai/           # RAG 检索、Tool 定义
├── mapper/           # MyBatis-Plus Mapper
├── entity/ dto/      # 数据模型
├── security/         # JWT 签发、过滤器
└── config/           # Security、MyBatis-Plus 公共字段填充
src/main/resources
├── application.yml   # 全部通过 ${ENV} 注入
├── schema.sql        # 启动时自动建表（幂等）
├── rag/              # 健身知识库文本
└── static/index.html # 前端单页
```

## 快速开始

环境要求：JDK 21、MySQL 8、Redis（可选，未配置时仅影响缓存相关逻辑）、DeepSeek API Key。

1. 建库：`CREATE DATABASE fitness DEFAULT CHARSET utf8mb4;`（表结构由 `schema.sql` 启动时自动创建）
2. 配置环境变量（参考 `.env.example`）：

```bash
DEEPSEEK_API_KEY=你的 key
DB_URL=jdbc:mysql://localhost:3306/fitness?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=fitness
DB_PASSWORD=你的密码
REDIS_HOST=localhost
JWT_SECRET=至少 32 字节的随机字符串
```

3. 启动：

```bash
./mvnw spring-boot:run     # macOS / Linux
run-dev.cmd                # Windows（需先设置 JAVA_HOME）
```

4. 打开 `http://localhost:8080` 即前端页面。

## 说明

- 所有敏感配置（数据库、Redis、模型 API Key、JWT 密钥）都只从环境变量读取，仓库里没有任何真实凭据。
- 项目数据均为演示数据，涉及用户身份信息的部分已做脱敏处理。
