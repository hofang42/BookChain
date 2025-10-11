package com.hofang.bookchainfe.ui.address;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hofang.bookchainfe.R;

public class AddAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        TextView tvStreet = findViewById(R.id.tv_street);
        TextView tvCity = findViewById(R.id.tv_city);
        TextView tvPostal = findViewById(R.id.tv_postal);
        TextView tvSave = findViewById(R.id.tv_save);

        if (getIntent() != null) {
            String street = getIntent().getStringExtra("street");
            String city = getIntent().getStringExtra("city");
            String postal = getIntent().getStringExtra("postal");
            boolean save = getIntent().getBooleanExtra("save", false);

            if (tvStreet != null) tvStreet.setText("Street: " + (street != null ? street : ""));
            if (tvCity != null) tvCity.setText("City: " + (city != null ? city : ""));
            if (tvPostal != null) tvPostal.setText("Postal: " + (postal != null ? postal : ""));
            if (tvSave != null) tvSave.setText("Saved: " + save);
        }
    }

}
