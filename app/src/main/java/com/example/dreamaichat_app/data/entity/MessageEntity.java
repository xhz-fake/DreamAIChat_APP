package com.example.dreamaichat_app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 消息实体类
 * 
 * Room 数据库中的消息表
 * 用于存储会话中的每条消息
 */
@Entity(
    tableName = "messages",
    foreignKeys = @ForeignKey(
        entity = ConversationEntity.class,
        parentColumns = "id",
        childColumns = "conversationId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("conversationId")}
)
public class MessageEntity {
    
    /**
     * 消息ID（主键）
     * 自动生成
     */
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    /**
     * 会话ID（外键）
     * 关联到 conversations 表
     */
    public Long conversationId;
    
    /**
     * 消息类型
     * user: 用户消息
     * assistant: AI消息
     * system: 系统消息
     */
    public String role; // "user", "assistant", "system"
    
    /**
     * 消息内容
     */
    public String content;
    
    /**
     * 消息状态
     * sending: 发送中
     * success: 发送成功
     * failed: 发送失败
     */
    public String status; // "sending", "success", "failed"
    
    /**
     * 错误信息（如果发送失败）
     */
    public String errorMessage;
    
    /**
     * 创建时间（时间戳）
     */
    public Long createdAt;
    
    /**
     * 更新时间（时间戳）
     */
    public Long updatedAt;
    
    // 构造函数
    public MessageEntity() {
    }
    
    public MessageEntity(Long conversationId, String role, String content) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.status = "success";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

