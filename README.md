# Smart Campus RESTful API

A high-performance, fully-featured RESTful Web Service built with **JAX-RS (Jersey 2)** and an embedded **Grizzly HTTP server** — no external servlet container required.

---

## API Overview

| Base URL | `http://localhost:8080/api/v1` |
|---|---|
| **Format** | JSON (`application/json`) |
| **Authentication** | None (campus-internal) |
| **Data persistence** | In-memory (`ConcurrentHashMap`) |

### Resource Hierarchy

```
/api/v1
├── /rooms
│   ├── GET    – List all rooms
│   ├── POST   – Create a new room
│   └── /{roomId}
│       ├── GET    – Get room details
│       └── DELETE – Decommission room (blocked if sensors present)
└── /sensors
    ├── GET    – List sensors (optional ?type= filter)
    ├── POST   – Register a new sensor
    └── /{sensorId}
        ├── GET    – Get sensor details
        ├── PUT    – Update sensor status/value
        ├── DELETE – Remove sensor
        └── /readings
            ├── GET  – Fetch reading history
            └── POST – Append a new reading
```

---

## Build & Run Instructions

### Prerequisites
- Java 11+
- Apache Maven 3.6+

### Build
```bash
mvn clean package -q
```

### Run
```bash
java -jar target/smart-campus-api-1.0.0.jar
```

The server starts at **http://localhost:8080/api/v1**.  
Press **ENTER** in the terminal to shut down cleanly.

---

## Sample `curl` Commands

### 1. Discovery – GET /api/v1
```bash
curl -s http://localhost:8080/api/v1/ | python -m json.tool
```

### 2. List all rooms – GET /api/v1/rooms
```bash
curl -s http://localhost:8080/api/v1/rooms | python -m json.tool
```

### 3. Create a new room – POST /api/v1/rooms
```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-001","name":"Innovation Hub","capacity":100}' | python -m json.tool
```

### 4. Register a sensor – POST /api/v1/sensors
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-002","type":"CO2","status":"ACTIVE","currentValue":395.0,"roomId":"HALL-001"}' | python -m json.tool
```

### 5. Filter sensors by type – GET /api/v1/sensors?type=CO2
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2" | python -m json.tool
```

### 6. Post a sensor reading – POST /api/v1/sensors/{sensorId}/readings
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/CO2-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":412.5}' | python -m json.tool
```

### 7. Get reading history – GET /api/v1/sensors/{sensorId}/readings
```bash
curl -s http://localhost:8080/api/v1/sensors/CO2-002/readings | python -m json.tool
```

### 8. Delete a room (conflict – has sensors)
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
# Expects: 409 Conflict
```

### 9. Post reading to a MAINTENANCE sensor
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15}' | python -m json.tool
# Expects: 403 Forbidden
```

### 10. Register sensor with invalid roomId
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"FAKE-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"ROOM-DOES-NOT-EXIST"}' | python -m json.tool
# Expects: 422 Unprocessable Entity
```

---

## Report – Answers to Coursework Questions

### Part 1.1 – JAX-RS Resource Lifecycle
By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request** (per-request scope). This means no shared mutable state can be held in instance fields of a resource class; doing so would cause data loss between requests.  
To safely share state, all data is stored in a **singleton `DataStore`** class backed by `ConcurrentHashMap`. `ConcurrentHashMap` provides thread-safe read/write operations without the need for explicit `synchronized` blocks, preventing race conditions when multiple requests arrive simultaneously.

### Part 1.2 – HATEOAS
HATEOAS (Hypermedia as the Engine of Application State) means embedding navigational links inside API responses rather than forcing clients to hard-code URLs. Benefits include:
- **Discoverability**: clients explore the API dynamically from the root without consulting static docs.
- **Decoupling**: URL structures can change server-side; clients follow links rather than constructing them.
- **Self-documentation**: responses describe what actions are possible at each state, reducing integration errors.

