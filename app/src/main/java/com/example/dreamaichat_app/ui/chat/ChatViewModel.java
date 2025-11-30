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
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final List<ModelOption> availableModels;

    private long activeConversationId = -1L;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(application);
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

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }

    public interface PromptCallback {
        void onQuickPrompt(String prompt);
    }
}
