package com.example.telemedicine.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.telemedicine.chat.ChatFragment;
import com.example.telemedicine.R;
import com.google.android.exoplayer2.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientProfilesFragment extends Fragment {

    private ImageView profileImage;
    private TextView nameTextView, ageTextView, genderTextView, patientDOB;
    private Button viewMedicalRecordsButton, chatButton;
    private String patientId, currentDoctorId;
    private FirebaseFirestore db;

    public PatientProfilesFragment(String patientId) {
        this.patientId = patientId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_profile, container, false);


        profileImage = view.findViewById(R.id.patientProfileImage);
        nameTextView = view.findViewById(R.id.patientName);
        ageTextView = view.findViewById(R.id.patientAge);
        genderTextView = view.findViewById(R.id.patientGender);
        patientDOB = view.findViewById(R.id.patient_dob);
        viewMedicalRecordsButton = view.findViewById(R.id.viewMedicalRecordsButton);
        chatButton = view.findViewById(R.id.chatButton);


        db = FirebaseFirestore.getInstance();
        currentDoctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadPatientDetails(patientId);

        viewMedicalRecordsButton.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), DoctorRecordsActivity.class).putExtra("patientId", patientId));
        });

        chatButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Log doctor and patient IDs for debugging
            Log.d("PatientProfilesFragment", "Doctor ID: " + currentDoctorId + ", Patient ID: " + patientId);

            // Construct a chat ID based on doctorId and patientId (or generate a UUID if needed)
            String chatId = currentDoctorId + "_" + patientId;
            DocumentReference chatDocRef = db.collection("chats").document(chatId);

            // Check if the chat document exists and if the participants array is present
            chatDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Check if the participants array exists in the document
                        if (document.contains("participants")) {
                            Log.d("PatientProfilesFragment", "Participants array exists. Continuing to ChatFragment.");
                        } else {
                            Log.d("PatientProfilesFragment", "Participants array does not exist. Creating it.");

                            // Create the participants array with doctorId and patientId
                            List<String> participants = new ArrayList<>();
                            participants.add(currentDoctorId);
                            participants.add(patientId);

                            // Update the document to include the participants array
                            chatDocRef.update("participants", participants)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("PatientProfilesFragment", "Participants array successfully added.")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.w("PatientProfilesFragment", "Error adding participants array", e)
                                    );
                        }
                    } else {
                        // Document does not exist, create it with participants array
                        List<String> participants = new ArrayList<>();
                        participants.add(currentDoctorId);
                        participants.add(patientId);

                        chatDocRef.set(Collections.singletonMap("participants", participants))
                                .addOnSuccessListener(aVoid ->
                                        Log.d("PatientProfilesFragment", "Chat document with participants array successfully created.")
                                )
                                .addOnFailureListener(e ->
                                        Log.w("PatientProfilesFragment", "Error creating chat document", e)
                                );
                    }

                    // Navigate to ChatFragment
                    ChatFragment chatFragment = new ChatFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("doctorId", currentDoctorId);
                    bundle.putString("patientId", patientId);
                    chatFragment.setArguments(bundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, chatFragment); // Assuming your container is content_frame
                    transaction.addToBackStack(null); // Optional
                    transaction.commit();
                } else {
                    Log.e("PatientProfilesFragment", "Failed to check chat document", task.getException());
                }
            });
        });
        return view;
    }

    private void loadPatientDetails(String patientId) {
        db.collection("patients").document(patientId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String age = documentSnapshot.getString("age");
                        String gender = documentSnapshot.getString("gender");
                        String dob = documentSnapshot.getString("dateOfBirth");

                        nameTextView.setText(name != null ? name : "N/A");
                        ageTextView.setText(age != null ?  age+" years" : "N/A");
                        genderTextView.setText(gender != null ? gender : "N/A");
                        patientDOB.setText(dob != null ? dob : "N/A");
                    } else {
                        Toast.makeText(getContext(), "Patient not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading patient details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
