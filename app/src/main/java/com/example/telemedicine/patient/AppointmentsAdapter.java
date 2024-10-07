package com.example.telemedicine.patient;

import android.content.Context;
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

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onCancelAppointment(Appointment appointment);
        void onJoinConsultation(Appointment appointment);
        void onVedioCall(Appointment appointment);
    }

    public AppointmentsAdapter(Context context, List<Appointment> appointments, OnAppointmentActionListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.doctorName.setText(appointment.getDoctorName());
        holder.slotTime.setText(appointment.getSlotTime());

        holder.joinButton.setOnClickListener(v -> listener.onJoinConsultation(appointment));
        holder.vedioCallBtn.setOnClickListener(v -> listener.onVedioCall(appointment));
        holder.cancelButton.setOnClickListener(v -> listener.onCancelAppointment(appointment));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, slotTime;
        Button cancelButton, joinButton, vedioCallBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            slotTime = itemView.findViewById(R.id.slotTime);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            joinButton = itemView.findViewById(R.id.joinButton);
            vedioCallBtn = itemView.findViewById(R.id.joinCall);
        }
    }
}
