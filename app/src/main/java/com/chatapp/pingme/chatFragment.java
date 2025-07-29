package com.chatapp.pingme;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class chatFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;

    private FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> chatAdapter;
    private RecyclerView mrecyclerview;
    private ImageView mimageviewofuser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chatfragment, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mrecyclerview = v.findViewById(R.id.recyclerview);

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid", firebaseAuth.getUid());
        FirestoreRecyclerOptions<firebasemodel> allusername = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query, firebasemodel.class).build();

        chatAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusername) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {
                noteViewHolder.particularusername.setText(firebasemodel.getName());
                String uri = firebasemodel.getImage();
                Picasso.get().load(uri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(noteViewHolder.imageviewofuser);

                if (firebasemodel.getStatus().equals("Online")) {
                    noteViewHolder.statusofuser.setText(firebasemodel.getStatus());
                    noteViewHolder.statusofuser.setTextColor(Color.GREEN);
                } else {
                    noteViewHolder.statusofuser.setText(firebasemodel.getStatus());
                }

                noteViewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), specificchat.class);
                    intent.putExtra("name", firebasemodel.getName());
                    intent.putExtra("receiveruid", firebasemodel.getUid());
                    intent.putExtra("imageuri", firebasemodel.getImage());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        mrecyclerview.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mrecyclerview.setLayoutManager(linearLayoutManager);
        mrecyclerview.setAdapter(chatAdapter);

        return v;
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
}