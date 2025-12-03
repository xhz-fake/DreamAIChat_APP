package com.example.dreamaichat_app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "dream_session";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FIRST_LOGIN_TIME = "first_login_time";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_ACCOUNT = "account";
    public static final int ROUTING_AUTO = 0;
    public static final int ROUTING_SPEED = 1;
    public static final int ROUTING_QUALITY = 2;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, long userId) {
        long now = System.currentTimeMillis();
        SharedPreferences.Editor editor = prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId);
        if (!prefs.contains(KEY_FIRST_LOGIN_TIME)) {
            editor.putLong(KEY_FIRST_LOGIN_TIME, now);
        }
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public long getFirstLoginTime() {
        return prefs.getLong(KEY_FIRST_LOGIN_TIME, -1L);
    }

    public void setDisplayName(String name) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply();
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, null);
    }

    public void setAccount(String account) {
        prefs.edit().putString(KEY_ACCOUNT, account).apply();
    }

    public String getAccount() {
        return prefs.getString(KEY_ACCOUNT, null);
    }

    public void setRoutingMode(int mode) {
        prefs.edit().putInt("routing_mode", mode).apply();
    }

    public int getRoutingMode() {
        return prefs.getInt("routing_mode", ROUTING_AUTO);
    }

    public void clear() {
        // 清除所有会话数据，包括token、userId等
        // 注意：不清除用户名和账户信息，以便下次登录时保持一致
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_FIRST_LOGIN_TIME)
            .apply();
    }
}

