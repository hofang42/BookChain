package com.hofang.bookchainfe.ui.chatbot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    
    private List<ChatMessage> messages = new ArrayList<>();
    private OnBookClickListener bookClickListener;
    
    public interface OnBookClickListener {
        void onBookClick(String bookId, String bookTitle);
    }
    
    public void setOnBookClickListener(OnBookClickListener listener) {
        this.bookClickListener = listener;
    }
    
    public void addMessage(ChatMessage message) {
        messages.add(message);
        android.util.Log.d("ChatAdapter", "Adding message. Total: " + messages.size() + ", Type: " + message.getType() + ", Content: " + message.getContent());
        notifyItemInserted(messages.size() - 1);
        // Also try notifyDataSetChanged as backup
        notifyDataSetChanged();
    }
    
    public void updateLastMessage(String content) {
        if (!messages.isEmpty()) {
            messages.get(messages.size() - 1).setContent(content);
            notifyItemChanged(messages.size() - 1);
        }
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("ChatAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view, this);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        android.util.Log.d("ChatAdapter", "onBindViewHolder called for position: " + position);
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        
        private LinearLayout layoutUserMessage;
        private LinearLayout layoutAiMessage;
        private TextView tvUserMessage;
        private TextView tvAiMessage;
        private ImageView ivUserImage;
        private ChatAdapter adapter;
        
        public MessageViewHolder(@NonNull View itemView, ChatAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            layoutUserMessage = itemView.findViewById(R.id.layout_user_message);
            layoutAiMessage = itemView.findViewById(R.id.layout_ai_message);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
            ivUserImage = itemView.findViewById(R.id.iv_user_image);
        }
        
        public void bind(ChatMessage message) {
            android.util.Log.d("ChatAdapter", "Binding message: " + message.getType() + " - " + message.getContent());
            if (message.isUser()) {
                // User message
                layoutUserMessage.setVisibility(View.VISIBLE);
                layoutAiMessage.setVisibility(View.GONE);
                tvUserMessage.setText(message.getContent());
                
                // Show image if available
                if (message.hasImage()) {
                    ivUserImage.setVisibility(View.VISIBLE);
                    try {
                        byte[] decodedString = Base64.decode(message.getImageBase64(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivUserImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        ivUserImage.setVisibility(View.GONE);
                    }
                } else {
                    ivUserImage.setVisibility(View.GONE);
                }
            } else {
                // AI message
                layoutUserMessage.setVisibility(View.GONE);
                layoutAiMessage.setVisibility(View.VISIBLE);
                
                // Parse markdown and make book links clickable
                SpannableString spannableString = formatMessage(message.getContent());
                tvAiMessage.setText(spannableString);
                tvAiMessage.setMovementMethod(LinkMovementMethod.getInstance());
                
                // Để TextView có thể clickable trong RecyclerView
                tvAiMessage.setClickable(false);
                tvAiMessage.setLongClickable(false);
            }
        }
        
        private SpannableString formatMessage(String text) {
            // 1. Replace book links format: * [BOOK:id] Title - Author -> Title (clickable)
            Pattern bookPattern = Pattern.compile("\\*?\\s*\\[BOOK:([a-fA-F0-9]{24})\\]\\s*([^\\n\\[]+)");
            Matcher bookMatcher = bookPattern.matcher(text);
            
            // Store book info for later
            java.util.List<BookLinkInfo> bookLinks = new java.util.ArrayList<>();
            String processedText = text;
            int offset = 0; // Track position shift after replacements
            
            while (bookMatcher.find()) {
                String bookId = bookMatcher.group(1);
                String bookInfo = bookMatcher.group(2).trim();
                
                // Extract title only (before " - ")
                String[] parts = bookInfo.split(" - ");
                String bookTitle = parts.length > 0 ? parts[0].trim() : bookInfo;
                
                String replacement = "📚 " + bookTitle;
                String originalMatch = bookMatcher.group();
                
                // Calculate new position after all previous replacements
                int originalStart = bookMatcher.start();
                int originalEnd = bookMatcher.end();
                int newStart = originalStart + offset;
                int newEnd = newStart + replacement.length();
                
                // Save position info with adjusted positions
                bookLinks.add(new BookLinkInfo(bookId, bookTitle, newStart, newEnd));
                
                // Replace in text
                processedText = processedText.replaceFirst(
                    Pattern.quote(originalMatch),
                    replacement
                );
                
                // Update offset for next iteration
                offset += replacement.length() - originalMatch.length();
            }
            
            SpannableString spannableString = new SpannableString(processedText);
            
            android.util.Log.d("ChatAdapter", "Original: " + text);
            android.util.Log.d("ChatAdapter", "Processed: " + processedText);
            
            // 2. Format bold text (**text**)
            Pattern boldPattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
            Matcher boldMatcher = boldPattern.matcher(processedText);
            
            while (boldMatcher.find()) {
                int start = boldMatcher.start();
                int end = boldMatcher.end();
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // 3. Make book links clickable
            for (BookLinkInfo linkInfo : bookLinks) {
                android.util.Log.d("ChatAdapter", "Adding clickable span: " + linkInfo.bookTitle + " [" + linkInfo.bookId + "]");
                
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (adapter.bookClickListener != null) {
                            android.util.Log.d("ChatAdapter", "Book clicked: " + linkInfo.bookId + " - " + linkInfo.bookTitle);
                            adapter.bookClickListener.onBookClick(linkInfo.bookId, linkInfo.bookTitle);
                        }
                    }
                    
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(Color.parseColor("#4CAF50"));
                        ds.setUnderlineText(true);
                        ds.setFakeBoldText(true);
                    }
                };
                
                spannableString.setSpan(clickableSpan, linkInfo.startPos, linkInfo.endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            return spannableString;
        }
        
        // Helper class to store book link info
        private static class BookLinkInfo {
            String bookId;
            String bookTitle;
            int startPos;
            int endPos;
            
            BookLinkInfo(String bookId, String bookTitle, int startPos, int endPos) {
                this.bookId = bookId;
                this.bookTitle = bookTitle;
                this.startPos = startPos;
                this.endPos = endPos;
            }
        }
    }
}
