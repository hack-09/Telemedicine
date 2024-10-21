package com.example.telemedicine.chat;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemedicine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<String> chatParticipants;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId ;

    public ChatListAdapter(List<String> chatParticipants, Context context, String currentUserId) {
        this.chatParticipants = chatParticipants;
        this.context = context;
        this.currentUserId = currentUserId;

        Log.d("ChatListAdapter", "current user id -> "+ currentUserId);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_participant_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        String participantId = chatParticipants.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(participantId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Log.d("ChatListAdapter", "paticipants id -> "+ participantId);
            if (documentSnapshot.exists()) {
                String participantName = documentSnapshot.getString("name");
                holder.participantName.setText("Chat with " + participantName);
            } else {
                holder.participantName.setText("Chat with " + participantId);
            }
        }).addOnFailureListener(e -> {
            Log.w("ChatListAdapter", "Error fetching participant name", e);
            holder.participantName.setText("Chat with " + participantId); // Fallback in case of an error
        });

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType"); // Assuming 'userType' field exists

                        // Set up the click listener after fetching userType
                        holder.itemView.setOnClickListener(v -> {
                            ChatFragment chatFragment = new ChatFragment();

                            // Pass doctorId and patientId based on userType
                            Bundle bundle = new Bundle();
                            if ("Doctor".equals(userType)) {
                                bundle.putString("doctorId", currentUserId);
                                bundle.putString("patientId", participantId);
                            } else if ("Patient".equals(userType)) {
                                bundle.putString("doctorId", participantId);
                                bundle.putString("patientId", currentUserId);
                            }
                            chatFragment.setArguments(bundle);

                            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, chatFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        });
                    } else {
                        Log.d("ChatListAdapter", "User type not found in Firestore for userId: " + currentUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatListAdapter", "Failed to fetch user type", e));
    }



    @Override
    public int getItemCount() {
        return chatParticipants.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView participantName;

        public ChatViewHolder(View itemView) {
            super(itemView);
            participantName = itemView.findViewById(R.id.participant_name);
        }
    }
}
