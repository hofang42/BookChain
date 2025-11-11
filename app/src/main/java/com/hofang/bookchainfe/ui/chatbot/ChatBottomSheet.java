package com.hofang.bookchainfe.ui.chatbot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ChatContext;
import com.hofang.bookchainfe.model.ChatMessage;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.BookApiService;
import com.hofang.bookchainfe.network.ChatAIApiService;
import com.hofang.bookchainfe.ui.home.BookItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBottomSheet extends BottomSheetDialogFragment {
    
    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnAttachImage;
    private ImageButton btnCloseChat;
    private ImageButton btnRemoveImage;
    private ProgressBar pbLoading;
    private TextView tvContextInfo;
    private RelativeLayout layoutImagePreview;
    private ImageView ivPreview;
    
    private ChatAdapter chatAdapter;
    private ChatAIApiService apiService;
    private ChatContext context;
    private String currentImageBase64;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    public static ChatBottomSheet newInstance(ChatContext context) {
        ChatBottomSheet sheet = new ChatBottomSheet();
        Bundle args = new Bundle();
        args.putString("screenName", context.getScreenName());
        args.putString("bookId", context.getBookId());
        args.putString("bookTitle", context.getBookTitle());
        sheet.setArguments(args);
        return sheet;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        handleImageSelected(imageUri);
                    }
                }
        );
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_chat, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initContext();
        initRecyclerView();
        initListeners();
        
        apiService = ApiConfig.getRetrofit().create(ChatAIApiService.class);
        
        // Add greeting message
        addAIMessage(getString(R.string.ai_greeting));
    }
    
    private void initViews(View view) {
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);
        btnAttachImage = view.findViewById(R.id.btn_attach_image);
        btnCloseChat = view.findViewById(R.id.btn_close_chat);
        btnRemoveImage = view.findViewById(R.id.btn_remove_image);
        pbLoading = view.findViewById(R.id.pb_loading);
        tvContextInfo = view.findViewById(R.id.tv_context_info);
        layoutImagePreview = view.findViewById(R.id.layout_image_preview);
        ivPreview = view.findViewById(R.id.iv_preview);
    }
    
    private void initContext() {
        context = new ChatContext();
        if (getArguments() != null) {
            context.setScreenName(getArguments().getString("screenName", "Trang chủ"));
            context.setBookId(getArguments().getString("bookId"));
            context.setBookTitle(getArguments().getString("bookTitle"));
            
            if (context.getBookTitle() != null) {
                tvContextInfo.setVisibility(View.VISIBLE);
                tvContextInfo.setText("Đang xem: " + context.getBookTitle());
                
                // Ẩn nút gửi ảnh ở trang details vì đã biết sách rồi
                btnAttachImage.setVisibility(View.GONE);
            } else {
                btnAttachImage.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void initRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);
        
        // Set book click listener
        chatAdapter.setOnBookClickListener((bookId, bookTitle) -> {
            // Navigate to book detail with title
            navigateToBookDetail(bookId, bookTitle);
        });
        
        android.util.Log.d("ChatBot", "RecyclerView initialized - Visibility: " + rvChatMessages.getVisibility() + ", Width: " + rvChatMessages.getWidth() + ", Height: " + rvChatMessages.getHeight());
        
        rvChatMessages.post(() -> {
            android.util.Log.d("ChatBot", "RecyclerView post - Width: " + rvChatMessages.getWidth() + ", Height: " + rvChatMessages.getHeight() + ", ChildCount: " + rvChatMessages.getChildCount());
        });
    }
    
    private void initListeners() {
        btnCloseChat.setOnClickListener(v -> dismiss());
        
        btnSend.setOnClickListener(v -> sendMessage());
        
        btnAttachImage.setOnClickListener(v -> openImagePicker());
        
        btnRemoveImage.setOnClickListener(v -> {
            currentImageBase64 = null;
            layoutImagePreview.setVisibility(View.GONE);
        });
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void handleImageSelected(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            
            // Resize image if too large
            int maxSize = 800;
            if (bitmap.getWidth() > maxSize || bitmap.getHeight() > maxSize) {
                float scale = Math.min(
                        (float) maxSize / bitmap.getWidth(),
                        (float) maxSize / bitmap.getHeight()
                );
                int newWidth = Math.round(bitmap.getWidth() * scale);
                int newHeight = Math.round(bitmap.getHeight() * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            // Convert to base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            currentImageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            
            // Show preview
            ivPreview.setImageBitmap(bitmap);
            layoutImagePreview.setVisibility(View.VISIBLE);
            
        } catch (IOException e) {
            Toast.makeText(getContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        
        if (message.isEmpty() && currentImageBase64 == null) {
            return;
        }
        
        // Add user message to chat
        ChatMessage userMessage = new ChatMessage(
                message.isEmpty() ? "Đây là sách gì?" : message,
                "user",
                currentImageBase64
        );
        chatAdapter.addMessage(userMessage);
        scrollToBottom();
        
        // Clear input
        etMessage.setText("");
        
        // Send to API
        if (currentImageBase64 != null) {
            sendImageMessage(currentImageBase64, message);
            currentImageBase64 = null;
            layoutImagePreview.setVisibility(View.GONE);
        } else {
            sendTextMessage(message);
        }
        
        setLoading(true);
    }
    
    private void sendTextMessage(String message) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("message", message);
            
            JSONObject contextJson = new JSONObject();
            contextJson.put("screenName", context.getScreenName());
            if (context.getBookId() != null) {
                contextJson.put("bookId", context.getBookId());
            }
            requestBody.put("context", contextJson);
            
            // Build conversation history từ chatAdapter (chỉ lấy 10 tin nhắn gần nhất)
            JSONArray historyArray = new JSONArray();
            java.util.List<ChatMessage> allMessages = chatAdapter.getMessages();
            int startIndex = Math.max(0, allMessages.size() - 10); // Chỉ lấy 10 tin nhắn cuối
            
            for (int i = startIndex; i < allMessages.size(); i++) {
                ChatMessage msg = allMessages.get(i);
                // Chỉ thêm text messages, bỏ qua image messages
                if (msg.getImageBase64() == null) {
                    JSONObject historyMsg = new JSONObject();
                    historyMsg.put("role", msg.getType().equals("user") ? "user" : "assistant");
                    historyMsg.put("content", msg.getContent());
                    historyArray.put(historyMsg);
                }
            }
            requestBody.put("conversationHistory", historyArray);
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    requestBody.toString()
            );
            
            apiService.sendChatMessage(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (getActivity() == null) return;
                    
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseStr = response.body().string();
                                android.util.Log.d("ChatBot", "Response: " + responseStr);
                                JSONObject jsonResponse = new JSONObject(responseStr);
                                
                                // Backend trả về có field "success"
                                if (jsonResponse.getBoolean("success")) {
                                    String aiMessage = jsonResponse.getString("message");
                                    android.util.Log.d("ChatBot", "AI Message: " + aiMessage);
                                    addAIMessage(aiMessage);
                                } else {
                                    addAIMessage("Xin lỗi, có lỗi xảy ra.");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ChatBot", "Parse error", e);
                                addAIMessage("Xin lỗi, tôi gặp sự cố khi xử lý câu trả lời: " + e.getMessage());
                            }
                        } else {
                            android.util.Log.e("ChatBot", "Response not successful: " + response.code());
                            addAIMessage("Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.");
                        }
                    });
                }
                
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (getActivity() == null) return;
                    
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                        addAIMessage("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.");
                    });
                }
            });
            
        } catch (Exception e) {
            setLoading(false);
            addAIMessage("Có lỗi xảy ra khi gửi tin nhắn.");
        }
    }
    
    private void sendImageMessage(String imageBase64, String message) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("imageBase64", "data:image/jpeg;base64," + imageBase64);
            requestBody.put("message", message.isEmpty() ? "Đây là sách gì?" : message);
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    requestBody.toString()
            );
            
            apiService.sendChatImage(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (getActivity() == null) return;
                    
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseStr = response.body().string();
                                android.util.Log.d("ChatBot", "Image Response: " + responseStr);
                                JSONObject jsonResponse = new JSONObject(responseStr);
                                
                                if (jsonResponse.getBoolean("success")) {
                                    String aiMessage = jsonResponse.getString("message");
                                    addAIMessage(aiMessage);
                                } else {
                                    addAIMessage("Xin lỗi, không thể nhận diện ảnh.");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ChatBot", "Image parse error", e);
                                addAIMessage("Xin lỗi, tôi không thể nhận diện ảnh này: " + e.getMessage());
                            }
                        } else {
                            addAIMessage("Xin lỗi, tôi đang gặp sự cố khi xử lý ảnh.");
                        }
                    });
                }
                
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (getActivity() == null) return;
                    
                    getActivity().runOnUiThread(() -> {
                        setLoading(false);
                        addAIMessage("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.");
                    });
                }
            });
            
        } catch (Exception e) {
            setLoading(false);
            addAIMessage("Có lỗi xảy ra khi gửi ảnh.");
        }
    }
    
    private void addAIMessage(String message) {
        android.util.Log.d("ChatBot", "addAIMessage called: " + message);
        ChatMessage aiMessage = new ChatMessage(message, "ai");
        android.util.Log.d("ChatBot", "ChatMessage created, adding to adapter");
        chatAdapter.addMessage(aiMessage);
        android.util.Log.d("ChatBot", "Message added, requesting layout");
        rvChatMessages.requestLayout();
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        android.util.Log.d("ChatBot", "scrollToBottom - Adapter count: " + chatAdapter.getItemCount() + ", RV childCount: " + rvChatMessages.getChildCount());
        if (chatAdapter.getItemCount() > 0) {
            rvChatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }
    
    private void setLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!loading);
        etMessage.setEnabled(!loading);
    }
    
    private void navigateToBookDetail(String bookId, String bookTitle) {
        // Show loading
        Toast.makeText(getContext(), "Đang tải thông tin sách...", Toast.LENGTH_SHORT).show();
        
        // Call API to get full book info
        BookApiService bookApiService = ApiConfig.getRetrofit().create(BookApiService.class);
        bookApiService.getBookById(bookId).enqueue(new Callback<BookItem>() {
            @Override
            public void onResponse(Call<BookItem> call, Response<BookItem> response) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        BookItem book = response.body();
                        
                        // Dismiss chat
                        dismiss();
                        
                        // Navigate with full book data
                        android.os.Bundle bundle = new android.os.Bundle();
                        bundle.putString("bookId", book.getId());
                        bundle.putString("title", book.getTitle());
                        bundle.putString("author", book.getAuthor());
                        bundle.putString("category", book.getCategory() != null ? book.getCategory().getName() : "");
                        bundle.putFloat("price", (float) book.getPrice());
                        bundle.putInt("discount", (int) book.getDiscount());
                        bundle.putString("coverUrl", book.getCoverImage());
                        
                        try {
                            androidx.navigation.NavController navController = 
                                androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                            navController.navigate(R.id.bookDetailFragment, bundle);
                        } catch (Exception e) {
                            android.util.Log.e("ChatBot", "Navigation error", e);
                            Toast.makeText(getContext(), "Không thể mở chi tiết sách", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onFailure(Call<BookItem> call, Throwable t) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("ChatBot", "Failed to load book", t);
                });
            }
        });
    }
}
