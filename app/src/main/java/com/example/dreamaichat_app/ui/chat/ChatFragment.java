package com.example.dreamaichat_app.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.databinding.FragmentChatBinding;
import com.example.dreamaichat_app.model.ModelOption;
import com.example.dreamaichat_app.model.QuickAction;
import com.example.dreamaichat_app.ui.chat.adapter.ChatMessageAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 鑱婂ぉ鐣岄潰 Fragment銆? */
public class ChatFragment extends Fragment implements ChatViewModel.PromptCallback {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private ChatMessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        setupRecyclerView();
        observeViewModel();
        initClicks();
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(new ArrayList<>());
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMessages.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            adapter.submit(messages);
            binding.rvMessages.scrollToPosition(Math.max(messages.size() - 1, 0));
        });

        viewModel.isGenerating().observe(getViewLifecycleOwner(), generating ->
            binding.generatingBar.setVisibility(Boolean.TRUE.equals(generating) ? View.VISIBLE : View.GONE));

        viewModel.getQuickActions().observe(getViewLifecycleOwner(), this::renderQuickActions);

        viewModel.getCurrentModel().observe(getViewLifecycleOwner(), modelOption -> {
            if (modelOption != null) {
                binding.tvCurrentModel.setText(modelOption.getDisplayName());
                binding.modelBadge.getBackground().setTint(modelOption.getAccentColor());
            }
        });

        viewModel.getToastEvent().observe(getViewLifecycleOwner(), message -> {
            if (!TextUtils.isEmpty(message)) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderQuickActions(List<QuickAction> actions) {
        binding.chipGroup.removeAllViews();
        for (QuickAction action : actions) {
            Chip chip = new Chip(requireContext());
            chip.setText(action.getLabel());
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            chip.setChipBackgroundColorResource(R.color.surface);
            chip.setOnClickListener(v -> viewModel.applyQuickPrompt(action, this));
            binding.chipGroup.addView(chip);
        }
    }

    private void initClicks() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessageInput.getText() == null ? "" : binding.etMessageInput.getText().toString();
            if (TextUtils.isEmpty(text)) {
                binding.etMessageInput.setError(getString(R.string.chat_input_hint));
                return;
            }
            viewModel.sendMessage(text.trim());
            binding.etMessageInput.setText("");
        });

        binding.btnModelSwitch.setOnClickListener(v -> showModelPicker());
        binding.modelBadge.setOnClickListener(v -> binding.btnModelSwitch.performClick());
    }

    private void showModelPicker() {
        List<ModelOption> options = viewModel.getAvailableModels();
        if (options == null || options.isEmpty()) {
            return;
        }
        CharSequence[] labels = new CharSequence[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = options.get(i).getDisplayName();
        }
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.model_picker_title))
            .setItems(labels, (dialog, which) -> viewModel.switchModel(options.get(which)))
            .show();
    }

    @Override
    public void onQuickPrompt(String prompt) {
        binding.etMessageInput.setText(prompt);
        binding.etMessageInput.setSelection(prompt.length());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
