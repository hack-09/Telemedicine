package com.example.telemedicine.models;

public class Doctor {
    private String name;
    private String specialty;
    private String profilePictureUrl;

    // Constructor
    public Doctor(String name, String specialty, String profilePictureUrl) {
        this.name = name;
        this.specialty = specialty;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
