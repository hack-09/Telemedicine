package com.example.telemedicine.patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Doctor;

import java.util.List;

public class DoctorListAdapter extends RecyclerView.Adapter<DoctorListAdapter.ViewHolder> {

    private List<Doctor> doctorList;
    private Context context;
    private OnDoctorSelectedListener listener;

    // Constructor to initialize list, context, and listener
    public DoctorListAdapter(Context context, List<Doctor> doctorList, OnDoctorSelectedListener listener) {
        this.context = context;
        this.doctorList = doctorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for doctor list item
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the doctor from the list
        Doctor doctor = doctorList.get(position);

        // Set doctor details to views
        holder.doctorName.setText(doctor.getName());
        holder.doctorSpecialty.setText(doctor.getSpecialty());
        holder.doctorContact.setText(doctor.getId());

        // Use Glide or similar library to load image (optional)
//        Glide.with(context)
//                .load(doctor.getImageUrl()) // Assuming you have image URL for the doctor
//                .into(holder.doctorImage);

        // Handle item click
        holder.itemView.setOnClickListener(v -> listener.onDoctorSelected(doctor));
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, doctorSpecialty, doctorContact;
        ImageView doctorImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctor_name);
            doctorSpecialty = itemView.findViewById(R.id.doctorSpecialty);
            doctorContact = itemView.findViewById(R.id.doctorContact);
//            doctorImage = itemView.findViewById(R.id.doctor_image); // New image view
        }
    }

    // Interface to handle doctor item selection
    public interface OnDoctorSelectedListener {
        void onDoctorSelected(Doctor doctor);
    }
}
