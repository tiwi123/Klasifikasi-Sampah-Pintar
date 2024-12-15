package com.example.wasteclassification.model;

public class Waste {
    private String type; // Jenis sampah
    private double weight; // Berat sampah

    // Constructor
    public Waste(String type, double weight) {
        this.type = type;
        this.weight = weight;
    }

    // Getter dan Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}