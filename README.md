# Smart Campus API

JAX-RS RESTful API for Smart Campus Sensor & Room Management coursework (5COSC022W). Features rooms, sensors, sensor readings, filtering, sub-resources, exception mapping, and request/response logging.

## Tech Stack

- Java 17
- JAX-RS / Jersey 3.1.3
- Grizzly HTTP Server
- Jackson (JSON serialization)
- Maven
- In-memory storage (HashMap / ArrayList)

## Project Structure

```
src/main/java/com/smartcampus/
├── Main.java                          # Entry point – starts Grizzly server
├── config/
│   └── SmartCampusApplication.java    # JAX-RS ResourceConfig
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── store/
│   └── DataStore.java                 # Singleton in-memory data store
├── resource/
│   ├── DiscoveryResource.java         # GET /api/v1
│   ├── RoomResource.java              # /api/v1/rooms
│   ├── SensorResource.java            # /api/v1/sensors
│   └── SensorReadingResource.java     # /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── filter/
    ├── RequestLoggingFilter.java
    └── ResponseLoggingFilter.java
```

## Prerequisites

- Java 17+
- Maven 3.8+

## Build & Run

```bash
# Build
mvn clean compile

# Run the server (starts on http://localhost:8080)
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

The API will be available at **http://localhost:8080/api/v1/**

## API Endpoints

### Discovery

```bash
# Get API info with version, admin contact, and resource links
curl http://localhost:8080/api/v1/
```

### Rooms

```bash
# Get all rooms
curl http://localhost:8080/api/v1/rooms

# Create a room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Lab A", "capacity": 30}'

# Get a specific room
curl http://localhost:8080/api/v1/rooms/room-1

# Delete a room (fails with 409 if room has sensors)
curl -X DELETE http://localhost:8080/api/v1/rooms/room-1
```

### Sensors

```bash
# Get all sensors
curl http://localhost:8080/api/v1/sensors

# Filter sensors by type
curl "http://localhost:8080/api/v1/sensors?type=CO2"

# Create a sensor (roomId must exist, otherwise returns 422)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "currentValue": 400, "roomId": "room-1"}'
```

### Sensor Readings (Sub-resource)

```bash
# Get all readings for a sensor
curl http://localhost:8080/api/v1/sensors/sensor-1/readings

# Add a reading (updates sensor.currentValue; rejects if sensor is in MAINTENANCE with 403)
curl -X POST http://localhost:8080/api/v1/sensors/sensor-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 450.5}'
```

## Exception Handling

| Exception                      | HTTP Status | Description                                |
| ------------------------------ | ----------- | ------------------------------------------ |
| RoomNotEmptyException          | 409         | Delete room that still has sensors         |
| LinkedResourceNotFoundException| 422         | Create sensor with non-existent roomId     |
| SensorUnavailableException     | 403         | Add reading to sensor in MAINTENANCE mode  |
| Global Throwable mapper        | 500         | Any unhandled server error                 |

## Logging

Request and response logging is implemented using JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` with `java.util.logging.Logger`. All incoming requests and outgoing responses are logged automatically.
