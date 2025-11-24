# API 集成说明文档

## 1. 什么是 API？

**简单理解**：
- API（Application Programming Interface）是应用编程接口
- 可以理解为"应用和服务器的对话方式"
- 就像点餐时和服务员说话一样，应用通过 API 告诉服务器"我要什么数据"

**实际例子**：
- 登录时：应用发送账号密码 → 服务器验证 → 返回用户信息
- 发送消息时：应用发送消息内容 → 服务器处理 → 返回 AI 回复

## 2. 网络请求的基本流程

```
用户操作（如点击登录按钮）
    ↓
应用创建请求（包含账号、密码）
    ↓
通过 Retrofit 发送 HTTP 请求
    ↓
服务器接收请求并处理
    ↓
服务器返回响应（成功或失败）
    ↓
应用接收响应并更新界面
```

## 3. Retrofit 是什么？

**Retrofit** 是 Android 中用于网络请求的库，它简化了网络请求的代码。

**为什么使用 Retrofit**：
- ✅ 代码简洁：几行代码就能完成网络请求
- ✅ 类型安全：编译时检查错误
- ✅ 自动转换：自动将 Java 对象转换为 JSON
- ✅ 支持 RxJava：方便处理异步操作

## 4. 项目中的网络架构

### 4.1 文件结构

```
data/remote/
├── RetrofitClient.java      # 网络客户端配置
├── api/
│   └── ApiService.java      # API 接口定义
└── model/                    # 数据模型
    ├── ApiResponse.java     # 统一响应格式
    ├── LoginRequest.java    # 登录请求
    ├── LoginResponse.java   # 登录响应
    ├── ChatRequest.java     # 聊天请求
    └── ChatResponse.java    # 聊天响应
```

### 4.2 各文件的作用

**RetrofitClient.java**：
- 配置网络客户端
- 设置服务器地址（BASE_URL）
- 配置超时时间、日志等

**ApiService.java**：
- 定义所有 API 接口
- 使用注解（@POST、@GET）定义请求方法
- 定义请求参数和返回值

**model 文件夹**：
- 定义请求和响应的数据结构
- 例如：登录需要发送什么数据，服务器返回什么数据

## 5. 如何配置 API 地址

### 5.1 找到配置文件

在 Android Studio 中：
1. 打开左侧 `Project` 面板
2. 展开路径：`app → src → main → java → com → example → dreamaichat_app → data → remote`
3. 双击 `RetrofitClient.java`

### 5.2 修改 BASE_URL

找到这一行（大约第 30 行）：
```java
private static final String BASE_URL = "https://api.example.com/";
```

**修改为你的实际地址**：
```java
private static final String BASE_URL = "https://your-api-server.com/";
```

### 5.3 地址格式要求

**必须遵守的规则**：
1. ✅ 必须以 `http://` 或 `https://` 开头
2. ✅ 末尾必须有斜杠 `/`
3. ✅ 只写基础地址，不包含具体接口路径

**正确示例**：
```java
// 生产环境
private static final String BASE_URL = "https://api.example.com/";

// 测试环境
private static final String BASE_URL = "https://test-api.example.com/";

// 本地开发（模拟器）
private static final String BASE_URL = "http://10.0.2.2:8080/";

// 本地开发（真机，需要替换为你的电脑 IP）
private static final String BASE_URL = "http://192.168.1.100:8080/";
```

**错误示例**：
```java
// ❌ 缺少协议
private static final String BASE_URL = "api.example.com/";

// ❌ 缺少末尾斜杠
private static final String BASE_URL = "https://api.example.com";

// ❌ 包含了接口路径
private static final String BASE_URL = "https://api.example.com/auth/login";
```

## 6. 如何测试 API 配置

### 6.1 使用 Logcat 查看网络请求

1. **运行应用**
   - 点击 Android Studio 顶部的绿色运行按钮
   - 或按快捷键 `Shift + F10`

2. **打开 Logcat**
   - 点击 Android Studio 底部的 `Logcat` 标签
   - 如果没有看到，点击 `View → Tool Windows → Logcat`

3. **过滤日志**
   - 在 Logcat 的搜索框输入：`OkHttp` 或 `Retrofit`
   - 这样只显示网络相关的日志

4. **查看请求**
   - 在应用中执行操作（如登录）
   - 在 Logcat 中应该能看到类似以下内容：
   ```
   D/OkHttp: --> POST https://api.example.com/auth/login
   D/OkHttp: Content-Type: application/json
   D/OkHttp: {"account":"test@example.com","password":"123456"}
   D/OkHttp: <-- 200 OK https://api.example.com/auth/login (1234ms)
   ```

### 6.2 常见日志含义

**成功请求**：
```
D/OkHttp: --> POST https://api.example.com/auth/login
D/OkHttp: <-- 200 OK (1234ms)
```
- `-->` 表示发送请求
- `<--` 表示收到响应
- `200 OK` 表示请求成功

**连接失败**：
```
D/OkHttp: --> POST https://api.example.com/auth/login
D/OkHttp: <-- FAILED java.net.ConnectException: Failed to connect to...
```
- 可能是地址错误、服务器未运行、或网络问题

