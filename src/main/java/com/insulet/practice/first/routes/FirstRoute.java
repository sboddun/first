package com.insulet.practice.first.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;

// @Component
public class FirstRoute extends RouteBuilder {

  private final SimpleLogger simpleLogger;

  @Autowired
  public FirstRoute(SimpleLogger simpleLogger) {
    this.simpleLogger = simpleLogger;
  }

  @Override
  public void configure() throws Exception {
    // Periodic timer route; logs incoming body and transformed message
    from("timer:first-timer?period=5000")
        .process(exchange -> simpleLogger.log("Timer fired"))
        .log("${body}")
        .transform()
        .simple("Hello from Sameer running first camel route")
        .log("${body}")
        .bean(simpleLogger, "log");
  }
}
