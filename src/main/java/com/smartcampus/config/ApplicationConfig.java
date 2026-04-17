package com.smartcampus.config;

import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationConfig extends ResourceConfig {

    public static final String BASE_PATH = "api/v1";

    public ApplicationConfig() {
        packages("com.smartcampus.resource", "com.smartcampus.exception", "com.smartcampus.filter");
    }
}
