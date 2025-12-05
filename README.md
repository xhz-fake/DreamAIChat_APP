# DreamAIChat_APP

> 字节跳动工程训练营-客户端 · Android AI 对话应用  
> 前端：Android (Java + Kotlin) · 后端：Spring Boot (Kotlin)

---

## 项目概览

DreamAIChat 是一款 Android 端 AI 对话应用，支持：
- 手机号/邮箱登录与注册（JWT 认证）  
- 多轮对话 & 文本 + 多图消息  
- 会话历史管理与搜索  
- 对话模板中心（编程助手、论文助手等）  
- 对话图谱（基于历史对话生成总结 + 流程提示）  
- 个人中心统计（对话数 / 消息数 / 使用天数）  

前端通过 Retrofit + OkHttp 调用本地 Spring Boot 后端，后端再转发请求到 DeepSeek / 豆包等大模型（可根据 API Key 配置启用或使用演示模式）。

---

## 目录结构

```text
DreamAIChat_APP/				# Android 前端工程（Android Studio 打开本目录）
├─ app/                         
│  ├─ src/main/java/com/example/dreamaichat_app/
│  │  ├─ ui/                    # 界面层（Login, Chat, History, Profile 等）
│  │  ├─ data/                  # Room + Retrofit + Repository
│  │  ├─ domain/usecase/        # 用例（登录、发消息、取历史等）
│  │  └─ mvi/ presentation/     # 登录入口使用的 MVI 基础
│  └─ src/main/res/             # 布局、图标、颜色、字符串资源
│
├─ DreamAIChat_backend/         # Spring Boot 后端工程（IDEA 打开本目录）
│  ├─ src/main/kotlin/com/dreamaichat/backend/
│  │  ├─ controller/            # Auth / Chat / Diagnostics 等控制器
│  │  ├─ service/               # AuthService / ConversationService / ModelRouterService
│  │  ├─ entity/ repository/    # JPA 实体与仓库
│  │  └─ dto/ config/           # DTO & 配置
│  └─ src/main/resources/
│     ├─ application.yml        # 默认配置（端口、数据源、模型 provider 配置）
│     └─ application-local.yml  # 本地开发时的 API Key（已忽略提交）
│
├─ 核心技术文档/                  # 架构设计白皮书、技术选型、部署指南、API 说明等
├─ 产品思维文档/                  # 界面设计白皮书、产品分析评估等
└─ vibe_coding实践文档/          # AI 协作实践、故障排查、Prompt 工程报告
```

---

## 运行方式（必读）

#### 部署时强烈建议阅读: --  /核心技术文档/部署指南.md

### 1. 启动后端服务（Spring Boot）

1. **导入工程**
   
   - 使用 IntelliJ IDEA 打开 `DreamAIChat_backend` 目录（作为独立 Gradle/Spring Boot 项目）。
   
2. **配置端口（可选）**
   
   - 默认端口：`application.yml` 中 `server.port: 8082`。  
   - 如需更改，请同步修改 Android 端 `RetrofitClient` 的 `BASE_URL` 端口。
   
3. **配置模型 API Key（可选，启用真实大模型时必配）**
   - 方式一：系统环境变量  
     - `DEEPSEEK_API_KEY=你的 DeepSeek Key`  
     - `DOUBAO_API_KEY=你的豆包 Key`
   - 方式二：`application-local.yml`（本地开发专用，已 .gitignore）  
     - 按部署指南中的示例填写 `chat.providers.deepseek.api-key` 等字段。  
   
4. **运行后端**
   - IDEA 中运行 `DreamAiChatBackendApplication`（可选激活 profile：`local`）；  
   - 或命令行：
     ```bash
     cd DreamAIChat_backend
     ./gradlew bootRun
     ```
   
5. **联网诊断（可选）**
   - 在后端机器执行：  
     ```bash
     curl http://localhost:8082/api/diagnostics/providers
     ```

### 2. 运行 Android 应用（模拟器 / 真机）

> 前提：已在本地成功启动后端，并确认端口与 `RetrofitClient` 中的 `BASE_URL` 一致。

1. **使用 Android Studio 打开 `DreamAIChat_APP` 根目录**；等待 Gradle 同步完成。  
2. **确认后端地址**
   
   - 打开 `app/src/main/java/com/example/dreamaichat_app/data/remote/RetrofitClient.java`：  
     ```java
     private static final String BASE_URL = "http://10.0.2.2:8082/";
     ```
   - Android 模拟器访问宿主机请使用 `10.0.2.2`；  
   - 如果在物理手机上调试，则需改为**电脑的局域网 IP**，例如：  
     ```java
     private static final String BASE_URL = "http://192.168.1.100:8082/";
     ```
3. **运行到模拟器**
   - 在 Android Studio 选择任意模拟器（API 30+），点击“Run”运行 `app` 模块；  
   - 登录/注册后即可开始与模型对话。
4. **导出调试 APK（供老师安装在手机上）**
   - 菜单：`Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`；  
   - 构建完成后，IDE 右下角会提示“APK generated”，点击“Locate”可打开生成目录；  
   - 默认生成文件类似：`app/build/outputs/apk/debug/app-debug.apk`；  
   - 将该 APK 拷贝到手机（或通过 ADB 安装）即可在真机上运行。  
   - 注意：真机要访问你的后端，需要与后端在**同一局域网**，并将 `BASE_URL` 改为电脑 IP。

> 如果后端未配置真实 API Key，应用仍可运行，但模型回复为“演示模式”，该状态在 UI 上会有明确提示。

---

## 技术栈与特性

- **Android 前端**
  - 语言：Java + 部分 Kotlin  
  - 架构：MVVM（主流程）+ 登录模块 MVI  
  - 数据：Room（本地会话与消息存储）、ViewModel + LiveData  
  - 网络：Retrofit 2 + OkHttp 3 + Gson + RxJava 3  
  - UI：Material Components、ConstraintLayout、RecyclerView、多图发送支持

- **后端服务**
  - 框架：Spring Boot 3（Kotlin）  
  - 安全：JWT 登录认证  
  - 数据：H2 内存数据库（开发环境，可按需替换为持久化数据库）  
  - 模型路由：`ModelRouterService` 聚合 DeepSeek / 豆包等大模型，支持文本 + 图像输入  

---

## 更多文档

如需了解架构细节、部署步骤与 AI 协作过程, 代码亮点及精彩快照，可参考：
- `核心技术文档/架构设计白皮书.md`  
- `核心技术文档/部署指南.md`  
- `核心技术文档/API集成说明.md`  
- `产品思维文档/APP界面设计白皮书_v3.0.md`（UI/交互规范）  
- `vibe_coding实践文档/Vibe_Coding实践与思考.md`  
- `vibe_coding实践文档/AI指令工程实践报告.md`  

-- 上述文档可以帮助评审老师, 及开发者小伙伴快速理解本项目的 **设计思路, 部署方式, 工程实践与 AI 协作方式.**

-- 最上方的导览图清晰的展示本项目的代码及相关文件的目录结构以及文件内容的梗概快照