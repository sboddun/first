package com.insulet.practice.first.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.insulet.practice.first.model.CustomerRequest;
import com.insulet.practice.first.model.RequestDefaults;
import com.insulet.practice.first.processors.ServerExceptionProcessor;
import com.insulet.practice.first.processors.ValidationExceptionProcessor;

@Component
public class CustomerRestService extends RouteBuilder {

  private static final Logger logger = LoggerFactory.getLogger(CustomerRestService.class);

  private final ServerExceptionProcessor serverExceptionProcessor;
  private final ValidationExceptionProcessor validationExceptionProcessor;
  private final RequestDefaults requestDefaults;

  public CustomerRestService(ServerExceptionProcessor serverExceptionProcessor,
      ValidationExceptionProcessor validationExceptionProcessor,
      RequestDefaults requestDefaults) {
    this.serverExceptionProcessor = serverExceptionProcessor;
    this.validationExceptionProcessor = validationExceptionProcessor;
    this.requestDefaults = requestDefaults;
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
              CustomerRequest request = CustomerRequest.fromExchange(exchange, requestDefaults.getLimit());
              logger.info("Received CustomerRequest: {}", request);
              exchange.getIn().setBody(request);
            })
        .to("bean-validator:validateCustomerRequest")
        .log("Validation successful for customer request")
        .log("Saving customer request to database ${body.startDate} - ${body.endDate}")
        .to(
            "sql:INSERT INTO customer_requests (customer_id, start_date, end_date, page, rec_limit) "
                + "VALUES (:#${body.customerId}, :#${body.startDate}, :#${body.endDate}, :#${body.page}, :#${body.limit})?dataSource=#dataSource")
        .log("Customer request saved successfully")
        .setBody(constant("Customer request created successfully"));
  }

  private void configureExceptionHandlers() {
    // Handle Bean Validation errors, respond with HTTP 400
    onException(org.apache.camel.ValidationException.class)
        .handled(true)
        .log("ValidationException occurred: ${exception.message}")
        .log("Stack trace: ${exception.stacktrace}")
        .setHeader("Content-Type", constant("application/json"))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
        .process(validationExceptionProcessor);

    onException(Exception.class)
        .handled(true)
        .log("Exception occurred: ${exception.message}")
        .log("Exception class: ${exception.class}")
        .log("Stack trace: ${exception.stacktrace}")
        .setBody(constant("Internal server error"))
        .setHeader("Content-Type", constant("application/json"))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .process(serverExceptionProcessor);
  }
}
