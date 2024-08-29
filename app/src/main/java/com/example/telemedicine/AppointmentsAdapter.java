package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.models.Appointment;

import java.util.List;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder> {
    private List<Appointment> appointments;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onAppointmentAction(Appointment appointment, String action);
    }

    public AppointmentsAdapter(List<Appointment> appointments, OnAppointmentActionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.timeTextView.setText(appointment.getSlotTime());
        holder.doctorNameTextView.setText(appointment.getDoctorName());
        holder.statusTextView.setText(appointment.getStatus());

        holder.itemView.setOnClickListener(v -> listener.onAppointmentAction(appointment, "view"));
        holder.cancelButton.setOnClickListener(v -> listener.onAppointmentAction(appointment, "cancel"));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView doctorNameTextView;
        TextView statusTextView;
        Button cancelButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.appointment_time);
            doctorNameTextView = itemView.findViewById(R.id.appointment_doctor_name);
            statusTextView = itemView.findViewById(R.id.appointment_status);
            cancelButton = itemView.findViewById(R.id.cancel_button);
        }
    }
}
