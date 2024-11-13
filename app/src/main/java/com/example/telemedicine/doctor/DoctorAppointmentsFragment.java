package com.example.telemedicine.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Appointment;
import com.example.telemedicine.ui.LoginActivity;
import com.example.telemedicine.util.JitsiUtils;
//import com.google.android.exoplayer2.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorAppointmentsFragment extends Fragment implements DoctorAppointmentsAdapter.OnDoctorAppointmentActionListener {

    private RecyclerView recyclerView;
    private DoctorAppointmentsAdapter adapter;
    private List<Appointment> appointments = new ArrayList<>();;  // Fetch this list from the backend (e.g., Firebase)
    private FirebaseFirestore db;
    private String userId;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerViewDoctorAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter and pass the list of appointments
        adapter = new DoctorAppointmentsAdapter(appointments, this);
        recyclerView.setAdapter(adapter);

        // Load the appointments from backend (Firebase query can be implemented here)
        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        appointments.clear();
        db.collection("appointments")
                .whereEqualTo("doctorId", userId) // Fetch appointments for the logged-in doctor
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            appointments.clear();  // Clear the list before adding new data
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Appointment appointment = document.toObject(Appointment.class);
                                appointment.setAppointmentId(document.getId());
                                appointments.add(appointment);
                            }
                            // Notify adapter that data has changed
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No appointments found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
//                        Log.e("DoctorAppointments", "Error getting appointments", task.getException());
                        Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching data.", Toast.LENGTH_SHORT).show();
                    // Hide the progress bar if there's an error
                    progressBar.setVisibility(View.GONE);
                });
    }


    public void onJoinConsultation(Appointment appointment) {
        String roomId = appointment.getAppointmentId();
        if (roomId == null || roomId.isEmpty()) {
            return;
        }
        JitsiUtils.startJitsiMeeting(getContext(), roomId);
    }

    public void onVedioCall(Appointment appointment){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

//    @Override
//    public void onJoinConsultation(Appointment appointment) {
//        // Start the call when Join button is clicked
//        String appointmentId = appointment.getAppointmentId();
//        String doctorId = appointment.getDoctorId();
//        String patientId = appointment.getPatientId();
//
//        if (appointmentId != null && doctorId != null && patientId != null) {
//            // Start CallActivity and pass patientId and doctorId
//            Intent intent = new Intent(getContext(), CallActivity.class);
//            intent.putExtra("doctorId", doctorId);
//            intent.putExtra("patientId", patientId);
//            startActivity(intent);
//        } else {
//            Toast.makeText(getContext(), "Invalid appointment data", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void startConsultation(String appointmentId) {
//        // This will start the WebRTC flow between doctor and patient using the appointmentId
//        Intent intent = new Intent(getContext(), CallActivity.class);
//        intent.putExtra("appointmentId", appointmentId); // Pass the appointmentId to the CallActivity
//        startActivity(intent);
//    }


    @Override
    public void onViewPatientProfile(Appointment appointment) {
        String patientId = appointment.getPatientId(); // Ensure this ID is retrieved correctly.
        if(patientId!= null){
            Fragment patientProfileFragment = new PatientProfilesFragment(patientId);

            // Replace the current fragment with the PatientProfileFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, patientProfileFragment)
                    .addToBackStack(null)
                    .commit();
        }
        else{
            Toast.makeText(getContext(), "Error loading patient details", Toast.LENGTH_SHORT).show();
        }

    }

}
