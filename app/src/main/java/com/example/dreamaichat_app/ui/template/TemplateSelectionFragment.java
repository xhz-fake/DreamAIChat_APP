package com.example.dreamaichat_app.ui.template;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamaichat_app.R;
import com.example.dreamaichat_app.ui.chat.ChatViewModel;
import com.example.dreamaichat_app.ui.main.MainActivity;

/**
 * 对话模板选择界面（简化版实现）
 */
public class TemplateSelectionFragment extends Fragment {

    private ChatViewModel chatViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_template_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        RecyclerView recyclerView = view.findViewById(R.id.recycler_templates);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(new TemplateSelectionSimpleAdapter(chatViewModel, () -> {
            // 关闭模板界面并回到聊天界面
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).showChatFragment();
            } else {
                requireActivity().onBackPressed();
            }
        }));
    }
}


