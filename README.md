# AI Code Platform · 智能 AI 代码生成平台


> AI 驱动的低代码网站生成平台后端。用户通过自然语言描述需求，系统自动生成可运行的 HTML / 多文件静态站 / Vue 工程，并提供 SSE 流式对话、一键部署、应用管理与 AI 可观测性。

## 核心能力

- **三种代码生成模式**：原生 HTML 单页、多文件静态站（HTML + CSS + JS）、Vue 工程（Tool Calling 写文件 + npm build）
- **智能路由选型**：独立轻量模型（通义 Qwen Turbo）分析用户 prompt，自动选择最合适的生成类型
- **SSE 流式生成**：AI 回复实时推送，支持多轮对话迭代
- **LangGraph4j 工作流**：图片收集 → Prompt 增强 → 路由 → 代码生成 → 质量检查 → 条件重试/构建
- **一键部署**：生成代码部署至静态资源目录，返回可访问 URL；部署后异步截图更新应用封面
- **微服务架构**：user / app / screenshot 拆分，Dubbo + Nacos 通信
- **工程化保障**：Redisson 分布式限流、Prompt 安全 Guardrail、Prometheus + Grafana 监控

## 三种生成模式

| 模式 | 枚举值 | 说明 | 适用场景 |
|------|--------|------|----------|
| 原生 HTML | `html` | 结构化输出 `HtmlCodeResult` | 简单落地页、活动页 |
| 多文件静态站 | `multi_file` | 输出 HTML + CSS + JS 多文件 | 中等复杂度展示站 |
| Vue 工程 | `vue_project` | AI Agent + Tool Calling 写文件，支持 npm build | 多页面、复杂交互的企业站 |

## 系统架构

```
                    ┌─────────────────────────┐
                    │   ai-code-app (8125)    │
                    │  应用管理 · 代码生成     │
                    │  SSE · 部署 · LangGraph │
                    └───────────┬─────────────┘
                                │ Dubbo + Nacos
              ┌─────────────────┼─────────────────┐
              ▼                 ▼                 ▼
    ┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
    │ ai-code-user    │ │ ai-code-ai   │ │ ai-code-screenshot│
    │    (8124)       │ │ LangChain4j  │ │     (8127)        │
    │ 注册登录 Session│ │ AI 层封装    │ │ Selenium 截图     │
    └─────────────────┘ └──────────────┘ └──────────────────┘
              │                 │                 │
              └─────────────────┼─────────────────┘
                                ▼
              MySQL · Redis · RocketMQ（可选）
                                │
                                ▼
                    DeepSeek / 通义千问 API
                                │
                                ▼
              Actuator / Prometheus → Grafana
```

仓库同时保留根目录 `src/` 单体形态，**当前主实现位于 `ai-code-microservice/`**。

## 微服务模块

| 模块 | 端口 | 职责 |
|------|------|------|
| `ai-code-common` | — | 公共工具、统一响应、JSON 配置 |
| `ai-code-model` | — | 实体、DTO、VO、枚举 |
| `ai-code-client` | — | Dubbo 接口定义 |
| `ai-code-ai` | — | LangChain4j AI 层、Prompt、Guardrail |
| `ai-code-user` | 8124 | 用户注册登录、Session 管理 |
| `ai-code-app` | 8125 | 核心业务：应用 CRUD、SSE 对话、代码生成、部署、限流、指标采集 |
| `ai-code-screenshot` | 8127 | 网页截图，更新应用封面 |

Dubbo 协议端口：user `50051`、screenshot `50052`、app `50053`。

## 技术栈

| 层次 | 技术 | 用途 |
|------|------|------|
| 基础 | Java 21 · Spring Boot 3.5 | 服务框架 |
| 持久化 | MyBatis-Flex · MySQL | 用户、应用、对话历史 |
| 缓存 / 会话 | Redis · Spring Session | Session 共享、对话记忆、Spring Cache |
| 微服务 | Dubbo 3 · Nacos | RPC 与服务注册 |
| 消息 | RocketMQ（可选） | 截图任务异步解耦 |
| 限流 | Redisson RRateLimiter | AI 对话接口分布式限流 |
| AI | LangChain4j · LangGraph4j | 流式对话、Tool Calling、工作流编排 |
| 模型 | DeepSeek · 通义千问 | 代码生成 / 推理 / 智能路由 |
| 部署 | 腾讯云 COS · Selenium | 静态资源存储 · 封面截图 |
| 可观测 | Actuator · Prometheus · Grafana | AI 请求数、Token、响应时间 |

## 项目结构

```
ai-code-platform/
├── ai-code-microservice/           # 微服务后端（主实现）
│   ├── ai-code-app/                # 核心业务服务
│   │   └── src/main/java/.../
│   │       ├── controller/         # REST API
│   │       ├── service/            # 业务逻辑、SSE
│   │       ├── core/               # 代码解析、保存、部署
│   │       ├── langgraph4j/        # LangGraph 工作流节点
│   │       ├── monitor/            # Prometheus 指标采集
│   │       └── ratelimter/         # Redisson 限流 AOP
│   ├── ai-code-user/               # 用户服务
│   ├── ai-code-screenshot/         # 截图服务
│   ├── ai-code-ai/                 # AI 层（Prompt、Model 配置）
│   ├── ai-code-common/
│   ├── ai-code-model/
│   └── ai-code-client/
├── src/                            # 单体版（演进中，端口 8123）
├── grafana/
│   └── ai_model_grafana_config.json  # AI 模型监控看板
└── scripts/
    ├── start-prod.sh               # 单体 jar 生产启动脚本
    └── rocketmq/                   # RocketMQ Docker 部署
```

