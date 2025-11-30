package com.example.dreamaichat_app.ui.history;

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
import com.example.dreamaichat_app.data.remote.model.ConversationSummaryResponse;
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

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private String lastQuery = "";

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(application);
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
        String token = sessionManager.getToken();
        if (TextUtils.isEmpty(token)) {
            error.setValue("请先登录后查看历史记录");
            return;
        }
        loading.setValue(true);
        disposables.add(
            apiService.conversations("Bearer " + token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> handleConversationsResponse(response),
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

    private void handleConversationsResponse(ApiResponse<List<ConversationSummaryResponse>> apiResponse) {
        loading.setValue(false);
        if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
            error.setValue(apiResponse != null ? apiResponse.message : "获取会话列表失败");
            return;
        }

        List<ConversationSummary> mapped = new ArrayList<>();
        for (ConversationSummaryResponse response : apiResponse.data) {
            mapped.add(mapToSummary(response));
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

    private ConversationSummary mapToSummary(ConversationSummaryResponse response) {
        long id = response != null && response.id != null ? response.id : System.currentTimeMillis();
        String title = response != null && !TextUtils.isEmpty(response.title) ? response.title : "未命名会话";
        String snippet = pickFirstNonEmpty(
            response != null ? response.snippet : null,
            response != null ? response.latestMessage : null,
            "暂无摘要"
        );
        long timestamp = response != null && response.updatedAt != null ? response.updatedAt : System.currentTimeMillis();
        String modelTag = response != null && !TextUtils.isEmpty(response.model)
            ? response.model
            : "AI";
        boolean pinned = response != null && Boolean.TRUE.equals(response.pinned);
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