package com.insulet.practice.first.routes;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.insulet.practice.first.FirstApplication;

@CamelSpringBootTest
@SpringBootTest(classes = { FirstApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableRouteCoverage
@Import(TestConfig.class)
class CustomerRestServiceTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void testGetCustomerById_withValidCustomerId_shouldReturnSuccess() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/data-share/v1/customers/CUST123",
        String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withAllValidQueryParameters_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("startDate", "2024-01-01T00:00:00.000Z")
        .queryParam("endDate", "2024-12-31T23:59:59.999Z")
        .queryParam("page", 1)
        .queryParam("limit", 100)
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withValidDates_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST789")
        .queryParam("startDate", "2024-06-01T12:30:45.123Z")
        .queryParam("endDate", "2024-06-30T18:45:30.456Z")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withMaxLimit_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST999")
        .queryParam("limit", 500)
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withZeroPageAndLimit_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST001")
        .queryParam("page", 0)
        .queryParam("limit", 0)
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withNullCustomerId_shouldReturn400() throws Exception {
    // In REST endpoints, path parameters cannot be truly null
    // This test verifies that missing path segments return 4xx error
    // Testing with trailing slash (no customer ID segment)
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/data-share/v1/customers/",
        String.class);

    // Should return 404 (not found) or 400 (bad request) for missing path parameter
    assertTrue(response.getStatusCode().is4xxClientError(),
        "Expected 4xx error for missing customer ID, got: " + response.getStatusCode());
  }

  @Test
  void testGetCustomerById_withInvalidStartDateFormat_shouldReturn400() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("startDate", "2024-01-01")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("startDate"));
  }

  @Test
  void testGetCustomerById_withInvalidEndDateFormat_shouldReturn400() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("endDate", "31/12/2024")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("endDate"));
  }

  @Test
  void testGetCustomerById_withStartDateAfterEndDate_shouldReturn400() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("startDate", "2024-12-31T23:59:59.999Z")
        .queryParam("endDate", "2024-01-01T00:00:00.000Z")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("startDate") || response.getBody().contains("endDate"));
  }

  @Test
  void testGetCustomerById_withNegativePage_shouldReturn400() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("page", -1)
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("page") || response.getBody().contains("negative"));
  }

  @Test
  void testGetCustomerById_withNegativeLimit_shouldReturn400() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("limit", -10)
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("limit") || response.getBody().contains("negative"));
  }

  @Test
  void testGetCustomerById_withLimitGreaterThan500_shouldReturn400() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST123")
        .queryParam("limit", 501)
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("limit") || response.getBody().contains("500"));
  }

  @Test
  void testGetCustomerById_withSameDateRange_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST999")
        .queryParam("startDate", "2024-06-15T12:00:00.000Z")
        .queryParam("endDate", "2024-06-15T12:00:00.000Z")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withOnlyStartDate_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST888")
        .queryParam("startDate", "2024-01-01T00:00:00.000Z")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withOnlyEndDate_shouldReturnSuccess() throws Exception {
    String url = UriComponentsBuilder.fromPath("/data-share/v1/customers/CUST777")
        .queryParam("endDate", "2024-12-31T23:59:59.999Z")
        .toUriString();

    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }

  @Test
  void testGetCustomerById_withLargeCustomerId_shouldReturnSuccess() throws Exception {
    String largeCustomerId = "CUST" + "X".repeat(100);
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/data-share/v1/customers/" + largeCustomerId,
        String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("\"Customer request created successfully\"", response.getBody());
  }
}