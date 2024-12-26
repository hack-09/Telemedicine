package com.example.telemedicine.patient;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Appointment;
import com.example.telemedicine.models.Doctor;
import com.example.telemedicine.models.Slot;
import com.example.telemedicine.ui.LoginActivity;
import com.example.telemedicine.util.JitsiUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsFragment extends Fragment implements AppointmentsAdapter.OnAppointmentActionListener {

    private CalendarView calendarView;
    private RecyclerView appointmentsRecyclerView;
    private TextView noAppointmentsText;
    private List<Appointment> appointmentList;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        calendarView = view.findViewById(R.id.calendarView);
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText);

        // Set up RecyclerViews
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAppointmentsList();
        setupCalendar();

        return view;
    }

    private void setupAppointmentsList() {
        appointmentList = new ArrayList<>();
        AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(getContext(), appointmentList, this);
        appointmentsRecyclerView.setAdapter(appointmentsAdapter);

        fetchAppointmentsFromFirebase(appointments -> {
            if (appointments.isEmpty()) {
                noAppointmentsText.setVisibility(View.VISIBLE);
                appointmentsRecyclerView.setVisibility(View.GONE);
            } else {
                noAppointmentsText.setVisibility(View.GONE);
                appointmentsRecyclerView.setVisibility(View.VISIBLE);

                appointmentList.clear();
                appointmentList.addAll(appointments);
                appointmentsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchAppointmentsFromFirebase(OnAppointmentsFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference appointmentsRef = db.collection("appointments");

        appointmentsRef.whereEqualTo("patientId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Appointment> appointments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointment.setAppointmentId(document.getId()); // Set the document ID as the appointment ID
                            appointments.add(appointment);
                        }
                        listener.onAppointmentsFetched(appointments);
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        cancelAppointment(appointment);
    }

    public void onVedioCall(Appointment appointment){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onJoinConsultation(Appointment appointment) {
        // Fetch appointment details, then start the meeting
        fetchAppointmentDetails(appointment.getAppointmentId());
    }
    private void fetchAppointmentDetails(String appointmentId) {
        if (appointmentId == null || appointmentId.isEmpty()) {
            Log.e("DoctorAppointments", "Appointment ID is null or empty!");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments").document(appointmentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve necessary information
                        String doctorId = documentSnapshot.getString("doctorId");
                        String patientId = documentSnapshot.getString("patientId");

                        // Ensure the current user is either the patient or the doctor
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if (currentUserId.equals(doctorId) || currentUserId.equals(patientId)) {
                            // Start the consultation using the appointment ID as the room ID
                            JitsiUtils.startJitsiMeeting(getContext(), appointmentId);
                        } else {
                            Toast.makeText(getContext(), "You do not have permission to join this consultation.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Appointment not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching appointment details.", Toast.LENGTH_SHORT).show();
                });
    }



    interface OnAppointmentsFetchedListener {
        void onAppointmentsFetched(List<Appointment> appointments);
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            List<Appointment> appointments = getAppointmentsForDate(year, month, dayOfMonth);
            updateAppointmentsList(appointments);
        });
    }

    private List<Appointment> getAppointmentsForDate(int year, int month, int dayOfMonth) {
        return new ArrayList<>();
    }

    interface OnSlotsFetchedListener {
        void onSlotsFetched(List<Slot> slots);
    }


    private void updateSlotStatus(String doctorId, String slotTime, boolean isBooked) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("doctors").document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> availableSlots = (Map<String, Object>) documentSnapshot.get("availableSlots");
                        if (availableSlots != null) {
                            for (Map.Entry<String, Object> entry : availableSlots.entrySet()) {
                                Map<String, Object> slotDetails = (Map<String, Object>) entry.getValue();
                                if (slotDetails.get("time").equals(slotTime)) {
                                    String slotId = entry.getKey();
                                    Log.e("Firestore", "Slot Id: " + slotId);
                                    db.collection("doctors").document(doctorId)
                                            .update("availableSlots." + slotId + ".isBooked", isBooked)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getActivity(), "Slot status updated.", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FirestoreError", "Failed to update slot status: " + e.getMessage());
                                                Toast.makeText(getActivity(), "Failed to update slot status.", Toast.LENGTH_SHORT).show();
                                            });
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private void cancelAppointment(Appointment appointment) {
        // Create a confirmation dialog
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel your appointment with " + appointment.getDoctorName() + " at " + appointment.getSlotTime() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with cancellation if user confirms
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("appointments")
                            .document(appointment.getAppointmentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Update slot status if needed, show success message
                                updateSlotStatus(appointment.getDoctorId(), appointment.getSlotTime(), false);
                                Toast.makeText(getActivity(), "Appointment canceled.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to cancel appointment.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog if the user cancels
                    dialog.dismiss();
                })
                .show();
    }

    private void updateAppointmentsList(List<Appointment> appointments) {
        AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(getContext(), appointments, this);
        appointmentsRecyclerView.setAdapter(appointmentsAdapter);
    }
}
