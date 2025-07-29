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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class specificchat extends AppCompatActivity {

    private static final String TAG = "SpecificChat";

    EditText mgetmessage;
    ImageButton msendmessagebutton;
    CardView msendmessagecardview;
    androidx.appcompat.widget.Toolbar mtoolbarofspecificchat;
    ImageView mimageviewofspecificuser;
    TextView mnameofspecificuser;

    private String enteredmessage;
    Intent intent;
    String mrecievername, mrecieveruid, msenderuid;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String senderroom, recieverroom;

    ImageButton mbackbuttonofspecificchat;
    RecyclerView mmessagerecyclerview;

    String currenttime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    private DatabaseReference messagesRef;
    private ChildEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificchat);

        // Initialize views
        initializeViews();

        // Setup Firebase
        setupFirebase();

        // Setup message list
        setupMessageList();

        // Setup UI components
        setupUI();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        mgetmessage = findViewById(R.id.getmessage);
        msendmessagecardview = findViewById(R.id.carviewofsendmessage);
        msendmessagebutton = findViewById(R.id.imageviewsendmessage);
        mtoolbarofspecificchat = findViewById(R.id.toolbarofspecificchat);
        mnameofspecificuser = findViewById(R.id.Nameofspecificuser);
        mimageviewofspecificuser = findViewById(R.id.specificuserimageinimageview);
        mbackbuttonofspecificchat = findViewById(R.id.backbuttonofspecificchat);
        mmessagerecyclerview = findViewById(R.id.recyclerviewofspecific);
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        msenderuid = currentUser.getUid();
        intent = getIntent();
        mrecieveruid = intent.getStringExtra("receiveruid");
        mrecievername = intent.getStringExtra("name");

        if (mrecieveruid == null || mrecieveruid.isEmpty()) {
            Toast.makeText(this, "Recipient information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        senderroom = msenderuid + mrecieveruid;
        recieverroom = mrecieveruid + msenderuid;

        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");
    }

    private void setupMessageList() {
        messagesArrayList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(specificchat.this, messagesArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mmessagerecyclerview.setLayoutManager(linearLayoutManager);
        mmessagerecyclerview.setAdapter(messagesAdapter);
    }

    private void setupUI() {
        // Set profile info
        mnameofspecificuser.setText(mrecievername != null ? mrecievername : "Unknown");
        String uri = intent.getStringExtra("imageuri");

        if (uri != null && !uri.isEmpty()) {
            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(mimageviewofspecificuser);
        } else {
            mimageviewofspecificuser.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupListeners() {
        // Setup messages listener
        setupMessagesListener();

        // Send message button
        msendmessagebutton.setOnClickListener(view -> sendMessage());

        // Back button
        mbackbuttonofspecificchat.setOnClickListener(view -> {
            onBackPressed();
        });

        // Toolbar click
        mtoolbarofspecificchat.setOnClickListener(view -> {
            // Handle toolbar click if needed
        });
    }

    private void setupMessagesListener() {
        messagesRef = firebaseDatabase.getReference()
                .child("chats")
                .child(senderroom)
                .child("messages");

        messagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);
                if (message != null) {
                    messagesArrayList.add(message);
                    messagesAdapter.notifyItemInserted(messagesArrayList.size() - 1);
                    mmessagerecyclerview.smoothScrollToPosition(messagesArrayList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Messages load error: " + error.getMessage());
                Toast.makeText(specificchat.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        };

        messagesRef.addChildEventListener(messagesListener);
    }

    private void sendMessage() {
        enteredmessage = mgetmessage.getText().toString().trim();
        if (enteredmessage.isEmpty()) {
            Toast.makeText(this, "Enter message first", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = new Date();
        currenttime = simpleDateFormat.format(calendar.getTime());

        // Create message object
        Messages message = new Messages(
                enteredmessage,
                firebaseAuth.getUid(),
                date.getTime(),
                currenttime
        );

        // Generate a unique key for the message
        String messageKey = messagesRef.push().getKey();
        if (messageKey == null) {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create message map
        Map<String, Object> updates = getStringObjectMap(message, messageKey);

        // Update both rooms atomically
        firebaseDatabase.getReference().updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mgetmessage.setText("");
                    } else {
                        Toast.makeText(specificchat.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Send message error: " + task.getException());
                    }
                });
    }

    @NonNull
    private Map<String, Object> getStringObjectMap(Messages message, String messageKey) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message.getMessage());
        messageMap.put("senderId", message.getSenderId());
        messageMap.put("timestamp", message.getTimestamp());
        messageMap.put("currenttime", message.getCurrenttime());

        // Create update paths
        Map<String, Object> updates = new HashMap<>();
        updates.put("chats/" + senderroom + "/messages/" + messageKey, messageMap);
        updates.put("chats/" + recieverroom + "/messages/" + messageKey, messageMap);
        return updates;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("Offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
        updateUserStatus("Offline");
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("status");

            userRef.setValue(status)
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Status update failed: " + e.getMessage())
                    );
        }
    }

    @Override
    public void onBackPressed() {
        // Clean up listener before going back
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
        finish();
    }
}