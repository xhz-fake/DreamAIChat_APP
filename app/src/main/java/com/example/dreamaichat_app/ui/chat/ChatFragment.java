package com.example.dreamaichat_app.ui.chat;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
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
    private ActivityResultLauncher<String[]> pickImageLauncher;
    private final List<Uri> pendingAttachments = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    addPendingAttachments(uris);
                }
            }
        );
    }

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
            boolean hasText = !TextUtils.isEmpty(text.trim());
            if (!hasText && pendingAttachments.isEmpty()) {
                binding.etMessageInput.setError(getString(R.string.chat_input_hint));
                return;
            }
            viewModel.sendMessage(hasText ? text.trim() : "", new ArrayList<>(pendingAttachments));
            binding.etMessageInput.setText("");
            pendingAttachments.clear();
            renderAttachmentPreview();
        });

        binding.btnImage.setOnClickListener(v -> {
            if (pickImageLauncher != null) {
                pickImageLauncher.launch(new String[]{"image/*"});
            }
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

    private void addPendingAttachments(@NonNull List<Uri> uris) {
        ContentResolver resolver = requireContext().getContentResolver();
        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignore) {
                // ignore
            }
            pendingAttachments.add(uri);
        }
        renderAttachmentPreview();
    }

    private void renderAttachmentPreview() {
        if (binding == null) {
            return;
        }
        if (pendingAttachments.isEmpty()) {
            binding.attachmentPreview.setVisibility(View.GONE);
            binding.attachmentPreviewContainer.removeAllViews();
            return;
        }
        binding.attachmentPreview.setVisibility(View.VISIBLE);
        binding.attachmentPreviewContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Uri uri : pendingAttachments) {
            View preview = inflater.inflate(R.layout.item_attachment_preview, binding.attachmentPreviewContainer, false);
            android.widget.ImageView imageView = preview.findViewById(R.id.previewImage);
            android.widget.ImageButton btnRemove = preview.findViewById(R.id.btnRemove);
            Glide.with(this).load(uri).centerCrop().into(imageView);
            btnRemove.setOnClickListener(v -> {
                pendingAttachments.remove(uri);
                renderAttachmentPreview();
            });
            binding.attachmentPreviewContainer.addView(preview);
        }
    }
}
