package com.example.dreamaichat_app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.presentation.login.LoginIntent;
import com.example.dreamaichat_app.presentation.login.LoginState;
import com.example.dreamaichat_app.presentation.login.LoginViewModel;
import com.example.dreamaichat_app.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText etAccount;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView btnRegister;
    private ProgressBar progressBar;
    
    private LoginViewModel viewModel;
    private Disposable stateDisposable;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        initViewModel();
        setupListeners();
        observeState();
    }
    
    private void initViews() {
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void initViewModel() {
        LoginViewModelFactory factory = new LoginViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
        sessionManager = new SessionManager(this);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());
    }
    
    private void observeState() {
        stateDisposable = viewModel.getState().subscribe(state -> {
            if (state instanceof LoginState.Idle) {
                showLoading(false);
            } else if (state instanceof LoginState.Loading) {
                showLoading(true);
            } else if (state instanceof LoginState.Success) {
                LoginState.Success success = (LoginState.Success) state;
                showLoading(false);
                handleLoginSuccess(success.token, success.userId);
            } else if (state instanceof LoginState.Error) {
                LoginState.Error error = (LoginState.Error) state;
                showLoading(false);
                showError(error.message);
            }
        });
    }
    
    private void handleLogin() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        viewModel.processIntent(new LoginIntent.Login(account, password));
    }
    
    private void handleRegister() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        viewModel.processIntent(new LoginIntent.Register(account, password, account));
    }
    
    private void handleLoginSuccess(String token, Long userId) {
        if (token != null && userId != null) {
            sessionManager.saveSession(token, userId);
            sessionManager.setAccount(etAccount.getText() != null ? etAccount.getText().toString().trim() : null);
        }
        // 跳转到主界面
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stateDisposable != null && !stateDisposable.isDisposed()) {
            stateDisposable.dispose();
        }
    }
}

