package com.example.telemedicine.doctor;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PatientListFragment extends Fragment {
    private RecyclerView patientRecyclerView;
    private PatientListAdapter adapter;
    private List<PatientProfile> patientList = new ArrayList<>();
    private FirebaseFirestore db;
    private String doctorId;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_list, container, false);
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);  // Initialize ProgressBar

        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        patientRecyclerView = view.findViewById(R.id.patientRecyclerView);
        patientRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PatientListAdapter(patientList);  // Pass the patientList directly
        patientRecyclerView.setAdapter(adapter);

        loadPatientList();
        return view;
    }

    private void loadPatientList() {
        progressBar.setVisibility(View.VISIBLE);
        patientList.clear();
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> patientIds = (List<String>) documentSnapshot.get("patientList");
                        if (patientIds != null) {
                            for (String patientId : patientIds) {
                                // Fetch each patient's detailed info from the "patients" collection
                                db.collection("patients").document(patientId).get()
                                        .addOnSuccessListener(patientSnapshot -> {
                                            if (patientSnapshot.exists()) {
                                                String name = patientSnapshot.getString("name");
                                                String age = patientSnapshot.getString("age");
                                                String gender = patientSnapshot.getString("gender");

                                                // Create PatientProfile with detailed info
                                                PatientProfile patientProfile = new PatientProfile(patientId, name, age, gender);
                                                patientList.add(patientProfile);
                                                adapter.notifyDataSetChanged(); // Notify adapter after each update
                                            } else {
                                                Log.d("Patient Info", "Patient not found: " + patientId);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Patient Info", "Error fetching patient data for ID: " + patientId, e);
                                        });
                            }
                        } else {
                            Log.d("Patient List", "No patient IDs found");
                        }
                    } else {
                        Toast.makeText(getContext(), "Doctor not found.", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching doctor data.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

}

