package com.example.telemedicine.chat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemedicine.R;
import com.google.android.exoplayer2.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private EditText messageInput;
    private Button sendMessageButton;
    private RecyclerView messagesRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    public Toolbar toolbar;
    public TextView chatHeading;
    private String doctorId, patientId;
    private String chatId; // Ensure this is initialized
    private FirebaseFirestore db;
    private CollectionReference chatRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatHeading = view.findViewById(R.id.chatHeading);
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            String doctorId = args.getString("doctorId");
            String patientId = args.getString("patientId");

            if (doctorId != null && patientId != null) {
                chatId = generateChatId(doctorId, patientId);
                fetchParticipantName(doctorId, patientId);
            } else {
                Log.e("ChatFragment", "doctorId or patientId is null!");
            }
        } else {
            Log.e("ChatFragment", "Arguments are null!");
        }

        messageInput = view.findViewById(R.id.messageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);

        chatAdapter = new ChatAdapter(messageList, this);
        messagesRecyclerView.setAdapter(chatAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sendMessageButton.setOnClickListener(v -> sendMessage());

        if (chatId != null) {
            fetchMessages();
        } else {
            Log.e("ChatFragment", "chatId is null, unable to fetch messages.");
        }

        return view;
    }


    private void fetchParticipantName(String doctorId, String patientId) {
        String participantId = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(doctorId) ? patientId : doctorId;

        db.collection("users").document(participantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String participantName = documentSnapshot.getString("name");
                        if (participantName != null) {
                            chatHeading.setText(participantName);
                        } else {
                            chatHeading.setText("Chat with Patient");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to fetch participant name", e));
    }

    private String generateChatId(String doctorId, String patientId) {
        // For example, concatenate the IDs or use some other unique generation method
        return doctorId + "_" + patientId;
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            if (chatId == null) {
                Toast.makeText(getContext(), "Chat ID is null. Unable to send message.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (userId == null) {
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> message = new HashMap<>();
            message.put("senderId", userId);
            message.put("message", messageText);
            message.put("timestamp", System.currentTimeMillis());
            message.put("readBy", new ArrayList<String>());

            db.collection("chats").document(chatId).collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        messageInput.setText("");
                        fetchMessages();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDeleteMessageDialog(ChatMessage message, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMessage(message, position); // Call the deleteMessage method
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteMessage(ChatMessage message, int position) {
        String messageId = message.getMessageId();

        if (messageId == null || messageId.isEmpty()) {
            Log.e("ChatFragment", "Message ID is null or empty!");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference messageRef = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId);

        messageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    messageList.remove(position);
                    chatAdapter.notifyItemRemoved(position);
                    Log.d("ChatFragment", "Message deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatAdapter", "Error deleting message", e);
                });
    }


    private void fetchMessages() {
        if (chatId != null) {
            db.collection("chats").document(chatId).collection("messages")
                    .orderBy("timestamp")
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Log.w("ChatFragment", "Listen failed.", e);
                            return;
                        }

                        List<ChatMessage> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            message.setMessageId(doc.getId());
                            messages.add(message);
                        }
                        updateUIWithMessages(messages);
                    });
        } else {
            Log.e("ChatFragment", "chatId is null");
        }
    }


    private void updateUIWithMessages(List<ChatMessage> messages) {
        messageList.clear();
        messageList.addAll(messages);
        chatAdapter.notifyDataSetChanged();
        messagesRecyclerView.scrollToPosition(messages.size() - 1);
    }
}
