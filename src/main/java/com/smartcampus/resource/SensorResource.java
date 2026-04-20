package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.service.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getAllSensors() {
        List<Sensor> sensors;
        synchronized (DataStore.sensors) {
            sensors = new ArrayList<>(DataStore.sensors.values());
        }
        return Response.ok(sensors).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        Response validationFailure = validateSensorPayload(sensor);
        if (validationFailure != null) {
            return validationFailure;
        }

        String sensorId = sensor.getId().trim();
        String roomId = sensor.getRoomId().trim();

        synchronized (DataStore.rooms) {
            Room room = DataStore.rooms.get(roomId);
            if (room == null) {
                return badRequest("Field 'roomId' must reference an existing room");
            }

            synchronized (DataStore.sensors) {
                if (DataStore.sensors.containsKey(sensorId)) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(errorBody("Sensor already exists"))
                            .build();
                }

                sensor.setId(sensorId);
                sensor.setRoomId(roomId);
                DataStore.sensors.put(sensorId, sensor);
            }

            if (room.getSensorIds() == null) {
                room.setSensorIds(new ArrayList<>());
            }
            if (!room.getSensorIds().contains(sensorId)) {
                room.getSensorIds().add(sensorId);
            }
        }

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = DataStore.sensors.get(id);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found"))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSensor(@PathParam("id") String id) {
        String sensorId = id == null ? null : id.trim();
        if (isBlank(sensorId)) {
            return badRequest("Path parameter 'id' is required");
        }

        Sensor removedSensor;
        synchronized (DataStore.sensors) {
            removedSensor = DataStore.sensors.remove(sensorId);
        }

        if (removedSensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found"))
                    .build();
        }

        String roomId = removedSensor.getRoomId();
        if (!isBlank(roomId)) {
            synchronized (DataStore.rooms) {
                Room room = DataStore.rooms.get(roomId);
                if (room != null && room.getSensorIds() != null) {
                    room.getSensorIds().remove(sensorId);
                }
            }
        }

        return Response.noContent().build();
    }

    private Response validateSensorPayload(Sensor sensor) {
        if (sensor == null) {
            return badRequest("Request body is required");
        }
        if (isBlank(sensor.getId())) {
            return badRequest("Field 'id' is required");
        }
        if (isBlank(sensor.getRoomId())) {
            return badRequest("Field 'roomId' is required");
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
