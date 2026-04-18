package com.smartcampus.service;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private Map<String, Room> rooms;
    private Map<String, Sensor> sensors;
    private Map<String, List<SensorReading>> sensorReadings;

    public DataStore() {
        this.rooms = new HashMap<>();
        this.sensors = new HashMap<>();
        this.sensorReadings = new HashMap<>();
    }

    public DataStore(Map<String, Room> rooms,
                     Map<String, Sensor> sensors,
                     Map<String, List<SensorReading>> sensorReadings) {
        this.rooms = rooms != null ? rooms : new HashMap<>();
        this.sensors = sensors != null ? sensors : new HashMap<>();
        this.sensorReadings = sensorReadings != null ? sensorReadings : new HashMap<>();
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, Room> rooms) {
        this.rooms = rooms != null ? rooms : new HashMap<>();
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(Map<String, Sensor> sensors) {
        this.sensors = sensors != null ? sensors : new HashMap<>();
    }

    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }

    public void setSensorReadings(Map<String, List<SensorReading>> sensorReadings) {
        this.sensorReadings = sensorReadings != null ? sensorReadings : new HashMap<>();
    }
}
