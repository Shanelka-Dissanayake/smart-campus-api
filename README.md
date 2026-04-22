# Smart Campus API

A RESTful API developed using **JAX-RS (Jersey)** and **Grizzly HTTP Server** to simulate a smart campus system. The API allows management of rooms, sensors, and sensor readings, while enforcing real-world constraints such as device availability and data consistency.

## API Design Overview

This API follows RESTful design principles and is structured around three main resources:

### 🔹 Rooms
- Represent physical locations on campus.
- Each room can contain multiple sensors.
- Supports creation, retrieval, and deletion.
- Cannot be deleted if sensors are still assigned (prevents data inconsistency).

### 🔹 Sensors
- Represent IoT devices installed in rooms.
- Each sensor must be linked to a valid room.
- Supports filtering using query parameters (e.g., `?type=CO2`).
- Maintains a `status` (ACTIVE or MAINTENANCE).

### 🔹 Sensor Readings
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

## ▶️ How to Build and Run the Project

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
Once the server starts, the API will be available at:

http://localhost:8080/api/v1

