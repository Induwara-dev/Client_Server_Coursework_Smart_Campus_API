package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Part 2 – Room Resource
 * Manages the /api/v1/rooms collection.
 *
 * JAX-RS default lifecycle: a NEW instance is created per HTTP request.
 * All shared state must therefore live in the singleton DataStore
 * (backed by ConcurrentHashMap) to avoid race conditions.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

  private final DataStore ds = DataStore.getInstance();

  // ------------------------------------------------------------------ GET /
  @GET
  public Response getAllRooms() {
    Collection<Room> rooms = ds.getRooms().values();
    return Response.ok(rooms).build();
  }

  // ----------------------------------------------------------------- POST /
  @POST
  public Response createRoom(Room room) {
    if (room == null || room.getId() == null || room.getId().isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(errorBody("Room ID is required."))
          .build();
    }
    if (ds.getRooms().containsKey(room.getId())) {
      return Response.status(Response.Status.CONFLICT)
          .entity(errorBody("A room with ID '" + room.getId() + "' already exists."))
          .build();
    }
    if (room.getSensorIds() == null) {
      room.setSensorIds(new ArrayList<>());
    }
    ds.getRooms().put(room.getId(), room);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", "Room created successfully.");
    body.put("room", room);
    return Response.status(Response.Status.CREATED).entity(body).build();
  }

  // --------------------------------------------------------------- GET /{id}
  @GET
  @Path("/{roomId}")
  public Response getRoom(@PathParam("roomId") String roomId) {
    Room room = ds.getRooms().get(roomId);
    if (room == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(errorBody("Room '" + roomId + "' not found."))
          .build();
    }
    return Response.ok(room).build();
  }

  // ------------------------------------------------------------ DELETE /{id}
  /**
   * DELETE is idempotent: the second call returns 404 (room gone) rather than
   * repeating the success or throwing an error, ensuring no side effects differ
   * from the first successful call.
   *
   * Business constraint: rooms with sensors assigned CANNOT be deleted.
   * RoomNotEmptyException is mapped to HTTP 409 Conflict.
   */
  @DELETE
  @Path("/{roomId}")
  public Response deleteRoom(@PathParam("roomId") String roomId) {
    Room room = ds.getRooms().get(roomId);
    if (room == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(errorBody("Room '" + roomId + "' not found."))
          .build();
    }
    if (!room.getSensorIds().isEmpty()) {
      throw new RoomNotEmptyException(
          "Cannot delete room '" + roomId + "'. It still has " +
              room.getSensorIds().size() + " sensor(s) assigned: " +
              room.getSensorIds());
    }
    ds.getRooms().remove(roomId);

    Map<String, String> body = new LinkedHashMap<>();
    body.put("message", "Room '" + roomId + "' has been decommissioned successfully.");
    return Response.ok(body).build();
  }

  // ----------------------------------------------------------------- helpers
  private Map<String, String> errorBody(String message) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("error", message);
    return m;
  }
}
