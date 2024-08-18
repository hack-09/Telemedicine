package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private RadioGroup userTypeRadioGroup;
    private Button signInButton, registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        signInButton = findViewById(R.id.signInButton);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, check their user type and redirect
            checkUserType(currentUser.getUid());
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        checkUserType(user.getUid());
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        int selectedRoleId = userTypeRadioGroup.getCheckedRadioButtonId();

        if (selectedRoleId == -1) {
            Toast.makeText(SignInActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        final String userType = selectedRoleButton.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserType(user.getUid(), userType);
                    }
                    Toast.makeText(SignInActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignInActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserType(String userId, String userType) {
        Map<String, Object> user = new HashMap<>();
        user.put("userType", userType);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkUserType(userId);
                    }
                });
    }

    private void checkUserType(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userType = documentSnapshot.getString("userType");
                        if ("Doctor".equals(userType)) {
                            startActivity(new Intent(SignInActivity.this, DoctorActivity.class));
                        } else if ("Patient".equals(userType)) {
                            startActivity(new Intent(SignInActivity.this, PatientActivity.class));
                        } else {
                            Toast.makeText(SignInActivity.this, "Unknown user type.", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                });
    }

    // New method to handle the case where a patient wants to register as a doctor
    private void registerAsDoctor(String userId) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("userType", "Doctor");

        db.collection("users").document(userId).update(userUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignInActivity.this, "Registered as Doctor successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignInActivity.this, DoctorActivity.class));
                        finish();
                    }
                });
    }
}
