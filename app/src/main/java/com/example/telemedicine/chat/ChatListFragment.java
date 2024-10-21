package com.example.telemedicine.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemedicine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private List<String> chatParticipants;
    private FirebaseFirestore db;
    private String currentUserId, userType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        db = FirebaseFirestore.getInstance();
        chatListRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatParticipants = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatListAdapter = new ChatListAdapter(chatParticipants, getContext(), currentUserId);
        chatListRecyclerView.setAdapter(chatListAdapter);

        fetchChats();

        return view;
    }

    private void fetchChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatParticipants.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<String> participants = (List<String>) document.get("participants");
                            for (String participant : participants) {
                                if (!participant.equals(currentUserId) && !chatParticipants.contains(participant)) {
                                    chatParticipants.add(participant);
                                }
                            }
                        }
                        chatListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ChatListFragment", "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(), "Failed to load chat list", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
