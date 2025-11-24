package com.example.dreamaichat_app.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.ui.chat.ChatFragment;
import com.example.dreamaichat_app.ui.history.HistoryFragment;
import com.example.dreamaichat_app.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * 主界面
 * 包含底部导航和侧边栏
 */
public class MainActivity extends AppCompatActivity {
    
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private NavigationView navView;
    
    private ChatFragment chatFragment;
    private HistoryFragment historyFragment;
    private ProfileFragment profileFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupDrawer();
        
        // 默认显示聊天界面
        showFragment(new ChatFragment());
    }
    
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        navView = findViewById(R.id.nav_view);
        
        chatFragment = new ChatFragment();
        historyFragment = new HistoryFragment();
        profileFragment = new ProfileFragment();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.menu, R.string.menu
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chat) {
                showFragment(chatFragment);
                return true;
            } else if (itemId == R.id.nav_history) {
                showFragment(historyFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            return false;
        });
    }
    
    private void setupDrawer() {
        navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // TODO: 处理侧边栏菜单项点击
            drawerLayout.closeDrawers();
            return true;
        });
    }
    
    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }
}

