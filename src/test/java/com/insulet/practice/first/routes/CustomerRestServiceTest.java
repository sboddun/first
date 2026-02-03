package com.insulet.practice.first.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.insulet.practice.first.FirstApplication;
import com.insulet.practice.first.GlobalExceptionHandler;

@SpringBootTest(classes = FirstApplication.class)
@CamelSpringBootTest
public class CustomerRestServiceTest {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Autowired
  private CamelContext camelContext;

  @Autowired
  private GlobalExceptionHandler globalExceptionHandler;

  @Test
  public void testGetCustomerById_withValidCustomerId_shouldReturnSuccess() throws Exception {
    String result = producerTemplate.requestBodyAndHeader(
        "direct:getCustomerById", null, "customerId", "CUST123", String.class);

    assertEquals("User created successfully", result);
  }
}