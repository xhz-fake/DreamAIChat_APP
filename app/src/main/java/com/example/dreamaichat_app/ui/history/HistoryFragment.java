package com.example.dreamaichat_app.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dreamaichat_app.databinding.FragmentHistoryBinding;
import com.example.dreamaichat_app.model.ConversationSummary;
import com.example.dreamaichat_app.ui.chat.ChatViewModel;
import com.example.dreamaichat_app.ui.history.adapter.ConversationAdapter;
import com.example.dreamaichat_app.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 鍘嗗彶璁板綍鐣岄潰 Fragment銆? */
public class HistoryFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        setupRecyclerView();
        observeViewModel();
        initSearch();
        viewModel.refresh();
    }

    private void setupRecyclerView() {
        adapter = new ConversationAdapter(new ArrayList<>(), this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHistory.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getConversations().observe(getViewLifecycleOwner(), this::renderConversations);
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressHistory.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderConversations(List<ConversationSummary> conversations) {
        adapter.submit(conversations);
        binding.emptyState.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void initSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.btnStartChat.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                ChatViewModel chatViewModel = new ViewModelProvider(mainActivity).get(ChatViewModel.class);
                chatViewModel.startNewChat();
                mainActivity.showChatFragment();
            }
        });
    }

    @Override
    public void onConversationClick(ConversationSummary summary) {
        // 获取 MainActivity 的 ChatViewModel
        if (getActivity() instanceof com.example.dreamaichat_app.ui.main.MainActivity) {
            com.example.dreamaichat_app.ui.main.MainActivity mainActivity = 
                (com.example.dreamaichat_app.ui.main.MainActivity) getActivity();
            
            // 从 modelTag 中提取模型ID（格式：模型：deepseek 或 模型：doubao）
            String modelId = extractModelId(summary.getModelTag());
            
            // 加载会话
            androidx.lifecycle.ViewModelProvider viewModelProvider = 
                new androidx.lifecycle.ViewModelProvider(mainActivity);
            com.example.dreamaichat_app.ui.chat.ChatViewModel chatViewModel = 
                viewModelProvider.get(com.example.dreamaichat_app.ui.chat.ChatViewModel.class);
            chatViewModel.loadConversation(summary.getId(), modelId);
            
            // 切换到聊天界面
            mainActivity.showChatFragment();
        }
    }
    
    /**
     * 从模型标签中提取模型ID
     * 支持格式："模型：deepseek"、"deepseek"、"doubao" 等
     */
    private String extractModelId(String modelTag) {
        if (modelTag == null || modelTag.isEmpty()) {
            return null;
        }
        // 移除 "模型：" 前缀（如果存在）
        String tag = modelTag.replace("模型：", "").trim();
        
        // 检查是否是已知的模型ID
        String lowerTag = tag.toLowerCase();
        if (lowerTag.contains("deepseek")) {
            return "deepseek";
        } else if (lowerTag.contains("doubao") || lowerTag.contains("豆包")) {
            return "doubao";
        }
        
        // 如果直接是模型ID，直接返回
        if (lowerTag.equals("deepseek") || lowerTag.equals("doubao")) {
            return lowerTag;
        }
        
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
