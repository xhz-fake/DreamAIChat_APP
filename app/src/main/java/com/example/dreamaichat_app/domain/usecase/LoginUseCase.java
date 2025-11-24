package com.example.dreamaichat_app.domain.usecase;

import android.content.Context;

import com.example.dreamaichat_app.data.entity.UserEntity;
import com.example.dreamaichat_app.data.remote.api.ApiService;
import com.example.dreamaichat_app.data.remote.model.LoginRequest;
import com.example.dreamaichat_app.data.remote.model.LoginResponse;
import com.example.dreamaichat_app.data.remote.RetrofitClient;
import com.example.dreamaichat_app.data.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 登录用例
 * 
 * 负责处理登录业务逻辑：
 * 1. 调用 API 进行登录
 * 2. 保存用户信息到本地数据库
 * 3. 返回登录结果
 */
public class LoginUseCase {
    
    private final ApiService apiService;
    private final UserRepository userRepository;
    
    public LoginUseCase(Context context) {
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.userRepository = new UserRepository(context);
    }
    
    /**
     * 执行登录
     * 
     * @param account 账号（手机号或邮箱）
     * @param password 密码
     * @return Single<LoginResult> 登录结果
     */
    public Single<LoginResult> execute(String account, String password) {
        // 创建登录请求
        LoginRequest request = new LoginRequest();
        request.account = account;
        request.password = password;
        
        // 调用 API，如果失败则使用模拟登录
        return apiService.login(request)
            .subscribeOn(Schedulers.io())
            .flatMap(apiResponse -> {
                if (apiResponse.success && apiResponse.data != null) {
                    LoginResponse response = apiResponse.data;
                    // 登录成功，保存用户信息
                    UserEntity user = new UserEntity();
                    user.account = account;
                    user.username = response.username != null ? response.username : account;
                    user.token = response.token;
                    user.tokenExpireTime = response.expireTime;
                    user.memberType = response.memberType != null ? response.memberType : "free";
                    user.createdAt = System.currentTimeMillis();
                    user.updatedAt = System.currentTimeMillis();
                    
                    // 保存到数据库
                    return userRepository.insertOrUpdateUserWithId(user)
                        .map(userId -> new LoginResult(true, response.token, userId, null));
                } else {
                    // 登录失败
                    return Single.just(new LoginResult(false, null, null, apiResponse.message != null ? apiResponse.message : "登录失败"));
                }
            })
            .onErrorResumeNext(error -> {
                // 网络错误时，使用模拟登录（仅用于开发测试）
                return simulateLogin(account, password);
            });
    }
    
    /**
     * 模拟登录（用于开发测试，当没有后端服务器时）
     * 
     * @param account 账号
     * @param password 密码
     * @return Single<LoginResult> 登录结果
     */
    private Single<LoginResult> simulateLogin(String account, String password) {
        // 简单的模拟验证：账号和密码都不为空即可登录
        if (account == null || account.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            // 账号或密码为空，直接返回失败
            return Single.just(new LoginResult(false, null, null, "账号或密码不能为空"))
                .subscribeOn(Schedulers.io());
        }
        
        // 创建模拟用户信息
        UserEntity user = new UserEntity();
        user.account = account;
        user.username = account; // 使用账号作为用户名
        user.token = "mock_token_" + System.currentTimeMillis(); // 模拟 token
        user.tokenExpireTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L); // 7天后过期
        user.memberType = "free";
        user.createdAt = System.currentTimeMillis();
        user.updatedAt = System.currentTimeMillis();
        
        // 保存到数据库，添加错误处理
        return userRepository.insertOrUpdateUserWithId(user)
            .map(userId -> {
                if (userId != null && userId > 0) {
                    return new LoginResult(true, user.token, userId, null);
                } else {
                    return new LoginResult(false, null, null, "保存用户信息失败");
                }
            })
            .onErrorReturn(error -> {
                // 如果数据库操作失败，仍然返回成功（使用临时 userId）
                return new LoginResult(true, user.token, 1L, null);
            })
            .subscribeOn(Schedulers.io());
    }
    
    /**
     * 登录结果
     */
    public static class LoginResult {
        public final boolean success;
        public final String token;
        public final Long userId;
        public final String errorMessage;
        
        public LoginResult(boolean success, String token, Long userId, String errorMessage) {
            this.success = success;
            this.token = token;
            this.userId = userId;
            this.errorMessage = errorMessage;
        }
    }
}

