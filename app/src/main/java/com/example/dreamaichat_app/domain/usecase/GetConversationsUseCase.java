package com.example.dreamaichat_app.domain.usecase;

import android.content.Context;

import com.example.dreamaichat_app.data.entity.ConversationEntity;
import com.example.dreamaichat_app.data.repository.ConversationRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 获取会话列表用例
 */
public class GetConversationsUseCase {
    
    private final ConversationRepository conversationRepository;
    
    public GetConversationsUseCase(Context context) {
        this.conversationRepository = new ConversationRepository(context);
    }
    
    /**
     * 获取所有会话
     */
    public Single<List<ConversationEntity>> execute() {
        return conversationRepository.getAllConversations()
            .subscribeOn(Schedulers.io());
    }
    
    /**
     * 获取所有会话（Flowable版本，用于响应式更新）
     */
    public Flowable<List<ConversationEntity>> executeFlowable() {
        return conversationRepository.getAllConversations()
            .toFlowable()
            .subscribeOn(Schedulers.io());
    }
}

