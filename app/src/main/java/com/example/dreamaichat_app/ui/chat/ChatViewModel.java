package com.example.dreamaichat_app.ui.chat;

import android.app.Application;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dreamaichat_app.data.local.SessionManager;
import com.example.dreamaichat_app.data.remote.RetrofitClient;
import com.example.dreamaichat_app.data.remote.api.ApiService;
import com.example.dreamaichat_app.data.remote.model.ApiResponse;
import com.example.dreamaichat_app.data.remote.model.ChatRequest;
import com.example.dreamaichat_app.data.remote.model.ChatResponse;
import com.example.dreamaichat_app.data.repository.MessageRepository;
import com.example.dreamaichat_app.data.repository.ConversationRepository;
import com.example.dreamaichat_app.data.entity.MessageEntity;
import com.example.dreamaichat_app.data.entity.ConversationEntity;
import com.example.dreamaichat_app.domain.usecase.GetMessagesUseCase;
import com.example.dreamaichat_app.model.ChatAttachment;
import com.example.dreamaichat_app.model.ChatMessage;
import com.example.dreamaichat_app.model.ChatRole;
import com.example.dreamaichat_app.model.MessageStatus;
import com.example.dreamaichat_app.model.ModelOption;
import com.example.dreamaichat_app.model.QuickAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatViewModel extends AndroidViewModel {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1000);
    private static final List<ChatAttachment> NO_ATTACHMENTS = Collections.emptyList();
    private static final int IMAGE_MAX_DIMENSION = 1280;

    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ModelOption> currentModel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGenerating = new MutableLiveData<>(false);
    private final MutableLiveData<List<QuickAction>> quickActions = new MutableLiveData<>(buildDefaultQuickActions());
    private final MutableLiveData<String> toastEvent = new MutableLiveData<>();

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Gson gson = new Gson();
    private final List<ModelOption> availableModels;

    private long activeConversationId = -1L;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(application);
        this.messageRepository = new MessageRepository(application);
        this.conversationRepository = new ConversationRepository(application);
        this.availableModels = buildDefaultModelOptions();
        if (!availableModels.isEmpty()) {
            currentModel.setValue(availableModels.get(0));
        }
        startNewChat();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<ModelOption> getCurrentModel() {
        return currentModel;
    }

    public LiveData<Boolean> isGenerating() {
        return isGenerating;
    }

    public LiveData<List<QuickAction>> getQuickActions() {
        return quickActions;
    }

    public LiveData<String> getToastEvent() {
        return toastEvent;
    }

    public List<ModelOption> getAvailableModels() {
        return availableModels;
    }

    public void switchModel(ModelOption modelOption) {
        currentModel.setValue(modelOption);
    }

    public void startNewChat() {
        activeConversationId = -1L;
        List<ChatMessage> seed = new ArrayList<>();
        seed.add(new ChatMessage(
            nextId(),
            ChatRole.SYSTEM,
            "新的对话空间已准备就绪，任何灵感都可以立即发出。",
            System.currentTimeMillis(),
            NO_ATTACHMENTS,
            MessageStatus.SUCCESS
        ));
        messages.setValue(seed);
    }

    /**
     * 加载指定会话的消息
     * @param conversationId 会话ID
     * @param modelId 模型ID（用于设置当前模型）
     */
    public void loadConversation(long conversationId, String modelId) {
        activeConversationId = conversationId;
        
        // 设置模型
        if (modelId != null) {
            for (ModelOption model : availableModels) {
                if (model.getId().equals(modelId)) {
                    currentModel.setValue(model);
                    break;
                }
            }
        }
        
        // 从本地数据库加载消息
        GetMessagesUseCase useCase = new GetMessagesUseCase(getApplication());
        disposables.add(
            useCase.execute(conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    messageEntities -> {
                        List<ChatMessage> chatMessages = new ArrayList<>();
                        for (com.example.dreamaichat_app.data.entity.MessageEntity entity : messageEntities) {
                            ChatRole role;
                            try {
                                role = ChatRole.valueOf(entity.role.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                role = ChatRole.USER;
                            }
                            
                            MessageStatus status;
                            try {
                                status = MessageStatus.valueOf(entity.status != null ? entity.status.toUpperCase() : "SUCCESS");
                            } catch (IllegalArgumentException e) {
                                status = MessageStatus.SUCCESS;
                            }

                            List<ChatAttachment> attachments = buildAttachmentsFromEntity(entity);
                            
                            ChatMessage chatMessage = new ChatMessage(
                                entity.id != null ? entity.id : nextId(),
                                role,
                                entity.content != null ? entity.content : "",
                                entity.createdAt != null ? entity.createdAt : System.currentTimeMillis(),
                                attachments,
                                status
                            );
                            chatMessages.add(chatMessage);
                        }
                        
                        // 如果没有消息，添加系统提示
                        if (chatMessages.isEmpty()) {
                            chatMessages.add(new ChatMessage(
                                nextId(),
                                ChatRole.SYSTEM,
                                "会话已加载，可以继续对话。",
                                System.currentTimeMillis(),
                                NO_ATTACHMENTS,
                                MessageStatus.SUCCESS
                            ));
                        }
                        
                        messages.setValue(chatMessages);
                    },
                    throwable -> {
                        toastEvent.postValue("加载会话失败：" + (throwable.getMessage() != null ? throwable.getMessage() : "未知错误"));
                        // 即使加载失败，也切换到该会话
                        startNewChat();
                        activeConversationId = conversationId;
                    }
                )
        );
    }

    public void applyQuickPrompt(QuickAction action, PromptCallback callback) {
        if (callback != null) {
            callback.onQuickPrompt(action.getPrompt());
        }
    }

    public void sendMessage(String text) {
        sendMessage(text, Collections.emptyList());
    }

    public void sendMessage(String text, @Nullable List<Uri> attachmentUris) {
        String trimmed = text != null ? text.trim() : "";
        if ((attachmentUris == null || attachmentUris.isEmpty()) && TextUtils.isEmpty(trimmed)) {
            toastEvent.postValue("请输入内容或添加图片");
            return;
        }
        if (attachmentUris == null || attachmentUris.isEmpty()) {
            dispatchMessage(trimmed, Collections.emptyList());
            return;
        }
        if (sessionManager.getToken() == null) {
            toastEvent.postValue("登录状态已失效，请重新登录");
            return;
        }
        disposables.add(
            Single.fromCallable(() -> prepareImagePayloads(attachmentUris))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    payloads -> dispatchMessage(trimmed, payloads),
                    throwable -> toastEvent.setValue("图片处理失败：" +
                        (throwable.getMessage() != null ? throwable.getMessage() : "未知错误"))
                )
        );
    }

    private void dispatchMessage(String text, List<ImagePayload> imagePayloads) {
        String token = sessionManager.getToken();
        if (token == null) {
            toastEvent.postValue("登录状态已失效，请重新登录");
            return;
        }

        String payload = text != null ? text.trim() : "";
        boolean hasAttachment = imagePayloads != null && !imagePayloads.isEmpty();
        if (!hasAttachment && payload.isEmpty()) {
            return;
        }

        List<ChatMessage> currentList = new ArrayList<>(ensureMessageList());
        List<ChatAttachment> attachments = hasAttachment
            ? buildChatAttachments(imagePayloads)
            : NO_ATTACHMENTS;
        String displayContent = payload.isEmpty() && hasAttachment ? "[图片]" : payload;
        ChatMessage pending = new ChatMessage(
            nextId(),
            ChatRole.USER,
            displayContent,
            System.currentTimeMillis(),
            attachments,
            MessageStatus.SENDING
        );
        currentList.add(pending);
        messages.setValue(currentList);
        isGenerating.setValue(true);

        ChatRequest request = new ChatRequest();
        request.message = payload;
        if (activeConversationId > 0) {
            request.conversationId = activeConversationId;
        }
        ModelOption option = currentModel.getValue();
        if (option != null) {
            request.model = option.getId();
        }
        if (hasAttachment) {
            request.images = new ArrayList<>();
            for (ImagePayload imagePayload : imagePayloads) {
                ChatRequest.ImagePayload dto = new ChatRequest.ImagePayload();
                dto.base64 = imagePayload.base64;
                dto.mime = imagePayload.mimeType;
                request.images.add(dto);
            }
        }

        disposables.add(
            apiService.sendMessage("Bearer " + token, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> handleSuccess(response, pending),
                    throwable -> handleError(throwable, pending)
                )
        );
    }

    private void handleSuccess(ApiResponse<ChatResponse> apiResponse, ChatMessage pending) {
        if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
            handleError(new RuntimeException(apiResponse != null ? apiResponse.message : "发送失败"), pending);
            return;
        }
        ChatResponse data = apiResponse.data;
        if (data.conversationId != null) {
            activeConversationId = data.conversationId;
        }
        pending.setStatus(MessageStatus.SUCCESS);
        List<ChatMessage> currentList = new ArrayList<>(ensureMessageList());
        replaceMessage(currentList, pending);

        ChatMessage aiMessage = new ChatMessage(
            nextId(),
            ChatRole.ASSISTANT,
            data.replyMessage != null ? data.replyMessage : "",
            System.currentTimeMillis(),
            NO_ATTACHMENTS,
            MessageStatus.SUCCESS
        );
        currentList.add(aiMessage);
        messages.setValue(currentList);
        isGenerating.setValue(false);

        persistConversationAndMessages(data, pending, aiMessage);
    }

    private void handleError(Throwable throwable, ChatMessage pending) {
        pending.setStatus(MessageStatus.FAILED);
        List<ChatMessage> currentList = new ArrayList<>(ensureMessageList());
        replaceMessage(currentList, pending);
        messages.setValue(currentList);
        isGenerating.setValue(false);
        toastEvent.setValue(throwable.getMessage() != null ? throwable.getMessage() : "发送失败，请稍后重试");
    }

    private void replaceMessage(List<ChatMessage> target, ChatMessage message) {
        for (int i = 0; i < target.size(); i++) {
            if (target.get(i).getId() == message.getId()) {
                target.set(i, message);
                return;
            }
        }
    }

    private List<ChatMessage> ensureMessageList() {
        List<ChatMessage> current = messages.getValue();
        if (current == null) {
            current = new ArrayList<>();
            messages.setValue(current);
        }
        return current;
    }

    private static List<QuickAction> buildDefaultQuickActions() {
        return Arrays.asList(
            new QuickAction("summarize", "总结内容", "请用要点总结上文"),
            new QuickAction("polish", "润色文案", "请帮我润色，使其适合商务场景"),
            new QuickAction("translate", "翻译英文", "请准确翻译为英文并保留专有名词"),
            new QuickAction("analyze", "结构分析", "请从要点、风险、建议三个角度分析")
        );
    }

    private static List<ModelOption> buildDefaultModelOptions() {
        return Arrays.asList(
            new ModelOption("deepseek", "DeepSeek · 精准", "逻辑严密，适合复杂推理", "Quality", 0xFF6C63FF, true),
            new ModelOption("doubao", "豆包 · 极速", "延迟低，适合实时对话", "Speed", 0xFF00B894, true)
        );
    }

    private static long nextId() {
        return ID_GENERATOR.getAndIncrement();
    }

    private void persistConversationAndMessages(ChatResponse data, ChatMessage userMessage, ChatMessage aiMessage) {
        Long conversationId = data.conversationId;
        if (conversationId == null || conversationId <= 0) {
            return;
        }
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            toastEvent.postValue("本地保存聊天记录失败：未找到登录用户信息");
            return;
        }

        String modelId = currentModel.getValue() != null ? currentModel.getValue().getId() : data.provider;
        long timestamp = System.currentTimeMillis();

        ConversationEntity conversation = new ConversationEntity();
        conversation.id = conversationId;
        conversation.userId = userId;
        conversation.title = buildConversationTitle(userMessage.getContent());
        conversation.model = modelId;
        conversation.messageCount = ensureMessageList().size();
        conversation.isFavorite = false;
        conversation.lastMessagePreview = aiMessage.getContent();
        conversation.lastMessageTime = aiMessage.getTimestamp();
        conversation.createdAt = timestamp;
        conversation.updatedAt = timestamp;

        List<MessageEntity> messageEntities = new ArrayList<>();
        messageEntities.add(mapToMessageEntity(userMessage, conversationId));
        messageEntities.add(mapToMessageEntity(aiMessage, conversationId));

        disposables.add(
            conversationRepository.insertOrUpdateConversation(conversation)
                .andThen(messageRepository.insertMessages(messageEntities))
                .subscribeOn(Schedulers.io())
                .subscribe(
                    () -> { /* ignore */ },
                    throwable -> toastEvent.postValue("本地保存聊天记录失败：" +
                        (throwable.getMessage() != null ? throwable.getMessage() : "未知错误"))
                )
        );
    }

    private List<ImagePayload> prepareImagePayloads(List<Uri> uris) throws IOException {
        List<ImagePayload> payloads = new ArrayList<>();
        for (Uri uri : uris) {
            payloads.add(prepareImagePayload(uri));
        }
        return payloads;
    }

    private List<ChatAttachment> buildChatAttachments(List<ImagePayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return NO_ATTACHMENTS;
        }
        List<ChatAttachment> attachments = new ArrayList<>();
        for (ImagePayload payload : payloads) {
            attachments.add(ChatAttachment.image(payload.localPath, payload.mimeType, null));
        }
        return attachments;
    }

    private MessageEntity mapToMessageEntity(ChatMessage message, long conversationId) {
        MessageEntity entity = new MessageEntity();
        entity.conversationId = conversationId;
        entity.role = message.getRole() != null ? message.getRole().name().toLowerCase() : "user";
        entity.content = message.getContent();
        entity.status = message.getStatus() != null ? message.getStatus().name().toLowerCase() : "success";
        entity.createdAt = message.getTimestamp();
        entity.updatedAt = System.currentTimeMillis();
        List<ChatAttachment> attachments = message.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            ChatAttachment attachment = attachments.get(0);
            if (attachment != null) {
                entity.attachmentType = attachment.getType();
                entity.attachmentLocalPath = attachment.getLocalPath();
                entity.attachmentMime = attachment.getMimeType();
                entity.attachmentRemoteUrl = attachment.getRemoteUrl();
            }
            entity.attachmentsJson = gson.toJson(attachments);
        } else {
            entity.attachmentsJson = null;
        }
        return entity;
    }

    private List<ChatAttachment> buildAttachmentsFromEntity(MessageEntity entity) {
        if (entity == null) {
            return NO_ATTACHMENTS;
        }
        if (!TextUtils.isEmpty(entity.attachmentsJson)) {
            try {
                List<ChatAttachment> result = gson.fromJson(
                    entity.attachmentsJson,
                    new TypeToken<List<ChatAttachment>>() {}.getType()
                );
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            } catch (Exception ignore) {
            }
        }
        boolean hasLocal = !TextUtils.isEmpty(entity.attachmentLocalPath);
        boolean hasRemote = !TextUtils.isEmpty(entity.attachmentRemoteUrl);
        if (!hasLocal && !hasRemote) {
            return NO_ATTACHMENTS;
        }
        if (!TextUtils.isEmpty(entity.attachmentType)
            && ChatAttachment.TYPE_IMAGE.equals(entity.attachmentType)) {
            return Collections.singletonList(
                ChatAttachment.image(entity.attachmentLocalPath, entity.attachmentMime, entity.attachmentRemoteUrl)
            );
        }
        return NO_ATTACHMENTS;
    }

    private String buildConversationTitle(String original) {
        if (TextUtils.isEmpty(original)) {
            return "新对话";
        }
        String trimmed = original.trim();
        return trimmed.length() > 18 ? trimmed.substring(0, 18) + "..." : trimmed;
    }

    private ImagePayload prepareImagePayload(Uri imageUri) throws IOException {
        ContentResolver resolver = getApplication().getContentResolver();
        if (resolver == null) {
            throw new IOException("无法访问图片内容");
        }
        String mimeType = resolveMimeType(imageUri);
        File imagesDir = new File(getApplication().getFilesDir(), "chat_images");
        if (!imagesDir.exists() && !imagesDir.mkdirs()) {
            throw new IOException("无法创建本地图片目录");
        }
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (TextUtils.isEmpty(extension)) {
            extension = "jpg";
        }
        File targetFile = new File(imagesDir, "img_" + System.currentTimeMillis() + "." + extension);
        try (InputStream inputStream = resolver.openInputStream(imageUri);
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            if (inputStream == null) {
                throw new IOException("无法读取图片数据");
            }
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        }
        byte[] compressed = compressForUpload(targetFile, mimeType);
        String base64 = Base64.encodeToString(compressed, Base64.NO_WRAP);
        return new ImagePayload(targetFile.getAbsolutePath(), mimeType, base64);
    }

    private String resolveMimeType(Uri uri) {
        ContentResolver resolver = getApplication().getContentResolver();
        if (resolver != null) {
            String detected = resolver.getType(uri);
            if (!TextUtils.isEmpty(detected)) {
                return detected;
            }
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (!TextUtils.isEmpty(extension)) {
            String guessed = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (!TextUtils.isEmpty(guessed)) {
                return guessed;
            }
        }
        return "image/jpeg";
    }

    private byte[] compressForUpload(File file, String mimeType) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(bounds, IMAGE_MAX_DIMENSION, IMAGE_MAX_DIMENSION);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), decodeOptions);
        if (bitmap == null) {
            throw new IOException("无法解析图片内容");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = mimeType != null && mimeType.contains("png")
            ? Bitmap.CompressFormat.PNG
            : Bitmap.CompressFormat.JPEG;
        bitmap.compress(format, format == Bitmap.CompressFormat.JPEG ? 85 : 100, bos);
        bitmap.recycle();
        return bos.toByteArray();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }

    private static class ImagePayload {
        final String localPath;
        final String mimeType;
        final String base64;

        ImagePayload(String localPath, String mimeType, String base64) {
            this.localPath = localPath;
            this.mimeType = mimeType;
            this.base64 = base64;
        }
    }

    public interface PromptCallback {
        void onQuickPrompt(String prompt);
    }
}