## 核心业务流程

1. **创建应用**：用户提交 prompt → 路由模型选择生成类型 → 写入 `app` 表
2. **AI 对话生成**：SSE 流式调用 LangChain4j → 解析代码 → 保存至本地/COS → 返回预览地址
3. **多轮迭代**：Redis 对话记忆 + MySQL `chat_history` 双写，支持上下文延续
4. **一键部署**：将生成产物同步至部署目录，返回 `deploy-host/{appId}/` 访问 URL
5. **异步截图**：部署成功后调用 screenshot 服务（Dubbo 或 RocketMQ），更新应用封面
6. **可观测性**：每次 AI 调用记录请求数、Token 消耗、响应耗时至 Prometheus

## AI 层设计

- **Factory + Caffeine 缓存**：按 `appId + codeGenType` 缓存 `AiCodeGeneratorService` 实例
- **对话记忆**：`MessageWindowChatMemory` + `RedisChatMemoryStore`，启动时从 MySQL 加载历史
- **并发安全**：StreamingChatModel 使用 prototype 多例 Bean，避免 SSE 并发串流
- **Vue 模式 Tool Calling**：FileWrite / Read / Modify / Delete / Exit，最多连续 20 次工具调用
- **输入护轨**：`PromptSafetyInputGuardrail` 拦截不安全 prompt
- **LangGraph 增强管线**（与主 SSE 路径并行）：图片收集（Pexels / Undraw / Mermaid / Logo）→ 质检闭环 → 条件边重试

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8+
- Redis 6+
- Nacos 2.x

### 1. 数据库

创建数据库 `aicodeplateform`，包含 `user`、`app`、`chat_history` 等表。

### 2. 配置

各服务配置文件位于 `ai-code-microservice/*/src/main/resources/application.yml`。

关键配置项（**请使用环境变量或本地 profile，勿将密钥提交至 Git**）：

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.*` | MySQL 连接 |
| `spring.data.redis.*` | Redis 连接 |
| `dubbo.registry.address` | Nacos 地址 |
| `langchain4j.open-ai.chat-model.*` | DeepSeek 代码生成模型 |
| `langchain4j.open-ai.reasoning-streaming-chat-model.*` | DeepSeek 推理模型（Vue 模式） |
| `langchain4j.open-ai.routing-chat-model.*` | 通义千问路由模型 |
| `cos.client.*` | 腾讯云 COS |
| `pexels.api-key` | Pexels 图片搜索 |
| `dashscope.api-key` | 阿里云 DashScope（Logo 生成） |
| `code.deploy-host` | 部署访问域名 |
| `app.screenshot.mq-enabled` | 截图走 RocketMQ（`false` 时走 Dubbo 异步） |

### 3. 启动 Nacos

```bash
# 默认地址 nacos://127.0.0.1:8848
```

### 4. 编译并启动微服务

```bash
cd ai-code-microservice
mvn clean install -DskipTests

# 按顺序启动（各模块 main 类或 spring-boot:run）
# ai-code-user       → http://localhost:8124/api
# ai-code-screenshot → http://localhost:8127/api
# ai-code-app        → http://localhost:8125/api
```

### 5. 验证

| 地址 | 说明 |
|------|------|
| http://localhost:8125/api/doc.html | Knife4j API 文档 |
| http://localhost:8125/api/actuator/health | 健康检查 |
| http://localhost:8125/api/actuator/prometheus | Prometheus 指标 |

### 6. 可选：RocketMQ

```bash
cd scripts/rocketmq
chmod +x *.sh
./deploy.sh
```

详见 [scripts/rocketmq/README.md](scripts/rocketmq/README.md)。

## 监控与运维

### Prometheus 指标

`AiModelMetricsCollector` 暴露以下指标：

| 指标名 | 说明 |
|--------|------|
| `ai_model_requests_total` | AI 请求总数（按 user / app / model / status 分标签） |
| `ai_model_errors_total` | AI 调用错误数 |
| `ai_model_tokens_total` | Token 消耗（input / output） |
| `ai_model_response_time` | 响应耗时 |

### Grafana 看板

1. 配置 Prometheus 抓取 `http://<app-host>:8125/api/actuator/prometheus`
2. 导入 [grafana/ai_model_grafana_config.json](grafana/ai_model_grafana_config.json)
3. 看板包含：总成功请求数、总 Token 消耗、平均响应时间、Token 累积趋势、输入/输出分布

### 限流

AI 对话接口使用 `@RateLimit` + Redisson，默认每用户 **5 次 / 60 秒**。

### 生产部署（单体 jar）

```bash
# 编译
mvn clean package -DskipTests

# 启动（配置随 jar 内 application-prod.yml）
bash scripts/start-prod.sh
```

## 中间件使用说明

| 中间件 | 用途 |
|--------|------|
| Redis | Session 共享、对话记忆、精选列表缓存、截图幂等（SETNX） |
| RocketMQ | 截图任务异步（当前默认 `mq-enabled: false`，走 Dubbo） |
| MySQL | 用户、应用、对话历史持久化 |

## License

MIT

