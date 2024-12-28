package com.example.telemedicine.patient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Document;
import com.example.telemedicine.models.Prescription;
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
import android.view.MenuItem;
import com.example.telemedicine.R;

public class MedicalRecordsActivity extends AppCompatActivity {

    private RecyclerView prescriptionsRecyclerView, documentsRecyclerView;
    private PrescriptionAdapter prescriptionAdapter;
    private DocumentAdapter documentAdapter;
    private List<Prescription> prescriptionList = new ArrayList<>();
    private List<Document> documentList = new ArrayList<>();
    private Button uploadDocumentButton;
    private String userId;
    private View prescriptionLayout, documentsLayout;
    private static final int REQUEST_CODE_SELECT_DOCUMENT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);

        initializeViews();
        setupFirebaseUser();
        setupRecyclerViews();
        fetchPrescriptions();
        fetchDocuments();

        setupBottomNavigation();

        uploadDocumentButton.setOnClickListener(v -> selectDocument());
    }

    private void initializeViews() {
        prescriptionLayout = findViewById(R.id.prescriptionLayout);
        documentsLayout = findViewById(R.id.documentsLayout);
        prescriptionsRecyclerView = findViewById(R.id.prescriptionsRecyclerView);
        documentsRecyclerView = findViewById(R.id.documentsRecyclerView);
        uploadDocumentButton = findViewById(R.id.uploadDocumentButton);
    }

    private void setupFirebaseUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e("FirebaseAuth", "User not logged in.");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.navigation_prescription) {
                switchToLayout(prescriptionLayout, documentsLayout);
                return true;
            }
            else if(item.getItemId()==R.id.navigation_documents) {
                switchToLayout(documentsLayout, prescriptionLayout);
                return true;
            }
            else{
                    return false;
            }
        });
    }

    private void switchToLayout(View showLayout, View hideLayout) {
        showLayout.setVisibility(View.VISIBLE);
        hideLayout.setVisibility(View.GONE);
    }

    private void selectDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_CODE_SELECT_DOCUMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DOCUMENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadDocument(data.getData(), "Medical Report");
        }
    }

    private void fetchPrescriptions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("prescriptions")
                .whereEqualTo("patientId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        prescriptionList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Prescription prescription = document.toObject(Prescription.class);
                            fetchDoctorName(prescription);
                        }
                    } else {
                        Log.e("Prescriptions", "Error getting prescriptions", task.getException());
                    }
                });
    }

    private void fetchDoctorName(Prescription prescription) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String doctorId = prescription.getDoctorId();
        if (doctorId != null) {
            db.collection("doctors").document(doctorId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            prescription.setDoctorName(documentSnapshot.getString("name"));
                            prescriptionList.add(prescription);
                            prescriptionAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("DoctorName", "Error getting doctor name", e));
        }
    }

    private void fetchDocuments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("documents")
                .whereEqualTo("patientId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        documentList.clear();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            Document document = documentSnapshot.toObject(Document.class);
                            if (document != null) {
                                document.setId(documentSnapshot.getId());
                                documentList.add(document);
                            }
                        }
                        documentAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Documents", "Error getting documents", task.getException());
                    }
                });
    }

    private void uploadDocument(Uri fileUri, String documentType) {
        if (fileUri != null) {
            String fileName = getFileName(fileUri);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("documents/" + System.currentTimeMillis() + ".pdf");

            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveDocumentMetadata(uri.toString(), documentType, fileName);
                        Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("Upload", "Failed to upload document", e);
                        Toast.makeText(this, "Failed to upload document", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result == null ? uri.getLastPathSegment() : result;
    }

    private void saveDocumentMetadata(String fileUrl, String documentType, String fileName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("patientId", userId);
        documentData.put("fileUrl", fileUrl);
        documentData.put("documentType", documentType);
        documentData.put("fileName", fileName);
        documentData.put("uploadDate", new Date());

        db.collection("documents").add(documentData)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Document metadata saved."))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to save document metadata", e));
    }
}
