package com.example.telemedicine.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.example.telemedicine.databinding.ActivityLoginBinding;

import com.example.telemedicine.repository.MainRepository;
import com.google.android.exoplayer2.util.Log;
import com.permissionx.guolindev.PermissionX;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    private MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        mainRepository = MainRepository.getInstance();
        binding.enterBtn.setOnClickListener(v -> {
            PermissionX.init(this)
                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (!allGranted) {
                            Log.e("CallActivity", "Permissions not granted");
                            finish();
                            return;
                        }

                        if (allGranted) {

                            mainRepository.login(
                                    binding.username.getText().toString(), getApplicationContext(), () -> {
                                        //if success then we want to move to call activity
                                        startActivity(new Intent(LoginActivity.this, CallActivity.class));
                                    }
                            );
                        }
                    });


        });
    }
}