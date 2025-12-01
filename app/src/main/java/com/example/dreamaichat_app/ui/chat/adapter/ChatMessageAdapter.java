package com.example.dreamaichat_app.ui.chat.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dreamaichat_app.databinding.ItemMessageAiBinding;
import com.example.dreamaichat_app.databinding.ItemMessageSystemBinding;
import com.example.dreamaichat_app.databinding.ItemMessageUserBinding;
import com.example.dreamaichat_app.model.ChatAttachment;
import com.example.dreamaichat_app.model.ChatMessage;
import com.example.dreamaichat_app.model.ChatRole;

import java.io.File;
import java.util.List;

/**
 * 鑱婂ぉ娑堟伅閫傞厤鍣紝鏀寔鐢ㄦ埛/AI/绯荤粺涓夌鏍峰紡銆? */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;
    private static final int TYPE_SYSTEM = 2;

    private List<ChatMessage> data;

    public ChatMessageAdapter(List<ChatMessage> data) {
        this.data = data;
    }

    public void submit(List<ChatMessage> messages) {
        this.data = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = data.get(position);
        if (message.getRole() == ChatRole.USER) {
            return TYPE_USER;
        } else if (message.getRole() == ChatRole.ASSISTANT) {
            return TYPE_AI;
        } else {
            return TYPE_SYSTEM;
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            return new UserHolder(ItemMessageUserBinding.inflate(inflater, parent, false));
        } else if (viewType == TYPE_AI) {
            return new AiHolder(ItemMessageAiBinding.inflate(inflater, parent, false));
        } else {
            return new SystemHolder(ItemMessageSystemBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = data.get(position);
        if (holder instanceof UserHolder) {
            ((UserHolder) holder).bind(message);
        } else if (holder instanceof AiHolder) {
            ((AiHolder) holder).bind(message);
        } else if (holder instanceof SystemHolder) {
            ((SystemHolder) holder).bind(message);
        }
    }

    class UserHolder extends RecyclerView.ViewHolder {
        private final ItemMessageUserBinding binding;

        UserHolder(ItemMessageUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            binding.tvContent.setText(message.getContent());
            binding.messageStatus.setText(message.getStatus().name());
            bindAttachments(binding.attachmentContainer, message);
        }
    }

    class AiHolder extends RecyclerView.ViewHolder {
        private final ItemMessageAiBinding binding;

        AiHolder(ItemMessageAiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            binding.tvContent.setText(message.getContent());
            bindAttachments(binding.attachmentContainer, message);
            if (TextUtils.isEmpty(message.getContent())) {
                binding.btnCopy.setVisibility(View.GONE);
            } else {
                binding.btnCopy.setVisibility(View.VISIBLE);
                binding.btnCopy.setOnClickListener(v -> copyToClipboard(v, message.getContent()));
            }
        }
    }

    class SystemHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSystemBinding binding;

        SystemHolder(ItemMessageSystemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            binding.tvSystemMessage.setText(message.getContent());
        }
    }

    private void bindAttachments(LinearLayout container, ChatMessage message) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        List<ChatAttachment> attachments = message.getAttachments();
        if (attachments == null || attachments.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        for (ChatAttachment attachment : attachments) {
            if (attachment == null || !attachment.isImage()) {
                continue;
            }
            Object source = null;
            if (attachment.getLocalPath() != null) {
                source = new File(attachment.getLocalPath());
            } else if (attachment.getRemoteUrl() != null) {
                source = attachment.getRemoteUrl();
            }
            if (source == null) {
                continue;
            }
            ImageView imageView = new ImageView(container.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = dpToPx(container, 8);
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(imageView)
                .load(source)
                .fitCenter()
                .into(imageView);
            container.addView(imageView);
        }
        container.setVisibility(container.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void copyToClipboard(View view, String content) {
        Context context = view.getContext();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("ai-reply", content);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}
