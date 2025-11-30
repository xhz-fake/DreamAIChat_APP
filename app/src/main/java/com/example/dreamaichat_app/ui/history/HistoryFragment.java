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
import com.example.dreamaichat_app.ui.history.adapter.ConversationAdapter;

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
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onConversationClick(ConversationSummary summary) {
        Toast.makeText(requireContext(), "打开会话：" + summary.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
