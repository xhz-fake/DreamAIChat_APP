package com.example.dreamaichat_app.domain.usecase;

import android.content.Context;

import com.example.dreamaichat_app.data.entity.UserEntity;
import com.example.dreamaichat_app.data.remote.RetrofitClient;
import com.example.dreamaichat_app.data.remote.api.ApiService;
import com.example.dreamaichat_app.data.remote.model.ApiResponse;
import com.example.dreamaichat_app.data.remote.model.LoginRequest;
import com.example.dreamaichat_app.data.remote.model.LoginResponse;
import com.example.dreamaichat_app.data.remote.model.RegisterRequest;
import com.example.dreamaichat_app.data.repository.UserRepository;
import com.google.gson.Gson;

import java.io.IOException;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

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
        LoginRequest request = new LoginRequest();
        request.account = account;
        request.password = password;
        return performAuthCall(apiService.login(request), account, null);
    }

    /**
     * 注册并返回登录态
     */
    public Single<LoginResult> register(String account, String password, String username) {
        RegisterRequest request = new RegisterRequest();
        request.account = account;
        request.password = password;
        request.username = username;
        return performAuthCall(apiService.register(request), account, username);
    }

    private Single<LoginResult> performAuthCall(Single<ApiResponse<LoginResponse>> single,
                                               String account,
                                               String usernameFallback) {
        return single
            .subscribeOn(Schedulers.io())
            .flatMap(apiResponse -> {
                if (apiResponse.success && apiResponse.data != null) {
                    return saveUserAndBuildResult(account, usernameFallback, apiResponse.data);
                }
                String msg = apiResponse.message != null ? apiResponse.message : "请求失败";
                return Single.just(new LoginResult(false, null, null, msg));
            })
            .onErrorReturn(error -> new LoginResult(false, null, null, extractErrorMessage(error)));
    }

    private Single<LoginResult> saveUserAndBuildResult(String account,
                                                       String usernameFallback,
                                                       LoginResponse response) {
        UserEntity user = new UserEntity();
        user.account = account;
        user.username = response.username != null ? response.username : (usernameFallback != null ? usernameFallback : account);
        user.token = response.token;
        user.tokenExpireTime = response.expireTime;
        user.memberType = response.memberType != null ? response.memberType : "free";
        user.createdAt = System.currentTimeMillis();
        user.updatedAt = System.currentTimeMillis();
        return userRepository.insertOrUpdateUserWithId(user)
            .map(userId -> new LoginResult(true, response.token, userId, null));
    }

    private String extractErrorMessage(Throwable error) {
        if (error instanceof HttpException) {
            HttpException http = (HttpException) error;
            try {
                if (http.response() != null && http.response().errorBody() != null) {
                    String body = http.response().errorBody().string();
                    ApiResponse<?> apiError = new Gson().fromJson(body, ApiResponse.class);
                    if (apiError != null && apiError.message != null) {
                        return apiError.message;
                    }
                }
            } catch (IOException ignored) {
            }
            return "服务器返回错误(" + http.code() + ")";
        }
        return error.getMessage() != null ? error.getMessage() : "网络错误";
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

