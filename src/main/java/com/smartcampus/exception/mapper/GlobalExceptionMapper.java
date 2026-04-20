package com.smartcampus.exception.mapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 – Global Safety Net: ExceptionMapper&lt;Throwable&gt;
 *
 * Catches any exception not handled by a more specific mapper.
 * Returns HTTP 500 with a generic message – NO stack trace is leaked.
 *
 * Security rationale: exposing stack traces reveals class names, file paths,
 * library versions, and business logic that attackers can leverage for
 * targeted exploits (e.g., known CVEs in revealed dependency versions).
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

  private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

  @Override
  public Response toResponse(Throwable ex) {
    // Let WebApplicationException (e.g. 404/405) pass through unchanged
    if (ex instanceof WebApplicationException) {
      return ((WebApplicationException) ex).getResponse();
    }

    // Log full stack trace server-side only
    LOGGER.log(Level.SEVERE, "Unhandled exception caught by GlobalExceptionMapper", ex);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", 500);
    body.put("error", "Internal Server Error");
    body.put("message", "An unexpected error occurred. Please contact the system administrator.");
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON)
        .entity(body)
        .build();
  }
}
