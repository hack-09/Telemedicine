package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private ProgressBar progressBar;
    private LinearLayout loginFormLayout, loadingLayout;
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
        progressBar = findViewById(R.id.progressBar);
        loginFormLayout = findViewById(R.id.loginFormLayout);
        loadingLayout = findViewById(R.id.loadingLayout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, show the loading layout and fetch user data
            showLoadingLayout();
            checkUserType(currentUser.getUid());
        } else {
            // No user is logged in, show the login form layout
            showLoginFormLayout();
        }

        signInButton.setOnClickListener(view -> signInUser());

        registerButton.setOnClickListener(view -> registerUser());
    }

    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Show loading layout when sign-in starts
        showLoadingLayout();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    checkUserType(user.getUid());
                }
            } else {
                showLoginFormLayout();
                Toast.makeText(SignInActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
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

        // Show loading layout when registration starts
        showLoadingLayout();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserType(user.getUid(), userType);
                }
                Toast.makeText(SignInActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
            } else {
                showLoginFormLayout();
                Toast.makeText(SignInActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserType(String userId, String userType) {
        Map<String, Object> user = new HashMap<>();
        user.put("userType", userType);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    checkUserType(userId);
                })
                .addOnFailureListener(e -> {
                    showLoginFormLayout();
                    Toast.makeText(SignInActivity.this, "Error saving user type.", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserType(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userType = documentSnapshot.getString("userType");
                    if ("Doctor".equals(userType)) {
                        startActivity(new Intent(SignInActivity.this, DoctorActivity.class));
                    } else if ("Patient".equals(userType)) {
                        startActivity(new Intent(SignInActivity.this, PatientActivity.class));
                    } else {
                        Toast.makeText(SignInActivity.this, "Unknown user type.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoginFormLayout();
                    Toast.makeText(SignInActivity.this, "Failed to check user type.", Toast.LENGTH_SHORT).show();
                });
    }

    // Utility methods to show or hide layouts
    private void showLoadingLayout() {
        loadingLayout.setVisibility(View.VISIBLE);
        loginFormLayout.setVisibility(View.GONE);
    }

    private void showLoginFormLayout() {
        loadingLayout.setVisibility(View.GONE);
        loginFormLayout.setVisibility(View.VISIBLE);
    }
}
