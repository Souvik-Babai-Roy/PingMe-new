package com.chatapp.pingme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final WeakReference<Context> contextRef;
    private final ArrayList<Messages> messagesArrayList;

    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.contextRef = new WeakReference<>(context);
        this.messagesArrayList = messagesArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ITEM_SEND) {
            View view = inflater.inflate(R.layout.senderchatlayout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.recieverchatlayout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messagesArrayList.get(position);

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder senderHolder = (SenderViewHolder) holder;
            senderHolder.senderMessage.setText(message.getMessage());
            senderHolder.senderTime.setText(message.getCurrenttime());
        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;
            receiverHolder.recieverMessage.setText(message.getMessage());
            receiverHolder.recieverTime.setText(message.getCurrenttime());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messagesArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    public void clearData() {
        messagesArrayList.clear();
        notifyDataSetChanged();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessage, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.senderMessage);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView recieverMessage, recieverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverMessage = itemView.findViewById(R.id.recieverMessage);
            recieverTime = itemView.findViewById(R.id.recieverTime);
        }
    }
}
