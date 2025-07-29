package com.chatapp.pingme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleSignIn";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.googleSignInButton).setOnClickListener(this::signInWithGoogle);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (validateInputs(email, password)) {
                progressBar.setVisibility(View.VISIBLE);
                signInWithEmail(email, password);
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Email required");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password required");
            return false;
        }
        return true;
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if user profile exists
                            checkUserProfileAndNavigate(user);
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle(View view) {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Google sign-in failed: Token missing", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.w(TAG, "Google sign-in failed", e);
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // For Google sign-in, automatically save user profile
                            saveUserToFirestore(user);
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "Unknown");
        userData.put("image", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail() != null ? user.getEmail() : "");
        userData.put("status", "Online");

        db.collection("Users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    startChatActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Still proceed to chat activity even if save fails
                    startChatActivity();
                });
    }

    private void checkUserProfileAndNavigate(FirebaseUser user) {
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // Profile exists, go to chat
                        startChatActivity();
                    } else {
                        // Profile doesn't exist, go to profile setup
                        startProfileSetupActivity();
                    }
                });
    }

    private void startChatActivity() {
        startActivity(new Intent(this, chatActivity.class));
        finish();
    }

    private void startProfileSetupActivity() {
        startActivity(new Intent(this, setProfile.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, check profile and navigate
            checkUserProfileAndNavigate(currentUser);
        }
    }
}