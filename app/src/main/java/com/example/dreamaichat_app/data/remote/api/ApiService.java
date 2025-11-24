package com.example.dreamaichat_app.data.remote.api;

import com.example.dreamaichat_app.data.remote.model.ApiResponse;
import com.example.dreamaichat_app.data.remote.model.ChatRequest;
import com.example.dreamaichat_app.data.remote.model.ChatResponse;
import com.example.dreamaichat_app.data.remote.model.LoginRequest;
import com.example.dreamaichat_app.data.remote.model.LoginResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;

/**
 * API 服务接口
 * 
 * 这个接口定义了所有与后端服务器通信的 API 方法
 * Retrofit 会根据这些注解自动生成实现代码
 * 
 * 工作原理：
 * 1. 使用注解（@POST, @GET 等）定义 HTTP 方法和路径
 * 2. 使用 @Body 注解传递请求体（JSON 数据）
 * 3. 使用 @Header 注解传递请求头（如认证 Token）
 * 4. Retrofit 会自动将 Java 对象转换为 JSON，并发送 HTTP 请求
 * 5. 返回的 JSON 响应会自动转换为 Java 对象
 */
public interface ApiService {
    
    /**
     * 用户登录接口
     * 
     * @param request 登录请求，包含账号和密码
     * @return 返回一个 Single<ApiResponse<LoginResponse>>
     *         Single 是 RxJava 的一个类型，表示只会发出一个值或错误
     *         ApiResponse 是统一的响应格式，包含 code、message、data
     *         LoginResponse 是登录成功后的数据，包含 userId、token 等
     * 
     * 实际请求：
     * POST https://api.example.com/auth/login
     * Content-Type: application/json
     * Body: {"account":"user@example.com","password":"123456"}
     */
    @POST("auth/login")
    Single<ApiResponse<LoginResponse>> login(@Body LoginRequest request);
    
    /**
     * 发送聊天消息接口
     * 
     * @param token 认证令牌，放在 HTTP 请求头中
     *              格式通常是 "Bearer your_token_here"
     * @param request 聊天请求，包含消息内容、会话ID、模型等
     * @return 返回 AI 的回复
     * 
     * 实际请求：
     * POST https://api.example.com/chat/send
     * Authorization: Bearer your_token_here
     * Content-Type: application/json
     * Body: {"message":"你好","conversationId":"123","model":"gpt-4"}
     */
    @POST("chat/send")
    Single<ApiResponse<ChatResponse>> sendMessage(
        @Header("Authorization") String token,
        @Body ChatRequest request
    );
}

