package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 – API Request &amp; Response Logging Filter
 *
 * Implements both ContainerRequestFilter and ContainerResponseFilter so that
 * one class handles the full request/response cycle as a cross-cutting concern.
 *
 * Advantages over inline Logger.info() calls:
 * - Single point of change – modify logging format once, affects all endpoints.
 * - Consistent output – no risk of forgetting to add logging to a new method.
 * - Separation of concerns – resource classes stay focused on business logic.
 * - Enables easy toggling/enrichment (add auth token redaction, MDC, etc.).
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    LOGGER.info(String.format("[REQUEST]  %s %s",
        requestContext.getMethod(),
        requestContext.getUriInfo().getRequestUri()));
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    LOGGER.info(String.format("[RESPONSE] %s %s → HTTP %d",
        requestContext.getMethod(),
        requestContext.getUriInfo().getRequestUri(),
        responseContext.getStatus()));
  }
}
