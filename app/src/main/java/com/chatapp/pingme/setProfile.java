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

public class setProfile extends AppCompatActivity {

    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private static int PICK_IMAGE = 123;
    private Uri imagepath;

    private EditText mgetusername;
    private android.widget.Button msaveprofile;

    private FirebaseAuth firebaseAuth;
    private String name;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String ImageUriAcessToken;
    private FirebaseFirestore firebaseFirestore;

    ProgressBar mprogressbarofsetprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        initializeFirebase();
        initializeViews();
        setupListeners();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        mgetusername = findViewById(R.id.getusername);
        mgetuserimage = findViewById(R.id.getuserimage);
        mgetuserimageinimageview = findViewById(R.id.getuserimageinimageview);
        msaveprofile = findViewById(R.id.saveProfile);
        mprogressbarofsetprofile = findViewById(R.id.progressbarofsetProfile);
    }

    private void setupListeners() {
        mgetuserimage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        msaveprofile.setOnClickListener(view -> {
            name = mgetusername.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imagepath == null) {
                Toast.makeText(getApplicationContext(), "Image is required", Toast.LENGTH_SHORT).show();
                return;
            }

            mprogressbarofsetprofile.setVisibility(View.VISIBLE);
            sendImagetoStorage();
        });
    }

    private void sendImagetoStorage() {
        // Create a proper file extension
        StorageReference imageref = storageReference.child("Images/" + firebaseAuth.getUid() + "/profile_pic.jpg");


        try {
            // Image compression
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            // Upload image to Firebase Storage
            UploadTask uploadTask = imageref.putBytes(data);

            uploadTask.addOnFailureListener(e -> {
                Log.e("UploadError", "Upload failed: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
            });

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageref.getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageUriAcessToken = uri.toString();
                    Log.d("UploadSuccess", "Image URL: " + ImageUriAcessToken);
                    sendDataToRealTimeDatabase();
                }).addOnFailureListener(e -> {
                    Log.e("URLError", "Failed to get URL: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                });
            });

        } catch (IOException e) {
            Log.e("ImageError", "Image processing failed: " + e.getMessage());
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
        }
    }

    private void sendDataToRealTimeDatabase() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(firebaseAuth.getUid());

        userprofile muserprofile = new userprofile(name, firebaseAuth.getUid());
        databaseReference.setValue(muserprofile)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RealtimeDB", "Profile saved to Realtime Database");
                    sendDataToCloudFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e("RealtimeDBError", "Failed to save profile: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                });
    }

    private void sendDataToCloudFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("name", name);
        userdata.put("image", ImageUriAcessToken);
        userdata.put("uid", firebaseAuth.getUid());
        userdata.put("status", "Online");

        documentReference.set(userdata)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Profile saved to Firestore");
                    Toast.makeText(getApplicationContext(), "Profile setup completed successfully", Toast.LENGTH_SHORT).show();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);

                    // Navigate to chat activity
                    Intent intent = new Intent(setProfile.this, chatActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Firestore save failed: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Failed to save to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                });
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
        // Handle back button press - sign out the user and go back to MainActivity
        super.onBackPressed();
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}