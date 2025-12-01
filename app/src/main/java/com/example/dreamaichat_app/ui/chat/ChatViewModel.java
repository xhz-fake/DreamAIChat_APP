package com.example.dreamaichat_app.ui.chat;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.data.remote.RetrofitClient;
import com.example.dreamaichat_app.data.remote.api.ApiService;
import com.example.dreamaichat_app.data.remote.model.ApiResponse;
import com.example.dreamaichat_app.data.remote.model.ChatRequest;
import com.example.dreamaichat_app.data.remote.model.ChatResponse;
import com.example.dreamaichat_app.data.repository.MessageRepository;
import com.example.dreamaichat_app.data.repository.ConversationRepository;
import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.entity.ConversationEntity;
import com.example.dreamaichat_app.domain.usecase.GetMessagesUseCase;
import com.example.dreamaichat_app.model.ChatMessage;
import com.example.dreamaichat_app.model.ChatRole;
import com.example.dreamaichat_app.model.MessageStatus;
import com.example.dreamaichat_app.model.ModelOption;
import com.example.dreamaichat_app.model.QuickAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatViewModel extends AndroidViewModel {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1000);

    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ModelOption> currentModel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGenerating = new MutableLiveData<>(false);
    private final MutableLiveData<List<QuickAction>> quickActions = new MutableLiveData<>(buildDefaultQuickActions());
    private final MutableLiveData<String> toastEvent = new MutableLiveData<>();

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final List<ModelOption> availableModels;

    private long activeConversationId = -1L;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(application);
        this.messageRepository = new MessageRepository(application);
        this.conversationRepository = new ConversationRepository(application);
        this.availableModels = buildDefaultModelOptions();
        if (!availableModels.isEmpty()) {
            currentModel.setValue(availableModels.get(0));
        }
        startNewChat();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<ModelOption> getCurrentModel() {
        return currentModel;
    }

    public LiveData<Boolean> isGenerating() {
        return isGenerating;
    }

    public LiveData<List<QuickAction>> getQuickActions() {
        return quickActions;
    }

    public LiveData<String> getToastEvent() {
        return toastEvent;
    }

    public List<ModelOption> getAvailableModels() {
        return availableModels;
    }

    public void switchModel(ModelOption modelOption) {
        currentModel.setValue(modelOption);
    }

    public void startNewChat() {
        activeConversationId = -1L;
        List<ChatMessage> seed = new ArrayList<>();
        seed.add(new ChatMessage(
            nextId(),
            ChatRole.SYSTEM,
            "新的对话空间已准备就绪，任何灵感都可以立即发出。",
            System.currentTimeMillis(),
            Collections.emptyList(),
            MessageStatus.SUCCESS
        ));
        messages.setValue(seed);
    }

    /**
     * 加载指定会话的消息
     * @param conversationId 会话ID
     * @param modelId 模型ID（用于设置当前模型）
     */
    public void loadConversation(long conversationId, String modelId) {
        activeConversationId = conversationId;
        
        // 设置模型
        if (modelId != null) {
            for (ModelOption model : availableModels) {
                if (model.getId().equals(modelId)) {
                    currentModel.setValue(model);
                    break;
                }
            }
        }
        
        // 从本地数据库加载消息
        GetMessagesUseCase useCase = new GetMessagesUseCase(getApplication());
        disposables.add(
            useCase.execute(conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    messageEntities -> {
                        List<ChatMessage> chatMessages = new ArrayList<>();
                        for (com.example.dreamaichat_app.data.entity.MessageEntity entity : messageEntities) {
                            ChatRole role;
                            try {
                                role = ChatRole.valueOf(entity.role.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                role = ChatRole.USER;
                            }
                            
                            MessageStatus status;
                            try {
                                status = MessageStatus.valueOf(entity.status != null ? entity.status.toUpperCase() : "SUCCESS");
                            } catch (IllegalArgumentException e) {
                                status = MessageStatus.SUCCESS;
                            }
                            
                            ChatMessage chatMessage = new ChatMessage(
                                entity.id != null ? entity.id : nextId(),
                                role,
                                entity.content != null ? entity.content : "",
                                entity.createdAt != null ? entity.createdAt : System.currentTimeMillis(),
                                Collections.emptyList(),
                                status
                            );
                            chatMessages.add(chatMessage);
                        }
                        
                        // 如果没有消息，添加系统提示
                        if (chatMessages.isEmpty()) {
                            chatMessages.add(new ChatMessage(
                                nextId(),
                                ChatRole.SYSTEM,
                                "会话已加载，可以继续对话。",
                                System.currentTimeMillis(),
                                Collections.emptyList(),
                                MessageStatus.SUCCESS
                            ));
                        }
                        
                        messages.setValue(chatMessages);
                    },
                    throwable -> {
                        toastEvent.postValue("加载会话失败：" + (throwable.getMessage() != null ? throwable.getMessage() : "未知错误"));
                        // 即使加载失败，也切换到该会话
                        startNewChat();
                        activeConversationId = conversationId;
                    }
                )
        );
    }

    public void applyQuickPrompt(QuickAction action, PromptCallback callback) {
        if (callback != null) {
            callback.onQuickPrompt(action.getPrompt());
        }
    }

    public void sendMessage(String text) {
        String token = sessionManager.getToken();
        if (token == null) {
            toastEvent.postValue("登录状态已失效，请重新登录");
            return;
        }
        if (TextUtils.isEmpty(text)) {
            return;
        }
        String payload = text.trim();
        if (payload.isEmpty()) {
            return;
        }

        List<ChatMessage> currentList = new ArrayList<>(ensureMessageList());
        ChatMessage pending = new ChatMessage(
            nextId(),
            ChatRole.USER,
            payload,
            System.currentTimeMillis(),
            Collections.emptyList(),
            MessageStatus.SENDING
        );
        currentList.add(pending);
        messages.setValue(currentList);
        isGenerating.setValue(true);

        ChatRequest request = new ChatRequest();
        request.message = payload;
        if (activeConversationId > 0) {
            request.conversationId = activeConversationId;
        }
        ModelOption option = currentModel.getValue();
        if (option != null) {
            request.model = option.getId();
        }

        disposables.add(
            apiService.sendMessage("Bearer " + token, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> handleSuccess(response, pending),
                    throwable -> handleError(throwable, pending)
                )
        );
    }

    private void handleSuccess(ApiResponse<ChatResponse> apiResponse, ChatMessage pending) {
        if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
            handleError(new RuntimeException(apiResponse != null ? apiResponse.message : "发送失败"), pending);
            return;
        }
        ChatResponse data = apiResponse.data;
        if (data.conversationId != null) {
            activeConversationId = data.conversationId;
        }
        pending.setStatus(MessageStatus.SUCCESS);
        List<ChatMessage> currentList = new ArrayList<>(ensureMessageList());
        replaceMessage(currentList, pending);

        ChatMessage aiMessage = new ChatMessage(
            nextId(),
            ChatRole.ASSISTANT,
            data.replyMessage != null ? data.replyMessage : "",
            System.currentTimeMillis(),
            Collections.emptyList(),
            MessageStatus.SUCCESS
        );
        currentList.add(aiMessage);
        messages.setValue(currentList);
        isGenerating.setValue(false);

        persistConversationAndMessages(data, pending, aiMessage);
    }

    private void handleError(Throwable throwable, ChatMessage pending) {
        pending.setStatus(MessageStatus.FAILED);
        List<ChatMessage> currentList = new ArrayList<>(ensureMessageList());
        replaceMessage(currentList, pending);
        messages.setValue(currentList);
        isGenerating.setValue(false);
        toastEvent.setValue(throwable.getMessage() != null ? throwable.getMessage() : "发送失败，请稍后重试");
    }

    private void replaceMessage(List<ChatMessage> target, ChatMessage message) {
        for (int i = 0; i < target.size(); i++) {
            if (target.get(i).getId() == message.getId()) {
                target.set(i, message);
                return;
            }
        }
    }

    private List<ChatMessage> ensureMessageList() {
        List<ChatMessage> current = messages.getValue();
        if (current == null) {
            current = new ArrayList<>();
            messages.setValue(current);
        }
        return current;
    }

    private static List<QuickAction> buildDefaultQuickActions() {
        return Arrays.asList(
            new QuickAction("summarize", "总结内容", "请用要点总结上文"),
            new QuickAction("polish", "润色文案", "请帮我润色，使其适合商务场景"),
            new QuickAction("translate", "翻译英文", "请准确翻译为英文并保留专有名词"),
            new QuickAction("analyze", "结构分析", "请从要点、风险、建议三个角度分析")
        );
    }

    private static List<ModelOption> buildDefaultModelOptions() {
        return Arrays.asList(
            new ModelOption("deepseek", "DeepSeek · 精准", "逻辑严密，适合复杂推理", "Quality", 0xFF6C63FF, true),
            new ModelOption("doubao", "豆包 · 极速", "延迟低，适合实时对话", "Speed", 0xFF00B894, true)
        );
    }

    private static long nextId() {
        return ID_GENERATOR.getAndIncrement();
    }

    private void persistConversationAndMessages(ChatResponse data, ChatMessage userMessage, ChatMessage aiMessage) {
        Long conversationId = data.conversationId;
        if (conversationId == null || conversationId <= 0) {
            return;
        }
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            toastEvent.postValue("本地保存聊天记录失败：未找到登录用户信息");
            return;
        }

        String modelId = currentModel.getValue() != null ? currentModel.getValue().getId() : data.provider;
        long timestamp = System.currentTimeMillis();

        ConversationEntity conversation = new ConversationEntity();
        conversation.id = conversationId;
        conversation.userId = userId;
        conversation.title = buildConversationTitle(userMessage.getContent());
        conversation.model = modelId;
        conversation.messageCount = ensureMessageList().size();
        conversation.isFavorite = false;
        conversation.lastMessagePreview = aiMessage.getContent();
        conversation.lastMessageTime = aiMessage.getTimestamp();
        conversation.createdAt = timestamp;
        conversation.updatedAt = timestamp;

        List<MessageEntity> messageEntities = new ArrayList<>();
        messageEntities.add(mapToMessageEntity(userMessage, conversationId));
        messageEntities.add(mapToMessageEntity(aiMessage, conversationId));

        disposables.add(
            conversationRepository.insertOrUpdateConversation(conversation)
                .andThen(messageRepository.insertMessages(messageEntities))
                .subscribeOn(Schedulers.io())
                .subscribe(
                    () -> { /* ignore */ },
                    throwable -> toastEvent.postValue("本地保存聊天记录失败：" +
                        (throwable.getMessage() != null ? throwable.getMessage() : "未知错误"))
                )
        );
    }

    private MessageEntity mapToMessageEntity(ChatMessage message, long conversationId) {
        MessageEntity entity = new MessageEntity();
        entity.conversationId = conversationId;
        entity.role = message.getRole() != null ? message.getRole().name().toLowerCase() : "user";
        entity.content = message.getContent();
        entity.status = message.getStatus() != null ? message.getStatus().name().toLowerCase() : "success";
        entity.createdAt = message.getTimestamp();
        entity.updatedAt = System.currentTimeMillis();
        return entity;
    }

    private String buildConversationTitle(String original) {
        if (TextUtils.isEmpty(original)) {
            return "新对话";
        }
        String trimmed = original.trim();
        return trimmed.length() > 18 ? trimmed.substring(0, 18) + "..." : trimmed;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }

    public interface PromptCallback {
        void onQuickPrompt(String prompt);
    }
}
