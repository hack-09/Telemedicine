package com.example.telemedicine;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

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
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(PatientActivity.this, NotificationsActivity.class));
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(PatientActivity.this, SettingsActivity.class));
                } else if (itemId == R.id.nav_help) {
                    startActivity(new Intent(PatientActivity.this, HelpActivity.class));
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }
}
