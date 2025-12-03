package com.example.dreamaichat_app.ui.profile;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.data.database.AppDatabase;
import com.example.dreamaichat_app.data.entity.ConversationEntity;
import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.ui.graph.ConversationGraphFragment;
import com.example.dreamaichat_app.ui.template.TemplateSelectionFragment;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 个人中心界面 Fragment
 */
public class ProfileFragment extends Fragment {

    private TextView tvUsername;
    private TextView tvConversationCount;
    private TextView tvMessageCount;
    private TextView tvDaysCount;
    private LinearLayout btnTemplates;
    private LinearLayout btnGraph;
    private View btnEditProfile;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvUsername = view.findViewById(R.id.tv_username);
        tvConversationCount = view.findViewById(R.id.tv_conversation_count);
        tvMessageCount = view.findViewById(R.id.tv_message_count);
        tvDaysCount = view.findViewById(R.id.tv_days_count);
        btnTemplates = view.findViewById(R.id.btn_templates);
        btnGraph = view.findViewById(R.id.btn_graph);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        setupClicks();
        loadStats();
        loadProfile();
    }

    private void loadProfile() {
        Context context = getContext();
        if (context == null) return;
        SessionManager sessionManager = new SessionManager(context);
        String displayName = sessionManager.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            tvUsername.setText(displayName);
        }
    }

    private void setupClicks() {
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AccountSettingsFragment())
                    .addToBackStack(null)
                    .commit()
            );
        }

        View.OnClickListener openTemplates = v ->
            requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new TemplateSelectionFragment())
                .addToBackStack(null)
                .commit();

        View.OnClickListener openGraph = v ->
            requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ConversationGraphFragment())
                .addToBackStack(null)
                .commit();

        if (btnTemplates != null) btnTemplates.setOnClickListener(openTemplates);
        if (btnGraph != null) btnGraph.setOnClickListener(openGraph);
    }

    private void loadStats() {
        Context context = getContext();
        if (context == null) return;

        Application application = requireActivity().getApplication();
        SessionManager sessionManager = new SessionManager(context);
        long userId = sessionManager.getUserId();
        
        // 即使未登录，也要查询数据库（可能有默认用户或演示数据）
        // 如果userId<=0，使用-1L作为默认值查询
        long actualUserId = userId > 0 ? userId : -1L;

        AppDatabase db = AppDatabase.getInstance(application);
        disposables.add(
            Single.fromCallable(() -> {
                    List<ConversationEntity> conversations = db.conversationDao().getAllForUserSync(actualUserId);
                    List<MessageEntity> messages = db.messageDao().getAllForUserSync(actualUserId);
                    long firstLoginTime = sessionManager.getFirstLoginTime();
                    if (firstLoginTime <= 0L && !messages.isEmpty()) {
                        firstLoginTime = messages.get(0).createdAt != null ? messages.get(0).createdAt : System.currentTimeMillis();
                    }
                    long days = 1L;
                    if (firstLoginTime > 0L) {
                        long diff = System.currentTimeMillis() - firstLoginTime;
                        days = Math.max(1L, diff / (1000L * 60L * 60L * 24L));
                    }
                    final long convCount = conversations.size();
                    final long msgCount = messages.size();
                    final long useDays = days;
                    return new long[]{convCount, msgCount, useDays};
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                        tvConversationCount.setText(String.valueOf(result[0]));
                        tvMessageCount.setText(String.valueOf(result[1]));
                        tvDaysCount.setText(String.valueOf(result[2]));
                    },
                    throwable -> {
                        tvConversationCount.setText("0");
                        tvMessageCount.setText("0");
                        tvDaysCount.setText("0");
                    })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次Fragment重新显示时刷新统计数据
        loadStats();
    }
}

