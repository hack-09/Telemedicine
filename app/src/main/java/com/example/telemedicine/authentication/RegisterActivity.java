package com.example.telemedicine.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telemedicine.DoctorActivity;
import com.example.telemedicine.PatientActivity;
import com.example.telemedicine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, contactEditText, specializationEditText;
    private RadioGroup userTypeRadioGroup;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // New layout for register

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        contactEditText = findViewById(R.id.contactEditText);
        specializationEditText = findViewById(R.id.specializationEditText);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        int selectedRoleId = userTypeRadioGroup.getCheckedRadioButtonId();

        if (selectedRoleId == -1) {
            Toast.makeText(RegisterActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        final String userType = selectedRoleButton.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserToFirestore(user.getUid(), name, userType, contact);
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToFirestore(String userId, String name, String userType, String contact) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("userType", userType);
        userData.put("contact", contact);

        // Save basic user data to 'users' collection
        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    if ("Doctor".equals(userType)) {
                        saveDoctorData(userId, name, contact);
                    } else if ("Patient".equals(userType)) {
                        savePatientData(userId, name, contact);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDoctorData(String userId, String name, String contact) {
        // Save doctor-specific data in the 'doctors' collection
        Map<String, Object> doctorData = new HashMap<>();
        doctorData.put("name", name);
        doctorData.put("contact", contact);
        doctorData.put("specialization", specializationEditText.getText().toString().trim());

        db.collection("doctors").document(userId).set(doctorData)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(RegisterActivity.this, DoctorActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Failed to save doctor data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void savePatientData(String userId, String name, String contact) {
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("name", name);
        patientData.put("contact", contact);

        db.collection("patients").document(userId).set(patientData)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(RegisterActivity.this, PatientActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Failed to save patient data.", Toast.LENGTH_SHORT).show();
                });
    }
}
