package com.example.telemedicine.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Doctor;
import com.example.telemedicine.models.Slot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DoctorDetailsFragment extends Fragment {

    private TextView doctorName, doctorSpecialty, doctorFees;
    private RecyclerView slotsRecyclerView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_doctor_details, container, false);

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
                                    slots.add(new Slot(entry.getKey(), time, false));
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
        // Booking logic (as implemented in the original code)
    }
}
