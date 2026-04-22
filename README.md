# Smart Campus API

A RESTful API developed using **JAX-RS (Jersey)** and **Grizzly HTTP Server** to simulate a smart campus system. The API allows management of rooms, sensors, and sensor readings, while enforcing real-world constraints such as device availability and data consistency.

## API Design Overview

This API follows RESTful design principles and is structured around three main resources:

### Rooms
- Represent physical locations on campus.
- Each room can contain multiple sensors.
- Supports creation, retrieval, and deletion.
- Cannot be deleted if sensors are still assigned (prevents data inconsistency).

### Sensors
- Represent IoT devices installed in rooms.
- Each sensor must be linked to a valid room.
- Supports filtering using query parameters (e.g., `?type=CO2`).
- Maintains a `status` (ACTIVE or MAINTENANCE).

### Sensor Readings
- Represent historical data recorded by sensors.
- Implemented using a **sub-resource pattern** (`/sensors/{id}/readings`).
- Adding a reading updates the sensor’s current value.
- Sensors in **MAINTENANCE mode cannot accept readings**.

## Architecture

- **Framework:** JAX-RS (Jersey)
- **Server:** Grizzly HTTP Server
- **Base Path:** `/api/v1`
- **Storage:** In-memory (`HashMap`, `ArrayList`)
- **Error Handling:** Custom exception mappers (409, 422, 403, 500)
- **Logging:** Implemented using JAX-RS filters

## How to Build and Run the Project

### Step 1: Open the Project
Navigate to the project folder in your terminal:

```bash
cd smart-campus-api
```

### Step 2: Build the Project
Compile and package the application using Maven:

```bash
mvn clean package
```

### Step 3: Run the Server

```bash
mvn exec:java
```

### Step 4: Access the API
Once the server starts, the API will be available at: http://localhost:8080/api/v1

## Sample cURL Commands

### 1. API Discovery Endpoint

```bash
curl http://localhost:8080/api/v1
```
### 2. Create a Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"R101","name":"Computer Lab","capacity":30}'
```

### 3. Get All Rooms

```bash
curl http://localhost:8080/api/v1/rooms
```

### 4. Create a Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"S-1","type":"CO2","roomId":"R101"}'
```

### 5. Add a Sensor Reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/S-1/readings \
-H "Content-Type: application/json" \
-d '{"id":"RD-1","value":45,"timestamp":1710000000}'
```

### 6. Get Sensor Readings

```bash
curl http://localhost:8080/api/v1/sensors/S-1/readings
```

### 7. Filter Sensors by Type

```bash
curl "http://localhost:8080/api/v1/sensors?type=CO2"
```
## Business Rules Implemented
- A room cannot be deleted if it has sensors assigned → 409 Conflict
- A sensor must reference an existing room → 422 Unprocessable Entity
- A sensor in MAINTENANCE mode cannot accept readings → 403 Forbidden
- Invalid input data returns appropriate 400 errors
- Unexpected errors are handled globally → 500 Internal Server Error

## Data Storage

All data is stored in-memory using Java collections:

- HashMap for rooms and sensors
- ArrayList for sensor readings

Data will reset when the server restarts (as no database is used).

## Testing

The API can be tested using:

- Postman
- cURL commands (provided above)

## Notes

- This project was developed strictly using JAX-RS as required.
- No frameworks like Spring Boot were used.
- No database was used.
