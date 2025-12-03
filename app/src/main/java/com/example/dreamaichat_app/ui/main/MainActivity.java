package com.example.dreamaichat_app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.ui.chat.ChatFragment;
import com.example.dreamaichat_app.ui.chat.ChatViewModel;
import com.example.dreamaichat_app.ui.history.HistoryFragment;
import com.example.dreamaichat_app.ui.history.HistoryViewModel;
import com.example.dreamaichat_app.ui.login.LoginActivity;
import com.example.dreamaichat_app.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * 涓荤晫闈紝鍖呭惈椤堕儴宸ュ叿鏍忋€佸簳閮ㄥ鑸笌渚ц竟鏍忋€? */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private NavigationView navView;

    private ChatViewModel chatViewModel;
    private HistoryViewModel historyViewModel;

    private final Fragment chatFragment = new ChatFragment();
    private final Fragment historyFragment = new HistoryFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initViewModels();
        setupToolbar();
        setupBottomNavigation();
        setupDrawer();
        showFragment(chatFragment);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        navView = findViewById(R.id.nav_view);
    }

    private void initViewModels() {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_24);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private boolean isUpdatingNavigation = false;

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            // 防止递归调用
            if (isUpdatingNavigation) {
                return true;
            }
            if (item.getItemId() == R.id.nav_chat) {
                showFragment(chatFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_history) {
                showFragment(historyFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void setupDrawer() {
        navView.setNavigationItemSelectedListener(item -> {
            handleDrawerAction(item);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void handleDrawerAction(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_templates) {
            showFragment(new com.example.dreamaichat_app.ui.template.TemplateSelectionFragment());
        } else if (id == R.id.nav_graph) {
            showFragment(new com.example.dreamaichat_app.ui.graph.ConversationGraphFragment());
        } else if (id == R.id.nav_settings) {
            // 打开“我的”页并让用户在其中进入设置
            showFragment(profileFragment);
        } else if (id == R.id.nav_logout) {
            // 清理会话并返回登录页
            if (sessionManager != null) {
                sessionManager.clear();
            }
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
        
        // 更新底部导航栏选中状态（防止递归调用）
        isUpdatingNavigation = true;
        try {
            if (fragment == chatFragment) {
                bottomNavigation.setSelectedItemId(R.id.nav_chat);
            } else if (fragment == historyFragment) {
                bottomNavigation.setSelectedItemId(R.id.nav_history);
            } else if (fragment == profileFragment) {
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
            }
        } finally {
            isUpdatingNavigation = false;
        }
    }
    
    /**
     * 切换到聊天界面（供其他 Fragment 调用）
     */
    public void showChatFragment() {
        showFragment(chatFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_new_chat) {
            chatViewModel.startNewChat();
            Toast.makeText(this, getString(R.string.toast_new_chat), Toast.LENGTH_SHORT).show();
            showFragment(chatFragment);
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            showFragment(profileFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
