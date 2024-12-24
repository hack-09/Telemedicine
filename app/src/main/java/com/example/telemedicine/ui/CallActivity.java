package com.example.telemedicine.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import com.example.telemedicine.databinding.ActivityCallBinding;
import com.example.telemedicine.R;
import com.example.telemedicine.databinding.ActivityCallBinding;
import com.example.telemedicine.repository.MainRepository;
import com.example.telemedicine.utils.DataModelType;
import android.util.Log;

public class CallActivity extends AppCompatActivity implements MainRepository.Listener {

    private ActivityCallBinding views;
    private MainRepository mainRepository;
    private Boolean isCameraMuted = false;
    private Boolean isMicrophoneMuted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("CallActivity", "onCreate: CallActivity started");
        super.onCreate(savedInstanceState);
        views = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        init();
    }

    private void init() {
        Log.d("CallActivity", "Initializing views and repository");

        mainRepository = MainRepository.getInstance();
        if (mainRepository == null) {
            Log.e("CallActivity", "MainRepository is null");
            finish();
            return;
        }

        views.callBtn.setOnClickListener(v -> {
            //start a call request here
            mainRepository.sendCallRequest(views.targetUserNameEt.getText().toString(), () -> {
                Toast.makeText(this, "couldn't find the target", Toast.LENGTH_SHORT).show();
            });
        });

        if (views.localView == null || views.remoteView == null) {
            Log.e("CallActivity", "Views are not properly initialized");
            finish();
            return;
        }

        try {
            mainRepository.initLocalView(views.localView);
            mainRepository.initRemoteView(views.remoteView);
        } catch (Exception e) {
            Log.e("CallActivity", "Error initializing views", e);
            finish();
        }

        // Setting the listener to this activity
        mainRepository.setListener(this);

        // Handling incoming call requests
        mainRepository.subscribeForLatestEvent(data -> {
            if (data.getType() == DataModelType.StartCall) {
                runOnUiThread(() -> {
                    views.incomingNameTV.setText(String.format("%s is Calling you", data.getSender()));
                    views.incomingCallLayout.setVisibility(View.VISIBLE);
                    views.acceptButton.setOnClickListener(v -> {
                        // Start the call here
                        mainRepository.startCall(data.getSender());
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                    views.rejectButton.setOnClickListener(v -> {
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                });
            }
        });

        // Switch camera functionality
        views.switchCameraButton.setOnClickListener(v -> mainRepository.switchCamera());

        // Microphone mute/unmute functionality
        views.micButton.setOnClickListener(v -> {
            isMicrophoneMuted = !isMicrophoneMuted;
            views.micButton.setImageResource(isMicrophoneMuted ? R.drawable.ic_baseline_mic_off_24 : R.drawable.ic_baseline_mic_24);
            mainRepository.toggleAudio(isMicrophoneMuted);
        });

        // Camera mute/unmute functionality
        views.videoButton.setOnClickListener(v -> {
            isCameraMuted = !isCameraMuted;
            views.videoButton.setImageResource(isCameraMuted ? R.drawable.ic_baseline_videocam_off_24 : R.drawable.ic_baseline_videocam_24);
            mainRepository.toggleVideo(isCameraMuted);
        });

        // End call functionality
        views.endCallButton.setOnClickListener(v -> {
            mainRepository.endCall();
            finish();
        });
    }

    @Override
    public void webrtcConnected() {
        runOnUiThread(() -> {
            views.incomingCallLayout.setVisibility(View.GONE);
            views.whoToCallLayout.setVisibility(View.GONE);
            views.callLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void webrtcClosed() {
        runOnUiThread(this::finish);
    }
}
