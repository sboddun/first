package com.insulet.practice.first.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.insulet.practice.first.GlobalExceptionHandler;

@Component
public class CustomerRestService extends RouteBuilder {

  private static final Logger logger = LoggerFactory.getLogger(CustomerRestService.class);

  private final GlobalExceptionHandler globalExceptionHandler;

  public CustomerRestService(GlobalExceptionHandler globalExceptionHandler) {
    this.globalExceptionHandler = globalExceptionHandler;
  }

  @Override
  public void configure() {
    // Use platform-http component (aligned with camel-platform-http-starter)
    restConfiguration()
        .component("platform-http")
        .bindingMode(RestBindingMode.json)
        .contextPath("/data-share/v1");

    configureExceptionHandlers();

    rest("/customers").get("/{customerId}").to("direct:getCustomerById");

    from("direct:getCustomerById")
        .log("Fetching customer with ID: ${header.customerId}")
        .process(
            exchange -> {
              CustomerRequest request = CustomerRequest.fromExchange(exchange);
              logger.info("Received CustomerRequest: {}", request);
              exchange.getIn().setBody(request);
            })
        .log("Saving customer request to database ${body.startDate} - ${body.endDate}")
        .to(
            "sql:INSERT INTO customer_requests (customer_id, start_date, end_date, page, rec_limit) "
                + "VALUES (:#${body.customerId}, :#${body.startDate}, :#${body.endDate}, :#${body.page}, :#${body.limit})?dataSource=#dataSource")
        .log("Customer request saved successfully")
        .setBody(constant("User created successfully"));
  }

  private void configureExceptionHandlers() {
    // Handle IllegalArgumentException, respond with HTTP 400
    onException(IllegalArgumentException.class)
        .handled(true)
        .log("IllegalArgumentException occurred: ${exception.message}")
        .log("Stack trace: ${exception.stacktrace}")
        .setBody(constant("Invalid request parameters"))
        .setHeader("Content-Type", constant("application/json"))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
        .bean(globalExceptionHandler, "handleBadRequests");

    onException(Exception.class)
        .handled(true)
        .log("Exception occurred: ${exception.message}")
        .log("Exception class: ${exception.class}")
        .log("Stack trace: ${exception.stacktrace}")
        .setBody(constant("Internal server error"))
        .setHeader("Content-Type", constant("application/json"))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .bean(globalExceptionHandler, "handleServerError");
  }

  public SuccessResponse processCustomers(CustomerRequest request) {
    // Process customer request
    logger.info("Processing customer request: {}", request);
    return new SuccessResponse("John Doe", "123 Main St", 50000.0, "1990-01-01");
  }
}
