package com.example.dreamaichat_app.ui.chat.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamaichat_app.databinding.ItemMessageAiBinding;
import com.example.dreamaichat_app.databinding.ItemMessageSystemBinding;
import com.example.dreamaichat_app.databinding.ItemMessageUserBinding;
import com.example.dreamaichat_app.model.ChatMessage;
import com.example.dreamaichat_app.model.ChatRole;

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
}
