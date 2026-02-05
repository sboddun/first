package com.insulet.practice.first.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.insulet.practice.first.model.CustomerRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, CustomerRequest> {

  @Override
  public void initialize(ValidDateRange constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(CustomerRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // null objects are valid, use @NotNull for null checks
    }

    String startDate = request.getStartDate();
    String endDate = request.getEndDate();

    // If either date is null or empty, skip the range validation
    if (startDate == null
        || startDate.isEmpty()
        || endDate == null
        || endDate.isEmpty()) {
      return true;
    }

    try {
      LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);

      if (start.isAfter(end)) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("startDate cannot be after endDate")
            .addPropertyNode("startDate")
            .addConstraintViolation();
        return false;
      }
    } catch (DateTimeParseException e) {
      // Date format validation is handled by @Pattern annotations
      return true;
    }
    return true;
  }
}
