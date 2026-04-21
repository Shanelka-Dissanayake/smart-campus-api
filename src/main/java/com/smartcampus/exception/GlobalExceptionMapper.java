package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webException) {
            int status = webException.getResponse() != null
                ? webException.getResponse().getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

            Map<String, String> error = new HashMap<>();
            error.put("error", status >= 500 ? "Internal server error" : "Invalid request");

            return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal server error");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
