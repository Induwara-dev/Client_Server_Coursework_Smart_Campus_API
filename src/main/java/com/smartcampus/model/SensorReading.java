package com.smartcampus.model;

/**
 * Represents a single timestamped reading captured by a sensor.
 */
public class SensorReading {

  private String id; // UUID
  private long timestamp; // Epoch millis
  private double value; // Measured value
  private String sensorId; // Parent sensor reference

  // Default constructor (required for Jackson)
  public SensorReading() {
  }

  public SensorReading(String id, long timestamp, double value, String sensorId) {
    this.id = id;
    this.timestamp = timestamp;
    this.value = value;
    this.sensorId = sensorId;
  }

  // ---------- Getters & Setters ----------

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }
}
