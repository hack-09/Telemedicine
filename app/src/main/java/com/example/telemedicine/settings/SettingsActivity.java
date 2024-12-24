package com.example.telemedicine.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telemedicine.R;
//import com.example.telemedicine.profile.EditProfileActivity;
import com.example.telemedicine.settings.PrivacyPolicyActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private TextView tabEditProfile, tabChangePassword, tabPrivacyPolicy;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        tabEditProfile = findViewById(R.id.tab_edit_profile);
        tabChangePassword = findViewById(R.id.tab_change_password);
        tabPrivacyPolicy = findViewById(R.id.tab_privacy_policy);

        // Set click listeners
        tabEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
            }
        });

        tabChangePassword.setOnClickListener(v-> showChangePasswordDialog());

        tabPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText oldPasswordEditText = dialogView.findViewById(R.id.old_password);
        EditText newPasswordEditText = dialogView.findViewById(R.id.new_password);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirm_password);

        builder.setTitle("Change Password");
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPassword = oldPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(getParent(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(getParent(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                changePassword(oldPassword, newPassword);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void changePassword(String oldPassword, final String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getParent(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getParent(), "Password change failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getParent(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
