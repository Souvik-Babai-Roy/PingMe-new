package com.chatapp.pingme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class setProfile extends AppCompatActivity {

    private static final String TAG = "SetProfile";
    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private static final int PICK_IMAGE = 123;
    private Uri imagepath;

    private EditText mgetusername;
    private android.widget.Button msaveprofile;

    private FirebaseAuth firebaseAuth;
    private String name;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String ImageUriAcessToken;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;

    private ProgressBar mprogressbarofsetprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        initializeFirebase();
        initializeViews();
        setupClickListeners();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void initializeViews() {
        mgetusername = findViewById(R.id.getusername);
        mgetuserimage = findViewById(R.id.getuserimage);
        mgetuserimageinimageview = findViewById(R.id.getuserimageinimageview);
        msaveprofile = findViewById(R.id.saveProfile);
        mprogressbarofsetprofile = findViewById(R.id.progressbarofsetProfile);
    }

    private void setupClickListeners() {
        mgetuserimage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        msaveprofile.setOnClickListener(view -> {
            name = mgetusername.getText().toString().trim();
            if (validateInput()) {
                mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                msaveprofile.setEnabled(false);
                
                if (imagepath != null) {
                    sendImagetoStorage();
                } else {
                    // Use default image and save data
                    ImageUriAcessToken = "";
                    saveAllUserData();
                }
            }
        });
    }

    private boolean validateInput() {
        if (name.isEmpty()) {
            mgetusername.setError("Name is required");
            Toast.makeText(getApplicationContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendImagetoStorage() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            resetUIState();
            return;
        }

        StorageReference imageref = storageReference.child("Images")
                .child(firebaseAuth.getUid())
                .child("Profile Pic");

        try {
            // Image compression
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            // Upload image to Firebase Storage
            UploadTask uploadTask = imageref.putBytes(data);

            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                Toast.makeText(getApplicationContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetUIState();
            });

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image uploaded successfully");
                imageref.getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageUriAcessToken = uri.toString();
                    Log.d(TAG, "Download URL obtained: " + ImageUriAcessToken);
                    saveAllUserData();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    Toast.makeText(getApplicationContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    resetUIState();
                });
            });

        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(getApplicationContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            resetUIState();
        }
    }

    private void saveAllUserData() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            resetUIState();
            return;
        }

        // Use AtomicInteger to track completion of both saves
        AtomicInteger completedSaves = new AtomicInteger(0);
        AtomicInteger totalSaves = new AtomicInteger(2); // Firestore + Realtime Database

        // Save to Realtime Database
        saveToRealtimeDatabase(() -> {
            if (completedSaves.incrementAndGet() == totalSaves.get()) {
                onAllSavesComplete();
            }
        }, () -> {
            resetUIState();
        });

        // Save to Firestore
        saveToFirestore(() -> {
            if (completedSaves.incrementAndGet() == totalSaves.get()) {
                onAllSavesComplete();
            }
        }, () -> {
            resetUIState();
        });
    }

    private void saveToRealtimeDatabase(Runnable onSuccess, Runnable onFailure) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("Users")
                .child(firebaseAuth.getUid());

        userprofile muserprofile = new userprofile(name, firebaseAuth.getUid());
        
        databaseReference.setValue(muserprofile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved to Realtime Database");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save to Realtime Database", e);
                    Toast.makeText(getApplicationContext(), "Failed to save user profile", Toast.LENGTH_SHORT).show();
                    onFailure.run();
                });
    }

    private void saveToFirestore(Runnable onSuccess, Runnable onFailure) {
        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(firebaseAuth.getUid());
        
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("name", name);
        userdata.put("image", ImageUriAcessToken != null ? ImageUriAcessToken : "");
        userdata.put("uid", firebaseAuth.getUid());
        userdata.put("status", "Online");
        userdata.put("email", firebaseAuth.getCurrentUser().getEmail() != null ? 
                     firebaseAuth.getCurrentUser().getEmail() : "");

        documentReference.set(userdata)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved to Firestore");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save to Firestore", e);
                    Toast.makeText(getApplicationContext(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                    onFailure.run();
                });
    }

    private void onAllSavesComplete() {
        Log.d(TAG, "All user data saved successfully");
        Toast.makeText(getApplicationContext(), "Profile created successfully", Toast.LENGTH_SHORT).show();
        
        mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
        
        Intent intent = new Intent(setProfile.this, chatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void resetUIState() {
        mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
        msaveprofile.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imagepath = data.getData();
            if (imagepath != null) {
                mgetuserimageinimageview.setImageURI(imagepath);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button press gracefully
        if (mprogressbarofsetprofile.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait while saving profile...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Sign out user and go back to login
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetUIState();
    }
}