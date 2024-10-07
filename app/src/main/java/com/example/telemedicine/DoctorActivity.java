package com.example.telemedicine;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.telemedicine.doctor.ChatFragment;
import com.example.telemedicine.doctor.ConsultationRemindersFragment;
import com.example.telemedicine.doctor.DoctorAppointmentsFragment;
import com.example.telemedicine.doctor.DoctorProfilesFragment;
import com.example.telemedicine.doctor.GenerateInvoiceFragment;
import com.example.telemedicine.doctor.ManageAvailabilityFragment;
import com.example.telemedicine.doctor.PatientMessagesFragment;
import com.example.telemedicine.doctor.PatientProfilesFragment;
import com.example.telemedicine.doctor.TrackEarningsFragment;
import com.example.telemedicine.patient.AppointmentsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class DoctorActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

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
                fragment = new AppointmentsFragment();
            } else if (item.getItemId() == R.id.nav_appointments) {
                fragment = new AppointmentsFragment();
            } else if (item.getItemId() == R.id.nav_manage_availability) {
                fragment = new ManageAvailabilityFragment();
            } else if (item.getItemId() == R.id.nav_track_earnings) {
                fragment = new TrackEarningsFragment();
            } else if (item.getItemId() == R.id.nav_generate_invoice) {
                fragment = new GenerateInvoiceFragment();
            } else if (item.getItemId() == R.id.nav_consultation_reminders) {
                fragment = new ConsultationRemindersFragment();
            } else if (item.getItemId() == R.id.nav_patient_messages) {
                fragment = new PatientMessagesFragment();
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
                    fragment = new AppointmentsFragment();
                } else if (item.getItemId() == R.id.bottom_navigation_chat) {
                    fragment = new ChatFragment();
                } else if (item.getItemId() == R.id.bottom_navigation_consultation) {
                    fragment = new ConsultationFragment();
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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
