package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.service.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId == null ? null : sensorId.trim();
    }

    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found"))
                    .build();
        }

        ensureSensorAvailable(sensor);

        List<SensorReading> readings;
        synchronized (DataStore.readings) {
            List<SensorReading> existing = DataStore.readings.get(sensorId);
            readings = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        }

        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found"))
                    .build();
        }

        ensureSensorAvailable(sensor);

        Response validationFailure = validateReadingPayload(reading);
        if (validationFailure != null) {
            return validationFailure;
        }

        synchronized (DataStore.readings) {
            DataStore.readings.computeIfAbsent(sensorId, key -> new ArrayList<>()).add(reading);
        }

        synchronized (DataStore.sensors) {
            Sensor liveSensor = DataStore.sensors.get(sensorId);
            if (liveSensor != null) {
                liveSensor.setCurrentValue(reading.getValue());
            }
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    private void ensureSensorAvailable(Sensor sensor) {
        if (sensor.getStatus() != null && sensor.getStatus().equalsIgnoreCase("MAINTENANCE")) {
            throw new SensorUnavailableException("Sensor is currently under maintenance");
        }
    }

    private Response validateReadingPayload(SensorReading reading) {
        if (reading == null) {
            return badRequest("Request body is required");
        }
        if (isBlank(reading.getId())) {
            return badRequest("Field 'id' is required");
        }
        if (reading.getTimestamp() <= 0) {
            return badRequest("Field 'timestamp' must be greater than zero");
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorBody(message))
                .build();
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
