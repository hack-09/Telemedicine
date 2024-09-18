package com.example.telemedicine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class ConsultationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consultation, container, false);

        // Handle window insets for edge-to-edge design
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button joinButton = view.findViewById(R.id.joinButton); // Assuming the button has id `joinButton`
        joinButton.setOnClickListener(v -> {
            try {
                onButtonClick(v);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        return view;
    }

    public void onButtonClick(View view) throws MalformedURLException {
        // Initialize editText with method findViewById()
        EditText editText = getView().findViewById(R.id.conferenceName);

        // Store the string input by user in a local variable
        String text = editText.getText().toString();

        // If user has typed some text in the EditText, then only the room will be created
        if (text.length() > 0) {
            // Creating a room using JitsiMeetConferenceOptions class
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(text)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setAudioMuted(true)
                    .setVideoMuted(true)
                    .build();

            // Launch the Jitsi Meet activity
            JitsiMeetActivity.launch(getContext(), options);
        }
    }
}