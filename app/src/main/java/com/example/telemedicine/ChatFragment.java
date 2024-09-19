package com.example.telemedicine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemedicine.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

public class ChatFragment extends Fragment {

    private EditText messageInput;
    private Button sendMessageButton;
    private RecyclerView messagesRecyclerView;

    private String appointmentId;
    private FirebaseFirestore db;
    private CollectionReference chatRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();
        messageInput = view.findViewById(R.id.messageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);

        appointmentId = getArguments().getString("appointmentId");

        chatRef = db.collection("appointments").document(appointmentId).collection("messages");

        sendMessageButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("message", messageText);
            message.put("timestamp", System.currentTimeMillis());

            chatRef.add(message)
                    .addOnSuccessListener(documentReference -> messageInput.setText(""))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show());
        }
    }
}
