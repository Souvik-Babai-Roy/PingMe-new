package com.chatapp.pingme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

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

    ProgressBar mprogressbarofupdateprofile;

    private Uri imagepath;
    Intent intent;

    private static int PICK_IMAGE = 123;

    android.widget.Button mupdateprofilebutton;
    String newname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        initializeViews();
        initializeFirebase();
        setupToolbar();
        loadCurrentData();
        setupListeners();
    }

    private void initializeViews() {
        mtoolbarofupdateprofile = findViewById(R.id.toolbarofupdateprofile);
        mbackbuttonofupdateprofile = findViewById(R.id.backbuttonofupdateprofile);
        mgetnewimageinimageview = findViewById(R.id.getnewuserimageinimageview);
        mprogressbarofupdateprofile = findViewById(R.id.progressbarofupdateprofile);
        mnewusername = findViewById(R.id.getnewusername);
        mupdateprofilebutton = findViewById(R.id.updateprofilebutton);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbarofupdateprofile);
    }

    private void loadCurrentData() {
        intent = getIntent();
        if (intent != null && intent.hasExtra("nameofuser")) {
            mnewusername.setText(intent.getStringExtra("nameofuser"));
        }

        // Load current profile image
        storageReference = firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic")
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
                    mgetnewimageinimageview.setImageResource(R.drawable.ic_profile);
                });
    }

    private void setupListeners() {
        mbackbuttonofupdateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mgetnewimageinimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        mupdateprofilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newname = mnewusername.getText().toString().trim();
                if (newname.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                mprogressbarofupdateprofile.setVisibility(View.VISIBLE);

                if (imagepath != null) {
                    // User selected a new image, upload it first
                    updateImageToStorage();
                } else {
                    // No new image selected, just update the name
                    updateProfile();
                }
            }
        });
    }

    private void updateImageToStorage() {
        if (imagepath == null) {
            updateProfile();
            return;
        }

        StorageReference imageref = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        try {
            // Image compression
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            // Upload image to Firebase Storage
            UploadTask uploadTask = imageref.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageref.getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageURIacessToken = uri.toString();
                    Toast.makeText(getApplicationContext(), "Image updated successfully", Toast.LENGTH_SHORT).show();
                    updateProfile();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
        }
    }

    private void updateProfile() {
        String currentUserId = firebaseAuth.getUid();
        if (currentUserId == null) {
            mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
            return;
        }

        // Update Realtime Database
        DatabaseReference databaseReference = firebaseDatabase.getReference(currentUserId);
        userprofile muserprofile = new userprofile(newname, currentUserId);

        databaseReference.setValue(muserprofile)
                .addOnSuccessListener(aVoid -> {
                    // Update Firestore
                    updateFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to update profile in database", Toast.LENGTH_SHORT).show();
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                });
    }

    private void updateFirestore() {
        String currentUserId = firebaseAuth.getUid();
        if (currentUserId == null) {
            mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
            return;
        }

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUserId);
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("name", newname);
        userdata.put("image", ImageURIacessToken != null ? ImageURIacessToken : "");
        userdata.put("uid", currentUserId);
        userdata.put("status", "Online");

        documentReference.set(userdata)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);

                    // Navigate back to chat activity
                    Intent intent = new Intent(UpdateProfile.this, chatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                });
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
                        // Status updated successfully
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure silently
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Simply finish the activity
        super.onBackPressed();
        finish();
    }
}