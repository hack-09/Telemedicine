package com.example.telemedicine.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Appointment;

import java.util.List;

public class DoctorAppointmentsAdapter extends RecyclerView.Adapter<DoctorAppointmentsAdapter.ViewHolder> {
    private List<Appointment> appointments;
    private OnDoctorAppointmentActionListener listener;


    public interface OnDoctorAppointmentActionListener {
        void onJoinConsultation(Appointment appointment);
        void onViewPatientProfile(Appointment appointment);
        void onVedioCall(Appointment appointment);
    }

    public DoctorAppointmentsAdapter(List<Appointment> appointments, OnDoctorAppointmentActionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView appointmentInfo;
        private Button joinButton, vedioCallBtn;
        private TextView viewProfileButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentInfo = itemView.findViewById(R.id.textAppointmentInfo);
            joinButton = itemView.findViewById(R.id.buttonJoinConsultation);
            vedioCallBtn = itemView.findViewById(R.id.joinCall);
            viewProfileButton = itemView.findViewById(R.id.buttonViewPatientProfile);
        }

        public void bind(Appointment appointment) {
            // Null check to avoid crashes if any field is null
            String patientName = appointment.getPatientName() != null ? appointment.getPatientName() : "Unknown";
            String slotTime = appointment.getSlotTime() != null ? appointment.getSlotTime() : "Unknown time";

            // Bind appointment details
            appointmentInfo.setText("Appointment with " + patientName + " at " + slotTime);

            joinButton.setOnClickListener(v -> listener.onJoinConsultation(appointment));
            vedioCallBtn.setOnClickListener(v -> listener.onVedioCall(appointment));
            viewProfileButton.setOnClickListener(v -> listener.onViewPatientProfile(appointment));
        }

    }
}
