package com.example.telemedicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import com.example.telemedicine.R;
import com.example.telemedicine.util.JitsiUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class ConsultationFragment extends Fragment {

    private Button videoCallButton, audioCallButton;
    private String appointmentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consultation, container, false);

        videoCallButton = view.findViewById(R.id.videoCallButton);
        audioCallButton = view.findViewById(R.id.audioCallButton);

        // Assuming appointmentId is passed through fragment arguments
        appointmentId = getArguments().getString("appointmentId", "defaultRoom");

        // Handle Video Call
        videoCallButton.setOnClickListener(v -> {
            JitsiUtils.startJitsiMeeting(getContext(), appointmentId);
        });

        // Handle Audio Call
        audioCallButton.setOnClickListener(v -> {
            // Start audio-only call by passing 'true' in JitsiUtils
            JitsiUtils.startJitsiMeeting(getContext(), appointmentId);
        });

        return view;
    }
}
