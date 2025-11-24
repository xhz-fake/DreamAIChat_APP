package com.example.dreamaichat_app.domain.usecase;

import android.content.Context;

import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.remote.api.ApiService;
import com.example.dreamaichat_app.data.remote.model.ChatRequest;
import com.example.dreamaichat_app.data.remote.model.ChatResponse;
import com.example.dreamaichat_app.data.remote.RetrofitClient;
import com.example.dreamaichat_app.data.repository.MessageRepository;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 发送消息用例
 */
public class SendMessageUseCase {
    
    private final ApiService apiService;
    private final MessageRepository messageRepository;
    
    public SendMessageUseCase(Context context) {
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.messageRepository = new MessageRepository(context);
    }
    
    /**
     * 发送消息
     */
    public Single<MessageEntity> execute(Long conversationId, String content, String token) {
        // 先保存用户消息到本地
        MessageEntity userMessage = new MessageEntity();
        userMessage.conversationId = conversationId;
        userMessage.role = "user";
        userMessage.content = content;
        userMessage.status = "sending";
        userMessage.createdAt = System.currentTimeMillis();
        userMessage.updatedAt = System.currentTimeMillis();
        
        return messageRepository.insertMessage(userMessage)
            .andThen(
                // 调用API发送消息
                createChatRequest(content, token)
                    .flatMap(request -> apiService.sendMessage("Bearer " + token, request))
                    .flatMap(apiResponse -> {
                        if (!apiResponse.success || apiResponse.data == null) {
                            throw new RuntimeException(apiResponse.message != null ? apiResponse.message : "发送失败");
                        }
                        ChatResponse response = apiResponse.data;
                        // 更新用户消息状态为成功
                        userMessage.status = "success";
                        userMessage.updatedAt = System.currentTimeMillis();
                        
                        // 保存AI回复
                        MessageEntity aiMessage = new MessageEntity();
                        aiMessage.conversationId = conversationId;
                        aiMessage.role = "assistant";
                        aiMessage.content = response.message != null ? response.message : "";
                        aiMessage.status = "success";
                        aiMessage.createdAt = System.currentTimeMillis();
                        aiMessage.updatedAt = System.currentTimeMillis();
                        
                        return messageRepository.insertMessage(userMessage)
                            .andThen(messageRepository.insertMessage(aiMessage))
                            .andThen(Single.just(aiMessage));
                    })
                    .onErrorReturn(error -> {
                        // 更新用户消息状态为失败
                        userMessage.status = "failed";
                        userMessage.errorMessage = error.getMessage();
                        userMessage.updatedAt = System.currentTimeMillis();
                        messageRepository.updateMessage(userMessage).subscribe();
                        return null;
                    })
            )
            .subscribeOn(Schedulers.io());
    }
    
    private Single<ChatRequest> createChatRequest(String content, String token) {
        ChatRequest request = new ChatRequest();
        request.message = content;
        request.conversationId = 1L; // TODO: 使用实际的会话ID
        request.model = "gpt-4"; // TODO: 使用用户选择的模型
        return Single.just(request);
    }
}

