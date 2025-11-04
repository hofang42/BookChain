package com.hofang.bookchainfe.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hofang.bookchainfe.R;

public class LoadingOverlay {
    private final Activity activity;
    private View overlayView;
    private TextView tvLoadingText;
    private TextView tvLoadingSubtext;
    private ProgressBar progressSpinner;
    private boolean isShowing = false;

    public LoadingOverlay(Activity activity) {
        this.activity = activity;
        initializeOverlay();
    }

    private void initializeOverlay() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        overlayView = inflater.inflate(R.layout.loading_overlay, null);
        
        tvLoadingText = overlayView.findViewById(R.id.tvLoadingText);
        tvLoadingSubtext = overlayView.findViewById(R.id.tvLoadingSubtext);
        progressSpinner = overlayView.findViewById(R.id.progressSpinner);
        
        // Thêm overlay vào root view
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        rootView.addView(overlayView);
        
        // Ẩn overlay ban đầu
        overlayView.setVisibility(View.GONE);
    }

    public void show() {
        show("Đang đăng nhập...", "Vui lòng đợi...");
    }

    public void show(String mainText) {
        show(mainText, "Vui lòng đợi...");
    }

    public void show(String mainText, String subText) {
        if (isShowing) return;
        
        isShowing = true;
        
        // Cập nhật text
        tvLoadingText.setText(mainText);
        tvLoadingSubtext.setText(subText);
        
        // Hiển thị overlay với animation
        overlayView.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in_overlay);
        overlayView.startAnimation(fadeIn);
        
        // Bắt đầu animation xoay cho spinner
        Animation rotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate_spinner);
        progressSpinner.startAnimation(rotateAnimation);
    }

    public void updateText(String mainText) {
        updateText(mainText, null);
    }

    public void updateText(String mainText, String subText) {
        if (!isShowing) return;
        
        tvLoadingText.setText(mainText);
        if (subText != null) {
            tvLoadingSubtext.setText(subText);
        }
    }

    public void hide() {
        if (!isShowing) return;
        
        isShowing = false;
        
        // Dừng animation xoay
        progressSpinner.clearAnimation();
        
        // Ẩn overlay với animation nhanh hơn
        Animation fadeOut = AnimationUtils.loadAnimation(activity, R.anim.fade_out_overlay);
        fadeOut.setDuration(200); // Giảm thời gian fade out để mượt hơn
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                overlayView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        overlayView.startAnimation(fadeOut);
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void destroy() {
        if (overlayView != null) {
            ViewGroup rootView = activity.findViewById(android.R.id.content);
            rootView.removeView(overlayView);
            overlayView = null;
        }
    }
}
