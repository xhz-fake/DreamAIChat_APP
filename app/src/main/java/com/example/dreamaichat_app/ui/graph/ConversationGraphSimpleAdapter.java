package com.example.dreamaichat_app.ui.graph;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.model.ConversationSummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 对话图谱界面使用的简易历史会话列表
 */
public class ConversationGraphSimpleAdapter extends RecyclerView.Adapter<ConversationGraphSimpleAdapter.VH> {

    public interface OnItemClick {
        void onClick(ConversationSummary item);
    }

    private final List<ConversationSummary> items = new ArrayList<>();
    private final OnItemClick listener;
    private final SimpleDateFormat fmt;

    public ConversationGraphSimpleAdapter(OnItemClick listener) {
        this.listener = listener;
        this.fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        this.fmt.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public void submit(List<ConversationSummary> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_graph_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ConversationSummary item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.snippet.setText(item.getSnippet());
        holder.time.setText(fmt.format(new Date(item.getLastTimestamp())));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView snippet;
        final TextView time;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            snippet = itemView.findViewById(R.id.tv_snippet);
            time = itemView.findViewById(R.id.tv_time);
        }
    }
}


