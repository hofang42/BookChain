package com.hofang.bookchainfe.ui.chatbot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ChatContext;

/**
 * Helper class để thêm floating chatbot button vào bất kỳ Activity nào
 */
public class FloatingChatButtonHelper {
    
    private static final int FLOATING_CHAT_BUTTON_ID = 999888777; // Unique ID
    private static final int CLOSE_BUTTON_ID = 999888778; // Close button ID
    
    /**
     * Thêm floating chat button vào Activity
     * @param activity FragmentActivity để show BottomSheet
     * @param rootView Root view của Activity (thường là findViewById(android.R.id.content))
     * @param context ChatContext với thông tin màn hình hiện tại
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void addFloatingChatButton(FragmentActivity activity, ViewGroup rootView, ChatContext context) {
        // Xóa button cũ nếu có
        View oldButton = rootView.findViewById(FLOATING_CHAT_BUTTON_ID);
        if (oldButton != null) {
            ((ViewGroup) oldButton.getParent()).removeView(oldButton);
        }
        
        // Xóa close button cũ nếu có
        View oldCloseBtn = rootView.findViewById(CLOSE_BUTTON_ID);
        if (oldCloseBtn != null) {
            ((ViewGroup) oldCloseBtn.getParent()).removeView(oldCloseBtn);
        }
        
        // Tạo nút X màu nâu ở dưới giữa màn hình
        ImageView closeButton = new ImageView(activity);
        closeButton.setId(CLOSE_BUTTON_ID);
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setColorFilter(Color.parseColor("#8B4513")); // Màu nâu
        closeButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        closeButton.setPadding(dpToPx(activity, 12), dpToPx(activity, 12), 
                              dpToPx(activity, 12), dpToPx(activity, 12));
        
        // Background tròn cho nút X
        GradientDrawable closeBackground = new GradientDrawable();
        closeBackground.setColor(Color.parseColor("#F5F5F5"));
        closeBackground.setCornerRadius(dpToPx(activity, 28));
        closeButton.setBackground(closeBackground);
        
        int closeSize = dpToPx(activity, 56);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(closeSize, closeSize);
        closeParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        closeParams.setMargins(0, 0, 0, dpToPx(activity, 20));
        closeButton.setLayoutParams(closeParams);
        closeButton.setVisibility(View.GONE); // Ẩn ban đầu
        
        // Thêm nút X vào root view
        if (rootView instanceof FrameLayout) {
            rootView.addView(closeButton);
        }
        
        // Tạo container chứa bubble + icon
        LinearLayout container = new LinearLayout(activity);
        container.setId(FLOATING_CHAT_BUTTON_ID); // Set ID để có thể tìm và xóa sau
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.END);
        
        // Tạo speech bubble
        TextView tvBubble = new TextView(activity);
        tvBubble.setText("Đạt bot sẽ hỗ trợ bạn!");
        tvBubble.setTextColor(Color.WHITE);
        tvBubble.setTextSize(12);
        tvBubble.setPadding(dpToPx(activity, 12), dpToPx(activity, 8), 
                           dpToPx(activity, 12), dpToPx(activity, 8));
        
        // Tạo background bo tròn cho bubble
        GradientDrawable bubbleBackground = new GradientDrawable();
        bubbleBackground.setColor(Color.parseColor("#4CAF50")); // Màu xanh lá
        bubbleBackground.setCornerRadius(dpToPx(activity, 16));
        tvBubble.setBackground(bubbleBackground);
        
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.setMargins(0, 0, dpToPx(activity, 8), dpToPx(activity, 8));
        tvBubble.setLayoutParams(bubbleParams);
        
        // Tạo ImageView cho chatbot icon
        ImageView imgChatbot = new ImageView(activity);
        imgChatbot.setImageResource(R.drawable.ic_chatbox);
        imgChatbot.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        int size = dpToPx(activity, 56);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(size, size);
        imgChatbot.setLayoutParams(imgParams);
        
        // Thêm bubble và icon vào container
        container.addView(tvBubble);
        container.addView(imgChatbot);
        
        // Set layout params cho container
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.BOTTOM | Gravity.END;
        containerParams.setMargins(0, 0, dpToPx(activity, 16), dpToPx(activity, 80));
        container.setLayoutParams(containerParams);
        
        // Biến lưu vị trí ban đầu và trạng thái kéo
        final float[] dX = {0};
        final float[] dY = {0};
        final boolean[] isDragging = {false};
        
        // Set touch listener cho container để kéo thả
        container.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX[0] = v.getX() - event.getRawX();
                    dY[0] = v.getY() - event.getRawY();
                    isDragging[0] = false;
                    
                    // Hiện nút X khi bắt đầu kéo
                    closeButton.setVisibility(View.VISIBLE);
                    closeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    isDragging[0] = true;
                    float newX = event.getRawX() + dX[0];
                    float newY = event.getRawY() + dY[0];
                    
                    // Giới hạn không cho kéo ra ngoài màn hình
                    newX = Math.max(0, Math.min(newX, rootView.getWidth() - v.getWidth()));
                    newY = Math.max(0, Math.min(newY, rootView.getHeight() - v.getHeight()));
                    
                    v.setX(newX);
                    v.setY(newY);
                    
                    // Kiểm tra nếu đang hover trên nút X
                    if (isViewOverlapping(v, closeButton)) {
                        closeButton.setColorFilter(Color.parseColor("#D32F2F")); // Đỏ khi hover
                        closeButton.animate().scaleX(1.5f).scaleY(1.5f).setDuration(100).start();
                    } else {
                        closeButton.setColorFilter(Color.parseColor("#8B4513")); // Nâu bình thường
                        closeButton.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start();
                    }
                    return true;
                    
                case MotionEvent.ACTION_UP:
                    // Kiểm tra nếu thả vào nút X
                    if (isViewOverlapping(v, closeButton)) {
                        // Ẩn cả container
                        container.animate()
                                .alpha(0f)
                                .scaleX(0f)
                                .scaleY(0f)
                                .setDuration(200)
                                .withEndAction(() -> container.setVisibility(View.GONE))
                                .start();
                    } else if (!isDragging[0]) {
                        // Nếu không kéo (chỉ click) thì mở chat
                        ChatBottomSheet bottomSheet = ChatBottomSheet.newInstance(context);
                        bottomSheet.show(activity.getSupportFragmentManager(), "ChatBottomSheet");
                    }
                    
                    // Ẩn nút X
                    closeButton.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .setDuration(200)
                            .withEndAction(() -> closeButton.setVisibility(View.GONE))
                            .start();
                    
                    return true;
            }
            return false;
        });
        
        // Xóa click listener cũ vì giờ dùng touch listener
        // container.setOnClickListener(...); // Đã xử lý trong ACTION_UP
        
        // Add to root view
        if (rootView instanceof FrameLayout) {
            rootView.addView(container);
        } else {
            // Wrap in FrameLayout if root is not FrameLayout
            FrameLayout wrapper = new FrameLayout(activity);
            wrapper.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            wrapper.addView(container);
            rootView.addView(wrapper);
        }
    }
    
    /**
     * Thêm floating chat button vào Activity với screen name đơn giản
     */
    public static void addFloatingChatButton(FragmentActivity activity, ViewGroup rootView, String screenName) {
        ChatContext context = new ChatContext(screenName);
        addFloatingChatButton(activity, rootView, context);
    }
    
    /**
     * Convert dp to pixels
     */
    private static int dpToPx(FragmentActivity activity, int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * Kiểm tra 2 view có overlap không
     */
    private static boolean isViewOverlapping(View view1, View view2) {
        int[] location1 = new int[2];
        int[] location2 = new int[2];
        
        view1.getLocationOnScreen(location1);
        view2.getLocationOnScreen(location2);
        
        int left1 = location1[0];
        int top1 = location1[1];
        int right1 = left1 + view1.getWidth();
        int bottom1 = top1 + view1.getHeight();
        
        int left2 = location2[0];
        int top2 = location2[1];
        int right2 = left2 + view2.getWidth();
        int bottom2 = top2 + view2.getHeight();
        
        return !(right1 < left2 || right2 < left1 || bottom1 < top2 || bottom2 < top1);
    }
}
