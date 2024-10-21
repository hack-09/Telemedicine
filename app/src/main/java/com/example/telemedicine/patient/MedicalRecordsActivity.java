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

        prescriptionLayout = findViewById(R.id.prescriptionLayout);
        documentsLayout = findViewById(R.id.documentsLayout);
        prescriptionsRecyclerView = findViewById(R.id.prescriptionsRecyclerView);
        documentsRecyclerView = findViewById(R.id.documentsRecyclerView);
        uploadDocumentButton = findViewById(R.id.uploadDocumentButton);

        // Fetch the current user's ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Set up RecyclerViews
        setupRecyclerViews();

        // Fetch prescriptions and documents
        fetchPrescriptions();
        fetchDocuments();

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


        uploadDocumentButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");  // For PDF files, or use "image/*" for images
            startActivityForResult(intent, REQUEST_CODE_SELECT_DOCUMENT);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DOCUMENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            uploadDocument(fileUri, "Medical Report");
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
                .whereEqualTo("patientId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Prescription> prescriptionList = new ArrayList<>();
                        prescriptionList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Prescription prescription = document.toObject(Prescription.class);
                            fetchDoctorName(prescription, prescriptionList);
                        }
                    } else {
                        Log.e("Prescriptions", "Error getting prescriptions", task.getException());
                    }
                });
    }

    private void fetchDoctorName(Prescription prescription, List<Prescription> prescriptionList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Use doctorId to fetch doctor details directly from the doctors collection
        String doctorId = prescription.getDoctorId();
        if (doctorId == null) {
            Log.e("DoctorName", "Doctor ID is null. Cannot fetch doctor details.");
            return;
        }

        db.collection("doctors")
                .document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String doctorName = documentSnapshot.getString("name");
                        prescription.setDoctorName(doctorName);  // Set the doctor name in the prescription
                        prescriptionList.add(prescription);      // Add prescription after setting the name
                        updatePrescriptionList(prescriptionList); // Update UI after setting name
                    } else {
                        Log.e("DoctorName", "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DoctorName", "Error getting doctor name", e);
                });
    }

    private void updatePrescriptionList(List<Prescription> prescriptions) {
        if (prescriptions != null && !prescriptions.isEmpty()) {
            Log.d("PrescriptionList", "Number of prescriptions fetched: " + prescriptions.size());
            for (Prescription prescription : prescriptions) {
                Log.d("PrescriptionItem", "Prescription URL: " + prescription.getPrescriptionUrl());
            }
        } else {
            Log.d("PrescriptionList", "No prescriptions found.");
        }

        PrescriptionAdapter adapter = new PrescriptionAdapter(prescriptions, this);
        prescriptionsRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void onPrescriptionDownload(Prescription prescription) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(prescription.getPrescriptionUrl());

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Log.e("Download", "Failed to download prescription", e);
        });
    }

    private void fetchDocuments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("documents")
                .whereEqualTo("patientId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Document> documentList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            Document document = documentSnapshot.toObject(Document.class);
                            if (document != null) {
                                document.setId(documentSnapshot.getId()); // Set the document ID for deletion
                                document.setFileUrl(document.getFileUrl());
                                documentList.add(document);
                            }

                        }
                        documentAdapter.notifyDataSetChanged();
                        updateDocumentList(documentList);
                    } else {
                        Log.e("Documents", "Error getting documents", task.getException());
                    }
                });
    }

    private void updateDocumentList(List<Document> documents) {
        DocumentAdapter adapter = new DocumentAdapter(documents, this);
        documentsRecyclerView.setAdapter(adapter);
    }

    private void onDocumentDownload(Document document) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(document.getFileUrl());

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Log.e("Download", "Failed to download document", e);
        });
    }
    public void viewDocument(String url) {
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "application/pdf"); // MIME type is for PDF
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid document URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadDocument(Uri fileUri, String documentType) {
        if (fileUri != null) {
            String fileName = getFileName(fileUri);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("documents/" + System.currentTimeMillis() + ".pdf");

            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveDocumentMetadata(uri.toString(), documentType, fileName);
                            Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e("Upload", "Failed to retrieve download URL", e);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Upload", "Failed to upload document", e);
                        Toast.makeText(this, "Failed to upload document", Toast.LENGTH_SHORT).show();  // Notify user of failure.
                    });

        }
    }
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
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
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Document uploaded successfully", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to save document metadata", e);
                });
    }
}
