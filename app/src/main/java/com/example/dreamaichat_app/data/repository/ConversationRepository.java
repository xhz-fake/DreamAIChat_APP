package com.example.dreamaichat_app.data.repository;

import android.content.Context;

import com.example.dreamaichat_app.data.dao.ConversationDao;
import com.example.dreamaichat_app.data.database.AppDatabase;
import com.example.dreamaichat_app.data.entity.ConversationEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * 会话数据仓库
 * 
 * 负责管理会话相关的数据操作
 */
public class ConversationRepository {
    
    private final ConversationDao conversationDao;
    
    /**
     * 构造函数
     * 
     * @param context 应用上下文
     */
    public ConversationRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.conversationDao = database.conversationDao();
    }
    
    /**
     * 插入或更新会话
     * 
     * @param conversation 会话实体
     * @return Completable - 操作完成通知
     */
    public Completable insertOrUpdateConversation(ConversationEntity conversation) {
        return conversationDao.insertConversation(conversation)
            .andThen(conversationDao.updateConversationMeta(
                conversation.id,
                conversation.title,
                conversation.model,
                conversation.messageCount,
                conversation.lastMessagePreview,
                conversation.lastMessageTime,
                conversation.updatedAt
            ));
    }
    
    /**
     * 根据ID查询会话
     * 
     * @param id 会话ID
     * @return Maybe<ConversationEntity> - 会话实体，如果不存在返回空
     */
    public Maybe<ConversationEntity> getConversationById(Long id) {
        return conversationDao.getConversationById(id);
    }
    
    /**
     * 根据用户ID查询所有会话
     * 
     * @param userId 用户ID
     * @return Single<List<ConversationEntity>> - 会话列表
     */
    public Single<List<ConversationEntity>> getConversationsByUserId(Long userId) {
        return conversationDao.getConversationsByUserId(userId);
    }
    
    /**
     * 获取所有会话（用于GetConversationsUseCase）
     * 注意：这个方法需要userId，暂时使用默认值1，实际应该从登录用户获取
     */
    public Single<List<ConversationEntity>> getAllConversations() {
        // TODO: 从SharedPreferences或数据库获取当前登录用户ID
        return getConversationsByUserId(1L);
    }
    
    /**
     * 查询收藏的会话
     * 
     * @param userId 用户ID
     * @return Single<List<ConversationEntity>> - 收藏的会话列表
     */
    public Single<List<ConversationEntity>> getFavoriteConversations(Long userId) {
        return conversationDao.getFavoriteConversations(userId);
    }
    
    /**
     * 搜索会话
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return Single<List<ConversationEntity>> - 匹配的会话列表
     */
    public Single<List<ConversationEntity>> searchConversations(Long userId, String keyword) {
        return conversationDao.searchConversations(userId, keyword);
    }
    
    /**
     * 删除会话
     * 
     * @param id 会话ID
     * @return Completable - 操作完成通知
     */
    public Completable deleteConversation(Long id) {
        return conversationDao.deleteConversation(id);
    }
    
    /**
     * 更新会话的最后消息信息
     * 
     * @param id 会话ID
     * @param lastMessagePreview 最后消息预览
     * @param lastMessageTime 最后消息时间
     * @return Completable - 操作完成通知
     */
    public Completable updateLastMessage(Long id, String lastMessagePreview, Long lastMessageTime) {
        return conversationDao.updateLastMessage(id, lastMessagePreview, lastMessageTime);
    }
    
    /**
     * 更新会话的消息数量
     * 
     * @param id 会话ID
     * @param messageCount 消息数量
     * @return Completable - 操作完成通知
     */
    public Completable updateMessageCount(Long id, Integer messageCount) {
        return conversationDao.updateMessageCount(id, messageCount, System.currentTimeMillis());
    }
}

