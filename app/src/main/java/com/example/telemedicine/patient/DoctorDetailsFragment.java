package com.example.telemedicine.patient;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Doctor;
import com.example.telemedicine.models.Slot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DoctorDetailsFragment extends Fragment {

    private TextView doctorName, doctorSpecialty, doctorFees;
    private RecyclerView slotsRecyclerView;
    private String userId;
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_doctor_details, container, false);
        auth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        doctorName = view.findViewById(R.id.doctorName);
        doctorSpecialty = view.findViewById(R.id.doctorSpecialty);
        doctorFees = view.findViewById(R.id.doctorFees);
        slotsRecyclerView = view.findViewById(R.id.slotsRecyclerView);
        slotsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle args = getArguments();
        if (args != null) {
            Doctor doctor = (Doctor) args.getSerializable("doctor");
            if (doctor != null) {
                displayDoctorDetails(doctor);
                fetchAvailableSlots(doctor, slots -> showAvailableSlots(slots, doctor));
            }
        }

        return view;
    }

    private void displayDoctorDetails(Doctor doctor) {
        doctorName.setText(doctor.getName());
        doctorSpecialty.setText(doctor.getSpecialty());
        doctorFees.setText("Fees: Rs. " + doctor.getFees());
    }

    private void fetchAvailableSlots(Doctor doctor, AppointmentsFragment.OnSlotsFetchedListener listener) {
        db.collection("doctors").document(doctor.getId()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Slot> slots = new ArrayList<>();
                        Map<String, Map<String, Object>> availableSlots =
                                (Map<String, Map<String, Object>>) task.getResult().get("availableSlots");

                        if (availableSlots != null) {
                            for (Map.Entry<String, Map<String, Object>> entry : availableSlots.entrySet()) {
                                Map<String, Object> slotData = entry.getValue();
                                boolean isBooked = (Boolean) slotData.get("isBooked");
                                if (!isBooked) {
                                    String time = (String) slotData.get("time");
                                    slots.add(new Slot(entry.getKey(), "", time, false));
                                }
                            }
                        }
                        listener.onSlotsFetched(slots);
                    }
                });
    }

    private void showAvailableSlots(List<Slot> slots, Doctor doctor) {
        SlotsAdapter adapter = new SlotsAdapter(slots, slot -> bookSlot(slot, doctor));
        slotsRecyclerView.setAdapter(adapter);
    }

    private void bookSlot(Slot slot, Doctor doctor) {
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
    }private void updateSlotStatus(String doctorId, String slotTime, boolean isBooked) {
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
}
