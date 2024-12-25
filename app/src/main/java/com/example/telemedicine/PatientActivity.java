package com.example.telemedicine;

<<<<<<< HEAD
import static androidx.core.app.PendingIntentCompat.getActivity;

import android.app.AlertDialog;
=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
<<<<<<< HEAD

import com.example.telemedicine.authentication.SignInActivity;
import com.example.telemedicine.chat.ChatListFragment;
import com.example.telemedicine.doctor.DoctorAppointmentsFragment;
import com.example.telemedicine.patient.AppointmentsFragment;
import com.example.telemedicine.patient.BillingActivity;
import com.example.telemedicine.patient.DoctorListFragment;
import com.example.telemedicine.patient.HealthFragment;
import com.example.telemedicine.patient.HelpActivity;
import com.example.telemedicine.patient.MedicalRecordsActivity;
import com.example.telemedicine.patient.NotificationsActivity;
import com.example.telemedicine.patient.ProfileFragment;
import com.example.telemedicine.settings.SettingsActivity;
import com.example.telemedicine.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.Toast;
=======
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928

public class PatientActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navView;
<<<<<<< HEAD
    private FirebaseAuth auth;
=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navView = findViewById(R.id.nav_view);

<<<<<<< HEAD
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

<<<<<<< HEAD
                if (itemId == R.id.nav_appointments) {
                    loadFragment(new AppointmentsFragment());
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    loadFragment(new ChatListFragment());
                    return true;
                } else if (itemId == R.id.doctor_list) {
                    loadFragment(new DoctorListFragment());
=======
                if (itemId == R.id.nav_home) {
                    loadFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.nav_appointments) {
                    loadFragment(new AppointmentsFragment());
                    return true;
                } else if (itemId == R.id.nav_consultation) {
                    loadFragment(new ConsultationFragment());
                    return true;
                } else if (itemId == R.id.nav_health) {
                    loadFragment(new HealthFragment());
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }

<<<<<<< HEAD


=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
                return false;
            }

        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_medical_records) {
                    startActivity(new Intent(PatientActivity.this, MedicalRecordsActivity.class));
                } else if (itemId == R.id.nav_billing) {
                    startActivity(new Intent(PatientActivity.this, BillingActivity.class));
<<<<<<< HEAD
                }
                else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(PatientActivity.this, SettingsActivity.class));
                }
                else if (itemId == R.id.nav_logout) {
                    showLogoutDialog();
                }
//                else if (itemId == R.id.nav_help) {
//                    Toast.makeText(getParent(), "This feature is not defined yet.", Toast.LENGTH_SHORT).show();
////                    startActivity(new Intent(PatientActivity.this, HelpActivity.class));
//                }
=======
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(PatientActivity.this, NotificationsActivity.class));
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(PatientActivity.this, SettingsActivity.class));
                } else if (itemId == R.id.nav_help) {
                    startActivity(new Intent(PatientActivity.this, HelpActivity.class));
                }
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928

                drawerLayout.closeDrawers();
                return true;
            }
        });
<<<<<<< HEAD

        if (savedInstanceState == null) {
            loadFragment(new AppointmentsFragment()); // Default fragment
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (auth != null) {
                        auth.signOut(); // Sign out the user
                        Intent intent = new Intent(this, SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }
}
