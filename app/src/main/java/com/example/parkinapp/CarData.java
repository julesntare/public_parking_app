package com.example.parkinapp;

public class CarData {
    private final int id;
    private final String plate;
    private final String phone;
    private final String time;
    private final String timeout;
    private final String names;
    private final String hours_used;
    private final String total_time;
    private final String pay;
    private final String carDetails;

    public CarData(int id, String plate, String phone, String time, String timeout, String names, String hours_used, String total_time, String pay, String carDetails) {
        this.id = id;
        this.plate = plate;
        this.time = time;
        this.timeout = timeout;
        this.names = names;
        this.hours_used = hours_used;
        this.total_time = total_time;
        this.pay = pay;
        this.phone = phone;
        this.carDetails = carDetails;
    }

    public int getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public String getTime() {
        return time;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getNames() {
        return names;
    }

    public String getHours_used() {
        return hours_used;
    }

    public String getTotal_time() {
        return total_time;
    }

    public String getPay() {
        return pay;
    }

    public String getPhone() {
        return phone;
    }

    public String getCarDetails() {
        return carDetails;
    }
}