package com.chatapp.pingme;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class chatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    
    private FirebaseFirestore firebaseFirestore;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;

    private FirestoreRecyclerAdapter<User, UserViewHolder> chatAdapter;
    private RecyclerView mrecyclerview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FloatingActionButton fabAddUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeFirebase();
        initializeViews(v);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFloatingActionButton();
        loadUsers();

        return v;
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        
        Log.d(TAG, "Firebase initialized");
        
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Current user: " + currentUser.getUid());
        } else {
            Log.e(TAG, "No current user found!");
        }
    }

    private void initializeViews(View v) {
        mrecyclerview = v.findViewById(R.id.recyclerview);
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        progressBar = v.findViewById(R.id.progress_bar);
        emptyStateText = v.findViewById(R.id.empty_state_text);
        fabAddUser = v.findViewById(R.id.fab_add_user);
    }

    private void setupRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mrecyclerview.setLayoutManager(linearLayoutManager);
        mrecyclerview.setHasFixedSize(true);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            );
            
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Swipe refresh triggered");
                loadUsers();
            });
        }
    }

    private void setupFloatingActionButton() {
        if (fabAddUser != null) {
            fabAddUser.setOnClickListener(v -> {
                // TODO: Implement add user or search users functionality
                Toast.makeText(getContext(), "Search users feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUsers() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            showError("User not authenticated");
            return;
        }

        Log.d(TAG, "Loading users for current user: " + currentUser.getUid());
        showLoading(true);

        // First, let's check if there are any users in the database
        firebaseFirestore.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            Log.d(TAG, "Total users in database: " + querySnapshot.size());
                            
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Log.d(TAG, "User found: " + doc.getId() + " - " + doc.getData());
                            }
                            
                            if (querySnapshot.isEmpty()) {
                                Log.w(TAG, "No users found in database");
                                showEmptyState("No users found. Register more users to start chatting!");
                                showLoading(false);
                                return;
                            }
                        }
                        
                        // Now setup the adapter with proper query
                        setupFirestoreAdapter(currentUser.getUid());
                        
                    } else {
                        Log.e(TAG, "Error getting users: ", task.getException());
                        showError("Failed to load users: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        showLoading(false);
                    }
                });
    }

    private void setupFirestoreAdapter(String currentUserId) {
        Query query = firebaseFirestore.collection("Users")
                .whereNotEqualTo("uid", currentUserId)
                .orderBy("uid")  // Add orderBy to make the query work with whereNotEqualTo
                .orderBy("name"); // Secondary sort by name
                
        Log.d(TAG, "Setting up Firestore adapter with query for user: " + currentUserId);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        chatAdapter = new FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User user) {
                try {
                    Log.d(TAG, "Binding user: " + user.getName() + " (UID: " + user.getUid() + ")");
                    
                    holder.userName.setText(user.getName() != null ? user.getName() : "Unknown User");
                    
                    // Set last message (placeholder for now)
                    holder.lastMessage.setText("Tap to start chatting");
                    
                    // Set timestamp (placeholder)
                    holder.timestamp.setText("");
                    
                    // Load profile image
                    String imageUrl = user.getImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(holder.profileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "Profile image loaded successfully for " + user.getName());
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "Failed to load profile image for " + user.getName(), e);
                                    }
                                });
                    } else {
                        holder.profileImage.setImageResource(R.drawable.ic_profile);
                    }

                    // Set online status
                    String status = user.getStatus();
                    if (status != null && status.equals("Online")) {
                        holder.onlineIndicator.setVisibility(View.VISIBLE);
                        holder.statusText.setText("Online");
                        holder.statusText.setTextColor(Color.GREEN);
                    } else {
                        holder.onlineIndicator.setVisibility(View.GONE);
                        holder.statusText.setText("Offline");
                        holder.statusText.setTextColor(Color.GRAY);
                    }

                    // Set click listener
                    holder.itemView.setOnClickListener(view -> {
                        if (getActivity() != null) {
                            Log.d(TAG, "Opening chat with user: " + user.getName());
                            Intent intent = new Intent(getActivity(), specificchat.class);
                            intent.putExtra("name", user.getName() != null ? user.getName() : "Unknown User");
                            intent.putExtra("receiveruid", user.getUid());
                            intent.putExtra("imageuri", user.getImage() != null ? user.getImage() : "");
                            startActivity(intent);
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error binding view holder", e);
                }
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                Log.e(TAG, "Firestore adapter error", e);
                if (getActivity() != null) {
                    showError("Error loading users: " + e.getMessage());
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                Log.d(TAG, "Data changed, item count: " + getItemCount());
                showLoading(false);
                
                if (getItemCount() == 0) {
                    showEmptyState("No other users found. Invite friends to join!");
                } else {
                    showEmptyState(null);
                }
                
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };

        mrecyclerview.setAdapter(chatAdapter);
        Log.d(TAG, "Firestore adapter set on RecyclerView");
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(String message) {
        if (emptyStateText != null) {
            if (message != null) {
                emptyStateText.setText(message);
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                emptyStateText.setVisibility(View.GONE);
            }
        }
    }

    private void showError(String error) {
        Log.e(TAG, error);
        if (getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
        showEmptyState("Error loading chats. Pull down to refresh.");
    }

    // ViewHolder class for RecyclerView
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage, timestamp, statusText;
        ImageView profileImage, onlineIndicator;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.timestamp);
            statusText = itemView.findViewById(R.id.status_text);
            profileImage = itemView.findViewById(R.id.profile_image);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Fragment started");
        if (chatAdapter != null) {
            chatAdapter.startListening();
            Log.d(TAG, "Started listening to Firestore changes");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Fragment stopped");
        if (chatAdapter != null) {
            chatAdapter.stopListening();
            Log.d(TAG, "Stopped listening to Firestore changes");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
        if (chatAdapter != null) {
            chatAdapter.startListening();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Fragment paused");
        if (chatAdapter != null) {
            chatAdapter.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destroyed");
        if (chatAdapter != null) {
            chatAdapter.stopListening();
        }
    }
}