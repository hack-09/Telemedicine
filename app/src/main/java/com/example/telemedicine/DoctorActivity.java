package com.example.telemedicine;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.telemedicine.authentication.SignInActivity;
import com.example.telemedicine.chat.ChatFragment;
import com.example.telemedicine.chat.ChatListFragment;
import com.example.telemedicine.doctor.ConsultationRemindersFragment;
import com.example.telemedicine.doctor.DoctorAppointmentsFragment;
import com.example.telemedicine.doctor.DoctorProfilesFragment;
import com.example.telemedicine.doctor.GenerateInvoiceFragment;
import com.example.telemedicine.doctor.ManageAvailabilityFragment;
import com.example.telemedicine.doctor.PatientListFragment;
import com.example.telemedicine.doctor.PatientMessagesFragment;
import com.example.telemedicine.doctor.PatientProfilesFragment;
import com.example.telemedicine.doctor.TrackEarningsFragment;
import com.example.telemedicine.patient.AppointmentsFragment;
import com.example.telemedicine.settings.HelpAndSupport;
import com.example.telemedicine.settings.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DoctorActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        fragmentManager = getSupportFragmentManager();

        // Set up drawer menu item click listeners
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            // Navigation Drawer Item Click Listener
            if (item.getItemId() == R.id.nav_patient_profiles) {
                fragment = new PatientListFragment();
            } else if (item.getItemId() == R.id.nav_help) {
                startActivity(new Intent(DoctorActivity.this, HelpAndSupport.class));
            } else if (item.getItemId() == R.id.nav_manage_availability) {
                fragment = new ManageAvailabilityFragment();
            } else if (item.getItemId() == R.id.nav_track_earnings) {
                fragment = new TrackEarningsFragment();
            } else if (item.getItemId() == R.id.nav_logout) {
                showLogoutDialog();
            }else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(DoctorActivity.this, SettingsActivity.class));
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        // Set up bottom navigation item click listeners
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                if (item.getItemId() == R.id.doctor_bottom_navigation_appointments) {
                    fragment = new DoctorAppointmentsFragment();
                } else if (item.getItemId() == R.id.bottom_navigation_profiles) {
                    fragment = new DoctorProfilesFragment();
                } else if (item.getItemId() == R.id.bottom_navigation_patient_queue) {
                    fragment = new PatientListFragment();
                } else if (item.getItemId() == R.id.bottom_navigation_chat) {
                    fragment = new ChatListFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                }
                return true;
            }
        });

        if (savedInstanceState == null) {
            loadFragment(new DoctorAppointmentsFragment()); // Default fragment
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
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
