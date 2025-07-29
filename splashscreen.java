package com.chatapp.pingme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splashscreen extends AppCompatActivity {

    private static final int SPLASH_TIMER = 2500; // Reduced splash time for better UX
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // Make it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();

        // Use modern Handler approach
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndRedirect, SPLASH_TIMER);
    }

    private void checkUserAndRedirect() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        
        Intent intent;
        if (currentUser != null) {
            // User is logged in, go to chat activity
            intent = new Intent(splashscreen.this, chatActivity.class);
        } else {
            // User is not logged in, go to login activity
            intent = new Intent(splashscreen.this, MainActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Do nothing
    }
}