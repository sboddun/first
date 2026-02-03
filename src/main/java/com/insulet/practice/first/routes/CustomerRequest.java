package com.insulet.practice.first.routes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.camel.Exchange;

public class CustomerRequest {

  private final String customerId;
  private final String startDate;
  private final String endDate;
  private final Integer page;
  private final Integer limit;

  public CustomerRequest(
      String customerId, String startDate, String endDate, Integer page, Integer limit) {
    if (customerId == null || customerId.isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }
    if (startDate != null && !startDate.isEmpty()) {
      // Validate startDate format (ISO 8601)
      if (!startDate.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$")) {
        throw new IllegalArgumentException(
            "Invalid startDate format. Expected ISO 8601 format YYYY-MM-DDThh:mm:ss.mmmZ");
      }
    }
    if (endDate != null && !endDate.isEmpty()) {
      // Validate endDate format (ISO 8601)
      if (!endDate.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$")) {
        throw new IllegalArgumentException(
            "Invalid endDate format. Expected ISO 8601 format YYYY-MM-DDThh:mm:ss.mmmZ");
      }
    }
    if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
      LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
      if (start.isAfter(end)) {
        throw new IllegalArgumentException("startDate cannot be after endDate");
      }
    }
    if (page != null && page < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }
    if (limit != null && limit < 0) {
      throw new IllegalArgumentException("Limit cannot be negative");
    }
    if (limit != null && limit > 500) {
      throw new IllegalArgumentException("Limit cannot be greater than 500");
    }

    this.customerId = customerId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.page = page;
    this.limit = limit;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getStartDate() {
    return startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public Integer getPage() {
    return page;
  }

  public Integer getLimit() {
    return limit;
  }

  public static CustomerRequest fromExchange(Exchange exchange) {
    Objects.requireNonNull(exchange, "Exchange object cannot be null");
    return new CustomerRequest(
        exchange.getIn().getHeader("customerId", String.class),
        exchange.getIn().getHeader("startDate", String.class),
        exchange.getIn().getHeader("endDate", String.class),
        exchange.getIn().getHeader("page", Integer.class),
        exchange.getIn().getHeader("limit", Integer.class));
  }

  @Override
  public String toString() {
    return "CustomerRequest{"
        + "customerId='"
        + customerId
        + '\''
        + ", startDate='"
        + startDate
        + '\''
        + ", endDate='"
        + endDate
        + '\''
        + ", page="
        + page
        + ", limit="
        + limit
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CustomerRequest that = (CustomerRequest) o;
    return Objects.equals(customerId, that.customerId)
        && Objects.equals(startDate, that.startDate)
        && Objects.equals(endDate, that.endDate)
        && Objects.equals(page, that.page)
        && Objects.equals(limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId, startDate, endDate, page, limit);
  }
}
