package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Part 3 – Sensor Resource
 * Manages the /api/v1/sensors collection.
 *
 * Also acts as a sub-resource locator for /api/v1/sensors/{sensorId}/readings
 * (Part 4).
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

  private final DataStore ds = DataStore.getInstance();

  // ------------------------------------------------------------------ GET /
  /**
   * Optional ?type= query parameter for filtering by sensor type.
   * e.g., GET /api/v1/sensors?type=CO2
   */
  @GET
  public Response getAllSensors(@QueryParam("type") String type) {
    Collection<Sensor> all = ds.getSensors().values();
    if (type != null && !type.isBlank()) {
      List<Sensor> filtered = all.stream()
          .filter(s -> s.getType() != null &&
              s.getType().equalsIgnoreCase(type.trim()))
          .collect(Collectors.toList());
      return Response.ok(filtered).build();
    }
    return Response.ok(new ArrayList<>(all)).build();
  }

  // ----------------------------------------------------------------- POST /
  /**
   * Registers a new sensor.
   * Validates that the referenced roomId actually exists (Part 3.1).
   * If not, throws LinkedResourceNotFoundException → HTTP 422.
   */
  @POST
  public Response createSensor(Sensor sensor) {
    if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(errorBody("Sensor ID is required."))
          .build();
    }
    if (ds.getSensors().containsKey(sensor.getId())) {
      return Response.status(Response.Status.CONFLICT)
          .entity(errorBody("A sensor with ID '" + sensor.getId() + "' already exists."))
          .build();
    }

    // Referential integrity check
    if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(errorBody("A roomId is required when registering a sensor."))
          .build();
    }
    if (!ds.getRooms().containsKey(sensor.getRoomId())) {
      throw new LinkedResourceNotFoundException(
          "The referenced roomId '" + sensor.getRoomId() +
              "' does not exist. Please create the room first.");
    }

    // Set defaults
    if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
      sensor.setStatus("ACTIVE");
    }

    ds.getSensors().put(sensor.getId(), sensor);
    // Register sensor in the room's list
    ds.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", "Sensor registered successfully.");
    body.put("sensor", sensor);
    return Response.status(Response.Status.CREATED).entity(body).build();
  }

  // -------------------------------------------------- GET /{sensorId}
  @GET
  @Path("/{sensorId}")
  public Response getSensor(@PathParam("sensorId") String sensorId) {
    Sensor sensor = ds.getSensors().get(sensorId);
    if (sensor == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(errorBody("Sensor '" + sensorId + "' not found."))
          .build();
    }
    return Response.ok(sensor).build();
  }

  // -------------------------------- PUT /{sensorId} (update status/value)
  @PUT
  @Path("/{sensorId}")
  public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updated) {
    Sensor existing = ds.getSensors().get(sensorId);
    if (existing == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(errorBody("Sensor '" + sensorId + "' not found."))
          .build();
    }
    if (updated.getType() != null)
      existing.setType(updated.getType());
    if (updated.getStatus() != null)
      existing.setStatus(updated.getStatus());
    existing.setCurrentValue(updated.getCurrentValue());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", "Sensor updated successfully.");
    body.put("sensor", existing);
    return Response.ok(body).build();
  }

  // ----------------------------------- DELETE /{sensorId}
  @DELETE
  @Path("/{sensorId}")
  public Response deleteSensor(@PathParam("sensorId") String sensorId) {
    Sensor sensor = ds.getSensors().remove(sensorId);
    if (sensor == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(errorBody("Sensor '" + sensorId + "' not found."))
          .build();
    }
    // Remove from room's list
    if (sensor.getRoomId() != null && ds.getRooms().containsKey(sensor.getRoomId())) {
      ds.getRooms().get(sensor.getRoomId()).getSensorIds().remove(sensorId);
    }
    Map<String, String> body = new LinkedHashMap<>();
    body.put("message", "Sensor '" + sensorId + "' removed successfully.");
    return Response.ok(body).build();
  }

  // ------------------------------------------------- Sub-resource locator
  /**
   * Part 4.1 – Sub-Resource Locator Pattern
   *
   * JAX-RS will delegate all paths under /{sensorId}/readings to
   * SensorReadingResource. The locator does NOT have @GET / @POST itself;
   * it just instantiates and returns the sub-resource with context.
   */
  @Path("/{sensorId}/readings")
  public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
    Sensor sensor = ds.getSensors().get(sensorId);
    if (sensor == null) {
      throw new WebApplicationException(
          Response.status(Response.Status.NOT_FOUND)
              .entity(errorBody("Sensor '" + sensorId + "' not found."))
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
    return new SensorReadingResource(sensor);
  }

  // ----------------------------------------------------------------- helpers
  private Map<String, String> errorBody(String message) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("error", message);
    return m;
  }
}
