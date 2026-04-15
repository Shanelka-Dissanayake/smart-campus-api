package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Sensor> sensors = new HashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    private int roomCounter = 0;
    private int sensorCounter = 0;
    private int readingCounter = 0;

    private DataStore() {
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Room operations
    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public Room addRoom(Room room) {
        roomCounter++;
        room.setId("room-" + roomCounter);
        rooms.put(room.getId(), room);
        return room;
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    // Sensor operations
    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public Sensor addSensor(Sensor sensor) {
        sensorCounter++;
        sensor.setId("sensor-" + sensorCounter);
        sensors.put(sensor.getId(), sensor);
        sensorReadings.put(sensor.getId(), new ArrayList<>());
        return sensor;
    }

    // Sensor reading operations
    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        readingCounter++;
        reading.setId("reading-" + readingCounter);
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        return reading;
    }
}
