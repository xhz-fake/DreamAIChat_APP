package com.example.dreamaichat_app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 会话实体类
 * 
 * Room 数据库中的会话表
 * 用于存储用户的对话会话信息
 */
@Entity(
    tableName = "conversations",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("userId")}
)
public class ConversationEntity {
    
    /**
     * 会话ID（主键）
     * 自动生成
     */
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    /**
     * 用户ID（外键）
     * 关联到 users 表
     */
    public Long userId;
    
    /**
     * 会话标题
     * 通常取第一条消息的前几个字符
     */
    public String title;
    
    /**
     * 使用的AI模型
     * 例如：gpt-4, gpt-3.5-turbo
     */
    public String model;
    
    /**
     * 消息总数
     */
    public Integer messageCount;
    
    /**
     * 是否收藏
     */
    public Boolean isFavorite;
    
    /**
     * 最后一条消息的内容（预览）
     */
    public String lastMessagePreview;
    
    /**
     * 最后一条消息的时间（时间戳）
     */
    public Long lastMessageTime;
    
    /**
     * 创建时间（时间戳）
     */
    public Long createdAt;
    
    /**
     * 更新时间（时间戳）
     */
    public Long updatedAt;
    
    // 构造函数
    public ConversationEntity() {
    }
    
    public ConversationEntity(Long userId, String title, String model) {
        this.userId = userId;
        this.title = title;
        this.model = model;
        this.messageCount = 0;
        this.isFavorite = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

