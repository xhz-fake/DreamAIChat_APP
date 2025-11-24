package com.example.dreamaichat_app.presentation.login;

import com.example.dreamaichat_app.mvi.Intent;

/**
 * 登录模块的 Intent
 * 表示用户的所有登录相关操作
 */
public class LoginIntent implements Intent {
    
    /**
     * 登录意图
     */
    public static class Login implements Intent {
        public final String account;
        public final String password;
        
        public Login(String account, String password) {
            this.account = account;
            this.password = password;
        }
    }
    
    /**
     * 注册意图
     */
    public static class Register implements Intent {
        public final String account;
        public final String password;
        public final String username;
        
        public Register(String account, String password, String username) {
            this.account = account;
            this.password = password;
            this.username = username;
        }
    }
    
    /**
     * 微信登录意图
     */
    public static class WeChatLogin implements Intent {}
    
    /**
     * 苹果登录意图
     */
    public static class AppleLogin implements Intent {}
}
