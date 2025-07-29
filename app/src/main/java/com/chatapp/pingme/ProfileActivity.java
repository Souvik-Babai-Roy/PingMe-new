package com.chatapp.pingme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

        initializeViews();
        initializeFirebase();
        setupToolbar();
        loadUserProfile();
        setupListeners();
    }

    private void initializeViews() {
        mviewuserimageinimageview = findViewById(R.id.viewuserimageinimageview);
        mviewusername = findViewById(R.id.viewusername);
        mmovetoupdateprofile = findViewById(R.id.movetoupdateprofile);
        mtoolbarofviewprofile = findViewById(R.id.toolbarofviewprofile);
        mbackbuttonofviewprofile = findViewById(R.id.backbuttonofviewprofile);
    }

    private void initializeFirebase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbarofviewprofile);
    }

    private void loadUserProfile() {
        String currentUserId = firebaseAuth.getUid();
        if (currentUserId == null) return;

        // Load profile image
        storageReference = firebaseStorage.getReference();
        storageReference.child("Images").child(currentUserId).child("Profile Pic").getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    ImageURIacessToken = uri.toString();
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(mviewuserimageinimageview);
                })
                .addOnFailureListener(e -> {
                    mviewuserimageinimageview.setImageResource(R.drawable.ic_profile);
                    // Don't show error toast as it's normal for users without profile pics
                });

        // Load username from Realtime Database
        DatabaseReference databaseReference = firebaseDatabase.getReference(currentUserId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userprofile muserprofile = snapshot.getValue(userprofile.class);
                    if (muserprofile != null) {
                        mviewusername.setText(muserprofile.getUsername());
                    }
                } else {
                    // If no data in Realtime Database, try Firestore
                    loadUsernameFromFirestore();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to fetch profile", Toast.LENGTH_SHORT).show();
                // Try loading from Firestore as fallback
                loadUsernameFromFirestore();
            }
        });
    }

    private void loadUsernameFromFirestore() {
        String currentUserId = firebaseAuth.getUid();
        if (currentUserId == null) return;

        firebaseFirestore.collection("Users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) {
                            mviewusername.setText(name);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        mbackbuttonofviewprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mmovetoupdateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, UpdateProfile.class);
                intent.putExtra("nameofuser", mviewusername.getText().toString());
                startActivity(intent);
            }
        });
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
        String currentUserId = firebaseAuth.getUid();
        if (currentUserId != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserId);
            documentReference.update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        // Status updated successfully - no need to show toast
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure silently or log it
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Simply finish the activity instead of calling super to avoid crashes
        super.onBackPressed();
        finish();
    }
}