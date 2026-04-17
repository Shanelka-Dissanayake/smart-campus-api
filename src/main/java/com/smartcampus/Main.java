package com.smartcampus;

import com.smartcampus.config.ApplicationConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public final class Main {

    private static final String BASE_URI = "http://localhost:8080/";

    private Main() {
    }

    public static HttpServer startServer() {
        URI baseUri = URI.create(BASE_URI);
        return GrizzlyHttpServerFactory.createHttpServer(baseUri, new ApplicationConfig());
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        System.out.println("Smart Campus API started at http://localhost:8080/api/v1");
        System.out.println("Press Enter to stop the server...");
        System.in.read();

        server.shutdownNow();
    }
}
