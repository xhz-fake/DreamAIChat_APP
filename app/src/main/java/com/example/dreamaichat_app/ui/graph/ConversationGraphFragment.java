package com.example.dreamaichat_app.ui.graph;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.repository.MessageRepository;
import com.example.dreamaichat_app.model.ConversationSummary;
import com.example.dreamaichat_app.ui.chat.ChatViewModel;
import com.example.dreamaichat_app.ui.history.HistoryViewModel;
import com.example.dreamaichat_app.ui.main.MainActivity;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 对话图谱界面：复用历史会话列表，点击后生成总结提示并填入当前聊天输入框
 */
public class ConversationGraphFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private ChatViewModel chatViewModel;
    private RecyclerView recyclerView;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView = view.findViewById(R.id.recycler_graph_conversations);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        ConversationGraphSimpleAdapter adapter = new ConversationGraphSimpleAdapter(item -> {
            // 加载该会话的所有消息
            loadConversationMessages(item.getId(), item.getTitle());
        });
        recyclerView.setAdapter(adapter);

        historyViewModel.getConversations().observe(getViewLifecycleOwner(), adapter::submit);
        historyViewModel.refresh();
    }

    private void loadConversationMessages(long conversationId, String title) {
        Application application = requireActivity().getApplication();
        MessageRepository messageRepository = new MessageRepository(application);

        disposables.add(
            messageRepository.getMessagesByConversationId(conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    messages -> {
                        // 格式化对话历史
                        String conversationText = formatConversationHistory(messages, title);
                        
                        // 构造提示词
                        StringBuilder sb = new StringBuilder();
                        sb.append("以下是我与 AI 的一段完整对话：\n\n");
                        sb.append(conversationText);
                        sb.append("\n\n请根据以上用户和 AI 的聊天内容，先提炼一段清晰的总结，然后给出对应的流程图描述。");

                        // 使用 ViewModel 的 pendingPrompt 机制，会在切换到聊天界面后自动填入
                        chatViewModel.applyQuickPrompt(
                            new com.example.dreamaichat_app.model.QuickAction("graph_summary", "对话图谱总结", sb.toString()),
                            null // 传入 null，让 ViewModel 存储到 pendingPrompt
                        );

                        // 返回聊天界面
                        if (requireActivity() instanceof MainActivity) {
                            ((MainActivity) requireActivity()).showChatFragment();
                        } else {
                            requireActivity().onBackPressed();
                        }
                    },
                    throwable -> {
                        Toast.makeText(requireContext(), "加载对话历史失败：" + 
                            (throwable.getMessage() != null ? throwable.getMessage() : "未知错误"), 
                            Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }

    private String formatConversationHistory(List<MessageEntity> messages, String title) {
        if (messages == null || messages.isEmpty()) {
            return "该会话暂无对话内容。";
        }

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(title)) {
            sb.append("对话主题：").append(title).append("\n\n");
        }

        for (MessageEntity message : messages) {
            if (message == null || TextUtils.isEmpty(message.content)) {
                continue;
            }

            String role = message.role != null ? message.role.toLowerCase() : "";
            String content = message.content.trim();

            // 根据角色格式化输出
            switch (role) {
                case "user":
                    sb.append("用户：").append(content).append("\n\n");
                    break;
                case "assistant":
                    // 尝试识别 AI 模型名称（从 modelTag 或其他字段），默认使用"AI"
                    sb.append("AI：").append(content).append("\n\n");
                    break;
                case "system":
                    // 系统消息可以跳过或特殊标记
                    sb.append("系统：").append(content).append("\n\n");
                    break;
                default:
                    // 未知角色，使用原始角色名
                    sb.append(role).append("：").append(content).append("\n\n");
                    break;
            }
        }

        return sb.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }
}


