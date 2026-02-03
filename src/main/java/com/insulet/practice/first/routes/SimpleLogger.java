package com.insulet.practice.first.routes;

import org.springframework.stereotype.Component;

@Component
public class SimpleLogger {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(SimpleLogger.class.getName());

  public void log(String message) {
    log.info(message + " at {}", java.time.LocalDateTime.now());
  }
}
