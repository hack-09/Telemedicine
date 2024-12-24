package com.example.telemedicine.patient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemedicine.R;
import com.example.telemedicine.models.Doctor;
import com.example.telemedicine.models.DoctorProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorListFragment extends Fragment {

    private RecyclerView doctorRecyclerView;
    private DoctorListAdapter doctorListAdapter;
    private List<Doctor> doctorList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_doctor_list, container, false);

        // Initialize RecyclerView
        doctorRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        doctorList = new ArrayList<>();

        // Set up doctor list and fetch doctors
        setupDoctorList();

        return view;
    }

    private void setupDoctorList() {
        getDoctors(doctors -> {
            // Update doctorList and notify adapter
            doctorList = doctors;

            // Initialize the adapter with the context, doctor list, and the selection listener
            doctorListAdapter = new DoctorListAdapter(getActivity(), doctorList, this::onDoctorSelected);
            doctorRecyclerView.setAdapter(doctorListAdapter);

            // Notify the adapter to update the RecyclerView
            doctorListAdapter.notifyDataSetChanged();
        });
    }

    private void getDoctors(OnDoctorsFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("doctors").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Doctor> doctors = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String specialty = document.getString("specialty");
                            String id = document.getId();
                            String profileUrl = document.getString("profileImageUrl");
                            String fees = document.getString("fees");
                            doctors.add(new Doctor(id, name, specialty, profileUrl, fees));
                        }
                        listener.onDoctorsFetched(doctors);
                    } else {
                        Toast.makeText(getActivity(), "Error getting doctors.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onDoctorSelected(Doctor doctor) {
        // Handle doctor selection, e.g., navigate to a detailed doctor profile or booking slots
        Toast.makeText(getActivity(), "Selected doctor: " + doctor.getName(), Toast.LENGTH_SHORT).show();
//        showAvailableSlotsDialog(doctor);
    }

    interface OnDoctorsFetchedListener {
        void onDoctorsFetched(List<Doctor> doctors);
    }
}
