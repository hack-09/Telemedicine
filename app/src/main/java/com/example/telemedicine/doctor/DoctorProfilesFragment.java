package com.example.telemedicine.doctor;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telemedicine.R;
import com.example.telemedicine.authentication.SignInActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorProfilesFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileEmail, profilePhone, profileSpecialty, profileDOB;
    private Button logoutbtn;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private boolean isEditing = false;
    private ImageButton editProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_profiles, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.doctor_name);
        profileEmail = view.findViewById(R.id.doctor_email);
        profileSpecialty = view.findViewById(R.id.doctor_specialty);
        profilePhone = view.findViewById(R.id.doctor_phone);
        profileDOB = view.findViewById(R.id.doctor_dob);
        logoutbtn = view.findViewById(R.id.btn_logout);

        editProfile = view.findViewById(R.id.imageButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            loadProfileData();
        } else {
            // User is not authenticated, redirect to SignInActivity
            startActivity(new Intent(getActivity(), SignInActivity.class));
            getActivity().finish();
            return view;
        }

        editProfile.setOnClickListener(v -> toggleEditProfile());
        logoutbtn.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadProfileData() {
        DocumentReference doctorRef = db.collection("doctors").document(userId);

        doctorRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    profileName.setText(document.getString("name"));
                    profileEmail.setText(document.getString("email"));
                    profileSpecialty.setText(document.getString("specialty"));
                    profilePhone.setText(document.getString("phone"));
                    profileDOB.setText(document.getString("dateOfBirth"));
                    String profileImageUrl = document.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        profileImage.setImageURI(Uri.parse(profileImageUrl));
                    } else {
                        profileImage.setImageResource(R.drawable.profile);
                    }

                } else {
                    Toast.makeText(getActivity(), "No such document", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditProfile() {
        if (isEditing) {
            // Save changes
            saveProfileData();
        } else {
            // Switch to edit mode
            showEditProfileDialog();
        }
        isEditing = !isEditing;
    }


    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText dialogName = dialogView.findViewById(R.id.dialog_edit_name);
        EditText dialogEmail = dialogView.findViewById(R.id.dialog_edit_email);
        EditText dialogPhone = dialogView.findViewById(R.id.dialog_edit_phone);
        EditText dialogSpecialty = dialogView.findViewById(R.id.dialog_edit_specialty);
        EditText dobEditText = dialogView.findViewById(R.id.dobEditText);
        Button dialogSaveButton = dialogView.findViewById(R.id.dialog_save_button);

        // Set current profile data to dialog fields
        dialogName.setText(profileName.getText().toString());
        dialogEmail.setText(profileEmail.getText().toString());
        dialogPhone.setText(profilePhone.getText().toString());
        dialogSpecialty.setText(profileSpecialty.getText().toString());
        dobEditText.setText(profileDOB.getText().toString()); // Pre-fill DOB field

        // Handle DOB input with DatePickerDialog
        dobEditText.setOnClickListener(v -> {
            // Get the current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format and set the selected date
                        String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dobEditText.setText(dob);
                    },
                    year, month, day
            );

            // Optional: Set the maximum date to today (no future dates allowed)
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });

        builder.setTitle("Edit Profile");
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Save the updated data on button click
        dialogSaveButton.setOnClickListener(v -> {
            String newName = dialogName.getText().toString().trim();
            String newEmail = dialogEmail.getText().toString().trim();
            String newPhone = dialogPhone.getText().toString().trim();
            String newSpecialty = dialogSpecialty.getText().toString().trim();
            String newDob = dobEditText.getText().toString().trim();

            if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newEmail)) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                saveProfileData(newName, newEmail, newPhone, newSpecialty, newDob);
                dialog.dismiss();
            }
        });
    }

    private void saveProfileData(String name, String email, String phone, String specialty, String dateOfBirth) {
        DocumentReference doctorRef = db.collection("doctors").document(userId);
        doctorRef.update("name", name, "email", email, "phone", phone, "specialty", specialty, "dateOfBirth", dateOfBirth)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                    profileName.setText(name);
                    profileEmail.setText(email);
                    profilePhone.setText(phone);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("DoctorProfilesFragment", "Update failed", e);
                });
    }


    private void saveProfileData() {
        String name = profileName.getText().toString();
        String email = profileEmail.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference doctorRef = db.collection("doctors").document(userId);
        doctorRef.update("name", name, "email", email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                    profileName.setEnabled(false);
                    profileEmail.setEnabled(false);
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText oldPasswordEditText = dialogView.findViewById(R.id.old_password);
        EditText newPasswordEditText = dialogView.findViewById(R.id.new_password);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirm_password);

        builder.setTitle("Change Password");
        builder.setPositiveButton("Change", (dialog, which) -> {
            String oldPassword = oldPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(oldPassword, newPassword);
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void changePassword(String oldPassword, final String newPassword) {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Password change failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    startActivity(new Intent(getActivity(), SignInActivity.class));
                    getActivity().finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void openHelpSupport() {
        // Open help and support page
        Toast.makeText(getActivity(), "Help and Support clicked", Toast.LENGTH_SHORT).show();
    }
}
