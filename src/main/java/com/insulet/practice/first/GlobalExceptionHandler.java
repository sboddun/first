package com.insulet.practice.first;

import com.insulet.practice.first.routes.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionHandler {

  @Handler
  public ErrorResponse handleServerError(Exchange exchange) {
    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    return new ErrorResponse(
        cause.getMessage(), cause.getClass().getSimpleName(), "500", System.currentTimeMillis());
  }

  @Handler
  public ErrorResponse handleBadRequests(Exchange exchange) {
    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    return new ErrorResponse(
        cause.getMessage(), cause.getClass().getSimpleName(), "400", System.currentTimeMillis());
  }
}
