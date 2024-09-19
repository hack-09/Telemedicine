package com.example.telemedicine.models;

public class Appointment {
    private String appointmentId;
    private String doctorId;
    private String doctorName;
    private String patientId;
    private String slotTime;
    private String status;
    private String patientName;

    // No-argument constructor
    public Appointment() {
    }

    // Constructor with parameters
    public Appointment(String appointmentId, String doctorId, String doctorName, String patientId, String slotTime, String status, String patientName) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.slotTime = slotTime;
        this.status = status;
        this.patientName=patientName;
    }

    // Getters and Setters
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    public String getAppointmentId() {
        return appointmentId;
    }
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientName() {
        return patientName;
    }
}
