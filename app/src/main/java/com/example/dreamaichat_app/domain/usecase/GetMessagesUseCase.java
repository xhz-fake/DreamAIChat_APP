package com.example.dreamaichat_app.domain.usecase;

import android.content.Context;

import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.repository.MessageRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 获取消息用例
 */
public class GetMessagesUseCase {
    
    private final MessageRepository messageRepository;
    
    public GetMessagesUseCase(Context context) {
        this.messageRepository = new MessageRepository(context);
    }
    
    /**
     * 获取会话的所有消息
     */
    public Single<List<MessageEntity>> execute(Long conversationId) {
        return messageRepository.getMessagesByConversationId(conversationId)
            .subscribeOn(Schedulers.io());
    }
    
    /**
     * 获取会话的所有消息（Flowable版本，用于响应式更新）
     */
    public Flowable<List<MessageEntity>> executeFlowable(Long conversationId) {
        return messageRepository.getMessagesByConversationId(conversationId)
            .toFlowable()
            .subscribeOn(Schedulers.io());
    }
}

