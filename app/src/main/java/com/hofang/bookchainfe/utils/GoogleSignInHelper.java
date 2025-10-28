package com.hofang.bookchainfe.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hofang.bookchainfe.R;

/**
 * Helper class for Google Sign-In with Firebase Authentication
 * Based on: https://github.com/Everyday-Programmer/Firebase-Google-Authentication
 */
public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    
    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    private GoogleSignInListener listener;
    
    public interface GoogleSignInListener {
        void onSignInSuccess(FirebaseUser user, String idToken);
        void onSignInFailure(String error);
        void onSignInCancelled();
    }
    
    public GoogleSignInHelper(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        // Debug: Log the Web Client ID being used
        String webClientId = context.getString(R.string.default_web_client_id);
        Log.d(TAG, "Using Web Client ID: " + webClientId);
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
                
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);
        Log.d(TAG, "GoogleSignInHelper initialized successfully");
    }
    
    public void setListener(GoogleSignInListener listener) {
        this.listener = listener;
    }
    
    /**
     * Start Google Sign-In flow
     */
    public void signIn(ActivityResultLauncher<Intent> launcher) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        launcher.launch(signInIntent);
    }
    
    /**
     * Handle Google Sign-In result
     */
    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google sign in successful: " + account.getEmail());
            
            // Authenticate with Firebase
            firebaseAuthWithGoogle(account.getIdToken());
            
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Log.e(TAG, "Error code: " + e.getStatusCode() + ", Message: " + e.getMessage());
            if (listener != null) {
                if (e.getStatusCode() == 12501) {
                    // User cancelled
                    listener.onSignInCancelled();
                } else {
                    listener.onSignInFailure("Google sign in failed: " + e.getStatusCode() + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Authenticate with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (listener != null && user != null) {
                            listener.onSignInSuccess(user, idToken);
                        }
                    } else {
                        Log.w(TAG, "Firebase authentication failed", task.getException());
                        if (listener != null) {
                            String error = task.getException() != null ? 
                                task.getException().getMessage() : "Authentication failed";
                            listener.onSignInFailure(error);
                        }
                    }
                });
    }
    
    /**
     * Sign out from Google and Firebase
     */
    public void signOut() {
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Google sign out completed");
        });
    }
    
    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Check if user is signed in
     */
    public boolean isSignedIn() {
        return getCurrentUser() != null;
    }
}
