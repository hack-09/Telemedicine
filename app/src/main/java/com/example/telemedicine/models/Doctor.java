package com.example.telemedicine.models;

<<<<<<< HEAD
import java.util.List;

public class Doctor {
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
=======
public class Doctor {
    private String name;
    private String specialty;
    private String profilePictureUrl;

    // Constructor
    public Doctor(String name, String specialty, String profilePictureUrl) {
        this.name = name;
        this.specialty = specialty;
        this.profilePictureUrl = profilePictureUrl;
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
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
<<<<<<< HEAD

    public String getFees(){ return fees; }

    public void setFees(String fees) {
        this.fees = fees;
    }
=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
}
