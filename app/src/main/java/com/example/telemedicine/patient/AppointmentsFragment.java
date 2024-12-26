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
    private RecyclerView doctorRecyclerView, appointmentsRecyclerView, slotsRecyclerView;
    private TextView noAppointmentsText;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;
    private List<Appointment> appointmentList;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        calendarView = view.findViewById(R.id.calendarView);
        doctorRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        slotsRecyclerView = view.findViewById(R.id.slotsRecyclerView);
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText);

        // Set up RecyclerViews
        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        slotsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        setupDoctorList();
        setupAppointmentsList();
        setupCalendar();

        return view;
    }

    private void setupDoctorList() {
        getDoctors(doctors -> {
            doctorList = doctors;
            doctorAdapter = new DoctorAdapter(doctorList, this::onDoctorSelected);
            doctorRecyclerView.setAdapter(doctorAdapter);
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
                            String profileurl = document.getString("profileImageUrl");
                            String fees = document.getString("fees");
                            doctors.add(new Doctor(id, name, specialty, profileurl, fees));
                        }
                        listener.onDoctorsFetched(doctors);
                    } else {
                        Toast.makeText(getActivity(), "Error getting doctors.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void onDoctorSelected(Doctor doctor) {
        fetchAvailableSlots(doctor, slots -> showAvailableSlots(slots, doctor));
    }

    private void showAvailableSlots(List<Slot> slots, Doctor doctor) {
        if (slots.isEmpty()) {
            Toast.makeText(getActivity(), "No available slots for " + doctor.getName(), Toast.LENGTH_SHORT).show();
        } else {
            SlotsAdapter slotsAdapter = new SlotsAdapter(slots, slot -> bookSlot(slot, doctor));
            slotsRecyclerView.setAdapter(slotsAdapter);
        }
    }

    private void bookSlot(Slot slot, Doctor doctor) {
        // Implement booking logic here, such as saving the appointment to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("patientId", userId);
        appointment.put("doctorId", doctor.getId());
        appointment.put("doctorName", doctor.getName());
        appointment.put("slotTime", slot.getTime());
        appointment.put("status", "Booked");

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    // Update the slot's booked status
                    updateSlotStatus(doctor.getId(), slot.getTime(), true);
                    Toast.makeText(getActivity(), "Appointment booked with " + doctor.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to book appointment.", Toast.LENGTH_SHORT).show();
                });
    }


    interface OnSlotsFetchedListener {
        void onSlotsFetched(List<Slot> slots);
    }

    private void fetchAvailableSlots(Doctor doctor, OnSlotsFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("doctors").document(doctor.getId()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        List<Slot> slots = new ArrayList<>();

                        Object availableSlotsObject = document.get("availableSlots");

                        if (availableSlotsObject instanceof Map) {
                            Map<String, Map<String, Object>> slotDataMap = (Map<String, Map<String, Object>>) availableSlotsObject;

                            for (Map.Entry<String, Map<String, Object>> entry : slotDataMap.entrySet()) {
                                String slotId = entry.getKey();
                                Map<String, Object> slotData = entry.getValue();

                                String date = (String) slotData.get("date");
                                String time = (String) slotData.get("time");
                                Boolean isBooked = (Boolean) slotData.get("isBooked");

                                if (isBooked != null && !isBooked) {
                                    Slot slot = new Slot(slotId, date, false);
                                    slots.add(slot);
                                } else {
                                    Toast.makeText(getActivity(), "No available slots for this doctor.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.e("fetchAvailableSlots", "Unexpected data structure for availableSlots");
                        }

                        listener.onSlotsFetched(slots);
                    } else {
                        Log.e("fetchAvailableSlots", "Error getting document: ", task.getException());
                        Toast.makeText(getActivity(), "Error getting available slots.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    // Define any additional listeners or interfaces here
    interface OnDoctorsFetchedListener {
        void onDoctorsFetched(List<Doctor> doctors);
    }
}
