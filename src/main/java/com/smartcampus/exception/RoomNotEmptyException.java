package com.smartcampus.exception;

/**
 * Thrown when a client attempts to DELETE a Room that still has sensors.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {
  public RoomNotEmptyException(String message) {
    super(message);
  }
}
