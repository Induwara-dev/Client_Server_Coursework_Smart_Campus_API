package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 5.1 – Maps RoomNotEmptyException → HTTP 409 Conflict
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

  @Override
  public Response toResponse(RoomNotEmptyException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", 409);
    body.put("error", "Conflict");
    body.put("message", ex.getMessage());
    body.put("hint", "Remove or reassign all sensors before deleting this room.");
    return Response.status(Response.Status.CONFLICT)
        .type(MediaType.APPLICATION_JSON)
        .entity(body)
        .build();
  }
}
