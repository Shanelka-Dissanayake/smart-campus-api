package com.smartcampus.model;

public class Sensor {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";

    private String id;
    private String type;
    private String status = STATUS_ACTIVE;
    private double currentValue;
    private String roomId;

    public Sensor() {
    }

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        setStatus(status);
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            this.status = STATUS_ACTIVE;
            return;
        }

        if (status.equalsIgnoreCase(STATUS_MAINTENANCE)) {
            this.status = STATUS_MAINTENANCE;
            return;
        }

        this.status = STATUS_ACTIVE;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
