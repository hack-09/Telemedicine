package com.example.telemedicine.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telemedicine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Toast;
import java.util.Calendar;

public class ManageAvailabilityFragment extends Fragment {

    private EditText etStartDate, etEndDate, etStartTime, etEndTime;
    private Button btnSaveSlots;
    private TextView tvStatus;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String doctorId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_availability, container, false);

        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        btnSaveSlots = view.findViewById(R.id.btnSaveSlots);
        tvStatus = view.findViewById(R.id.tvStatus);

        // Set up date and time pickers for start and end date and time
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));

        btnSaveSlots.setOnClickListener(v -> {
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();

            if (!startDate.isEmpty() && !startTime.isEmpty() && !endTime.isEmpty()) {
                saveSlots(startDate, endDate, startTime, endTime);
            } else {
                tvStatus.setText("Please select both dates and times.");
                tvStatus.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            if (isStart) {
                etStartDate.setText(date);
            } else {
                etEndDate.setText(date);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, selectedHour, selectedMinute) -> {
            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
            if (isStart) {
                etStartTime.setText(time);
            } else {
                etEndTime.setText(time);
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private void saveSlots(String startDate, String endDate, String startTime, String endTime) {
        String slotIdPrefix = String.valueOf(System.currentTimeMillis());

        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime.split(":")[0]));
        startCal.set(Calendar.MINUTE, Integer.parseInt(startTime.split(":")[1]));

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime.split(":")[0]));
        endCal.set(Calendar.MINUTE, Integer.parseInt(endTime.split(":")[1]));

        Map<String, Object> slotMap = new HashMap<>();
        int slotIndex = 0;

        // Generate 30-minute slots between the start and end times
        while (startCal.before(endCal)) {
            String slotTime = String.format("%02d:%02d", startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE));
            String slotId = slotIdPrefix + "_" + slotIndex;

            Map<String, Object> slotData = new HashMap<>();
            slotData.put("date", startDate);
            slotData.put("time", slotTime);
            slotData.put("isBooked", false);

            slotMap.put(slotId, slotData);

            startCal.add(Calendar.MINUTE, 30);  // Increment by 30 minutes
            slotIndex++;
        }

        // Save the slots to Firestore
        db.collection("doctors").document(doctorId)
                .set(new HashMap<String, Object>() {{
                    put("availableSlots", slotMap);
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    tvStatus.setText("Slots saved successfully.");
                    tvStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Slots saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    tvStatus.setText("Error saving slots: " + e.getMessage());
                    tvStatus.setVisibility(View.VISIBLE);
                });
    }

    private String getMonthString(int month) {
        return new java.text.DateFormatSymbols().getMonths()[month];
    }
}
