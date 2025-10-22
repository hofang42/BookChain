package com.hofang.bookchainfe.ui.categories; // Gói của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.RangeSlider;
import com.hofang.bookchainfe.R; // Đảm bảo import R đúng

import java.util.List;
import java.util.Locale;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private TextView tvPriceRange;
    private RangeSlider rangeSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tải layout của bottom sheet
        return inflater.inflate(R.layout.categories_bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các views
        ImageButton btnClose = view.findViewById(R.id.btn_close_filter);
        TextView tvReset = view.findViewById(R.id.tv_reset_filter);
        tvPriceRange = view.findViewById(R.id.tv_price_range);
        rangeSlider = view.findViewById(R.id.range_slider_price);

        // Nút đóng
        btnClose.setOnClickListener(v -> dismiss());

        // Nút Reset (ví dụ: reset slider)
        tvReset.setOnClickListener(v -> {
            rangeSlider.setValues(0f, 80f);
            // reset các chip nếu cần
        });

        // Cập nhật text khi kéo slider
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> values = slider.getValues();
                String priceText = String.format(Locale.US, "%.0f-%.0f$", values.get(0), values.get(1));
                tvPriceRange.setText(priceText);
            }
        });

        // Set giá trị text ban đầu
        List<Float> initialValues = rangeSlider.getValues();
        String initialPriceText = String.format(Locale.US, "%.0f-%.0f$", initialValues.get(0), initialValues.get(1));
        tvPriceRange.setText(initialPriceText);
    }

    // Áp dụng style để có nền bo góc
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }
}