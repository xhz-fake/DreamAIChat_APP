package com.example.dreamaichat_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dreamaichat_app.data.entity.MessageEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 消息数据访问对象（DAO）
 * 
 * 定义对 messages 表的数据库操作
 * 使用 RxJava 3 进行异步处理
 */
@Dao
public interface MessageDao {
    
    /**
     * 插入消息
     * 如果消息已存在（根据 id），则替换
     * 
     * @param message 消息实体
     * @return Completable - 操作完成通知
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessage(MessageEntity message);
    
    /**
     * 批量插入消息
     * 
     * @param messages 消息列表
     * @return Completable - 操作完成通知
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMessages(List<MessageEntity> messages);
    
    /**
     * 更新消息
     * 
     * @param message 消息实体
     * @return Completable - 操作完成通知
     */
    @Update
    Completable updateMessage(MessageEntity message);
    
    /**
     * 根据ID查询消息
     * 
     * @param id 消息ID
     * @return Maybe<MessageEntity> - 消息实体，如果不存在返回空
     */
    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    Maybe<MessageEntity> getMessageById(Long id);
    
    /**
     * 根据会话ID查询所有消息
     * 按创建时间正序排列（最早的消息在前）
     * 
     * @param conversationId 会话ID
     * @return Single<List<MessageEntity>> - 消息列表
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    Single<List<MessageEntity>> getMessagesByConversationId(Long conversationId);
    
    /**
     * 查询会话的最后一条消息
     * 
     * @param conversationId 会话ID
     * @return Maybe<MessageEntity> - 最后一条消息，如果不存在返回空
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT 1")
    Maybe<MessageEntity> getLastMessageByConversationId(Long conversationId);
    
    /**
     * 查询发送失败的消息
     * 
     * @param conversationId 会话ID
     * @return Single<List<MessageEntity>> - 失败的消息列表
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND status = 'failed' ORDER BY createdAt DESC")
    Single<List<MessageEntity>> getFailedMessages(Long conversationId);
    
    /**
     * 删除消息
     * 
     * @param id 消息ID
     * @return Completable - 操作完成通知
     */
    @Query("DELETE FROM messages WHERE id = :id")
    Completable deleteMessage(Long id);
    
    /**
     * 删除会话的所有消息
     * 
     * @param conversationId 会话ID
     * @return Completable - 操作完成通知
     */
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    Completable deleteMessagesByConversationId(Long conversationId);
    
    /**
     * 更新消息状态
     * 
     * @param id 消息ID
     * @param status 新状态（sending, success, failed）
     * @param errorMessage 错误信息（如果失败）
     * @return Completable - 操作完成通知
     */
    @Query("UPDATE messages SET status = :status, errorMessage = :errorMessage, updatedAt = :updatedAt WHERE id = :id")
    Completable updateMessageStatus(Long id, String status, String errorMessage, Long updatedAt);
    
    /**
     * 统计会话的消息数量
     * 
     * @param conversationId 会话ID
     * @return Single<Integer> - 消息数量
     */
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    Single<Integer> getMessageCount(Long conversationId);
}

