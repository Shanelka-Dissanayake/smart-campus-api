package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {

    public static final String BASE_PATH = "api/v1";

    public ApplicationConfig() {
        packages("com.smartcampus.resource", "com.smartcampus.exception", "com.smartcampus.filter");
        register(JacksonFeature.class);
    }
}
