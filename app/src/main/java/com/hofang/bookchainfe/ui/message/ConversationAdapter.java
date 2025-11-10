package com.hofang.bookchainfe.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hofang.bookchainfe.R;

import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private ArrayList<Conversation> conversationList;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(ArrayList<Conversation> conversationList, OnConversationClickListener listener) {
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void updateConversations(ArrayList<Conversation> newList) {
        this.conversationList = newList;
        notifyDataSetChanged();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvLastMessage;
        private TextView tvTimestamp;
        private View badgeUnread;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            badgeUnread = itemView.findViewById(R.id.badge_unread);
        }

        public void bind(Conversation conversation) {
            tvName.setText(conversation.getName());
            tvLastMessage.setText(conversation.getLastMessage());
            tvTimestamp.setText(conversation.getTimestamp());

            // Set avatar (for now using placeholder, can be replaced with Glide later)
            if (conversation.getAvatarResId() != 0) {
                ivAvatar.setImageResource(conversation.getAvatarResId());
            }

            // Show/hide unread badge
            badgeUnread.setVisibility(conversation.hasUnread() ? View.VISIBLE : View.GONE);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }
    }
}

