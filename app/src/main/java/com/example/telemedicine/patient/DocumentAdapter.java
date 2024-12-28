package com.example.telemedicine.patient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;
import com.example.telemedicine.models.Document;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documents;
    private Context context; // Use Context instead of MedicalRecordsActivity

    public DocumentAdapter(List<Document> documents, Context context) {
        this.documents = documents;
        this.context = context;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.fileName.setText(document.getFileName());

        // Formatting the upload date
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(document.getUploadDate().toDate());
        holder.uploadDate.setText(formattedDate);

        // Set the onClick listener for the view button
        holder.viewButton.setOnClickListener(v -> {
            String url = document.getFileUrl();
            viewDocument(url); // Call a method to view the document
        });

        holder.deleteButton.setOnClickListener(v -> {
            String documentId = document.getId();
            deleteDocument(documentId);
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, uploadDate;
        Button viewButton, deleteButton;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            uploadDate = itemView.findViewById(R.id.uploadDate);
            viewButton = itemView.findViewById(R.id.viewButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // View document method using context to start an intent
    private void viewDocument(String url) {
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Invalid document URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDocument(String documentId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to delete this document?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Get document reference from Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("documents").document(documentId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Get the file URL (or file name) from the document
                                    String fileUrl = documentSnapshot.getString("fileUrl");

                                    // Delete the file from Firebase Storage
                                    if (fileUrl != null && !fileUrl.isEmpty()) {
                                        FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Delete the document from Firestore
                                                    db.collection("documents").document(documentId)
                                                            .delete()
                                                            .addOnSuccessListener(aVoid2 -> {
                                                                Toast.makeText(context, "Document deleted successfully", Toast.LENGTH_SHORT).show();
                                                                documents.removeIf(doc -> doc.getId().equals(documentId));
                                                                notifyDataSetChanged();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(context, "Failed to delete document from Firestore", Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to delete document from Firebase Storage", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(context, "Invalid file URL", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "Document not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to get document details", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
