package com.example.telemedicine.patient;

import static com.dropbox.core.json.JsonWriter.formatDate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsFragment extends Fragment implements AppointmentsAdapter.OnAppointmentActionListener {

    private Button selectDateButton;
    private RecyclerView appointmentsRecyclerView;
    private TextView noAppointmentsText;
    private List<Appointment> appointmentList;
    private AppointmentsAdapter appointmentsAdapter;
    private String userId;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        selectDateButton = view.findViewById(R.id.selectDateButton);
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText);

        // Set up RecyclerView
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        appointmentList = new ArrayList<>();
        appointmentsAdapter = new AppointmentsAdapter(getContext(), appointmentList, this);
        appointmentsRecyclerView.setAdapter(appointmentsAdapter);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate = formatDate(year, month, dayOfMonth);

        // Update button to show current date
        selectDateButton.setText(selectedDate);

        // Fetch appointments for current date
        fetchAppointmentsForDate(selectedDate);

        setupDateSelection();

        return view;
    }

    private void setupDateSelection() {
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = formatDate(selectedYear, selectedMonth, selectedDay);
                    selectDateButton.setText(selectedDate); // Update button text
                    fetchAppointmentsForDate(selectedDate); // Fetch appointments
                },
                year, month, dayOfMonth
        );
        datePickerDialog.show();
    }

    private String formatDate(int year, int month, int dayOfMonth) {
        month++;
        return String.format("%04d-%02d-%02d", year, month, dayOfMonth);
    }

    private void fetchAppointmentsForDate(String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .whereEqualTo("patientId", userId)
                .whereEqualTo("slotDate", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Appointment> filteredAppointments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointment.setAppointmentId(document.getId());
                            filteredAppointments.add(appointment);
                        }
                        updateAppointmentsList(filteredAppointments);
                    } else {
                        Log.w("Firestore", "Error fetching appointments.", task.getException());
                        Toast.makeText(getActivity(), "Error fetching appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAppointmentsList(List<Appointment> appointments) {
        appointmentList.clear();
        if (appointments.isEmpty()) {
            noAppointmentsText.setVisibility(View.VISIBLE);
            appointmentsRecyclerView.setVisibility(View.GONE);
        } else {
            noAppointmentsText.setVisibility(View.GONE);
            appointmentsRecyclerView.setVisibility(View.VISIBLE);
            appointmentList.addAll(appointments);
        }
        appointmentsAdapter.notifyDataSetChanged();
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
        fetchAppointmentDetails(appointment.getAppointmentId());
    }

    private void fetchAppointmentDetails(String appointmentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String doctorId = documentSnapshot.getString("doctorId");
                        String patientId = documentSnapshot.getString("patientId");
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (currentUserId.equals(doctorId) || currentUserId.equals(patientId)) {
                            JitsiUtils.startJitsiMeeting(getContext(), appointmentId);
                        } else {
                            Toast.makeText(getContext(), "Permission denied for this consultation.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Appointment not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to fetch appointment details.", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel your appointment with " + appointment.getDoctorName() + " at " + appointment.getSlotTime() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("appointments")
                            .document(appointment.getAppointmentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Appointment canceled.", Toast.LENGTH_SHORT).show();
                                fetchAppointmentsForDate(selectedDate);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to cancel appointment.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

}
