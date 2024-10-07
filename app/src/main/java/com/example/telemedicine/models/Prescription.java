package com.example.telemedicine.models;

import com.google.firebase.Timestamp;

public class Prescription {
    private String id;
    private String doctorName;
    private String prescriptionUrl;
    private String notes;
    private Timestamp prescriptionDate;
    private String doctorId;
    private String patientId;
    private String appointmentId;

    public Prescription() {}

    public Prescription(String id, String doctorName, String prescriptionUrl,
                        String notes, Timestamp prescriptionDate, String doctorId, String patientId, String appointmentId) {
        this.id = id;
        this.doctorName = doctorName;
        this.prescriptionUrl = prescriptionUrl;
        this.notes = notes;
        this.prescriptionDate = prescriptionDate;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
    }

    // Getters and Setters for all fields

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPrescriptionUrl() {
        return prescriptionUrl;
    }

    public void setPrescriptionUrl(String prescriptionUrl) {
        this.prescriptionUrl = prescriptionUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(Timestamp prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
}
