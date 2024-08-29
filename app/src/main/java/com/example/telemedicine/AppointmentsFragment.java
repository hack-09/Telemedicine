package com.example.telemedicine;

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

import com.example.telemedicine.models.Appointment;
import com.example.telemedicine.models.Doctor;
import com.example.telemedicine.models.Slot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsFragment extends Fragment {

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
                            // Assuming 'id' field exists in Firestore
                            String id = document.getId();
                            doctors.add(new Doctor(id, name, specialty, "https://example.com/profile.jpg"));
                        }
                        listener.onDoctorsFetched(doctors);
                    } else {
                        Toast.makeText(getActivity(), "Error getting doctors.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupAppointmentsList() {
        appointmentList = new ArrayList<>(); // Initialize with an empty list
        AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(appointmentList, this::onAppointmentAction);
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

        // Query for appointments related to a specific doctor or patient (adjust query as necessary)
        appointmentsRef
                .whereEqualTo("patientId", userId) // Example: filter by doctorId (change as necessary)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Appointment> appointments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointments.add(appointment);
                        }
                        listener.onAppointmentsFetched(appointments);
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }


    interface OnAppointmentsFetchedListener {
        void onAppointmentsFetched(List<Appointment> appointments);
    }



    private List<Appointment> getAppointmentsForToday() {
        // Dummy implementation, replace with actual Firebase fetching logic
        return new ArrayList<>();
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            List<Appointment> appointments = getAppointmentsForDate(year, month, dayOfMonth); // Implement actual fetching logic
            updateAppointmentsList(appointments);
        });
    }

    private List<Appointment> getAppointmentsForDate(int year, int month, int dayOfMonth) {
        // Dummy implementation, replace with actual Firebase fetching logic
        return new ArrayList<>();
    }

    private void onDoctorSelected(Doctor doctor) {
        fetchAvailableSlots(doctor, slots -> showAvailableSlots(slots, doctor));
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

                                // Only add slots that are not booked
                                if (isBooked != null && !isBooked) {
                                    Slot slot = new Slot(slotId, date, time, false);
                                    slots.add(slot);
                                }else {
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


    private void onAppointmentAction(Appointment appointment, String action) {
        if ("cancel".equals(action)) {
            cancelAppointment(appointment);
        } else if ("reschedule".equals(action)) {
            rescheduleAppointment(appointment);
        }
    }

    private void rescheduleAppointment(Appointment appointment) {
        // Implement rescheduling logic here
    }

    private void cancelAppointment(Appointment appointment) {
        // Implement cancel logic here
    }

    private void updateAppointmentsList(List<Appointment> appointments) {
        AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(appointments, this::onAppointmentAction);
        appointmentsRecyclerView.setAdapter(appointmentsAdapter);

        if (appointments == null || appointments.isEmpty()) {
            noAppointmentsText.setVisibility(View.VISIBLE);
            appointmentsRecyclerView.setVisibility(View.GONE);
        } else {
            noAppointmentsText.setVisibility(View.GONE);
            appointmentsRecyclerView.setVisibility(View.VISIBLE);
        }
    }



    private void showAvailableSlots(List<Slot> slots, Doctor doctor) {
        if (slots != null && !slots.isEmpty()) {
            SlotsAdapter slotsAdapter = new SlotsAdapter(slots, slot -> bookAppointment(doctor, slot));
            slotsRecyclerView.setAdapter(slotsAdapter);
            slotsAdapter.notifyDataSetChanged();
            slotsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(), "No available slots for this doctor.", Toast.LENGTH_SHORT).show();
        }
    }


    private void bookAppointment(Doctor doctor, Slot slot) {
        Log.d("bookAppointment", "Booking appointment for slot: " + slot.getId());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("doctorId", doctor.getId());
        appointmentData.put("doctorName", doctor.getName());
        appointmentData.put("slotTime", slot.getTime());
        appointmentData.put("patientId", userId);
        appointmentData.put("status", "Booked");

        db.collection("appointments").add(appointmentData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("bookAppointment", "Appointment booked successfully!");
                    Toast.makeText(getActivity(), "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                    updateSlotStatus(doctor, slot);
                })
                .addOnFailureListener(e -> {
                    Log.e("bookAppointment", "Failed to book appointment.", e);
                    Toast.makeText(getActivity(), "Failed to book appointment.", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateSlotStatus(Doctor doctor, Slot slot) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String slotId = slot.getId(); // Ensure Slot class has getId() method returning slot ID

        // Use doctor ID and slot ID to locate the correct field in the Map
        String slotFieldPath = "availableSlots." + slotId + ".isBooked";

        db.collection("doctors").document(doctor.getId())
                .update(slotFieldPath, true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Slot status updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to update slot status.", Toast.LENGTH_SHORT).show();
                });
    }



    // Define interface for fetching doctors callback
    private interface OnDoctorsFetchedListener {
        void onDoctorsFetched(List<Doctor> doctors);
    }
}
