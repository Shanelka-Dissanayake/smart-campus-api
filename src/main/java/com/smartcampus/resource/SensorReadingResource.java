package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Instant;
import java.util.List;

@Path("/sensors/{sensorId}/readings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getReadings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new LinkedResourceNotFoundException("Sensor with id '" + sensorId + "' does not exist.");
        }
        List<SensorReading> readings = store.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(@PathParam("sensorId") String sensorId,
                               SensorReading reading,
                               @Context UriInfo uriInfo) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new LinkedResourceNotFoundException("Sensor with id '" + sensorId + "' does not exist.");
        }
        if (sensor.getStatus() == Sensor.SensorStatus.MAINTENANCE) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is in MAINTENANCE mode. Cannot accept readings.");
        }

        // Set timestamp if not provided
        if (reading.getTimestamp() == null || reading.getTimestamp().isEmpty()) {
            reading.setTimestamp(Instant.now().toString());
        }

        SensorReading created = store.addReading(sensorId, reading);

        // Update the sensor's current value
        sensor.setCurrentValue(created.getValue());

        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }
}
