package com.example.dreamaichat_app.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.ui.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 账号设置 / 编辑资料界面
 */
public class AccountSettingsFragment extends Fragment {

    private TextInputEditText etUsername;
    private TextView tvPhone;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        etUsername = view.findViewById(R.id.et_username);
        tvPhone = view.findViewById(R.id.tv_phone_value);
        View btnSave = view.findViewById(R.id.btn_save);
        View btnDeleteAccount = view.findViewById(R.id.btn_delete_account);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // 简单示例：从 SharedPreferences 读取并显示昵称和账号
        String storedName = sessionManager.getDisplayName();
        if (!TextUtils.isEmpty(storedName)) {
            etUsername.setText(storedName);
        }
        String account = sessionManager.getAccount();
        if (!TextUtils.isEmpty(account)) {
            tvPhone.setText(account);
        }

        btnSave.setOnClickListener(v -> {
            String name = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            sessionManager.setDisplayName(name);
            Toast.makeText(requireContext(), R.string.profile_save_success, Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });

        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.account_delete_title)
            .setMessage(R.string.account_delete_message)
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                // 当前版本：本地清理 + 返回登录页
                SessionManager manager = new SessionManager(requireContext());
                manager.clear();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            })
            .show();
    }
}


