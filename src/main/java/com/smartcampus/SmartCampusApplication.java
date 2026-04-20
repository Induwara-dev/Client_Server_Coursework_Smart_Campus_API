package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * The @ApplicationPath annotation sets the versioned base URI segment.
 * Combined with Grizzly's base URI (http://localhost:8080/), every resource
 * is reachable under http://localhost:8080/api/v1/...
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
  // Jersey auto-scans packages declared in ResourceConfig (see Main.java).
  // No additional configuration needed here.
}
