package com.chatapp.pingme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    EditText mviewusername;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    TextView mmovetoupdateprofile;
    FirebaseFirestore firebaseFirestore;
    ImageView mviewuserimageinimageview;
    StorageReference storageReference;
    private String ImageURIacessToken;
    androidx.appcompat.widget.Toolbar mtoolbarofviewprofile;
    ImageButton mbackbuttonofviewprofile;
    FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeFirebase();
        initializeViews();
        setupToolbar();
        loadUserProfile();
        setupClickListeners();
    }

    private void initializeFirebase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    private void initializeViews() {
        mviewuserimageinimageview = findViewById(R.id.viewuserimageinimageview);
        mviewusername = findViewById(R.id.viewusername);
        mmovetoupdateprofile = findViewById(R.id.movetoupdateprofile);
        mtoolbarofviewprofile = findViewById(R.id.toolbarofviewprofile);
        mbackbuttonofviewprofile = findViewById(R.id.backbuttonofviewprofile);
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbarofviewprofile);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Load profile image
        loadProfileImage();

        // Load username from Realtime Database
        loadUsername();
    }

    private void loadProfileImage() {
        storageReference.child("Images")
                .child(firebaseAuth.getUid())
                .child("Profile Pic")
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    ImageURIacessToken = uri.toString();
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(mviewuserimageinimageview);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load profile image", e);
                    mviewuserimageinimageview.setImageResource(R.drawable.ic_profile);
                });
    }

    private void loadUsername() {
        DatabaseReference databaseReference = firebaseDatabase.getReference("Users")
                .child(firebaseAuth.getUid());
                
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userprofile muserprofile = snapshot.getValue(userprofile.class);
                if (muserprofile != null) {
                    mviewusername.setText(muserprofile.getUsername());
                } else {
                    // Try to get name from Firestore if not in Realtime Database
                    loadUsernameFromFirestore();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch username from Realtime Database", error.toException());
                loadUsernameFromFirestore();
            }
        });
    }

    private void loadUsernameFromFirestore() {
        firebaseFirestore.collection("Users")
                .document(firebaseAuth.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            mviewusername.setText(name);
                        } else {
                            mviewusername.setText("User");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch username from Firestore", e);
                    Toast.makeText(getApplicationContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        mbackbuttonofviewprofile.setOnClickListener(view -> finish());

        mmovetoupdateprofile.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, UpdateProfile.class);
            intent.putExtra("nameofuser", mviewusername.getText().toString());
            startActivity(intent);
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("Offline");
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("Online");
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users")
                    .document(currentUser.getUid());
            documentReference.update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User status updated to: " + status);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Status update failed", e);
                    });
        }
    }
}