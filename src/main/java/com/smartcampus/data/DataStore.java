package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory data store (singleton).
 *
 * Uses ConcurrentHashMap to handle concurrent requests safely without
 * explicit synchronization, since JAX-RS creates a new resource instance
 * per request but all instances share this singleton.
 */
public class DataStore {

  private static final DataStore INSTANCE = new DataStore();

  // Primary collections – keyed by entity ID
  private final Map<String, Room> rooms = new ConcurrentHashMap<>();
  private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
  // Readings keyed by sensorId -> list of readings
  private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

  private DataStore() {
  }

  public static DataStore getInstance() {
    return INSTANCE;
  }

  public Map<String, Room> getRooms() {
    return rooms;
  }

  public Map<String, Sensor> getSensors() {
    return sensors;
  }

  public List<SensorReading> getReadingsForSensor(String sensorId) {
    return readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
  }
}
