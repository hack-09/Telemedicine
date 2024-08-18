package com.example.telemedicine;

import android.os.Bundle;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView doctorRecyclerView, appointmentsRecyclerView;
    private TextView noAppointmentsText;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;
    private List<Appointment> appointmentList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        doctorRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText);

        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupDoctorList();
        setupAppointmentsList();
        setupCalendar();

        return view;
    }

    private void setupDoctorList() {
        // Initialize doctor list (horizontal RecyclerView)
        getDoctors(new OnDoctorsFetchedListener() {
            @Override
            public void onDoctorsFetched(List<Doctor> doctors) {
                doctorList = doctors;
                doctorAdapter = new DoctorAdapter(doctorList, AppointmentsFragment.this::onDoctorSelected);
                doctorRecyclerView.setAdapter(doctorAdapter);
            }
        });
    }

    private void getDoctors(OnDoctorsFetchedListener listener) {
        final List<Doctor> doctors = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("doctors").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String specialty = document.getString("specialty");
                                // Create a new Doctor object
                                Doctor doctor = new Doctor(name, specialty, "https://example.com/profile.jpg");
                                doctors.add(doctor);
                            }
                            listener.onDoctorsFetched(doctors);
                        } else {
                            Toast.makeText(getActivity(), "Error getting documents.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupAppointmentsList() {
        // Initialize appointments list (vertical RecyclerView)
        appointmentList = getAppointmentsForToday();
        updateAppointmentsList(appointmentList);
    }

    private List<Appointment> getAppointmentsForToday() {
        // Dummy implementation, replace with actual Firebase fetching logic
        return new ArrayList<>();
    }

    private void setupCalendar() {
        // Handle date selection from CalendarView
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Fetch and display available slots and appointments for the selected date
                List<Appointment> appointments = getAppointmentsForDate(year, month, dayOfMonth);
                updateAppointmentsList(appointments);
            }
        });
    }

    private List<Appointment> getAppointmentsForDate(int year, int month, int dayOfMonth) {
        // Dummy implementation, replace with actual Firebase fetching logic
        return new ArrayList<>();
    }

    private void onDoctorSelected(Doctor doctor) {
        // Show available slots and book an appointment with the selected doctor
        showDoctorDetails(doctor);
    }

    private void onAppointmentAction(Appointment appointment, String action) {
        // Handle actions like cancel or reschedule
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
        if (appointments.isEmpty()) {
            noAppointmentsText.setVisibility(View.VISIBLE);
            appointmentsRecyclerView.setVisibility(View.GONE);
        } else {
            noAppointmentsText.setVisibility(View.GONE);
            appointmentsRecyclerView.setVisibility(View.VISIBLE);
            AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(appointments, this::onAppointmentAction);
            appointmentsRecyclerView.setAdapter(appointmentsAdapter);
        }
    }

    private void showDoctorDetails(Doctor doctor) {
        // Implement showing doctor details and booking logic here
    }

    // Define interface for fetching doctors callback
    private interface OnDoctorsFetchedListener {
        void onDoctorsFetched(List<Doctor> doctors);
    }
}
