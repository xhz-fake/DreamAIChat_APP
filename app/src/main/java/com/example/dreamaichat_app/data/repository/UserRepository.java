package com.example.dreamaichat_app.data.repository;

import android.content.Context;

import com.example.dreamaichat_app.data.dao.UserDao;
import com.example.dreamaichat_app.data.database.AppDatabase;
import com.example.dreamaichat_app.data.entity.UserEntity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 用户数据仓库
 * 统一管理用户数据的访问
 */
public class UserRepository {
    
    private final UserDao userDao;
    
    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
    }
    
    /**
     * 根据用户ID获取用户
     */
    public Maybe<UserEntity> getUserById(Long userId) {
        return userDao.getUserById(userId);
    }
    
    /**
     * 根据账号获取用户
     */
    public Maybe<UserEntity> getUserByAccount(String account) {
        return userDao.getUserByAccount(account);
    }
    
    /**
     * 插入或更新用户
     */
    public Completable insertOrUpdateUser(UserEntity user) {
        return userDao.insertUser(user);
    }
    
    /**
     * 插入或更新用户（返回ID）
     */
    public Single<Long> insertOrUpdateUserWithId(UserEntity user) {
        return userDao.insertOrUpdateUser(user);
    }
    
    /**
     * 更新用户
     */
    public Completable updateUser(UserEntity user) {
        return userDao.updateUser(user);
    }
    
    /**
     * 删除用户
     */
    public Completable deleteUser(Long userId) {
        return userDao.deleteUser(userId);
    }
    
    /**
     * 检查用户是否存在
     */
    public Single<Boolean> userExists(String account) {
        return userDao.userExists(account);
    }
}
