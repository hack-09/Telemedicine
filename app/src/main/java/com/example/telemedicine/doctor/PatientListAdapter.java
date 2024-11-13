package com.example.telemedicine.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemedicine.R;
import java.util.List;

public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.PatientViewHolder> {

    private List<PatientProfile> patientList;

    public PatientListAdapter(List<PatientProfile> patientList) {
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientProfile patient = patientList.get(position);

        holder.patientName.setText(patient.getName());
        holder.patientAge.setText("Age: " + patient.getAge());
        holder.patientGender.setText("Gender: " + patient.getGender());

        holder.itemView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
            PatientProfilesFragment fragment = new PatientProfilesFragment(patient.getId());
            Bundle args = new Bundle();
            args.putString("patientId", patient.getId());
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientName, patientAge, patientGender;

        public PatientViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patientName);
            patientAge = itemView.findViewById(R.id.patientAge);
            patientGender = itemView.findViewById(R.id.patientGender);
        }
    }
}