**404 错误**：
```
D/OkHttp: <-- 404 Not Found
```
- 接口路径错误，检查 `ApiService.java` 中的路径

## 7. 本地开发配置

### 7.1 使用 Android 模拟器

如果你在 Android 模拟器中运行应用，需要使用特殊地址访问本地服务器：

```java
// 模拟器访问本地服务器
private static final String BASE_URL = "http://10.0.2.2:8080/";
```

**为什么是 10.0.2.2**：
- `10.0.2.2` 是 Android 模拟器专门用来访问宿主电脑（你的电脑）的地址
- 相当于模拟器中的 `localhost` 或 `127.0.0.1`

### 7.2 使用真机调试

如果使用真机（手机）调试：

1. **查看电脑 IP 地址**
   - Windows：打开命令提示符（cmd），输入 `ipconfig`
   - 找到 "IPv4 地址"，例如：`192.168.1.100`
   - Mac/Linux：打开终端，输入 `ifconfig`，找到 "inet" 地址

2. **配置地址**
   ```java
   private static final String BASE_URL = "http://192.168.1.100:8080/";
   ```
   将 `192.168.1.100` 替换为你的实际 IP 地址

3. **确保手机和电脑在同一网络**
   - 手机和电脑必须连接同一个 WiFi
   - 否则无法访问

## 8. 数据模型说明

### 8.1 请求模型（Request）

**LoginRequest** - 登录请求：
```java
public class LoginRequest {
    public String account;   // 账号（手机号或邮箱）
    public String password;  // 密码
}
```

**ChatRequest** - 聊天请求：
```java
public class ChatRequest {
    public String message;        // 消息内容
    public String conversationId; // 会话ID
    public String model;          // 使用的AI模型
}
```

### 8.2 响应模型（Response）

**ApiResponse** - 统一响应格式：
```java
public class ApiResponse<T> {
    public int code;      // 状态码（200表示成功）
    public String message; // 消息
    public T data;        // 数据（泛型，可以是任何类型）
}
```

**LoginResponse** - 登录响应：
```java
public class LoginResponse {
    public String userId;     // 用户ID
    public String username;   // 用户名
    public String token;      // 认证令牌
    public String memberType; // 会员类型
}
```

## 9. 如何添加新的 API 接口

### 9.1 步骤

1. **在 ApiService.java 中添加接口方法**
   ```java
   @POST("your/endpoint")
   Single<ApiResponse<YourResponse>> yourMethod(@Body YourRequest request);
   ```

2. **创建请求和响应模型**
   - 在 `model` 文件夹中创建 `YourRequest.java` 和 `YourResponse.java`

3. **在 UseCase 中使用**
   - 在对应的 UseCase 中调用这个接口

### 9.2 示例：添加获取用户信息接口

**步骤 1**：在 `ApiService.java` 中添加：
```java
@GET("user/info")
Single<ApiResponse<UserInfoResponse>> getUserInfo(@Header("Authorization") String token);
```

**步骤 2**：创建响应模型 `UserInfoResponse.java`：
```java
public class UserInfoResponse {
    public String userId;
    public String username;
    public String avatarUrl;
}
```

**步骤 3**：在 UseCase 中调用：
```java
public Single<UserInfoResponse> execute(String token) {
    return apiService.getUserInfo("Bearer " + token)
        .map(response -> response.data);
}
```

## 10. 常见问题 FAQ

### Q1: 如何知道 API 地址是什么？

**A**: 询问后端开发人员，他们会提供：
- 开发环境地址
- 测试环境地址
- 生产环境地址

### Q2: 修改了 BASE_URL 后需要重启应用吗？

**A**: 需要重新编译运行应用，修改才会生效。

### Q3: 如何判断 API 配置是否正确？

**A**: 
1. 查看 Logcat 是否有网络请求日志
2. 尝试登录，看是否能收到服务器响应
3. 检查是否有连接错误

### Q4: 出现 SSL 证书错误怎么办？

**A**: 
- 开发环境：可以暂时使用 HTTP（不推荐生产环境）
- 生产环境：需要配置正确的 SSL 证书

### Q5: 如何调试网络请求？

**A**: 
1. 查看 Logcat 中的 OkHttp 日志
2. 使用浏览器工具（如 Postman）测试 API
3. 检查请求和响应的 JSON 格式

## 11. 安全注意事项

1. **不要硬编码敏感信息**
   - 不要在代码中写死密码、密钥等
   - 使用配置文件或环境变量

2. **使用 HTTPS**
   - 生产环境必须使用 HTTPS
   - HTTP 传输的数据可能被窃听

3. **保护 Token**
   - Token 应该安全存储（使用加密）
   - 不要将 Token 打印到日志中

## 12. 总结

配置 API 地址的简单步骤：
1. ✅ 打开 `RetrofitClient.java`
2. ✅ 找到 `BASE_URL`
3. ✅ 替换为你的实际地址
4. ✅ 保存并运行应用
5. ✅ 查看 Logcat 确认连接成功

记住：地址格式必须是 `https://example.com/` 这样的格式！

