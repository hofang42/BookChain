package com.hofang.bookchainfe.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hofang.bookchainfe.R;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import com.hofang.bookchainfe.network.ApiConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Window insets for bottom navigation are now handled by AccountActivity
        // No need to handle here

        // Initialize views and set up click listeners here
        // Example: view.findViewById(R.id.btn_edit).setOnClickListener(v -> { ... });
        // Bind UI
        final TextView tvName = view.findViewById(R.id.value_name);
        final TextView tvEmail = view.findViewById(R.id.value_email);
        final TextView tvPassword = view.findViewById(R.id.value_password);
        final TextView tvAddress = view.findViewById(R.id.value_address);
        final ImageView avatar = view.findViewById(R.id.avatar);

        // Fetch profile from backend and populate UI
        fetchProfileAndPopulate(tvName, tvEmail, tvPassword, tvAddress, avatar);
    }

    // Basic HTTP GET using HttpURLConnection on background thread
    private void fetchProfileAndPopulate(final TextView tvName,
                                         final TextView tvEmail,
                                         final TextView tvPassword,
                                         final TextView tvAddress,
                                         final ImageView avatar) {
    // Use central ApiConfig so other screens can reuse the same base URL/endpoints
    final String endpoint = ApiConfig.USERS; // returns array of users

        ExecutorService ex = Executors.newSingleThreadExecutor();
        Handler uiHandler = new Handler(Looper.getMainLooper());

        ex.submit(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String body = readStream(is);

                if (code >= 200 && code < 300) {
                    // Expecting an array of users; take the first as current user for now
                    Log.d("AccountFragment", "users response body: " + body);
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() > 0) {
                        JSONObject user = arr.getJSONObject(0);
                        final String name = user.optString("fullName", user.optString("username", "-"));
                        final String email = user.optString("email", "-");
                        // We don't expose password; show masked placeholder
                        final String passwordMask = "********";
                        final String address = user.optString("address", "");

                        uiHandler.post(() -> {
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvPassword.setText(passwordMask);
                            tvAddress.setText(address.isEmpty() ? "-" : address);
                            // show a small confirmation so we can tell the app loaded remote data
                            Toast.makeText(getContext(), "Loaded user: " + name, Toast.LENGTH_SHORT).show();
                            // TODO: load avatar from URL if available (e.g., with Glide/Picasso)
                        });
                    } else {
                        uiHandler.post(() -> tvName.setText("(no user)") );
                    }
                } else {
                    Log.w("AccountFragment", "Failed to fetch users: " + code + " - " + body);
                }

            } catch (Exception e) {
                Log.e("AccountFragment", "Error fetching profile", e);
            } finally {
                if (conn != null) conn.disconnect();
                ex.shutdown();
            }
        });
    }

    private String readStream(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
