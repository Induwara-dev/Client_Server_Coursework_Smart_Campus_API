package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Part 4 – Sub-Resource for Sensor Readings
 *
 * Handles:
 * GET /api/v1/sensors/{sensorId}/readings
 * POST /api/v1/sensors/{sensorId}/readings
 *
 * This class is NOT annotated with @Path at the class level because
 * it is instantiated by the sub-resource locator in SensorResource and
 * JAX-RS resolves the remainder of the path dynamically.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

  private final Sensor sensor;
  private final DataStore ds;

  public SensorReadingResource(Sensor sensor) {
    this.sensor = sensor;
    this.ds = DataStore.getInstance();
  }

  // ------------------------------------------------------------------ GET /
  @GET
  public Response getReadings() {
    List<SensorReading> history = ds.getReadingsForSensor(sensor.getId());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("sensorId", sensor.getId());
    body.put("totalReadings", history.size());
    body.put("readings", history);
    return Response.ok(body).build();
  }

  // ----------------------------------------------------------------- POST /
  /**
   * Appends a new reading to the sensor's history.
   *
   * Business rules:
   * 1. Sensor in MAINTENANCE status → HTTP 403 (SensorUnavailableException)
   * 2. Sensor in OFFLINE status → HTTP 403 as well
   * 3. On success → updates parent sensor's currentValue (data consistency)
   */
  @POST
  public Response addReading(SensorReading reading) {
    // --- State constraint check (Part 5.3) ---
    String status = sensor.getStatus();
    if ("MAINTENANCE".equalsIgnoreCase(status) || "OFFLINE".equalsIgnoreCase(status)) {
      throw new SensorUnavailableException(
          "Sensor '" + sensor.getId() + "' is currently " + status +
              " and cannot accept new readings.");
    }

    if (reading == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(errorBody("Reading body is required."))
          .build();
    }

    // Generate ID / timestamp if not provided by client
    if (reading.getId() == null || reading.getId().isBlank()) {
      reading.setId(UUID.randomUUID().toString());
    }
    if (reading.getTimestamp() == 0) {
      reading.setTimestamp(System.currentTimeMillis());
    }
    reading.setSensorId(sensor.getId());

    // Persist reading
    ds.getReadingsForSensor(sensor.getId()).add(reading);

    // Side-effect: update parent sensor's currentValue (Part 4.2)
    sensor.setCurrentValue(reading.getValue());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", "Reading recorded and sensor currentValue updated.");
    body.put("reading", reading);
    body.put("updatedSensorValue", sensor.getCurrentValue());
    return Response.status(Response.Status.CREATED).entity(body).build();
  }

  // ---------------------------------------------------------------- helpers
  private Map<String, String> errorBody(String message) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("error", message);
    return m;
  }
}
