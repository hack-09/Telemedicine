package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.models.Doctor;

import java.util.List;

public class AppointmentsFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView doctorRecyclerView, appointmentsRecyclerView;
    private TextView noAppointmentsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        doctorRecyclerView = view.findViewById(R.id.doctorRecyclerView);
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText);

        setupDoctorList();
        setupAppointmentsList();
        setupCalendar();

        return view;
    }

    private void setupDoctorList() {
        // Initialize doctor list (horizontal RecyclerView)
        List<Doctor> doctorList = getDoctors();
        DoctorAdapter doctorAdapter = new DoctorAdapter(doctorList, this::onDoctorSelected);
        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        doctorRecyclerView.setAdapter(doctorAdapter);
    }

    private void setupAppointmentsList() {
        // Initialize appointments list (vertical RecyclerView)
        List<Appointment> appointmentList = getAppointmentsForToday();
        if (appointmentList.isEmpty()) {
            noAppointmentsText.setVisibility(View.VISIBLE);
        } else {
            noAppointmentsText.setVisibility(View.GONE);
            AppointmentsAdapter appointmentsAdapter = new AppointmentsAdapter(appointmentList, this::onAppointmentAction);
            appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            appointmentsRecyclerView.setAdapter(appointmentsAdapter);
        }
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

    // Implement methods to get doctors, appointments, show doctor details, cancel, and reschedule appointments
}
