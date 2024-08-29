package com.example.telemedicine.models;

import java.util.List;

public class Doctor {
    private String id;
    private String name;
    private String specialty;
    private String profilePictureUrl;

    public Doctor(String id, String name, String specialty, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.profilePictureUrl = profileImageUrl;
    }

    // Add getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
