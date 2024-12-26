package com.example.telemedicine.models;
import java.io.Serializable;
import java.util.List;

public class Doctor implements Serializable {
    private String id;
    private String name;
    private String specialty;
    private String profilePictureUrl;
    private String fees;

    public Doctor(String id, String name, String specialty, String profileImageUrl, String fees) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.profilePictureUrl = profileImageUrl;
        this.fees = fees;
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

    public String getFees(){ return fees; }

    public void setFees(String fees) {
        this.fees = fees;
    }
}
