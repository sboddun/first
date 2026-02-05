package com.insulet.practice.first.model;

import java.util.Objects;

import org.apache.camel.Exchange;

import com.insulet.practice.first.utils.ValidDateRange;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@ValidDateRange
public class CustomerRequest {

  @NotEmpty(message = "Customer ID cannot be null or empty")
  private final String customerId;

  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", message = "Invalid startDate format. Expected ISO 8601 format YYYY-MM-DDThh:mm:ss.mmmZ")
  private final String startDate;

  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", message = "Invalid endDate format. Expected ISO 8601 format YYYY-MM-DDThh:mm:ss.mmmZ")
  private final String endDate;

  @Min(value = 0, message = "Page number cannot be negative")
  private final Integer page;

  @Min(value = 0, message = "Limit cannot be negative")
  @Max(value = 500, message = "Limit cannot be greater than 500")
  private final Integer limit;

  public CustomerRequest(
      String customerId, String startDate, String endDate, Integer page, Integer limit) {
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

  public static CustomerRequest fromExchange(Exchange exchange, Integer defaultLimit) {
    Objects.requireNonNull(exchange, "Exchange object cannot be null");

    Integer page = exchange.getIn().getHeader("page", Integer.class);
    Integer limit = exchange.getIn().getHeader("limit", Integer.class);

    return new CustomerRequest(
        exchange.getIn().getHeader("customerId", String.class),
        exchange.getIn().getHeader("startDate", String.class),
        exchange.getIn().getHeader("endDate", String.class),
        page != null ? page : 0,        limit != null ? limit : defaultLimit);
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