### Part 2.1 – IDs vs Full Objects in List Responses
Returning **only IDs** reduces payload size dramatically (important with thousands of rooms) but forces clients to make N additional requests to retrieve details — the N+1 problem. Returning **full objects** costs more bandwidth but saves round-trips. A common best practice is to return a compact summary representation (id + name) in the list and full details in the individual `/{id}` endpoint.

### Part 2.2 – DELETE Idempotency
Yes, DELETE is idempotent in this implementation. The **first** successful call removes the room and returns `200 OK`. Any **subsequent** call for the same room ID finds nothing and returns `404 Not Found`. The resource state on the server is identical after both calls (the room is absent) — the only difference is the HTTP status code, which is an acceptable and standard behaviour for idempotent operations (RFC 9110).

### Part 3.1 – @Consumes and Content-Type Mismatch
When a client sends a body with `Content-Type: text/plain` to a method annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS cannot find a matching `MessageBodyReader` for that media type and automatically returns **HTTP 415 Unsupported Media Type** — without ever invoking the resource method. This protects the API from malformed or unexpected data formats without any custom code.

### Part 3.2 – @QueryParam vs Path Segment for Filtering
Using `?type=CO2` (query parameter) is preferred for filtering because:
- Filtering is **optional** — the path remains clean when no filter is applied (`/sensors`).
- Query parameters are designed for **variable, optional metadata** while path segments identify resources.
- Multiple filters compose naturally: `?type=CO2&status=ACTIVE`.
- Path-based filtering (`/sensors/type/CO2`) implies `CO2` is a sub-resource, which is semantically incorrect.

### Part 4.1 – Sub-Resource Locator Pattern Benefits
A Sub-Resource Locator delegates handling of nested paths to dedicated classes. Each class has a single responsibility (readings in this case), improving **maintainability**, **testability**, and **readability**. Compared to one monolithic controller with hundreds of methods, separate sub-resource classes keep route logic cohesive and allow the team to work on components independently without merge conflicts.

### Part 5.2 – HTTP 422 vs 404
`404 Not Found` conventionally means the **endpoint URL** does not exist. `422 Unprocessable Entity` means the request URL and format are valid, but the **semantic content** of the body is invalid. In this case, the `/sensors` endpoint exists and the JSON is well-formed; the problem is a broken foreign-key reference inside the payload. Using 422 gives clients a much clearer, more actionable signal.

### Part 5.4 – Security Risks of Exposing Stack Traces
Stack traces reveal:
1. **Library names & versions** – attackers cross-reference these with known CVEs to plan targeted exploits.
2. **Internal class names & package structure** – discloses the application's architecture and facilitates code injection attacks against known entry points.
3. **File paths** – exposes deployment directory structure, aiding path-traversal attacks.
4. **Business logic details** – method call chains reveal hidden flows that attackers can manipulate.
The global `ExceptionMapper<Throwable>` ensures only a generic message is returned to the client while the full trace is logged securely server-side.

### Part 5.5 – Filter-Based Logging vs Inline Logging
Filters implement the **cross-cutting concern** of logging in a single, centralised place:
- **DRY**: one class logs all requests/responses; no repetitive boilerplate in every method.
- **Consistency**: impossible to "forget" to add logging when creating new endpoints.
- **Separation of concerns**: resource methods focus on business logic; filters handle observability.
- **Flexibility**: can be toggled, enriched (add correlation IDs, auth info), or replaced without touching business code.

---

## Technology Stack
- **Language**: Java 11
- **Framework**: JAX-RS via Jersey 2.41
- **HTTP Server**: Grizzly 2 (embedded, no external Tomcat/WildFly needed)
- **JSON**: Jackson 2 via jersey-media-json-jackson
- **Build**: Apache Maven (Shade plugin for fat JAR)
- **Data**: In-memory `ConcurrentHashMap` (no database)
# Client_Server_Coursework_Smart_Campus_API
