package com.example.dreamaichat_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dreamaichat_app.data.entity.UserEntity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 用户数据访问对象（DAO）
 * 
 * 定义对 users 表的数据库操作
 * 使用 RxJava 3 进行异步处理
 */
@Dao
public interface UserDao {
    
    /**
     * 插入用户
     * 如果用户已存在（根据 account），则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(UserEntity user);
    
    /**
     * 插入或更新用户（返回ID）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertOrUpdateUser(UserEntity user);
    
    /**
     * 更新用户信息
     */
    @Update
    Completable updateUser(UserEntity user);
    
    /**
     * 更新用户信息（返回影响行数）
     */
    @Update
    Single<Integer> updateUserWithResult(UserEntity user);
    
    /**
     * 根据账号查询用户
     * 
     * @param account 用户账号（手机号或邮箱）
     * @return 用户实体，如果不存在返回空
     */
    @Query("SELECT * FROM users WHERE account = :account LIMIT 1")
    Maybe<UserEntity> getUserByAccount(String account);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户实体，如果不存在返回空
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    Maybe<UserEntity> getUserById(Long id);
    
    /**
     * 检查用户是否存在
     * 
     * @param account 用户账号
     * @return true 如果用户存在，false 如果不存在
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE account = :account)")
    Single<Boolean> userExists(String account);
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    @Query("DELETE FROM users WHERE id = :id")
    Completable deleteUser(Long id);
    
    /**
     * 删除所有用户（用于测试或重置）
     */
    @Query("DELETE FROM users")
    Completable deleteAllUsers();
}
