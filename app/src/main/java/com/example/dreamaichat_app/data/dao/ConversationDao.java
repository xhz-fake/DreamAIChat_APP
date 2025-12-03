package com.example.dreamaichat_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dreamaichat_app.data.entity.ConversationEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 会话数据访问对象（DAO）
 * 
 * 定义对 conversations 表的数据库操作
 * 使用 RxJava 3 进行异步处理
 * 
 * DAO 的作用：
 * 1. 封装数据库操作（增删改查）
 * 2. 提供类型安全的 SQL 查询
 * 3. 自动生成 SQL 代码（Room 框架处理）
 */
@Dao
public interface ConversationDao {
    
    /**
     * 插入会话
     * 如果会话已存在（根据 id），则替换
     * 
     * @param conversation 会话实体
     * @return Completable - 操作完成通知
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertConversation(ConversationEntity conversation);
    
    /**
     * 批量插入会话
     * 
     * @param conversations 会话列表
     * @return Completable - 操作完成通知
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertConversations(List<ConversationEntity> conversations);
    
    /**
     * 更新会话信息
     * 
     * @param conversation 会话实体
     * @return Completable - 操作完成通知
     */
    @Update
    Completable updateConversation(ConversationEntity conversation);
    
    /**
     * 根据ID查询会话
     * 
     * @param id 会话ID
     * @return Maybe<ConversationEntity> - 会话实体，如果不存在返回空
     */
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    Maybe<ConversationEntity> getConversationById(Long id);
    
    /**
     * 根据用户ID查询所有会话
     * 按最后消息时间倒序排列（最新的在前）
     * 
     * @param userId 用户ID
     * @return Single<List<ConversationEntity>> - 会话列表
     */
    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY lastMessageTime DESC")
    Single<List<ConversationEntity>> getConversationsByUserId(Long userId);
    
    /**
     * 查询收藏的会话
     * 
     * @param userId 用户ID
     * @return Single<List<ConversationEntity>> - 收藏的会话列表
     */
    @Query("SELECT * FROM conversations WHERE userId = :userId AND isFavorite = 1 ORDER BY lastMessageTime DESC")
    Single<List<ConversationEntity>> getFavoriteConversations(Long userId);
    
    /**
     * 搜索会话（根据标题）
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return Single<List<ConversationEntity>> - 匹配的会话列表
     */
    @Query("SELECT * FROM conversations WHERE userId = :userId AND title LIKE '%' || :keyword || '%' ORDER BY lastMessageTime DESC")
    Single<List<ConversationEntity>> searchConversations(Long userId, String keyword);
    
    /**
     * 删除会话
     * 
     * @param id 会话ID
     * @return Completable - 操作完成通知
     */
    @Query("DELETE FROM conversations WHERE id = :id")
    Completable deleteConversation(Long id);
    
    /**
     * 删除用户的所有会话
     * 
     * @param userId 用户ID
     * @return Completable - 操作完成通知
     */
    @Query("DELETE FROM conversations WHERE userId = :userId")
    Completable deleteConversationsByUserId(Long userId);
    
    /**
     * 更新会话的最后消息信息
     * 
     * @param id 会话ID
     * @param lastMessagePreview 最后消息预览
     * @param lastMessageTime 最后消息时间
     * @return Completable - 操作完成通知
     */
    @Query("UPDATE conversations SET lastMessagePreview = :lastMessagePreview, lastMessageTime = :lastMessageTime, updatedAt = :lastMessageTime WHERE id = :id")
    Completable updateLastMessage(Long id, String lastMessagePreview, Long lastMessageTime);
    
    /**
     * 更新会话的消息数量
     * 
     * @param id 会话ID
     * @param messageCount 消息数量
     * @return Completable - 操作完成通知
     */
    @Query("UPDATE conversations SET messageCount = :messageCount, updatedAt = :updatedAt WHERE id = :id")
    Completable updateMessageCount(Long id, Integer messageCount, Long updatedAt);

    /**
     * 更新会话的基础信息（标题、模型、最后消息等）
     */
    @Query("UPDATE conversations SET title = :title, model = :model, messageCount = :messageCount, lastMessagePreview = :lastMessagePreview, lastMessageTime = :lastMessageTime, updatedAt = :updatedAt WHERE id = :id")
    Completable updateConversationMeta(Long id,
                                       String title,
                                       String model,
                                       Integer messageCount,
                                       String lastMessagePreview,
                                       Long lastMessageTime,
                                       Long updatedAt);

    /**
     * 同步查询：获取某个用户的所有会话（用于统计）
     */
    @Query("SELECT * FROM conversations WHERE userId = :userId")
    List<ConversationEntity> getAllForUserSync(Long userId);
}

