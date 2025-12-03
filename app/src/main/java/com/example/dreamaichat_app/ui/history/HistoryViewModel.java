package com.example.dreamaichat_app.ui.history;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dreamaichat_app.data.entity.ConversationEntity;
import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.data.repository.ConversationRepository;
import com.example.dreamaichat_app.model.ConversationSummary;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoryViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ConversationSummary>> source = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ConversationSummary>> conversations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final SessionManager sessionManager;
    private final ConversationRepository conversationRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private String lastQuery = "";

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application);
        this.conversationRepository = new ConversationRepository(application);
    }

    public LiveData<List<ConversationSummary>> getConversations() {
        return conversations;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void refresh() {
        loading.setValue(true);
        long userId = sessionManager.getUserId();
        // 使用默认用户ID -1L 作为兜底，确保能获取到本地数据
        long actualUserId = userId > 0 ? userId : -1L;
        
        disposables.add(
            conversationRepository.getConversationsByUserId(actualUserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    conversationEntities -> handleConversationsResponse(conversationEntities),
                    throwable -> {
                        loading.setValue(false);
                        error.setValue(throwable.getMessage() != null ? throwable.getMessage() : "获取会话列表失败");
                    }
                )
        );
    }

    public void search(String query) {
        lastQuery = query == null ? "" : query.trim();
        applyFilter(lastQuery, source.getValue());
    }

    private void handleConversationsResponse(List<ConversationEntity> conversationEntities) {
        loading.setValue(false);
        if (conversationEntities == null) {
            error.setValue("获取会话列表失败");
            return;
        }

        List<ConversationSummary> mapped = new ArrayList<>();
        for (ConversationEntity entity : conversationEntities) {
            mapped.add(mapToSummary(entity));
        }
        source.setValue(mapped);
        applyFilter(lastQuery, mapped);
    }

    private void applyFilter(String keyword, List<ConversationSummary> dataset) {
        List<ConversationSummary> working = dataset == null ? new ArrayList<>() : new ArrayList<>(dataset);
        if (!TextUtils.isEmpty(keyword)) {
            String lower = keyword.toLowerCase();
            List<ConversationSummary> filtered = new ArrayList<>();
            for (ConversationSummary summary : working) {
                String title = summary.getTitle() != null ? summary.getTitle() : "";
                String snippet = summary.getSnippet() != null ? summary.getSnippet() : "";
                if (title.toLowerCase().contains(lower) ||
                    snippet.toLowerCase().contains(lower)) {
                    filtered.add(summary);
                }
            }
            conversations.setValue(filtered);
        } else {
            conversations.setValue(working);
        }
    }

    private ConversationSummary mapToSummary(ConversationEntity entity) {
        long id = entity.id != null ? entity.id : System.currentTimeMillis();
        String title = !TextUtils.isEmpty(entity.title) ? entity.title : "未命名会话";
        String snippet = pickFirstNonEmpty(
            entity.lastMessagePreview,
            "暂无摘要"
        );
        long timestamp = entity.lastMessageTime != null ? entity.lastMessageTime : System.currentTimeMillis();
        String modelTag = !TextUtils.isEmpty(entity.model)
            ? entity.model
            : "AI";
        boolean pinned = entity.isFavorite;
        return new ConversationSummary(id, title, snippet, timestamp, modelTag, pinned);
    }

    private String pickFirstNonEmpty(String... value) {
        if (value == null) {
            return "";
        }
        for (String candidate : value) {
            if (!TextUtils.isEmpty(candidate)) {
                return candidate;
            }
        }
        return "";
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}