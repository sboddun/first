package com.insulet.practice.first.model;

public record ErrorResponse(String message, String error, String code, Long timestamp) {}
