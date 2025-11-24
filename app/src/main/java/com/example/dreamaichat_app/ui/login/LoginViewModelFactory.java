package com.example.dreamaichat_app.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.dreamaichat_app.presentation.login.LoginViewModel;

/**
 * LoginViewModel 的工厂类
 * 用于创建 LoginViewModel 实例
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {
    
    private final Application application;
    
    public LoginViewModelFactory(Application application) {
        this.application = application;
    }
    
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

