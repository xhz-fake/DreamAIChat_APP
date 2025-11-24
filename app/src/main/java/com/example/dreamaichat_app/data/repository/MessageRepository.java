package com.example.dreamaichat_app.data.repository;

import android.content.Context;

import com.example.dreamaichat_app.data.dao.MessageDao;
import com.example.dreamaichat_app.data.database.AppDatabase;
import com.example.dreamaichat_app.data.entity.MessageEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 消息数据仓库
 * 
 * 负责管理消息相关的数据操作
 */
public class MessageRepository {
    
    private final MessageDao messageDao;
    
    /**
     * 构造函数
     * 
     * @param context 应用上下文
     */
    public MessageRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.messageDao = database.messageDao();
    }
    
    /**
     * 插入消息
     * 
     * @param message 消息实体
     * @return Completable - 操作完成通知
     */
    public Completable insertMessage(MessageEntity message) {
        return messageDao.insertMessage(message);
    }
    
    /**
     * 插入或更新消息
     * 
     * @param message 消息实体
     * @return Completable - 操作完成通知
     */
    public Completable insertOrUpdateMessage(MessageEntity message) {
        return messageDao.insertMessage(message);
    }
    
    /**
     * 更新消息
     * 
     * @param message 消息实体
     * @return Completable - 操作完成通知
     */
    public Completable updateMessage(MessageEntity message) {
        return messageDao.updateMessage(message);
    }
    
    /**
     * 批量插入消息
     * 
     * @param messages 消息列表
     * @return Completable - 操作完成通知
     */
    public Completable insertMessages(List<MessageEntity> messages) {
        return messageDao.insertMessages(messages);
    }
    
    /**
     * 根据ID查询消息
     * 
     * @param id 消息ID
     * @return Maybe<MessageEntity> - 消息实体，如果不存在返回空
     */
    public Maybe<MessageEntity> getMessageById(Long id) {
        return messageDao.getMessageById(id);
    }
    
    /**
     * 根据会话ID查询所有消息
     * 
     * @param conversationId 会话ID
     * @return Single<List<MessageEntity>> - 消息列表
     */
    public Single<List<MessageEntity>> getMessagesByConversationId(Long conversationId) {
        return messageDao.getMessagesByConversationId(conversationId);
    }
    
    /**
     * 查询会话的最后一条消息
     * 
     * @param conversationId 会话ID
     * @return Maybe<MessageEntity> - 最后一条消息，如果不存在返回空
     */
    public Maybe<MessageEntity> getLastMessageByConversationId(Long conversationId) {
        return messageDao.getLastMessageByConversationId(conversationId);
    }
    
    /**
     * 查询发送失败的消息
     * 
     * @param conversationId 会话ID
     * @return Single<List<MessageEntity>> - 失败的消息列表
     */
    public Single<List<MessageEntity>> getFailedMessages(Long conversationId) {
        return messageDao.getFailedMessages(conversationId);
    }
    
    /**
     * 删除消息
     * 
     * @param id 消息ID
     * @return Completable - 操作完成通知
     */
    public Completable deleteMessage(Long id) {
        return messageDao.deleteMessage(id);
    }
    
    /**
     * 删除会话的所有消息
     * 
     * @param conversationId 会话ID
     * @return Completable - 操作完成通知
     */
    public Completable deleteMessagesByConversationId(Long conversationId) {
        return messageDao.deleteMessagesByConversationId(conversationId);
    }
    
    /**
     * 更新消息状态
     * 
     * @param id 消息ID
     * @param status 新状态（sending, success, failed）
     * @param errorMessage 错误信息（如果失败）
     * @return Completable - 操作完成通知
     */
    public Completable updateMessageStatus(Long id, String status, String errorMessage) {
        return messageDao.updateMessageStatus(id, status, errorMessage, System.currentTimeMillis());
    }
    
    /**
     * 统计会话的消息数量
     * 
     * @param conversationId 会话ID
     * @return Single<Integer> - 消息数量
     */
    public Single<Integer> getMessageCount(Long conversationId) {
        return messageDao.getMessageCount(conversationId);
    }
}

