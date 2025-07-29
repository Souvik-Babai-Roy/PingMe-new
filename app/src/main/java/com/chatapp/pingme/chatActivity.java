package com.chatapp.pingme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class chatActivity extends AppCompatActivity {

    TabLayout tabLayout;
    TabItem mchat, mcall, mstatus;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    androidx.appcompat.widget.Toolbar mtoolbar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        initializeFirebase();
        setupToolbar();
        setupViewPager();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.include);
        mchat = findViewById(R.id.chat);
        mcall = findViewById(R.id.calls);
        mstatus = findViewById(R.id.status);
        viewPager = findViewById(R.id.fragmentcontainer);
        mtoolbar = findViewById(R.id.toolbar);
    }

    private void initializeFirebase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbar);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_more_vert_24);
        if (mtoolbar.getOverflowIcon() == null) {
            mtoolbar.setOverflowIcon(drawable);
        }
    }

    private void setupViewPager() {
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        // Replace deprecated setOnTabSelectedListener with modern approach
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional: Handle tab unselection
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: Handle tab reselection (e.g., scroll to top)
            }
        });

        // Setup bidirectional synchronization between ViewPager and TabLayout
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.profile) {
            startActivity(new Intent(chatActivity.this, ProfileActivity.class));
            return true;
        } else if (id == R.id.settings) {
            Toast.makeText(this, "Settings is clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("Offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("Online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("Offline");
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users")
                    .document(currentUser.getUid());

            documentReference.update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        // Only show toast for status changes during development
                        if (BuildConfig.DEBUG) {
                            // Commented out to reduce toast spam
                            // Toast.makeText(this, "User is now " + status, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(this, "Status update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Instead of calling super.onBackPressed() which can cause crashes,
        // move the app to background
        super.onBackPressed();
        moveTaskToBack(true);
    }
}