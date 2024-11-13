package com.example.telemedicine.models;

public class DoctorProfile {
    private String id;
    private String name;
    private String specialty;
    private String contact;

    public DoctorProfile(String id, String name, String specialty, String contact) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.contact = contact;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getContact() {
        return contact;
    }
}

