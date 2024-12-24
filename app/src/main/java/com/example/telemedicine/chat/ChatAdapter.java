package com.example.telemedicine.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import android.graphics.Color;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private ChatFragment chatFragment;

    public ChatAdapter(List<ChatMessage> chatMessages,  ChatFragment chatFragment) {
        this.chatMessages = chatMessages;
        this.chatFragment = chatFragment;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
        return new ChatViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        params.width = 700;

        if (message.getSenderId().equals(currentUserId)) {
            params.gravity = Gravity.END;
            params.setMargins(10, 0, 0, 0);
            holder.messageContainer.setLayoutParams(params);
            holder.messageContainer.setBackgroundResource(R.drawable.message_bubble_right);
            holder.messageTextView.setTextColor(Color.WHITE);
        } else {
            params.gravity = Gravity.START;
            params.setMargins(0, 0, 10, 0);
            holder.messageContainer.setLayoutParams(params);
            holder.messageContainer.setBackgroundResource(R.drawable.message_bubble_left);
        }

        holder.messageTextView.setText(message.getMessage());
        holder.senderTextView.setText((message.getSenderId()));
        holder.itemView.setOnLongClickListener(view -> {
            chatFragment.showDeleteMessageDialog(message, position); // Show dialog or menu to delete
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView;
        LinearLayout messageContainer;

        public ChatViewHolder(View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            messageContainer = itemView.findViewById(R.id.message_container);

        }
    }
}
