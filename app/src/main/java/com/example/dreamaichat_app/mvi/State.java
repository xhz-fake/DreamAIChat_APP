package com.example.dreamaichat_app.mvi;

/**
 * State 接口
 * 
 * MVI 架构中的 State 表示 UI 的状态
 * 例如：加载中、成功、失败等
 * 
 * 每个功能模块应该定义自己的 State 类，实现此接口
 */
public interface State {
    // State 是一个标记接口，具体的 State 由各个模块定义
}

