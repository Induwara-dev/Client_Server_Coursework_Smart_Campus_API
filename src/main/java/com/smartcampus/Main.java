package com.smartcampus;

import com.smartcampus.data.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Main entry point – starts the embedded Grizzly HTTP server.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://0.0.0.0:9090/api/v1/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.smartcampus");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        seedSampleData();

        final HttpServer server = startServer();
        LOGGER.info("Smart Campus API started at http://localhost:9090/api/v1");
        LOGGER.info("Press ENTER to stop the server.");
        System.in.read();
        server.shutdownNow();
    }

    /**
     * Seeds a small amount of demo data so the API is immediately usable.
     */
    private static void seedSampleData() {
        DataStore ds = DataStore.getInstance();

        // Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab 1", 30);
        Room r3 = new Room("LEC-201", "Lecture Hall A", 200);
        ds.getRooms().put(r1.getId(), r1);
        ds.getRooms().put(r2.getId(), r2);
        ds.getRooms().put(r3.getId(), r3);

        // Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 410.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LAB-101");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "ACTIVE", 21.0, "LEC-201");

        for (Sensor s : new Sensor[] { s1, s2, s3, s4 }) {
            ds.getSensors().put(s.getId(), s);
            Room room = ds.getRooms().get(s.getRoomId());
            if (room != null)
                room.getSensorIds().add(s.getId());
        }
    }
}
