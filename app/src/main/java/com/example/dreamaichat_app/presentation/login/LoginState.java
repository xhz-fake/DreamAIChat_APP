package com.example.dreamaichat_app.presentation.login;

import com.example.dreamaichat_app.mvi.State;

/**
 * 登录模块的 State
 * 表示登录界面的所有可能状态
 */
public class LoginState implements State {
    
    /**
     * 初始状态
     */
    public static class Idle extends LoginState {}
    
    /**
     * 加载中
     */
    public static class Loading extends LoginState {}
    
    /**
     * 登录成功
     */
    public static class Success extends LoginState {
        public final String token;
        public final Long userId;
        
        public Success(String token, Long userId) {
            this.token = token;
            this.userId = userId;
        }
    }
    
    /**
     * 登录失败
     */
    public static class Error extends LoginState {
        public final String message;
        
        public Error(String message) {
            this.message = message;
        }
    }
}
