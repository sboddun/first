package com.insulet.practice.first.processors;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import com.insulet.practice.first.model.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@Component
public class ValidationExceptionProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    org.apache.camel.ValidationException validationException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
        org.apache.camel.ValidationException.class);

    String errorMessage;
    if (validationException.getCause() instanceof ConstraintViolationException cve) {
      Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
      errorMessage = violations.stream()
          .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
          .collect(Collectors.joining(", "));
    } else {
      errorMessage = validationException.getMessage();
    }

    ErrorResponse response = new ErrorResponse(
        errorMessage, "ValidationException", "400", System.currentTimeMillis());

    exchange.getMessage().setBody(response);
  }
}