package com.example.dreamaichat_app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 用户实体类
 * 
 * Room 数据库中的用户表
 * 用于存储用户的基本信息
 */
@Entity(tableName = "users")
public class UserEntity {
    
    /**
     * 用户ID（主键）
     * 自动生成
     */
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    /**
     * 用户账号（手机号或邮箱）
     * 唯一标识，不能为空
     */
    public String account;
    
    /**
     * 用户密码（加密后）
     */
    public String password;
    
    /**
     * 用户名
     */
    public String username;
    
    /**
     * 用户头像URL
     */
    public String avatarUrl;
    
    /**
     * 会员类型
     * 例如：free（免费版）、premium（高级版）
     */
    public String memberType;
    
    /**
     * 登录Token
     * 用于API请求认证
     */
    public String token;
    
    /**
     * Token过期时间（时间戳）
     */
    public Long tokenExpireTime;
    
    /**
     * 创建时间（时间戳）
     */
    public Long createdAt;
    
    /**
     * 更新时间（时间戳）
     */
    public Long updatedAt;
    
    // 构造函数
    public UserEntity() {
    }
    
    public UserEntity(String account, String password, String username) {
        this.account = account;
        this.password = password;
        this.username = username;
        this.memberType = "free"; // 默认免费版
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

