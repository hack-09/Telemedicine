package com.example.telemedicine.doctor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Document;
import com.example.telemedicine.models.Prescription;
import com.example.telemedicine.patient.DocumentAdapter;
import com.example.telemedicine.patient.PrescriptionAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorRecordsActivity extends AppCompatActivity {

    private RecyclerView prescriptionsRecyclerView, documentsRecyclerView;
    private PrescriptionAdapter prescriptionAdapter;
    private DocumentAdapter documentAdapter;
    private List<Prescription> prescriptionList = new ArrayList<>();
    private List<Document> documentList = new ArrayList<>();
    private Button uploadPrescriptionButton;
    private String userId, patientId;
    private View prescriptionLayout, documentsLayout;
    private static final int REQUEST_CODE_SELECT_PRESCRIPTION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_records);

        prescriptionLayout = findViewById(R.id.prescriptionLayout);
        documentsLayout = findViewById(R.id.documentsLayout);
        prescriptionsRecyclerView = findViewById(R.id.prescriptionsRecyclerView);
        documentsRecyclerView = findViewById(R.id.documentsRecyclerView);
        uploadPrescriptionButton = findViewById(R.id.uploadPrescriptionButton);

        // Fetch the current doctor's ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        patientId = getIntent().getStringExtra("patientId");

        // Set up RecyclerViews
        setupRecyclerViews();

        // Fetch prescriptions and documents
        fetchPrescriptions();
        fetchPatientDocuments();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();  // Get the selected item ID

            if (itemId == R.id.navigation_prescription) {
                prescriptionLayout.setVisibility(View.VISIBLE);
                documentsLayout.setVisibility(View.GONE);
                return true;


            } else if (itemId == R.id.navigation_documents) {
                documentsLayout.setVisibility(View.VISIBLE);
                prescriptionLayout.setVisibility(View.GONE);
                return true;
            }

            return false;  // Return false if none of the items match
        });

        uploadPrescriptionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");  // For PDF prescriptions
            startActivityForResult(intent, REQUEST_CODE_SELECT_PRESCRIPTION);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_PRESCRIPTION && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            uploadPrescription(fileUri, "Prescription");
        }
    }

    private void setupRecyclerViews() {
        prescriptionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        prescriptionAdapter = new PrescriptionAdapter(prescriptionList, this);
        prescriptionsRecyclerView.setAdapter(prescriptionAdapter);

        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        documentAdapter = new DocumentAdapter(documentList, this);
        documentsRecyclerView.setAdapter(documentAdapter);
    }

    private void fetchPrescriptions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("prescriptions")
                .whereEqualTo("doctorId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Prescription> prescriptionList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Prescription prescription = document.toObject(Prescription.class);
                            prescriptionList.add(prescription);
                        }
                        updatePrescriptionList(prescriptionList);
                    } else {
                        Log.e("Prescriptions", "Error getting prescriptions", task.getException());
                    }
                });
    }

    private void updatePrescriptionList(List<Prescription> prescriptions) {
        PrescriptionAdapter adapter = new PrescriptionAdapter(prescriptions, this);
        prescriptionsRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void fetchPatientDocuments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("documents")
                .whereEqualTo("patientId", patientId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Document> documentList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            Log.d("DocumentFetch", "Document ID: " + documentSnapshot.getId());  // Debug log
                            Document document = documentSnapshot.toObject(Document.class);
                            if (document != null) {
                                document.setId(documentSnapshot.getId());
                                document.setFileUrl(document.getFileUrl());
                                documentList.add(document);
                            }
                        }
                        updateDocumentList(documentList);
                    } else {
                        Log.e("Documents", "Error getting documents", task.getException());
                    }
                });

    }

    private void updateDocumentList(List<Document> documents) {
        documentList.clear();
        documentList.addAll(documents);
        documentAdapter.notifyDataSetChanged();
    }

    private void uploadPrescription(Uri fileUri, String prescriptionType) {
        if (fileUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("prescriptions/" + System.currentTimeMillis() + ".pdf");

            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            savePrescriptionMetadata(uri.toString(), prescriptionType);
                        }).addOnFailureListener(e -> {
                            Log.e("Upload", "Failed to retrieve download URL", e);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Upload", "Failed to upload prescription", e);
                        Toast.makeText(this, "Failed to upload prescription", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void savePrescriptionMetadata(String fileUrl, String prescriptionType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> prescriptionData = new HashMap<>();
        prescriptionData.put("doctorId", userId);
        prescriptionData.put("patientId", patientId);
        prescriptionData.put("prescriptionUrl", fileUrl);
        prescriptionData.put("notes", "NULL");
        prescriptionData.put("prescriptionDate", new Date());

        db.collection("prescriptions").add(prescriptionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Prescription saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to save prescription metadata", e);
                });
    }
}
