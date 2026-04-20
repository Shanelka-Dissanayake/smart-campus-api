package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response getAllRooms() {
        List<Room> rooms;
        synchronized (DataStore.rooms) {
            rooms = new ArrayList<>(DataStore.rooms.values());
        }
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Response validationFailure = validateRoomPayload(room);
        if (validationFailure != null) {
            return validationFailure;
        }

        String roomId = room.getId().trim();
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        synchronized (DataStore.rooms) {
            if (DataStore.rooms.containsKey(roomId)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(errorBody("Room already exists"))
                        .build();
            }
            room.setId(roomId);
            room.setName(room.getName().trim());
            DataStore.rooms.put(roomId, room);
        }

        URI location = UriBuilder.fromUri(uriInfo.getAbsolutePath()).path(roomId).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        Room room = DataStore.rooms.get(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room not found"))
                    .build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        String roomId = id == null ? null : id.trim();
        if (isBlank(roomId)) {
            return badRequest("Path parameter 'id' is required");
        }

        Room room;
        synchronized (DataStore.rooms) {
            room = DataStore.rooms.get(roomId);
            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorBody("Room not found"))
                        .build();
            }

            if (hasSensors(roomId, room)) {
                throw new RoomNotEmptyException("Room contains sensors and cannot be deleted");
            }

            DataStore.rooms.remove(roomId);
        }
        return Response.noContent().build();
    }

    private boolean hasSensors(String roomId, Room room) {
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            return true;
        }

        synchronized (DataStore.sensors) {
            for (Sensor sensor : DataStore.sensors.values()) {
                if (roomId.equals(sensor.getRoomId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Response validateRoomPayload(Room room) {
        if (room == null) {
            return badRequest("Request body is required");
        }
        if (isBlank(room.getId())) {
            return badRequest("Field 'id' is required");
        }
        if (isBlank(room.getName())) {
            return badRequest("Field 'name' is required");
        }
        if (room.getCapacity() < 0) {
            return badRequest("Field 'capacity' must be zero or greater");
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