package com.example.telemedicine.models;

public class Slot {
    private String id;  // ID of the slot in the map
    private String date;
    private String time;
    private boolean isBooked;

    public Slot(String id, String date, String time, boolean isBooked) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.isBooked = isBooked;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}
