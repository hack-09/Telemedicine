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
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Toast;
import java.util.Calendar;

public class ManageAvailabilityFragment extends Fragment {

    private EditText etDate;
    private EditText etTime;
    private Button btnSaveSlot;
    private TextView tvStatus;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String doctorId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_availability, container, false);

        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        btnSaveSlot = view.findViewById(R.id.btnSaveSlot);
        tvStatus = view.findViewById(R.id.tvStatus);

        // Set up date picker for date EditText
        etDate.setOnClickListener(v -> showDatePicker());

        // Set up time picker for time EditText
        etTime.setOnClickListener(v -> showTimePicker());

        btnSaveSlot.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (!date.isEmpty() && !time.isEmpty()) {
                saveSlot(date, time);
            } else {
                tvStatus.setText("Please select both date and time.");
                tvStatus.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + " " + getMonthString(selectedMonth) + " " + selectedYear;
            etDate.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private String getMonthString(int month) {
        return new java.text.DateFormatSymbols().getMonths()[month];
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, selectedHour, selectedMinute) -> {
            String time = String.format("%02d:%02d %s", (selectedHour <= 12 ? selectedHour : selectedHour - 12),
                    selectedMinute, (selectedHour < 12 ? "AM" : "PM"));
            etTime.setText(time);
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private void saveSlot(String date, String time) {
        String slotId = String.valueOf(System.currentTimeMillis());

        Map<String, Object> slotData = new HashMap<>();
        slotData.put("date", date);
        slotData.put("isBooked", false);
        slotData.put("time", time);

        Map<String, Object> slotMap = new HashMap<>();
        slotMap.put(slotId, slotData);

        db.collection("doctors").document(doctorId)
                .set(new HashMap<String, Object>() {{
                    put("availableSlots", slotMap);
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    tvStatus.setText("Slot saved successfully.");
                    tvStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Slot saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    tvStatus.setText("Error saving slot: " + e.getMessage());
                    tvStatus.setVisibility(View.VISIBLE);
                });
    }
}
