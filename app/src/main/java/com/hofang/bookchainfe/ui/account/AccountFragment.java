package com.hofang.bookchainfe.ui.account;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hofang.bookchainfe.R;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.network.AuthApiService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.SocketService;
import com.hofang.bookchainfe.utils.TokenManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.android.material.card.MaterialCardView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TokenManager tokenManager;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ImageView avatarImageView;

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

        // Initialize token manager
        tokenManager = new TokenManager(requireContext());

        // Show bottom navigation when on Account screen
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }

        // Bind UI
        final TextView tvName = view.findViewById(R.id.value_name);
        final TextView tvEmail = view.findViewById(R.id.value_email);
        final TextView tvPassword = view.findViewById(R.id.value_password);
        final TextView tvPhone = view.findViewById(R.id.value_phone);
        avatarImageView = view.findViewById(R.id.avatar);

        // Display user info from stored session (will refresh when coming back from edit)
        displayUserInfo(tvName, tvEmail, tvPassword, tvPhone);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            }
        );

        // Avatar click listener
        view.findViewById(R.id.avatar_container).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        // Load existing avatar if available
        loadAvatar();

        // Add Edit button functionality
        Button btnEdit = view.findViewById(R.id.btn_edit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Navigation.findNavController(view).navigate(R.id.editProfileFragment);
            });
        }

        // Add logout button functionality
        Button btnLogout = view.findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }

        // Add Manage Addresses functionality
        view.findViewById(R.id.card_manage_addresses).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.addressListFragment);
        });

        MaterialCardView cardOrderHistory = view.findViewById(R.id.card_order_history);
        cardOrderHistory.setOnClickListener(v -> {
            // Chúng ta sẽ tạo một action tên là 'action_accountFragment_to_orderHistoryFragment'
            // trong nav_graph.xml
            try {
                Navigation.findNavController(view).navigate(R.id.action_accountFragment_to_orderHistoryFragment);
            } catch (Exception e) {
                Log.e("AccountFragment", "Navigation to OrderHistory failed. Did you add it to nav_graph?", e);
                Toast.makeText(getContext(), "Feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Change Password functionality
        view.findViewById(R.id.card_change_password).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_accountFragment_to_changePasswordFragment);
        });

        // Fetch profile from backend and populate UI (if needed for additional data)
        // fetchProfileAndPopulate(tvName, tvEmail, tvPassword, tvPhone, avatar);
    }

    private void displayUserInfo(TextView tvName, TextView tvEmail, TextView tvPassword, TextView tvPhone) {
        if (tokenManager.isLoggedIn()) {
            tvName.setText(tokenManager.getFullName() != null ? tokenManager.getFullName() : tokenManager.getUsername());
            tvEmail.setText(tokenManager.getEmail());
            tvPassword.setText("********"); // Always mask password

            // Display phone number if available
            String phone = tokenManager.getPhone();
            tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "-");
        } else {
            tvName.setText("Guest User");
            tvEmail.setText("-");
            tvPassword.setText("-");
            tvPhone.setText("-");
        }
    }

    private void performLogout() {
        // Disconnect Socket.IO first to clear old connection
        if (getContext() != null) {
            SocketService socketService = SocketService.getInstance(getContext());
            socketService.disconnect();
            Log.d("AccountFragment", "Socket.IO disconnected on logout");
        }
        
        // Clear user session
        tokenManager.clearSession();
        
        // Hide bottom navigation
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
        
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to welcome screen and clear back stack
        NavController navController = Navigation.findNavController(requireView());
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        navController.navigate(R.id.welcomeFragment, null, navOptions);
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

    private void loadAvatar() {
        String avatarUrl = tokenManager.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_person_white)
                .error(R.drawable.ic_person_white)
                .into(avatarImageView);
        }
    }

    private void uploadAvatar(Uri imageUri) {
        try {
            // Show loading toast
            Toast.makeText(requireContext(), "Uploading avatar...", Toast.LENGTH_SHORT).show();

            // Convert URI to File
            File file = createTempFileFromUri(imageUri);

            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                file
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                "avatar",
                file.getName(),
                requestFile
            );

            String token = tokenManager.getToken();
            String authHeader = "Bearer " + token;

            AuthApiService apiService = ApiConfig.getRetrofit().create(AuthApiService.class);
            Call<ApiResponse<UploadResponse>> call = apiService.uploadAvatar(authHeader, body);

            call.enqueue(new Callback<ApiResponse<UploadResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<UploadResponse>> call,
                                       @NonNull Response<ApiResponse<UploadResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<UploadResponse> apiResponse = response.body();
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            String avatarUrl = apiResponse.getData().getAvatar();

                            // Save to TokenManager
                            tokenManager.saveAvatar(avatarUrl);

                            // Load image with Glide
                            Glide.with(AccountFragment.this)
                                .load(avatarUrl)
                                .circleCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.ic_person_white)
                                .into(avatarImageView);

                            Toast.makeText(requireContext(), "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                        Log.e("AccountFragment", "Upload failed: " + response.code());
                    }

                    // Delete temp file
                    file.delete();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<UploadResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AccountFragment", "Network error", t);
                    file.delete();
                }
            });

        } catch (Exception e) {
            Log.e("AccountFragment", "Error uploading avatar", e);
            Toast.makeText(requireContext(), "Error preparing image", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("avatar", ".jpg", requireContext().getCacheDir());

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }
}
