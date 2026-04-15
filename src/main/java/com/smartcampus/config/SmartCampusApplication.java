package com.smartcampus.config;

import org.glassfish.jersey.server.ResourceConfig;

public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        packages("com.smartcampus.resource",
                 "com.smartcampus.exception",
                 "com.smartcampus.filter");
    }
}
