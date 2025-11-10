package com.hofang.bookchainfe.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hofang.bookchainfe.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<MessageItem> messageList;
    private String currentUserId;

    public MessageAdapter(ArrayList<MessageItem> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageItem message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateMessages(ArrayList<MessageItem> newList) {
        this.messageList = newList;
        notifyDataSetChanged();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardMessageSent;
        private MaterialCardView cardMessageReceived;
        private TextView tvMessageSent;
        private TextView tvMessageReceived;
        private TextView tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMessageSent = itemView.findViewById(R.id.card_message_sent);
            cardMessageReceived = itemView.findViewById(R.id.card_message_received);
            tvMessageSent = itemView.findViewById(R.id.tv_message_sent);
            tvMessageReceived = itemView.findViewById(R.id.tv_message_received);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }

        public void bind(MessageItem message) {
            if (message.isSent()) {
                // Show sent message
                cardMessageSent.setVisibility(View.VISIBLE);
                cardMessageReceived.setVisibility(View.GONE);
                tvMessageSent.setText(message.getContent());
                tvTimestamp.setText(message.getTimestamp());
                tvTimestamp.setVisibility(View.VISIBLE);
            } else {
                // Show received message
                cardMessageSent.setVisibility(View.GONE);
                cardMessageReceived.setVisibility(View.VISIBLE);
                tvMessageReceived.setText(message.getContent());
                tvTimestamp.setText(message.getTimestamp());
                tvTimestamp.setVisibility(View.VISIBLE);
            }
        }
    }
}

