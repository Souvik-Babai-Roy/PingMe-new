package com.chatapp.pingme;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class chatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    
    private FirebaseFirestore firebaseFirestore;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;

    private FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> chatAdapter;
    private RecyclerView mrecyclerview;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chatfragment, container, false);

        initializeFirebase();
        initializeViews(v);
        setupRecyclerView();
        setupSwipeRefresh();

        return v;
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initializeViews(View v) {
        mrecyclerview = v.findViewById(R.id.recyclerview);
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
    }

    private void setupRecyclerView() {
        if (firebaseAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated");
            return;
        }

        Query query = firebaseFirestore.collection("Users")
                .whereNotEqualTo("uid", firebaseAuth.getUid());
                
        FirestoreRecyclerOptions<firebasemodel> allusername = 
                new FirestoreRecyclerOptions.Builder<firebasemodel>()
                        .setQuery(query, firebasemodel.class)
                        .build();

        chatAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusername) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {
                try {
                    noteViewHolder.particularusername.setText(firebasemodel.getName() != null ? firebasemodel.getName() : "Unknown User");
                    
                    String uri = firebasemodel.getImage();
                    if (uri != null && !uri.isEmpty()) {
                        Picasso.get().load(uri)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(noteViewHolder.imageviewofuser);
                    } else {
                        noteViewHolder.imageviewofuser.setImageResource(R.drawable.ic_profile);
                    }

                    String status = firebasemodel.getStatus();
                    if (status != null) {
                        if (status.equals("Online")) {
                            noteViewHolder.statusofuser.setText(status);
                            noteViewHolder.statusofuser.setTextColor(Color.GREEN);
                        } else {
                            noteViewHolder.statusofuser.setText(status);
                            noteViewHolder.statusofuser.setTextColor(Color.GRAY);
                        }
                    } else {
                        noteViewHolder.statusofuser.setText("Offline");
                        noteViewHolder.statusofuser.setTextColor(Color.GRAY);
                    }

                    noteViewHolder.itemView.setOnClickListener(view -> {
                        if (getActivity() != null) {
                            Intent intent = new Intent(getActivity(), specificchat.class);
                            intent.putExtra("name", firebasemodel.getName() != null ? firebasemodel.getName() : "Unknown User");
                            intent.putExtra("receiveruid", firebasemodel.getUid());
                            intent.putExtra("imageuri", firebasemodel.getImage() != null ? firebasemodel.getImage() : "");
                            startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error binding view holder", e);
                }
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout, parent, false);
                return new NoteViewHolder(view);
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                Log.e(TAG, "Firestore adapter error", e);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Error loading users", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mrecyclerview.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mrecyclerview.setLayoutManager(linearLayoutManager);
        mrecyclerview.setAdapter(chatAdapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (chatAdapter != null) {
                    chatAdapter.notifyDataSetChanged();
                }
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    // ViewHolder class for RecyclerView
    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView particularusername;
        private TextView statusofuser;
        private ImageView imageviewofuser;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            particularusername = itemView.findViewById(R.id.nameofuser);
            statusofuser = itemView.findViewById(R.id.statusofuser);
            imageviewofuser = itemView.findViewById(R.id.imageviewofuser);
        }
    }

    // Start listening for Firestore data changes when the fragment starts
    @Override
    public void onStart() {
        super.onStart();
        if (chatAdapter != null) {
            chatAdapter.startListening();
        }
    }

    // Stop listening for Firestore data changes when the fragment stops
    @Override
    public void onStop() {
        super.onStop();
        if (chatAdapter != null) {
            chatAdapter.stopListening();
        }
    }

    // Restart listener when the fragment is resumed
    @Override
    public void onResume() {
        super.onResume();
        if (chatAdapter != null) {
            chatAdapter.startListening();
        }
    }

    // Stop listener when the fragment is paused
    @Override
    public void onPause() {
        super.onPause();
        if (chatAdapter != null) {
            chatAdapter.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatAdapter != null) {
            chatAdapter.stopListening();
        }
    }
}