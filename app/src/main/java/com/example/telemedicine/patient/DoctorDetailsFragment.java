package com.example.telemedicine.patient;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DoctorDetailsFragment extends Fragment {

    private TextView doctorName, doctorSpecialty, doctorFees, noAvailable;
    private Button datePickerButton;
    private Calendar selectedDate;
    private RecyclerView slotsRecyclerView;
    private String doctorId;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Slot> allSlots;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_doctor_details, container, false);

        doctorName = view.findViewById(R.id.doctorName);
        doctorSpecialty = view.findViewById(R.id.doctorSpecialty);
        doctorFees = view.findViewById(R.id.doctorFees);
        datePickerButton = view.findViewById(R.id.datePickerButton);
        noAvailable = view.findViewById(R.id.notAvailable);
        slotsRecyclerView = view.findViewById(R.id.slotsRecyclerView);
        slotsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle args = getArguments();
        if (args != null) {
            Doctor doctor = (Doctor) args.getSerializable("doctor");
            if (doctor != null) {
                displayDoctorDetails(doctor);
                setupDatePicker(doctor);
            }
        }

        return view;
    }

    private void displayDoctorDetails(Doctor doctor) {
        doctorId = doctor.getId();
        doctorName.setText(doctor.getName());
        doctorSpecialty.setText(doctor.getSpecialty());
        doctorFees.setText("Fees: Rs. " + doctor.getFees());
    }

    private void setupDatePicker(Doctor doctor) {
        selectedDate = Calendar.getInstance();
        updateDateButton();

        datePickerButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Objects.requireNonNull(getActivity()),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateButton();
                        fetchAvailableSlots(doctor);
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        fetchAvailableSlots(doctor);
    }

    private void updateDateButton() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        datePickerButton.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void fetchAvailableSlots(Doctor doctor) {
        allSlots = new ArrayList<>();
        String selectedDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

        db.collection("doctors").document(doctor.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();

                Object availableSlotsObject = document.get("availableSlots");
                if (availableSlotsObject instanceof Map) {
                    Map<String, Map<String, Object>> slotDataMap = (Map<String, Map<String, Object>>) availableSlotsObject;

                    Date currentDate = new Date();
                    for (Map.Entry<String, Map<String, Object>> entry : slotDataMap.entrySet()) {
                        String slotId = entry.getKey();
                        Map<String, Object> slotData = entry.getValue();

                        try {
                            String date = (String) slotData.get("date");
                            String time = (String) slotData.get("time");
                            Boolean isBooked = (Boolean) slotData.get("isBooked");

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String formattedDate = dateFormat.format(currentDate);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String formattedTime = timeFormat.format(currentDate);
                            Date currentTiming = timeFormat.parse(formattedTime);
                            selectedDate.setTime(currentTiming);
                            selectedDate.add(Calendar.HOUR, 2);
                            Date bufferTime = selectedDate.getTime();

                            Date slotDate = dateFormat.parse(date);
                            Date slotTime = timeFormat.parse(time);
                            if (!isBooked && date.equals(selectedDateStr)) {
                                if (slotDate.after(currentDate)) {
                                    allSlots.add(new Slot(slotId, date, time, false));
                                } else if (date.equals(formattedDate) && slotTime.after(bufferTime)) {
                                    allSlots.add(new Slot(slotId, date, time, false));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(allSlots.isEmpty()){
                        noAvailable.setVisibility(View.VISIBLE);
                    }
                }
                showAvailableSlots(doctor);
            } else {
                Toast.makeText(getActivity(), "Error fetching slots.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAvailableSlots(Doctor doctor) {
        SlotsAdapter adapter = new SlotsAdapter(allSlots, slot -> bookSlot(slot,doctor));
        slotsRecyclerView.setAdapter(adapter);
    }

    private void bookSlot(Slot slot, Doctor doctor) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Booking")
                .setMessage("Are you sure you want to book this slot on " + slot.getDate() + " at " + slot.getTime() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("doctors").document(doctorId).update(
                            "availableSlots." + slot.getId() + ".isBooked", true
                    ).addOnSuccessListener(aVoid -> {
                        Map<String, Object> appointmentData = new HashMap<>();
                        appointmentData.put("doctorId", doctorId);
                        appointmentData.put("doctorName", doctorName.getText().toString());
                        appointmentData.put("patientId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        appointmentData.put("slotTime", slot.getTime());
                        appointmentData.put("slotDate", slot.getDate());
                        appointmentData.put("status", "booked");

                        db.collection("appointments").add(appointmentData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getActivity(), "Slot booked successfully!", Toast.LENGTH_SHORT).show();
                                    fetchAvailableSlots(doctor);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Error adding appointment details.", Toast.LENGTH_SHORT).show();
                                    db.collection("doctors").document(doctorId).update(
                                            "availableSlots." + slot.getId() + ".isBooked", false
                                    );
                                });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error booking slot.", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

}
