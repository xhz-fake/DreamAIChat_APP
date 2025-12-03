package com.example.dreamaichat_app.ui.template;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.ui.chat.ChatViewModel;

import java.util.Arrays;
import java.util.List;

/**
 * 简单的本地模板列表，点击后将模板文案填入当前聊天输入框
 */
public class TemplateSelectionSimpleAdapter extends RecyclerView.Adapter<TemplateSelectionSimpleAdapter.VH> {

    public interface OnTemplateSelectedListener {
        void onTemplateSelected();
    }

    private static class TemplateItem {
        final int iconRes;
        final String label;
        final String prompt;

        TemplateItem(int iconRes, String label, String prompt) {
            this.iconRes = iconRes;
            this.label = label;
            this.prompt = prompt;
        }
    }

    private final List<TemplateItem> templates = Arrays.asList(
        new TemplateItem(
            R.drawable.ic_template_code,
            "编程助手",
            "你是一名专业的编程助手，请帮我写一段 Java 代码，功能是："
        ),
        new TemplateItem(
            R.drawable.ic_template_paper,
            "论文助手",
            "你是一名学术写作助手，请帮我润色下面的学术段落，并给出修改说明："
        ),
        new TemplateItem(
            R.drawable.ic_template_medical,
            "医疗咨询（仅供参考）",
            "你是一名健康咨询助手，根据以下症状给出生活建议（不构成医疗诊断）："
        ),
        new TemplateItem(
            R.drawable.ic_template_translate,
            "翻译专家",
            "你是一名翻译专家，请将下面的中文翻译成自然流畅的英文："
        )
    );

    private final ChatViewModel chatViewModel;
    private final OnTemplateSelectedListener listener;

    public TemplateSelectionSimpleAdapter(ChatViewModel chatViewModel, OnTemplateSelectedListener listener) {
        this.chatViewModel = chatViewModel;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_template_simple, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TemplateItem item = templates.get(position);
        holder.tvTitle.setText(item.label);
        holder.tvDesc.setText(item.prompt);
        holder.icon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> {
            if (chatViewModel != null) {
                // 使用 ViewModel 的 pendingPrompt 机制，会在切换到聊天界面后自动填入
                chatViewModel.applyQuickPrompt(
                    new com.example.dreamaichat_app.model.QuickAction("template_" + position, item.label, item.prompt),
                    null // 传入 null，让 ViewModel 存储到 pendingPrompt
                );
            }
            if (listener != null) {
                listener.onTemplateSelected();
            }
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView tvTitle;
        final TextView tvDesc;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_template_icon);
            tvTitle = itemView.findViewById(R.id.tv_template_title);
            tvDesc = itemView.findViewById(R.id.tv_template_desc);
        }
    }
}


