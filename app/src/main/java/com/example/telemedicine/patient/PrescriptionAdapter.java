package com.example.telemedicine.patient;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Prescription;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {

    private List<Prescription> prescriptions;
    private Context context;

    public PrescriptionAdapter(List<Prescription> prescriptions, Context context) {
        this.prescriptions = prescriptions;
        this.context = context;
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        Prescription prescription = prescriptions.get(position);
        holder.doctorName.setText(prescription.getDoctorName());

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(prescription.getPrescriptionDate().toDate());
        holder.date.setText(formattedDate);

        holder.downloadButton.setOnClickListener(v -> {
            // Handle download functionality
            String url = prescription.getPrescriptionUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return prescriptions.size();
    }

    public static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, date;
        Button downloadButton;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            date = itemView.findViewById(R.id.date);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }

    private void downloadFile(Context context, String url, String fileName) {
        // Logic for downloading file
    }
}

