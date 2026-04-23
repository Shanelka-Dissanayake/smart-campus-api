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

# Part 1: Service Architecture & Setup

## 1. Project & Application Configuration

### JAX-RS Resource Lifecycle

In this implementation, JAX-RS resources behave as **per-request instances**. This is because none of the resource classes are annotated with `@Singleton`, and Jersey’s default lifecycle creates a new instance for each incoming HTTP request.

This design has important implications for how in-memory data is managed. Since each request gets a new resource object, the application cannot rely on instance variables to persist state. Instead, shared data is stored in centralized static structures within the `DataStore` class, specifically using `HashMap` collections for rooms, sensors, and readings.

However, this introduces potential concurrency issues because multiple requests may access or modify these shared maps simultaneously. To mitigate this, the implementation uses `synchronized` blocks around critical operations such as inserting or deleting data. This helps prevent race conditions and ensures consistency, although it is a basic form of concurrency control.

Overall, the per-request lifecycle promotes statelessness at the resource level, while requiring careful handling of shared in-memory data to avoid inconsistencies.

## 2. The Discovery Endpoint

### HATEOAS and Hypermedia

Hypermedia (HATEOAS) is considered a hallmark of advanced RESTful design because it allows clients to dynamically discover available actions through links provided in API responses, rather than relying on hardcoded knowledge of endpoints.

In this project, the discovery endpoint (`GET /api/v1`) provides basic API metadata and links to primary resources such as rooms and sensors. This approach improves usability by guiding the client through the API structure.

Compared to static documentation, hypermedia-driven APIs are more flexible and resilient to change. For example, if an endpoint path changes, clients can still function correctly as long as they follow links provided in responses. This reduces tight coupling between client and server and improves long-term maintainability.


# Part 2: Room Management

## 1. Room Resource Implementation

### Returning IDs vs Full Objects

Returning only IDs when listing rooms reduces network bandwidth usage and results in smaller response payloads. This can improve performance, especially when dealing with large datasets.

However, returning full room objects provides richer information in a single request, reducing the need for additional API calls from the client. This simplifies client-side logic and improves usability.

In this implementation, full room objects are returned. This design choice prioritizes ease of use and reduces the number of requests a client must make, at the cost of slightly higher bandwidth usage.

## 2. Room Deletion & Safety Logic

### DELETE Idempotency

Yes, the DELETE operation in this implementation is idempotent.

When a room is deleted successfully, the first request returns a `204 No Content` response. If the same DELETE request is sent again for the same room, the system returns a `404 Not Found` because the resource no longer exists.

Despite returning a different status code, the operation is still idempotent because the state of the system remains unchanged after the first deletion. Repeating the request does not cause additional side effects or further modifications to the system.

Therefore, the behavior satisfies the definition of idempotency: multiple identical requests produce the same final state.

# Part 3: Sensor Operations

## 1. Sensor Resource & Integrity

### @Consumes and Media Type Handling

The `@Consumes(MediaType.APPLICATION_JSON)` annotation specifies that the API only accepts JSON input.

If a client sends data in a different format such as `text/plain` or `application/xml`, JAX-RS will not be able to match the request to the method. As a result, the server typically responds with a `415 Unsupported Media Type` error.

This mechanism ensures that the API only processes data in the expected format, preventing parsing errors and maintaining consistency in request handling.

## 2. Filtered Retrieval & Search

### QueryParam vs Path Parameter

Using `@QueryParam` for filtering (e.g., `/sensors?type=CO2`) is more appropriate than embedding the filter in the path (e.g., `/sensors/type/CO2`).

Query parameters are designed for optional filtering and searching, while path parameters represent hierarchical resources. In this case, “type” is not a resource but a filter condition.

Using query parameters makes the API more flexible, allowing multiple filters to be combined easily (e.g., `/sensors?type=CO2&status=ACTIVE`). It also keeps the URL structure clean and semantically correct.

# Part 4: Sub-Resources

## 1. The Sub-Resource Locator Pattern

The sub-resource locator pattern allows the API to delegate nested resource handling to a separate class.

In this implementation, `SensorResource` returns a `SensorReadingResource` for paths like `/sensors/{id}/readings`. This keeps the code modular and avoids having one large, complex controller.

The main benefit is improved maintainability. Each resource class focuses on a specific responsibility:

- `SensorResource` handles sensor operations  
- `SensorReadingResource` handles reading-related operations  

This separation reduces complexity, improves readability, and makes the system easier to extend.

## 2. Historical Data Management

In this part, the `SensorReadingResource` is used to manage readings for each sensor through the endpoint `/sensors/{id}/readings`.

The `GET` method returns all the past readings of a sensor, while the `POST` method is used to add a new reading.

All readings are stored in a map where each sensor ID has its own list of readings. This makes it easy to keep track of the history for each sensor.

An important requirement here is to keep the sensor’s latest value updated. So whenever a new reading is added, the system also updates the `currentValue` field of that sensor.

This means:
- The system keeps a full history of readings  
- The latest value is always available instantly  

This keeps both the historical data and the current state in sync, making the API efficient and easy to use.

# Part 5: Error Handling & Logging

## 2. Dependency Validation (422 Unprocessable Entity)

HTTP `422 Unprocessable Entity` is more appropriate than `404` in this scenario because the request itself is valid, but contains invalid data.

When creating a sensor with a non-existent `roomId`, the endpoint `/sensors` exists and the request format is correct. The problem lies in the semantic validity of the data.

Using `404` would imply that the endpoint itself is missing, which is incorrect. Therefore, `422` provides a more accurate and meaningful response.

## 4. The Global Safety Net (500)

### Security Risks of Stack Traces

Exposing raw Java stack traces to API consumers is a serious security risk.

Stack traces can reveal:
- Internal class names and package structures  
- File paths and server environment details  
- Specific libraries and frameworks in use  

An attacker could use this information to identify vulnerabilities or craft targeted attacks.

In this implementation, a global exception mapper is used to prevent this. Instead of returning stack traces, the API returns generic error messages such as `"Internal server error"`, ensuring that sensitive information is not exposed.

## 5. API Request & Response Logging Filters

Using JAX-RS filters for logging is more efficient than adding logging statements inside each resource method.

Filters handle cross-cutting concerns in a centralized way. This means:
- No duplicated logging code across multiple methods  
- Consistent logging behavior for all requests  
- Easier maintenance and updates  

In this project, the `LoggingFilter` logs both incoming requests and outgoing responses, ensuring full observability without cluttering the business logic.
