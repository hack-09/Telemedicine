package com.example.telemedicine.doctor;

import android.os.Bundle;
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
import com.example.telemedicine.models.Appointment;
import com.example.telemedicine.util.JitsiUtils;
import com.google.android.exoplayer2.util.Log;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

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
                        Log.e("DoctorAppointments", "Error getting appointments", task.getException());
                        Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void onJoinConsultation(Appointment appointment) {
        // Start Jitsi meeting with the roomId from the appointment (e.g., appointmentId)
        String roomId = appointment.getAppointmentId();
        if (roomId == null || roomId.isEmpty()) {
            Log.e("DoctorAppointments", "Appointment ID is null or empty!");
            return;
        }
        JitsiUtils.startJitsiMeeting(getContext(), roomId);
    }

    @Override
    public void onViewPatientProfile(Appointment appointment) {
        // Handle viewing the patient's profile based on the appointment's patientId
        Toast.makeText(getContext(), "Viewing profile for: " + appointment.getPatientName(), Toast.LENGTH_SHORT).show();

        // TODO: Implement navigation to the patient's profile fragment
    }
}
