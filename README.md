# DreamAIChat_APP

DreamAIChat_APP 是一个面向 AI 对话创作场景的 Android 客户端，目前已经完成“前端 + Spring Boot 后端”核心链路，支持账号登录、模型切换、实时向大模型发送对话并回显结果。

## 功能概览

- **三段式首页**：顶部模型状态栏 + 中部消息流 + 底部多工具输入条。
- **真实后端**：`DreamAIChat_backend` 使用 Spring Boot 3、H2、WebClient 和 JWT，可同时打通 Doubao / DeepSeek（通过配置 API Key 即可）。
- **历史会话**：客户端接入 `/api/chat/conversations`，支持搜索、空状态提示。
- **状态管理**：`ChatViewModel`、`HistoryViewModel` 统一处理网络请求、错误提示、加载指示。

## 目录结构

```
DreamAIChat_APP/
├── app/                       # Android 模块
└── DreamAIChat_backend/       # Spring Boot 后端
```

## 快速使用

1. **配置后端**
   ```bash
   cd DreamAIChat_APP/DreamAIChat_backend
   ./gradlew bootRun
   ```
   - 必填环境变量：`DEEPSEEK_API_KEY`、`DOUBAO_API_KEY`（根据需要启用其中之一）。
   - 默认端口 `8081`，可在 `application.yml` 中修改。

2. **运行 Android 客户端**
   - 用 Android Studio 打开根目录，确保 Android 模拟器或真机可访问 `10.0.2.2:8081`。
   - 点击 *Run*，首次登录输入任意账号后可以通过注册接口创建用户。

3. **测试对话**
   - 登录成功后，下方输入框发送内容，后端会根据所选模型（默认 DeepSeek）请求实际大模型，并把回复回写到会话中。

## 配置项

| 位置 | 说明 |
| --- | --- |
| `DreamAIChat_backend/src/main/resources/application.yml` | 端口、H2 数据库、模型提供商配置 |
| `app/src/main/java/.../RetrofitClient.java` | App 端 BASE_URL (默认 `http://10.0.2.2:8081/`) |
| `app/src/main/res/xml/network_security_config.xml` | 允许在开发环境使用 HTTP |

## 下一步计划

- 增加 `/api/chat/messages` 接口，让历史详情页可查看完整对话。
- 在客户端 `Profile`、`Settings` 页面补齐真实数据源。
- 后端补充注册/密码找回、限流与审计日志。

更多部署与联调细节请参考 `核心技术文档/部署指南.md`。