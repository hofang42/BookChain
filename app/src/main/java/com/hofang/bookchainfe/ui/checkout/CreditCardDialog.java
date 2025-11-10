package com.hofang.bookchainfe.ui.checkout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.CreditCard;

public class CreditCardDialog extends Dialog {
    private TextInputEditText etCardNumber;
    private TextInputEditText etExpiryDate;
    private TextInputEditText etCvv;
    private TextInputEditText etCardholderName;
    private TextInputEditText etAddress;
    private TextInputEditText etPostalCode;
    private TextInputLayout tilAddress;
    private TextView tvAddressError;
    private Button btnComplete;

    private CreditCardListener listener;

    public interface CreditCardListener {
        void onCreditCardSubmitted(CreditCard card);
    }

    public CreditCardDialog(@NonNull Context context, CreditCardListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_credit_card);

        initViews();
        setupListeners();
        setupValidation();
    }

    private void initViews() {
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCvv = findViewById(R.id.et_cvv);
        etCardholderName = findViewById(R.id.et_cardholder_name);
        etAddress = findViewById(R.id.et_address);
        etPostalCode = findViewById(R.id.et_postal_code);
        tilAddress = findViewById(R.id.til_address);
        tvAddressError = findViewById(R.id.tv_address_error);
        btnComplete = findViewById(R.id.btn_complete);
    }

    private void setupListeners() {
        btnComplete.setOnClickListener(v -> {
            if (validateForm()) {
                CreditCard card = new CreditCard();
                card.setCardNumber(etCardNumber.getText().toString().trim());
                card.setExpiryDate(etExpiryDate.getText().toString().trim());
                card.setCvv(etCvv.getText().toString().trim());
                card.setCardholderName(etCardholderName.getText().toString().trim());
                card.setBillingAddress(etAddress.getText().toString().trim());
                card.setPostalCode(etPostalCode.getText().toString().trim());

                if (listener != null) {
                    listener.onCreditCardSubmitted(card);
                }
                dismiss();
            }
        });
    }

    private void setupValidation() {
        // Auto-format card number with spaces
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private static final int SPACE_INTERVAL = 4;
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String text = s.toString().replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < text.length(); i++) {
                    if (i > 0 && i % SPACE_INTERVAL == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(text.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());
                isFormatting = false;
            }
        });

        // Auto-format expiry date with slash
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String text = s.toString().replaceAll("/", "");
                if (text.length() >= 2) {
                    text = text.substring(0, 2) + "/" + text.substring(2);
                }
                if (text.length() > 5) {
                    text = text.substring(0, 5);
                }

                s.replace(0, s.length(), text);
                isFormatting = false;
            }
        });

        // Address validation on focus change
        etAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateAddress();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate card number
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        if (cardNumber.isEmpty() || cardNumber.length() < 13) {
            etCardNumber.setError("Số thẻ không hợp lệ");
            isValid = false;
        }

        // Validate expiry date
        String expiryDate = etExpiryDate.getText().toString();
        if (expiryDate.isEmpty() || expiryDate.length() != 5) {
            etExpiryDate.setError("Ngày hết hạn không hợp lệ");
            isValid = false;
        }

        // Validate CVV
        String cvv = etCvv.getText().toString();
        if (cvv.isEmpty() || (cvv.length() != 3 && cvv.length() != 4)) {
            etCvv.setError("Mã CVV không hợp lệ");
            isValid = false;
        }

        // Validate cardholder name
        String cardholderName = etCardholderName.getText().toString().trim();
        if (cardholderName.isEmpty()) {
            etCardholderName.setError("Vui lòng nhập họ tên chủ thẻ");
            isValid = false;
        }

        // Validate address
        if (!validateAddress()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateAddress() {
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            tilAddress.setError(" ");
            tvAddressError.setVisibility(View.VISIBLE);
            return false;
        } else {
            tilAddress.setError(null);
            tvAddressError.setVisibility(View.GONE);
            return true;
        }
    }
}
