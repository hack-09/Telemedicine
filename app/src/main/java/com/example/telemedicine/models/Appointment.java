package com.example.telemedicine.models;

public class Appointment {
    private String date;
    private String time;
    private Doctor doctor; // Assuming you have a Doctor class
    private String status; // e.g., "Scheduled", "Cancelled", "Completed"

    // Constructor
    public Appointment(String date, String time, Doctor doctor, String status) {
        this.date = date;
        this.time = time;
        this.doctor = doctor;
        this.status = status;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
