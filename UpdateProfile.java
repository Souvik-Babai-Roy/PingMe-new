package com.chatapp.pingme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateProfile extends AppCompatActivity {

    private static final String TAG = "UpdateProfile";
    private static final int PICK_IMAGE = 123;

    private EditText mnewusername;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private ImageView mgetnewimageinimageview;
    private StorageReference storageReference;
    private String ImageURIacessToken;
    private androidx.appcompat.widget.Toolbar mtoolbarofupdateprofile;
    private ImageButton mbackbuttonofupdateprofile;
    private FirebaseStorage firebaseStorage;
    private ProgressBar mprogressbarofupdateprofile;
    private Uri imagepath;
    private Intent intent;
    private android.widget.Button mupdateprofilebutton;
    private String newname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        initializeFirebase();
        initializeViews();
        setupToolbar();
        loadCurrentProfileData();
        setupClickListeners();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    private void initializeViews() {
        mtoolbarofupdateprofile = findViewById(R.id.toolbarofupdateprofile);
        mbackbuttonofupdateprofile = findViewById(R.id.backbuttonofupdateprofile);
        mgetnewimageinimageview = findViewById(R.id.getnewuserimageinimageview);
        mprogressbarofupdateprofile = findViewById(R.id.progressbarofupdateprofile);
        mnewusername = findViewById(R.id.getnewusername);
        mupdateprofilebutton = findViewById(R.id.updateprofilebutton);
        intent = getIntent();
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbarofupdateprofile);
    }

    private void loadCurrentProfileData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Set current username from intent
        String currentName = intent.getStringExtra("nameofuser");
        if (currentName != null) {
            mnewusername.setText(currentName);
        }

        // Load current profile image
        loadCurrentProfileImage();
    }

    private void loadCurrentProfileImage() {
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
                            .into(mgetnewimageinimageview);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load current profile image", e);
                    mgetnewimageinimageview.setImageResource(R.drawable.ic_profile);
                });
    }

    private void setupClickListeners() {
        mbackbuttonofupdateprofile.setOnClickListener(view -> finish());

        mgetnewimageinimageview.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        mupdateprofilebutton.setOnClickListener(view -> {
            newname = mnewusername.getText().toString().trim();
            if (validateInput()) {
                mprogressbarofupdateprofile.setVisibility(View.VISIBLE);
                mupdateprofilebutton.setEnabled(false);
                updateProfile();
            }
        });
    }

    private boolean validateInput() {
        if (newname.isEmpty()) {
            mnewusername.setError("Name is required");
            Toast.makeText(getApplicationContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateProfile() {
        if (imagepath != null) {
            // Update with new image
            updateImageToStorage();
        } else {
            // Update only name, keep existing image
            updateAllUserData();
        }
    }

    private void updateImageToStorage() {
        StorageReference imageref = storageReference.child("Images")
                .child(firebaseAuth.getUid())
                .child("Profile Pic");

        try {
            // Image compression
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            // Upload image to storage
            UploadTask uploadTask = imageref.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image uploaded successfully");
                imageref.getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageURIacessToken = uri.toString();
                    Log.d(TAG, "Download URL obtained: " + ImageURIacessToken);
                    updateAllUserData();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    Toast.makeText(getApplicationContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    resetUIState();
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                resetUIState();
            });

        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(getApplicationContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            resetUIState();
        }
    }

    private void updateAllUserData() {
        // Use AtomicInteger to track completion of both updates
        AtomicInteger completedUpdates = new AtomicInteger(0);
        AtomicInteger totalUpdates = new AtomicInteger(2); // Firestore + Realtime Database

        // Update Realtime Database
        updateRealtimeDatabase(() -> {
            if (completedUpdates.incrementAndGet() == totalUpdates.get()) {
                onAllUpdatesComplete();
            }
        }, this::resetUIState);

        // Update Firestore
        updateFirestore(() -> {
            if (completedUpdates.incrementAndGet() == totalUpdates.get()) {
                onAllUpdatesComplete();
            }
        }, this::resetUIState);
    }

    private void updateRealtimeDatabase(Runnable onSuccess, Runnable onFailure) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("Users")
                .child(firebaseAuth.getUid());

        userprofile muserprofile = new userprofile(newname, firebaseAuth.getUid());
        
        databaseReference.setValue(muserprofile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile updated in Realtime Database");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update Realtime Database", e);
                    onFailure.run();
                });
    }

    private void updateFirestore(Runnable onSuccess, Runnable onFailure) {
        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(firebaseAuth.getUid());
        
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("name", newname);
        userdata.put("image", ImageURIacessToken != null ? ImageURIacessToken : "");
        userdata.put("uid", firebaseAuth.getUid());
        userdata.put("status", "Online");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userdata.put("email", currentUser.getEmail());
        }

        documentReference.set(userdata)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile updated in Firestore");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update Firestore", e);
                    onFailure.run();
                });
    }

    private void onAllUpdatesComplete() {
        Log.d(TAG, "All profile updates completed successfully");
        Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        
        mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
        
        Intent intent = new Intent(UpdateProfile.this, chatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void resetUIState() {
        mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
        mupdateprofilebutton.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imagepath = data.getData();
            if (imagepath != null) {
                mgetnewimageinimageview.setImageURI(imagepath);
            }
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mprogressbarofupdateprofile.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait while updating profile...", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
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
                    .addOnFailureListener(e -> Log.e(TAG, "Status update failed", e));
        }
    }
}