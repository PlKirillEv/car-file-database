package org.example.model;

public class Car {
    private int id;
    private String brand;
    private String model;
    private int year;
    private double price;
    private boolean hadAccident;

    public Car(int id, String brand, String model, int year,
               double price, boolean hadAccident) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.price = price;
        this.hadAccident = hadAccident;
    }

    public int getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public double getPrice() {
        return price;
    }

    public boolean isHadAccident() {
        return hadAccident;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setHadAccident(boolean hadAccident) {
        this.hadAccident = hadAccident;
    }
}
