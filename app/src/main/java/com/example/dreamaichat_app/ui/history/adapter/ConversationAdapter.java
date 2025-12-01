package com.example.dreamaichat_app.ui.history.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamaichat_app.databinding.ItemConversationBinding;
import com.example.dreamaichat_app.model.ConversationSummary;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 鍘嗗彶璁板綍鍒楄〃閫傞厤鍣ㄣ€? */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    public interface OnConversationClickListener {
        void onConversationClick(ConversationSummary summary);
    }

    private final OnConversationClickListener listener;
    private List<ConversationSummary> data;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public ConversationAdapter(List<ConversationSummary> data, OnConversationClickListener listener) {
        this.data = data;
        this.listener = listener;
        this.timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public void submit(List<ConversationSummary> conversations) {
        this.data = conversations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;

        Holder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ConversationSummary summary) {
            binding.tvTitle.setText(summary.getTitle());
            binding.tvSnippet.setText(summary.getSnippet());
            binding.tvTime.setText(timeFormat.format(summary.getLastTimestamp()));
            binding.tvModelTag.setText(summary.getModelTag());
            binding.pinIndicator.setVisibility(summary.isPinned() ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(summary);
                }
            });
        }
    }
}
