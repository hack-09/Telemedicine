package com.example.telemedicine;

import static androidx.core.app.PendingIntentCompat.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.telemedicine.chat.ChatListFragment;
import com.example.telemedicine.doctor.DoctorAppointmentsFragment;
import com.example.telemedicine.patient.AppointmentsFragment;
import com.example.telemedicine.patient.BillingActivity;
import com.example.telemedicine.patient.DoctorListFragment;
import com.example.telemedicine.patient.HealthFragment;
import com.example.telemedicine.patient.HelpActivity;
import com.example.telemedicine.patient.HomeFragment;
import com.example.telemedicine.patient.MedicalRecordsActivity;
import com.example.telemedicine.patient.NotificationsActivity;
import com.example.telemedicine.patient.ProfileFragment;
import com.example.telemedicine.patient.SettingsActivity;
import com.example.telemedicine.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import android.widget.Toast;

public class PatientActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navView = findViewById(R.id.nav_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_appointments) {
                    loadFragment(new AppointmentsFragment());
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    loadFragment(new ChatListFragment());
                    return true;
                } else if (itemId == R.id.nav_health) {
                    loadFragment(new DoctorListFragment());
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }



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
                }
//                else if (itemId == R.id.nav_settings) {
//                    Toast.makeText(getParent(), "This feature is not defined yet.", Toast.LENGTH_SHORT).show();
//                } else if (itemId == R.id.nav_help) {
//                    Toast.makeText(getParent(), "This feature is not defined yet.", Toast.LENGTH_SHORT).show();
////                    startActivity(new Intent(PatientActivity.this, HelpActivity.class));
//                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        if (savedInstanceState == null) {
            loadFragment(new AppointmentsFragment()); // Default fragment
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }
}
