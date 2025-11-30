package com.example.dreamaichat_app.presentation.login;

import android.app.Application;

import com.example.dreamaichat_app.domain.usecase.LoginUseCase;
import com.example.dreamaichat_app.mvi.BaseViewModel;
import com.example.dreamaichat_app.mvi.Intent;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 登录 ViewModel
 * 
 * 处理登录相关的业务逻辑和状态管理
 */
public class LoginViewModel extends BaseViewModel<Intent, LoginState> {
    
    private final LoginUseCase loginUseCase;
    
    public LoginViewModel(Application application) {
        super();
        this.loginUseCase = new LoginUseCase(application);
        // 初始状态
        updateState(new LoginState.Idle());
    }
    
    @Override
    public void processIntent(Intent intent) {
        if (intent instanceof LoginIntent.Login) {
            LoginIntent.Login login = (LoginIntent.Login) intent;
            handleLogin(login.account, login.password);
        } else if (intent instanceof LoginIntent.Register) {
            LoginIntent.Register register = (LoginIntent.Register) intent;
            handleRegister(register.account, register.password, register.username);
        } else if (intent instanceof LoginIntent.WeChatLogin) {
            handleWeChatLogin();
        } else if (intent instanceof LoginIntent.AppleLogin) {
            handleAppleLogin();
        }
    }
    
    /**
     * 处理登录
     */
    private void handleLogin(String account, String password) {
        updateState(new LoginState.Loading());
        
        disposables.add(
            loginUseCase.execute(account, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        if (result.success) {
                            updateState(new LoginState.Success(result.token, result.userId));
                        } else {
                            updateState(new LoginState.Error(result.errorMessage != null ? result.errorMessage : "登录失败"));
                        }
                    },
                    error -> {
                        updateState(new LoginState.Error(error.getMessage() != null ? error.getMessage() : "网络错误"));
                    }
                )
        );
    }
    
    /**
     * 处理注册
     */
    private void handleRegister(String account, String password, String username) {
        updateState(new LoginState.Loading());
        disposables.add(
            loginUseCase.register(account, password, username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        if (result.success) {
                            updateState(new LoginState.Success(result.token, result.userId));
                        } else {
                            updateState(new LoginState.Error(result.errorMessage != null ? result.errorMessage : "注册失败"));
                        }
                    },
                    error -> updateState(new LoginState.Error(error.getMessage() != null ? error.getMessage() : "网络错误"))
                )
        );
    }
    
    /**
     * 处理微信登录
     */
    private void handleWeChatLogin() {
        // TODO: 实现微信登录逻辑
        updateState(new LoginState.Error("微信登录功能暂未实现"));
    }
    
    /**
     * 处理苹果登录
     */
    private void handleAppleLogin() {
        // TODO: 实现苹果登录逻辑
        updateState(new LoginState.Error("Apple登录功能暂未实现"));
    }
}
