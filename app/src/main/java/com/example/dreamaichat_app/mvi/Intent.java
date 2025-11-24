package com.example.dreamaichat_app.mvi;

/**
 * Intent 接口
 * 
 * MVI 架构中的 Intent 表示用户的意图（操作）
 * 例如：登录、发送消息、删除会话等
 * 
 * 每个功能模块应该定义自己的 Intent 类，实现此接口
 */
public interface Intent {
    // Intent 是一个标记接口，具体的 Intent 由各个模块定义
}

