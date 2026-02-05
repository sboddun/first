package com.insulet.practice.first.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import com.insulet.practice.first.model.ErrorResponse;

@Component
public class ServerExceptionProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    ErrorResponse response = new ErrorResponse(
        cause.getMessage(),
        cause.getClass().getSimpleName(),
        "500",
        System.currentTimeMillis());

    exchange.getMessage().setBody(response);
  }
}